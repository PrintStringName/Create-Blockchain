package com.divnectar.createblockchain.block.entity;

import com.divnectar.createblockchain.Config;
import com.divnectar.createblockchain.CreateBlockchain;
import com.divnectar.createblockchain.block.CurrencyMinerBlock;
import com.divnectar.createblockchain.block.ModBlocks;
import com.divnectar.createblockchain.fluid.ModFluids;
import com.divnectar.createblockchain.item.ModItems;
import com.divnectar.createblockchain.sound.ModSounds;
import com.divnectar.createblockchain.world.CurrencyTracker;
import com.mojang.logging.LogUtils;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

public class CurrencyMinerBlockEntity extends BlockEntity implements GeoBlockEntity, IHaveGoggleInformation {
    // --- CONSTANTS ---
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_HEAT = 100;
    protected static final RawAnimation ACTIVE_ANIM = RawAnimation.begin().thenLoop("animation.Currency MinerGGL.transition");
    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");

    // --- COMPONENTS ---
    private final EnergyStorage energyStorage;
    private final ItemStackHandler itemHandler = new ItemStackHandler(2); // Slot 0: Output, Slot 1: Maintenance
    private final FluidTank fluidTank = new FluidTank(4000);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // --- STATE ---
    private long energyToMine;
    private long accumulatedEnergy = 0;
    private int lastEnergyConsumed = 0;
    private double heat = 0;

