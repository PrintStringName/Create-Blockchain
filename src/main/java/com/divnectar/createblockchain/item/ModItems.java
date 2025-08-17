package com.divnectar.createblockchain.item;

import com.divnectar.createblockchain.CreateBlockchain;
import com.divnectar.createblockchain.block.ModBlocks;
import com.divnectar.createblockchain.fluid.ModFluids;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    // Updated for 1.21.1: Must specify the registry type.
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, CreateBlockchain.MODID);

    public static final DeferredHolder<Item, Item> MINING_CORE = ITEMS.register("mining_core",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE).stacksTo(1).durability(1000)));

    public static final DeferredHolder<Item, Item> REMOTE_FINDER = ITEMS.register("remote_finder",
            () -> new RemoteFinderItem(new Item.Properties().stacksTo(1))); // stacksTo(1) makes it unique


    public static final DeferredHolder<Item, BlockItem> PIGGY_BANK_ITEM = ITEMS.register("piggy_bank",
            () -> new BlockItem(ModBlocks.PIGGY_BANK.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final DeferredHolder<Item, BlockItem> MINING_CORE_GEODE_ITEM = ModItems.ITEMS.register("mining_core_geode",
            () -> new BlockItem(ModBlocks.MINING_CORE_GEODE.get(), new Item.Properties().rarity(Rarity.RARE)));



    // Block Items
    public static final DeferredHolder<Item, BlockItem> CURRENCY_MINER_ITEM = ITEMS.register("currency_miner",
            () -> new BlockItem(ModBlocks.CURRENCY_MINER.get(), new Item.Properties()));


    public static final DeferredHolder<Item, BucketItem> CRYOTHEUM_COOLANT_BUCKET = ITEMS.register("cryotheum_coolant_bucket",
            () -> new BucketItem(ModFluids.SOURCE_CRYOTHEUM_COOLANT.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}