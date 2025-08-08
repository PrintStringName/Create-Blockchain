package com.divnectar.createblockchain.datagen;

import com.divnectar.createblockchain.CreateBlockchain;
import com.divnectar.createblockchain.fluid.ModFluids;
import com.simibubi.create.api.data.recipe.MixingRecipeGen;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.concurrent.CompletableFuture;

public class BlockchainMixingRecipeGen extends MixingRecipeGen {

    public BlockchainMixingRecipeGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, CreateBlockchain.MODID);
    }

//    @Override
//    public void generate() {
//        // Use the 'create' helper method from the base class to build the recipe
//        create("cryotheum_coolant_from_mixing", b -> b
//                .require(Fluids.WATER, 1000)
//                .require(Blocks.ICE)
//                .require(BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:powdered_obsidian")))
//                .require(Items.IRON_NUGGET)
//                .output(new FluidStack(ModFluids.SOURCE_CRYOTHEUM_COOLANT.get(), 1000))
//                .requiresHeat(HeatCondition.HEATED)
//        );
//    }
}