    public CurrencyMinerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CURRENCY_MINER_BE.get(), pos, state);
        this.energyStorage = new EnergyStorage(Config.ENERGY_CAPACITY.get(), Config.MAX_ENERGY_CONSUMPTION.get(), Config.MAX_ENERGY_CONSUMPTION.get());
        this.energyToMine = Config.BASE_ENERGY_PER_COIN.get();
    }

    // --- GETTERS ---
    public EnergyStorage getEnergyStorage() { return this.energyStorage; }
    public ItemStackHandler getItemHandler() { return this.itemHandler; }
    public FluidTank getFluidTank() { return this.fluidTank; }

    // --- TICK LOGIC ---
    public static void tick(Level level, BlockPos pos, BlockState state, CurrencyMinerBlockEntity be) {
        if (level.isClientSide) return;

        be.updateMiningCost();

        // Determine the machine's various possible states
        boolean isBroken = be.isCoreBroken();
        boolean isOverheating = be.heat >= Config.OVERHEAT_THRESHOLD.get();
        int potentialEnergyInput = be.energyStorage.extractEnergy(Config.MAX_ENERGY_CONSUMPTION.get(), true);
        boolean hasPower = potentialEnergyInput > 0;

        // The machine only makes *progress* if all conditions are met.
        boolean isMakingProgress = hasPower && !isBroken && !isOverheating;

        if (isMakingProgress) {
            // If it can work, then it consumes energy, generates heat, and does its job.
            be.processWork(potentialEnergyInput);
            be.lastEnergyConsumed = potentialEnergyInput;
        } else {
            // If it's not making progress (broken, overheated, or no power), it consumes no energy.
            be.lastEnergyConsumed = 0;
        }

        // Cooling should happen every tick, regardless of the working state.
        be.applyCooling();

        // If the machine is overheating, emit smoke particles.
        if (isOverheating && level instanceof ServerLevel serverLevel) {
            if (level.random.nextInt(4) == 0) { // Control particle density
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        pos.getX() + level.random.nextDouble(),
                        pos.getY() + 1.2,
                        pos.getZ() + level.random.nextDouble(),
                        1, 0.0, 0.05, 0.0, 0.02);
            }
        }

        // The machine is visually "active" as long as it has power and isn't broken.
        // This decouples the visual/animation state from the overheating flicker.
        boolean isVisuallyActive = hasPower && !isBroken;

        // Update the block's visual state based on whether it's visually active.
        if (state.getValue(CurrencyMinerBlock.POWERED) != isVisuallyActive) {
            level.setBlock(pos, state.setValue(CurrencyMinerBlock.POWERED, isVisuallyActive), 3);
        }

        // Send periodic updates to the client for the goggle display.
        if (level.getGameTime() % 20 == 0) {
            setChanged(level, pos, state);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        }
    }

    private void processWork(int energyConsumed) {
        // 1. Actually consume the energy from the buffer.
        energyStorage.extractEnergy(energyConsumed, false);

        // 2. Generate heat based on the energy consumed.
        // This is a simple additive process.
        double heatGenerated = energyConsumed * Config.HEAT_PER_FE.get();
        this.heat = Math.min(MAX_HEAT, this.heat + heatGenerated);

        // 3. Add the energy (with heat modifiers) to the accumulated total for mining.
        accumulatedEnergy += getEnergyWithHeatModifier(energyConsumed);

        if (accumulatedEnergy >= energyToMine) {
            int coinsToMine = (int) (accumulatedEnergy / energyToMine);
            for (int i = 0; i < coinsToMine; i++) {
                mineCoin();
            }
            accumulatedEnergy %= energyToMine;
        }
    }

    private void applyCooling() {
        if (this.heat <= 0) {
            return; // No cooling needed if there's no heat.
        }

        // Start with the base passive cooling factor.
        double totalCoolingFactor = Config.PASSIVE_COOLING_PERCENT.get();

        // Check for active coolants and add their power.
        if (!this.fluidTank.isEmpty()) {
            if (this.fluidTank.getFluid().getFluid() == Fluids.WATER) {
                totalCoolingFactor += Config.WATER_COOLING_PERCENT.get();
                this.fluidTank.drain(5, FluidTank.FluidAction.EXECUTE); // Consume a small amount of coolant
            } else if (this.fluidTank.getFluid().getFluid() == ModFluids.SOURCE_CRYOTHEUM_COOLANT.get()) {
                totalCoolingFactor += Config.CRYOTHEUM_COOLING_PERCENT.get();
                this.fluidTank.drain(5, FluidTank.FluidAction.EXECUTE); // Cryotheum is more effective but consumed at the same rate
            }
        }

        // Apply the combined cooling percentage.
        // The hotter the machine, the more heat is dissipated.
        this.heat *= (1.0 - totalCoolingFactor);

        // Ensure heat doesn't dip into negatives from floating point errors.
        if (this.heat < 0) {
            this.heat = 0;
        }
    }

    private boolean isCoreBroken() {
        ItemStack coreStack = this.itemHandler.getStackInSlot(1);
        return coreStack.isEmpty() || coreStack.getDamageValue() >= coreStack.getMaxDamage();
    }

    public ItemStack removeCoreForPlayer() {
        return this.itemHandler.extractItem(1, 1, false);
    }

    private int getEnergyWithHeatModifier(int energyIn) {
        if (this.heat > 75) return energyIn / 2;
        return energyIn;
    }

    private void mineCoin() {
        if (this.level instanceof ServerLevel serverLevel) {
            ItemStack coreStack = this.itemHandler.getStackInSlot(1);
            if (coreStack.isEmpty() || !coreStack.is(ModItems.MINING_CORE.get())) return;

            CurrencyTracker.get(serverLevel).incrementMined();

            int damage = Config.DURABILITY_DAMAGE_STANDARD.get();
            if (!this.fluidTank.isEmpty() && this.fluidTank.getFluid().getFluid() == Fluids.WATER) {
                damage = Config.DURABILITY_DAMAGE_WATER.get();
            }

            coreStack.hurtAndBreak(damage, serverLevel, null, (item) -> {
                this.itemHandler.setStackInSlot(1, ItemStack.EMPTY);
//                serverLevel.playSound(null, this.worldPosition, SoundEvents.ITEM_SHIELD_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
            });

            String coinTypeName = Config.COIN_TO_GENERATE.get().toUpperCase();
            Coin coinToMine;
            try {
                coinToMine = Coin.valueOf(coinTypeName);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Invalid coin type '{}' in config. Defaulting to BEVEL.", coinTypeName);
                coinToMine = Coin.BEVEL;
            }
            ItemStack coinStack = new ItemStack(coinToMine.asStack().getItem());
            this.itemHandler.insertItem(0, coinStack, false);
            serverLevel.playSound(null, this.worldPosition, ModSounds.COIN_CHACHING.get(), SoundSource.BLOCKS, 0.5f, 1.5f);
        }
    }

    private void updateMiningCost() {
        if (this.level instanceof ServerLevel serverLevel) {
            int totalMined = CurrencyTracker.get(serverLevel).getTotalMined();
            long difficultyBonus = (long) Math.floor((double) totalMined / Config.DIFFICULTY_INTERVAL.get()) * Config.DIFFICULTY_BONUS.get();
            long newEnergyToMine = Config.BASE_ENERGY_PER_COIN.get() + difficultyBonus;
            if (this.energyToMine != newEnergyToMine) {
                this.energyToMine = newEnergyToMine;
                setChanged(level, this.worldPosition, this.getBlockState());
                level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    // --- GECKOLIB ANIMATION ---
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private PlayState predicate(AnimationState<CurrencyMinerBlockEntity> state) {
        if (state.getAnimatable().getBlockState().getValue(CurrencyMinerBlock.POWERED)) {
            return state.setAndContinue(ACTIVE_ANIM);
        }
        return state.setAndContinue(IDLE_ANIM);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() { return this.cache; }

    // --- DATA SYNC & NBT ---
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, provider);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        this.loadAdditional(tag, provider);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putLong("accumulatedEnergy", this.accumulatedEnergy);
        tag.put("energy", energyStorage.serializeNBT(provider));
        tag.put("inventory", itemHandler.serializeNBT(provider));
        tag.put("fluid", fluidTank.writeToNBT(provider, new CompoundTag()));
        tag.putInt("lastEnergyConsumed", this.lastEnergyConsumed);
        tag.putLong("energyToMine", this.energyToMine);
        tag.putDouble("heat", this.heat);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.accumulatedEnergy = tag.getLong("accumulatedEnergy");
        if (tag.contains("energy", Tag.TAG_COMPOUND)) energyStorage.deserializeNBT(provider, tag.getCompound("energy"));
        if (tag.contains("inventory", Tag.TAG_COMPOUND)) itemHandler.deserializeNBT(provider, tag.getCompound("inventory"));
        if (tag.contains("fluid")) fluidTank.readFromNBT(provider, tag.getCompound("fluid"));
        this.lastEnergyConsumed = tag.getInt("lastEnergyConsumed");
        this.energyToMine = tag.getLong("energyToMine");
        this.heat = tag.getDouble("heat");
    }

    // --- GOGGLE INFORMATION ---
    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        String modId = CreateBlockchain.MODID;
        tooltip.add(Component.translatable("goggle." + modId + ".info.header"));

        ItemStack coreStack = this.itemHandler.getStackInSlot(1);
        if (coreStack.isEmpty()) {
            tooltip.add(Component.literal("  ").append(Component.translatable("goggle." + modId + ".info.no_core").withStyle(ChatFormatting.RED)));
        } else {
            int currentDurability = coreStack.getMaxDamage() - coreStack.getDamageValue();
            int maxDurability = coreStack.getMaxDamage();
            tooltip.add(Component.literal("  ")
                    .append(Component.translatable("goggle." + modId + ".info.durability"))
                    .append(Component.literal(": " + currentDurability + " / " + maxDurability).withStyle(currentDurability < maxDurability * 0.1 ? ChatFormatting.YELLOW : ChatFormatting.WHITE)));
        }

        if (this.heat >= Config.OVERHEAT_THRESHOLD.get()) {
            tooltip.add(Component.literal("  ").append(Component.translatable("goggle." + modId + ".info.overheated").withStyle(ChatFormatting.RED)));
        } else {
            tooltip.add(Component.literal("  ")
                    .append(Component.translatable("goggle." + modId + ".info.heat"))
                    .append(Component.literal(": " + (int)this.heat + "%").withStyle(this.heat > 75 ? ChatFormatting.YELLOW : ChatFormatting.WHITE)));
        }

        tooltip.add(Component.literal("  ")
                .append(Component.translatable("goggle." + modId + ".info.coolant"))
                .append(Component.literal(": " + this.fluidTank.getFluidAmount() + "mb ").append(this.fluidTank.getFluid().getDisplayName()).withStyle(ChatFormatting.AQUA)));

        tooltip.add(Component.literal("  ").append(Component.translatable("goggle." + modId + ".info.usage")).append(Component.literal(": " + this.lastEnergyConsumed + " FE/t").withStyle(ChatFormatting.AQUA)));

        double coinsPerMinute = 0;
        if (this.lastEnergyConsumed > 0 && this.heat < Config.OVERHEAT_THRESHOLD.get()) {
            double ticksPerCoin = (double) this.energyToMine / getEnergyWithHeatModifier(this.lastEnergyConsumed);
            coinsPerMinute = (1200.0) / ticksPerCoin;
        }
        tooltip.add(Component.literal("  ").append(Component.translatable("goggle." + modId + ".info.rate")).append(Component.literal(String.format(": %.2f Coins/min", coinsPerMinute)).withStyle(ChatFormatting.GREEN)));

        tooltip.add(Component.literal("  ").append(Component.translatable("goggle." + modId + ".info.cost")).append(Component.literal(": " + this.energyToMine + " FE").withStyle(ChatFormatting.GOLD)));

        ItemStack storedStack = this.itemHandler.getStackInSlot(0);
        tooltip.add(Component.literal("  ").append(Component.translatable("goggle." + modId + ".info.stored")).append(Component.literal(": " + storedStack.getCount() + " ").append(storedStack.getHoverName()).withStyle(ChatFormatting.WHITE)));

        return true;
    }
}
