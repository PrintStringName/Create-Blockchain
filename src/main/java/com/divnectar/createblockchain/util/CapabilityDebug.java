package com.divnectar.createblockchain.util;

import com.divnectar.createblockchain.block.entity.CurrencyMinerBlockEntity;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.reflect.Method;

public class CapabilityDebug {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Reflection-based inspector for the miner's energy storage.
     * Call from server-side code (e.g., during testing) to log stored/max energy.
     */
    public static void logEnergy(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof CurrencyMinerBlockEntity miner)) {
            LOGGER.warn("No CurrencyMiner at {} to inspect.", pos);
            return;
        }

        Object energy = miner.getEnergyStorage();
        if (energy == null) {
            LOGGER.warn("CurrencyMiner at {} has no energy storage instance.", pos);
            return;
        }

        try {
            Method getStored = energy.getClass().getMethod("getEnergyStored");
            Method getMax = energy.getClass().getMethod("getMaxEnergyStored");
            Object stored = getStored.invoke(energy);
            Object max = getMax.invoke(energy);
            LOGGER.info("CurrencyMiner at {}: energy={} / {} FE", pos, stored, max);
        } catch (NoSuchMethodException e) {
            LOGGER.warn("Energy storage at {} does not expose getEnergyStored/getMaxEnergyStored methods.", pos);
        } catch (Throwable t) {
            LOGGER.error("Error while inspecting energy storage at {}", pos, t);
        }
    }

    @Nullable
    public static CurrencyMinerBlockEntity getMiner(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CurrencyMinerBlockEntity) return (CurrencyMinerBlockEntity) be;
        return null;
    }
}
