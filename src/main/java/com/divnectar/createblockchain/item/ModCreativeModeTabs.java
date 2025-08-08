package com.divnectar.createblockchain.item;

import com.divnectar.createblockchain.CreateBlockchain;
import com.divnectar.createblockchain.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateBlockchain.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BLOCKCHAIN_TAB = CREATIVE_MODE_TABS.register("createblockchain_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.CURRENCY_MINER.get().asItem())) // Set the tab icon
                    .title(Component.translatable("creativetab.createblockchain_tab")) // Set the tab title
                    .displayItems((pParameters, pOutput) -> {
                        // Add all your items and blocks to the tab here
                        pOutput.accept(ModBlocks.CURRENCY_MINER.get());
                        pOutput.accept(ModItems.MINING_CORE.get());
                        pOutput.accept(ModItems.CRYOTHEUM_COOLANT_BUCKET.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}