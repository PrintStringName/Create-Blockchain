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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
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
    private static final Logger LOGGER = LogUtils.getLogger();
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected static final RawAnimation ACTIVE_ANIM = RawAnimation.begin().thenLoop("animation.Currency MinerGGL.transition");
    protected static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");

    private final EnergyStorage energyStorage;
    private final ItemStackHandler itemHandler = new ItemStackHandler(2);
    private final FluidTank fluidTank = new FluidTank(4000);

    private long energyToMine;
    private long accumulatedEnergy = 0;
    private int lastEnergyConsumed = 0;
    private int heat = 0;
    private boolean isCoolingDown = false;
    private static final int MAX_HEAT = 100;

    public CurrencyMinerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CURRENCY_MINER_BE.get(), pos, state);
        this.energyStorage = new EnergyStorage(Config.ENERGY_CAPACITY.get(), Config.MAX_ENERGY_CONSUMPTION.get(), Config.MAX_ENERGY_CONSUMPTION.get());
        this.energyToMine = Config.BASE_ENERGY_PER_COIN.get();
    }

    public EnergyStorage getEnergyStorage() { return this.energyStorage; }
    public ItemStackHandler getItemHandler() { return this.itemHandler; }
    public FluidTank getFluidTank() { return this.fluidTank; }

    public static void tick(Level level, BlockPos pos, BlockState state, CurrencyMinerBlockEntity be) {
        if (level.isClientSide) return;

        be.updateMiningCost();

        ItemStack coreStack = be.itemHandler.getStackInSlot(1);
        boolean isBroken = coreStack.isEmpty() || coreStack.getDamageValue() >= coreStack.getMaxDamage();

        if (be.isCoolingDown) {
            if (be.heat <= Config.COOLDOWN_THRESHOLD.get()) {
                be.isCoolingDown = false;
            }
        } else if (be.heat >= Config.OVERHEAT_THRESHOLD.get()) {
            be.isCoolingDown = true;
        }

        int energyToConsume = 0;
        boolean isWorking = false;

        if (!isBroken && !be.isCoolingDown) {
            energyToConsume = be.energyStorage.extractEnergy(Config.MAX_ENERGY_CONSUMPTION.get(), true);
            if (energyToConsume > 0) {
                isWorking = true;
                be.energyStorage.extractEnergy(energyToConsume, false);
                be.accumulatedEnergy += be.getEnergyWithHeatModifier(energyToConsume);
                be.heat = Math.min(MAX_HEAT, be.heat + Config.HEAT_GENERATION_RATE.get());
            }
        }

        be.handleCooling();

        if (state.getValue(CurrencyMinerBlock.POWERED) != isWorking) {
            level.setBlock(pos, state.setValue(CurrencyMinerBlock.POWERED, isWorking), 3);
        }

        if (be.accumulatedEnergy >= be.energyToMine) {
            int coinsToMine = (int) (be.accumulatedEnergy / be.energyToMine);
            for (int i = 0; i < coinsToMine; i++) be.mineCoin();
            be.accumulatedEnergy %= be.energyToMine;
        }

        if (be.lastEnergyConsumed != energyToConsume || level.getGameTime() % 20 == 0) {
            be.lastEnergyConsumed = energyToConsume;
            setChanged(level, pos, state);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        }
    }

    // This method was accidentally removed and is now restored.
    private void tryConsumeCore() {
        // This logic is now handled by checking the core's durability directly.
        // This method is a placeholder in case we want to add logic for when a new core is inserted.
    }

    public ItemStack removeCoreForPlayer() {
        return this.itemHandler.extractItem(1, 1, false);
    }

    private void handleCooling() {
        if (this.heat > 0) {
            int coolingPower = Config.PASSIVE_COOLING_RATE.get();
            if (!this.fluidTank.isEmpty()) {
                if (this.fluidTank.getFluid().getFluid() == Fluids.WATER || this.fluidTank.getFluid().getFluid() == ModFluids.SOURCE_CRYOTHEUM_COOLANT.get()) {
                    coolingPower += Config.ACTIVE_COOLING_RATE.get();
                    this.fluidTank.drain(1, FluidTank.FluidAction.EXECUTE);
                }
            }
            this.heat = Math.max(0, this.heat - coolingPower);
        }
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
        tag.putInt("heat", this.heat);
        tag.putBoolean("isCoolingDown", this.isCoolingDown);
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
        this.heat = tag.getInt("heat");
        this.isCoolingDown = tag.getBoolean("isCoolingDown");
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

        if (this.isCoolingDown) {
            tooltip.add(Component.literal("  ").append(Component.translatable("goggle." + modId + ".info.overheated").withStyle(ChatFormatting.RED)));
        } else {
            tooltip.add(Component.literal("  ")
                    .append(Component.translatable("goggle." + modId + ".info.heat"))
                    .append(Component.literal(": " + this.heat + "%").withStyle(this.heat > 75 ? ChatFormatting.YELLOW : ChatFormatting.WHITE)));
        }

        tooltip.add(Component.literal("  ")
                .append(Component.translatable("goggle." + modId + ".info.coolant"))
                .append(Component.literal(": " + this.fluidTank.getFluidAmount() + "mb ").append(this.fluidTank.getFluid().getDisplayName()).withStyle(ChatFormatting.AQUA)));

        tooltip.add(Component.literal("  ").append(Component.translatable("goggle." + modId + ".info.usage")).append(Component.literal(": " + this.lastEnergyConsumed + " FE/t").withStyle(ChatFormatting.AQUA)));

        double coinsPerMinute = 0;
        if (this.lastEnergyConsumed > 0 && !this.isCoolingDown) {
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