package com.divnectar.createblockchain.block;

import com.divnectar.createblockchain.CreateBlockchain;
import com.divnectar.createblockchain.block.entity.CurrencyMinerBlockEntity;
import com.divnectar.createblockchain.block.entity.PiggyBankBlockEntity;
import com.divnectar.createblockchain.fluid.ModFluids;
import com.divnectar.createblockchain.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
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

    // Block Definitions
    public static final DeferredHolder<Block, Block> CURRENCY_MINER = BLOCKS.register("currency_miner",
            () -> new CurrencyMinerBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.0f).requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredHolder<Block, Block> MINING_CORE_GEODE = BLOCKS.register("mining_core_geode",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE)
                    .strength(3.0f, 6.0f)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(state -> 4)));

    public static final DeferredHolder<Block, LiquidBlock> CRYOTHEUM_COOLANT_BLOCK = BLOCKS.register("cryotheum_coolant_block",
            () -> new LiquidBlock(ModFluids.SOURCE_CRYOTHEUM_COOLANT.get(), BlockBehaviour.Properties.of().noCollission().strength(100f).noLootTable().replaceable()));

    public static final DeferredHolder<Block, Block> PIGGY_BANK = BLOCKS.register("piggy_bank",
            () -> new PiggyBankBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_PINK).strength(1.0f).sound(SoundType.DECORATED_POT).noOcclusion()));

    // Block Entity Type Definitions
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CurrencyMinerBlockEntity>> CURRENCY_MINER_BE =
            BLOCK_ENTITIES.register("currency_miner_be", () ->
                    BlockEntityType.Builder.of(CurrencyMinerBlockEntity::new, CURRENCY_MINER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PiggyBankBlockEntity>> PIGGY_BANK_BE =
            BLOCK_ENTITIES.register("piggy_bank_be", () ->
                    BlockEntityType.Builder.of(PiggyBankBlockEntity::new, PIGGY_BANK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
    }
}