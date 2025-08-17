package com.divnectar.createblockchain.datagen;

import com.divnectar.createblockchain.worldgen.ModFeatures;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public class ModPlacedFeatureProvider {

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        // FIX: Changed the type here from HolderGetter.Reference to Holder.Reference
        Holder.Reference<ConfiguredFeature<?, ?>> miningCoreGeode = configuredFeatures.getOrThrow(ModFeatures.MINING_CORE_GEODE_KEY);

        register(context, ModFeatures.MINING_CORE_GEODE_PLACED_KEY,
                miningCoreGeode, // Pass the corrected variable here
                List.of(
                        RarityFilter.onAverageOnceEvery(25),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(0))
                        // FIX: Removed the BiomeFilter.simple() line
                ));
    }


    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key,
                                 // FIX: Changed the parameter type here to Holder.Reference
                                 Holder.Reference<ConfiguredFeature<?, ?>> feature, List<PlacementModifier> placementModifiers) {
        context.register(key, new PlacedFeature(feature, placementModifiers));
    }
}