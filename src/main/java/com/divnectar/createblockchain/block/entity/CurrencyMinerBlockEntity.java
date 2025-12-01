package com.divnectar.createblockchain.block.entity;

import com.divnectar.createblockchain.Config;
import com.divnectar.createblockchain.CreateBlockchain;
import com.divnectar.createblockchain.block.ModBlocks;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;

public class CurrencyMinerBlockEntity extends BlockEntity implements IHaveGoggleInformation {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final EnergyStorage energyStorage;
    private final ItemStackHandler itemHandler = new ItemStackHandler(1); // One slot for the output coin

    private long energyToMine;
    private long accumulatedEnergy = 0;
    private int lastEnergyConsumed = 0;

    public CurrencyMinerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CURRENCY_MINER_BE.get(), pos, state);
        this.energyStorage = new EnergyStorage(Config.ENERGY_CAPACITY.get(), Config.MAX_ENERGY_CONSUMPTION.get(), Config.MAX_ENERGY_CONSUMPTION.get());
        this.energyToMine = Config.BASE_ENERGY_PER_COIN.get();
    }

    public EnergyStorage getEnergyStorage() {
        return this.energyStorage;
    }

    public ItemStackHandler getItemHandler() {
        return this.itemHandler;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CurrencyMinerBlockEntity be) {
        if (level.isClientSide) {
            return;
        }

        be.updateMiningCost();

        int energyToConsume = be.energyStorage.extractEnergy(Config.MAX_ENERGY_CONSUMPTION.get(), true);

        if (energyToConsume > 0) {
            be.energyStorage.extractEnergy(energyToConsume, false);
            be.accumulatedEnergy += energyToConsume;
            LOGGER.info("CurrencyMiner at {} consumed {} FE this tick (accumulated: {} FE)", be.getBlockPos(), energyToConsume, be.accumulatedEnergy);
        }

        if (be.accumulatedEnergy >= be.energyToMine) {
            int coinsToMine = (int) (be.accumulatedEnergy / be.energyToMine);
            for (int i = 0; i < coinsToMine; i++) {
                be.mineCoin();
            }
            be.accumulatedEnergy %= be.energyToMine;
        }

        if (be.lastEnergyConsumed != energyToConsume || level.getGameTime() % 20 == 0) {
            be.lastEnergyConsumed = energyToConsume;
            setChanged(level, pos, state);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        }
    }

    private void mineCoin() {
        if (this.level instanceof ServerLevel serverLevel) {
            CurrencyTracker.get(serverLevel).incrementMined();

            // Get the coin type from the config file
            String coinTypeName = Config.COIN_TO_GENERATE.get().toUpperCase();
            Coin coinToMine;
            try {
                coinToMine = Coin.valueOf(coinTypeName);
            } catch (IllegalArgumentException e) {
                // If the user enters an invalid name, log a warning and default to COPPER.
                LOGGER.warn("Invalid coin type '{}' in config. Defaulting to COPPER.", coinTypeName);
                coinToMine = Coin.SPUR;
            }

            ItemStack coinStack = new ItemStack(coinToMine.asStack().getItem());
            serverLevel.playSound(null, this.worldPosition, ModSounds.COIN_CHACHING.get(), SoundSource.BLOCKS, 0.5f, 1.5f);
            this.itemHandler.insertItem(0, coinStack, false);
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
        tag.put("inventory", itemHandler.serializeNBT(provider)); // Save the inventory
        tag.putInt("lastEnergyConsumed", this.lastEnergyConsumed);
        tag.putLong("energyToMine", this.energyToMine);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.accumulatedEnergy = tag.getLong("accumulatedEnergy");
        if (tag.contains("energy", Tag.TAG_COMPOUND)) {
            energyStorage.deserializeNBT(provider, tag.getCompound("energy"));
        }
        if (tag.contains("inventory", Tag.TAG_COMPOUND)) {
            itemHandler.deserializeNBT(provider, tag.getCompound("inventory")); // Load the inventory
        }
        this.lastEnergyConsumed = tag.getInt("lastEnergyConsumed");
        this.energyToMine = tag.getLong("energyToMine");
    }

    // --- GOGGLE INFORMATION ---

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        String modId = CreateBlockchain.MODID;

        tooltip.add(Component.translatable("goggle." + modId + ".info.header"));

        tooltip.add(Component.literal("  ")
                .append(Component.translatable("goggle." + modId + ".info.usage"))
                .append(Component.literal(": " + this.lastEnergyConsumed + " FE/t").withStyle(ChatFormatting.AQUA)));

        double coinsPerMinute = 0;
        if (this.lastEnergyConsumed > 0) {
            double ticksPerCoin = (double) this.energyToMine / this.lastEnergyConsumed;
            coinsPerMinute = (1200.0) / ticksPerCoin;
        }
        tooltip.add(Component.literal("  ")
                .append(Component.translatable("goggle." + modId + ".info.rate"))
                .append(Component.literal(String.format(": %.2f Coins/min", coinsPerMinute)).withStyle(ChatFormatting.GREEN)));

        tooltip.add(Component.literal("  ")
                .append(Component.translatable("goggle." + modId + ".info.cost"))
                .append(Component.literal(": " + this.energyToMine + " FE").withStyle(ChatFormatting.GOLD)));

        // Add inventory information
        ItemStack storedStack = this.itemHandler.getStackInSlot(0);
        tooltip.add(Component.literal("  ")
                .append(Component.translatable("goggle." + modId + ".info.stored"))
                .append(Component.literal(": " + storedStack.getCount() + " ").append(storedStack.getHoverName()).withStyle(ChatFormatting.WHITE)));

        return true;
    }
}