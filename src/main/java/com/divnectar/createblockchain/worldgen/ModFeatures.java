package com.divnectar.createblockchain.worldgen;

import com.divnectar.createblockchain.CreateBlockchain;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class ModFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> MINING_CORE_GEODE_KEY = registerKey("mining_core_geode");
    public static final ResourceKey<PlacedFeature> MINING_CORE_GEODE_PLACED_KEY = registerPlacedKey("mining_core_geode_placed");

    private static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(CreateBlockchain.MODID, name));
    }

    private static ResourceKey<PlacedFeature> registerPlacedKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(CreateBlockchain.MODID, name));
    }
}