package com.divnectar.createblockchain.datagen;

import com.divnectar.createblockchain.CreateBlockchain;
import com.divnectar.createblockchain.fluid.ModFluids;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.tags.FluidTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModFluidTagsProvider extends FluidTagsProvider {
    public ModFluidTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, CreateBlockchain.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        // Tag our custom fluid as a type of water.
        // This allows it to interact with pipes and other modded components.
        this.tag(FluidTags.WATER)
                .add(ModFluids.SOURCE_CRYOTHEUM_COOLANT.get())
                .add(ModFluids.FLOWING_CRYOTHEUM_COOLANT.get());

        this.tag(Tags.Fluids.WATER)
                .add(ModFluids.SOURCE_CRYOTHEUM_COOLANT.get())
                .add(ModFluids.FLOWING_CRYOTHEUM_COOLANT.get());

    }
}