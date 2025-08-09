package com.divnectar.createblockchain.util;

import com.divnectar.createblockchain.block.CurrencyMinerBlock;
import com.divnectar.createblockchain.block.ModBlocks;
import com.divnectar.createblockchain.block.entity.CurrencyMinerBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class CapabilityRegistry {

    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Register Energy Capability
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlocks.CURRENCY_MINER_BE.get(),
                (blockEntity, context) -> {
                    Direction facing = blockEntity.getBlockState().getValue(CurrencyMinerBlock.FACING);
                    // EAST and WEST relative to where the block is facing
                    if (context == facing.getClockWise() || context == facing.getCounterClockWise()) {
                        return blockEntity.getEnergyStorage();
                    }
                    return null;
                }
        );

        // Register Item Handler Capability
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlocks.CURRENCY_MINER_BE.get(),
                (blockEntity, context) -> {
                    Direction facing = blockEntity.getBlockState().getValue(CurrencyMinerBlock.FACING);
                    // SOUTH (back) relative to where the block is facing
                    if (context == facing.getOpposite()) {
                        return new SidedItemHandler(blockEntity.getItemHandler(), 0, false, true);
                    }
                    // NORTH (front) relative to where the block is facing
                    if (context == facing) {
                        return new SidedItemHandler(blockEntity.getItemHandler(), 1, true, false);
                    }
                    return null;
                }
        );

        // Register Fluid Handler Capability
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlocks.CURRENCY_MINER_BE.get(),
                (blockEntity, context) -> {
                    // UP is always up, regardless of rotation
                    if (context == Direction.UP) {
                        return blockEntity.getFluidTank();
                    }
                    return null;
                }
        );
    }

    // Custom wrapper class to handle side-specific item interactions
    private static class SidedItemHandler implements IItemHandler {
        private final ItemStackHandler parent;
        private final int slotIndex;
        private final boolean canInsert;
        private final boolean canExtract;

        public SidedItemHandler(ItemStackHandler parent, int slotIndex, boolean canInsert, boolean canExtract) {
            this.parent = parent;
            this.slotIndex = slotIndex;
            this.canInsert = canInsert;
            this.canExtract = canExtract;
        }

        @Override
        public int getSlots() { return 1; }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            if (slot == 0) return parent.getStackInSlot(this.slotIndex);
            return ItemStack.EMPTY;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!canInsert || slot != 0) return stack;
            return parent.insertItem(this.slotIndex, stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!canExtract || slot != 0) return ItemStack.EMPTY;
            return parent.extractItem(this.slotIndex, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            if (slot == 0) return parent.getSlotLimit(this.slotIndex);
            return 0;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (slot == 0) return parent.isItemValid(this.slotIndex, stack);
            return false;
        }
    }
}