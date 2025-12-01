package com.divnectar.createblockchain.util;

import com.divnectar.createblockchain.block.ModBlocks;
import com.divnectar.createblockchain.block.entity.CurrencyMinerBlockEntity;
import net.minecraft.core.Direction;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public class CapabilityRegistry {

    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Register Energy Capability
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlocks.CURRENCY_MINER_BE.get(),
                (blockEntity, context) -> {
                    // Provide the energy capability on all sides (and when context is null).
                    // Log the capability request for debugging external mod interactions.
                    LOGGER.info("Energy capability requested for CurrencyMiner at {} side={}", blockEntity.getBlockPos(), context);
                    return blockEntity.getEnergyStorage();
                }
        );

        // Register Item Handler Capability
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlocks.CURRENCY_MINER_BE.get(),
                (blockEntity, context) -> {
                    // Only provide the item handler capability on the SOUTH side.
                    LOGGER.info("ItemHandler capability requested for CurrencyMiner at {} side={}", blockEntity.getBlockPos(), context);
                    if (context == Direction.SOUTH) {
                        LOGGER.info("Providing ItemHandler for CurrencyMiner at {}", blockEntity.getBlockPos());
                        return blockEntity.getItemHandler();
                    }
                    // For any other side, provide nothing.
                    return null;
                }
        );

        // NOTE: Avoiding a direct runtime registration of Forge's IEnergyStorage here because
        // the neoforge `RegisterCapabilitiesEvent.registerBlockEntity` API expects a
        // `BlockCapability<T,C>` from neoforge. Attempting to pass a reflected Forge capability
        // object caused a compile-time type mismatch. If Forge compatibility is required,
        // implement a separate runtime adapter that registers via Forge's event bus (using
        // reflection) or add a compile-time optional module that depends on Forge.
    }
}