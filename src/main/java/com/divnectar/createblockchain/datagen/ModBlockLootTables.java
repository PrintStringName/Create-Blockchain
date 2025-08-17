package com.divnectar.createblockchain.datagen;

import com.divnectar.createblockchain.block.ModBlocks;
import com.divnectar.createblockchain.item.ModItems;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ModBlockLootTables extends BlockLootSubProvider {

    public ModBlockLootTables(HolderLookup.Provider provider) {
        super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        // When a MINING_CORE_GEODE is broken, it drops one MINING_CORE item.
        this.add(ModBlocks.MINING_CORE_GEODE.get(),
                (block) -> createSingleItemTable(ModItems.MINING_CORE.get()));

        // These blocks should drop themselves when broken
        this.dropSelf(ModBlocks.CURRENCY_MINER.get());
        this.add(ModBlocks.PIGGY_BANK.get(),
                createPiggyBankLootTable(Coin.SPUR.asStack().getItem()) // Or whichever coin item you have registered
        );
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        // The stream is now collected to a list to resolve the generic type mismatch.
        return ModBlocks.BLOCKS.getEntries().stream().map(DeferredHolder::get).collect(Collectors.toList());
    }

    // Helper method to create the complex loot table
    private LootTable.Builder createPiggyBankLootTable(ItemLike coinItem) {
        // This loot table will have 4 separate pools, one for each of your weighted chances.
        // The game will try each pool, and only one will succeed due to the random chance conditions.
        return LootTable.lootTable()
                // 1% chance to drop 250 coins
                .withPool(LootPool.lootPool()
                        .when(LootItemRandomChanceCondition.randomChance(0.01f)) // 1% chance
                        .add(LootItem.lootTableItem(coinItem))
                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(250.0f)))
                )
                // 9% chance (10% - 1%) to drop 151-249 coins
                .withPool(LootPool.lootPool()
                        .when(LootItemRandomChanceCondition.randomChance(0.09f)) // 9% chance
                        .add(LootItem.lootTableItem(coinItem))
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(151.0f, 249.0f)))
                )
                // 30% chance (40% - 10%) to drop 51-150 coins
                .withPool(LootPool.lootPool()
                        .when(LootItemRandomChanceCondition.randomChance(0.30f)) // 30% chance
                        .add(LootItem.lootTableItem(coinItem))
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(51.0f, 150.0f)))
                )
                // 60% chance (100% - 40%) to drop 6-50 coins
                .withPool(LootPool.lootPool()
                        .when(LootItemRandomChanceCondition.randomChance(0.60f)) // 60% chance
                        .add(LootItem.lootTableItem(coinItem))
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(6.0f, 50.0f)))
                );
    }
}

