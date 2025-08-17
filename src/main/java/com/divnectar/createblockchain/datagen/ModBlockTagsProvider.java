package com.divnectar.createblockchain.datagen;

import com.divnectar.createblockchain.CreateBlockchain;
import com.divnectar.createblockchain.block.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, CreateBlockchain.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        // Tag our custom fluid's block as replaceable so other blocks can be placed in it.
        this.tag(BlockTags.REPLACEABLE)
                .add(ModBlocks.CRYOTHEUM_COOLANT_BLOCK.get());

        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.MINING_CORE_GEODE.get());

        this.tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.MINING_CORE_GEODE.get());
    }
}