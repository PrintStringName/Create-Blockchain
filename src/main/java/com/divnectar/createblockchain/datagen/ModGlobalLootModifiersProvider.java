package com.divnectar.createblockchain.datagen;

import com.divnectar.createblockchain.CreateBlockchain; // Your main mod class
import com.divnectar.createblockchain.item.ModItems;
import com.divnectar.createblockchain.loot.AddItemModifier;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import java.util.concurrent.CompletableFuture;

public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifiersProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider, CreateBlockchain.MODID);
    }

    @Override
    protected void start() {
        // Modifier for various treasure chests
        add("miner_core_in_treasure", new AddItemModifier(new LootItemCondition[] {
                // Use AnyOfCondition to target multiple loot tables
                AnyOfCondition.anyOf(
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:chests/buried_treasure")),
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:chests/shipwreck_treasure"))
                ).build(),
                // Condition for a 20% chance to apply
                LootItemRandomChanceCondition.randomChance(0.2f).build()
        }, new ItemStack(ModItems.MINING_CORE.get()))); // Pass an ItemStack

        add("piggy_banks_in_chests", new AddItemModifier(new LootItemCondition[] {
                // Use AnyOfCondition to target multiple loot tables
                AnyOfCondition.anyOf(
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:chests/buried_treasure")),
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:chests/shipwreck_treasure")),
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:chests/simple_dungeon")),
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:chests/abandoned_mineshaft")),
                        LootTableIdCondition.builder(ResourceLocation.parse("minecraft:chests/desert_pyramid"))
                ).build(),
                // Condition for a 20% chance to apply
                LootItemRandomChanceCondition.randomChance(0.3f).build()
        }, new ItemStack(ModItems.PIGGY_BANK_ITEM.get()))); // Pass an ItemStack

        // Modifier specifically for Trial Chamber reward chests
        add("miner_core_in_trial_chambers", new AddItemModifier(new LootItemCondition[] {
                LootTableIdCondition.builder(
                        ResourceLocation.parse("minecraft:chests/trial_chambers/reward")
                ).build(),
                LootItemRandomChanceCondition.randomChance(0.2f).build()
        }, new ItemStack(ModItems.MINING_CORE.get()))); // Pass an ItemStack
    }
}