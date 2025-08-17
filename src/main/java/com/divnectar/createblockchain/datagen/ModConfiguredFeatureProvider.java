package com.divnectar.createblockchain.datagen;

import com.divnectar.createblockchain.block.ModBlocks;
import com.divnectar.createblockchain.worldgen.ModFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;

import java.util.ArrayList;
import java.util.List;

public class ModConfiguredFeatureProvider {

    public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {

        GeodeCrackSettings crackSettings = new GeodeCrackSettings(0.95, 2.0, 2);

        // This setting controls the thickness of the geode's layers.
        // The third value (middle layer) was causing your issue. It's been reduced.
        GeodeLayerSettings layerSettings = new GeodeLayerSettings(1.7, 2.2, 2.2, 4.3);

        // --- Build a weighted list: lots of AIR, 1–2 of your core block ---
        List<BlockState> innerPlacements = new ArrayList<>(64);
        // Tune these counts: more AIR -> fewer cores per geode on average.
        for (int i = 0; i < 62; i++) innerPlacements.add(Blocks.AIR.defaultBlockState());
        // Add your core 1–2 times to target ~1–2 cores/geode on average.
        innerPlacements.add(ModBlocks.MINING_CORE_GEODE.get().defaultBlockState());

        GeodeBlockSettings blockSettings = new GeodeBlockSettings(
                // 1. fillingProvider: What's in the absolute center. Correctly set to AIR.
                BlockStateProvider.simple(Blocks.AIR),
                // 2. innerLayerProvider: The first layer surrounding the air. Calcite is a good choice.
                BlockStateProvider.simple(Blocks.CALCITE),
                // 3. alternateInnerLayerProvider: A variation of the inner layer. Also Calcite.
                BlockStateProvider.simple(Blocks.CALCITE),
                // 4. middleLayerProvider: This is now part of the shell, NOT your valuable block.
                BlockStateProvider.simple(Blocks.CALCITE),
                // 5. outerLayerProvider: The outermost shell. Correctly set to Smooth Basalt.
                BlockStateProvider.simple(Blocks.SMOOTH_BASALT),
                // 6. innerPlacements: THIS is where you place specific blocks inside the air pocket.
                innerPlacements,
                // 7. cannotReplace tag
                BlockTags.FEATURES_CANNOT_REPLACE,
                // 8. invalidBlocks tag
                BlockTags.GEODE_INVALID_BLOCKS
        );

        register(context, ModFeatures.MINING_CORE_GEODE_KEY, Feature.GEODE, new GeodeConfiguration(
                blockSettings,
                layerSettings,
                crackSettings,
                0.4, // use_potential_placements_chance
                0.0, // use_alternate_layer0_chance
                false, // placements_require_layer0_alternate
                UniformInt.of(4, 6), // outer_wall_distance
                UniformInt.of(3, 4), // distribution_points
                UniformInt.of(1, 2), // point_offset
                -16, // min_gen_offset
                16, // max_gen_offset
                0.075, // crack_point_offset
                1 // generation_spawn_chance
        ));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(
            BootstrapContext<ConfiguredFeature<?, ?>> context,
            ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC config) {
        context.register(key, new ConfiguredFeature<>(feature, config));
    }
}