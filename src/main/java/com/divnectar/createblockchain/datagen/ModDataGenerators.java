package com.divnectar.createblockchain.datagen;

import com.divnectar.createblockchain.CreateBlockchain;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder; // <-- Import this
import net.minecraft.core.registries.Registries; // <-- Import this
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider; // <-- Import this
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set; // <-- Import this
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = CreateBlockchain.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModDataGenerators {

    // 1. Define the RegistrySetBuilder.
    // This tells the game which bootstrap methods to run for which registries.
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatureProvider::bootstrap)
            .add(Registries.PLACED_FEATURE, ModPlacedFeatureProvider::bootstrap);

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        // Your existing providers
        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new ModGlobalLootModifiersProvider(packOutput, lookupProvider));
        generator.addProvider(event.includeServer(), new ModFluidTagsProvider(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModBlockTagsProvider(packOutput, lookupProvider, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModLootTableProvider(packOutput, lookupProvider));

        // 2. Add the DatapackBuiltinEntriesProvider.
        // This is the special provider that runs the RegistrySetBuilder.
        generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(
                packOutput, lookupProvider, BUILDER, Set.of(CreateBlockchain.MODID)
        ));
    }
}