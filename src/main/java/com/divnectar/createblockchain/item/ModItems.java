package com.divnectar.createblockchain.item;

import com.divnectar.createblockchain.CreateBlockchain;
import com.divnectar.createblockchain.fluid.ModFluids;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    // Updated for 1.21.1: Must specify the registry type.
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, CreateBlockchain.MODID);

    public static final DeferredHolder<Item, Item> MINING_CORE = ITEMS.register("mining_core",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE)));

    public static final DeferredHolder<Item, BucketItem> CRYOTHEUM_COOLANT_BUCKET = ITEMS.register("cryotheum_coolant_bucket",
            () -> new BucketItem(ModFluids.SOURCE_CRYOTHEUM_COOLANT.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));


    // We no longer register our own currency, as we're using Create: Numismatics.
    // This is where you would register any other custom items your mod might have.

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}