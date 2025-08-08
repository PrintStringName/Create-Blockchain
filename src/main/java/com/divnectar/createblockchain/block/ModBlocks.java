package com.divnectar.createblockchain.block;

import com.divnectar.createblockchain.CreateBlockchain;
import com.divnectar.createblockchain.block.entity.CurrencyMinerBlockEntity;
import com.divnectar.createblockchain.fluid.ModFluids;
import com.divnectar.createblockchain.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, CreateBlockchain.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CreateBlockchain.MODID);

    public static final DeferredHolder<Block, Block> CURRENCY_MINER = BLOCKS.register("currency_miner",
            () -> new CurrencyMinerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0f).requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredHolder<Item, BlockItem> CURRENCY_MINER_ITEM = ModItems.ITEMS.register("currency_miner",
            () -> new BlockItem(CURRENCY_MINER.get(), new Item.Properties()));

    // Register the physical block for our new fluid
    public static final DeferredHolder<Block, LiquidBlock> CRYOTHEUM_COOLANT_BLOCK = BLOCKS.register("cryotheum_coolant_block",
            () -> new LiquidBlock(ModFluids.SOURCE_CRYOTHEUM_COOLANT.get(), BlockBehaviour.Properties.of().noCollission().strength(100f).noLootTable()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CurrencyMinerBlockEntity>> CURRENCY_MINER_BE =
            BLOCK_ENTITIES.register("currency_miner_be", () ->
                    BlockEntityType.Builder.of(CurrencyMinerBlockEntity::new, CURRENCY_MINER.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
    }
}