package com.divnectar.createblockchain.datagen;

import com.divnectar.createblockchain.CreateBlockchain;
import com.divnectar.createblockchain.block.ModBlocks;
import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.MechanicalCraftingRecipeBuilder;
import com.simibubi.create.api.data.recipe.MixingRecipeGen;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, pRegistries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        // Recipe generation logic will be placed here.
        MechanicalCraftingRecipeBuilder.shapedRecipe(ModBlocks.CURRENCY_MINER.get())
                // Define the 5x4 pattern of the recipe
                .patternLine("AAAAAA")
                .patternLine("ABEEBA")
                .patternLine("AILCIA")
                .patternLine("ABWWBA")
                .patternLine("AAAAAA")
                // Define what each character in the pattern corresponds to
                .key('A', BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:andesite_alloy")))
                .key('B', BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:brass_casing")))
                .key('E', BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:electron_tube")))
                .key('L', BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:redstone_link")))
                .key('C', BuiltInRegistries.ITEM.get(ResourceLocation.parse("numismatics:crown")))
                .key('I', BuiltInRegistries.ITEM.get(ResourceLocation.parse("createaddition:iron_wire")))
                .key('W', BuiltInRegistries.ITEM.get(ResourceLocation.parse("createaddition:copper_wire")))
                .build(recipeOutput, ResourceLocation.fromNamespaceAndPath(CreateBlockchain.MODID, "mechanical_crafting/currency_miner"));
        

//        // Example 1: Simple recipe - Netherrack + Water -> Magma Block
//        new MixingRecipe(recipeId("magma_block_from_netherrack"))
//                .require(Items.NETHERRACK)
//                .require(Fluids.WATER, 250) // Requires 250mb of Water
//                .output(Items.MAGMA_BLOCK)
//                .duration(200) // Ticks to process
//                .save(recipeOutput); // Use .save() instead of .register()


    }
}
