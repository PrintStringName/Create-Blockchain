package com.divnectar.createblockchain;

import com.divnectar.createblockchain.block.ModBlocks;
import com.divnectar.createblockchain.client.CurrencyMinerRenderer;
import com.divnectar.createblockchain.fluid.ModFluidTypes;
import com.divnectar.createblockchain.fluid.ModFluids;
import com.divnectar.createblockchain.item.ModCreativeModeTabs;
import com.divnectar.createblockchain.item.ModItems;
import com.divnectar.createblockchain.loot.AddItemModifier;
import com.divnectar.createblockchain.setup.ModLootModifiers;
import com.divnectar.createblockchain.sound.ModSounds;
import com.divnectar.createblockchain.util.CapabilityRegistry;
import com.mojang.serialization.MapCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger; // <-- ADDED IMPORT

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

// REMOVED: import static com.mojang.text2speech.Narrator.LOGGER;

@Mod(CreateBlockchain.MODID)
public class CreateBlockchain {
    public static final String MODID = "createblockchain";
    // ADDED: The standard, side-safe way to get a logger.
    private static final Logger LOGGER = LogUtils.getLogger();

    public CreateBlockchain(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        LOGGER.info("Initializing Create: Blockchain for Minecraft 1.21.1");
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModSounds.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        ModFluids.register(modEventBus);
        ModFluidTypes.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus); // Register the creative tab
        modEventBus.register(new CapabilityRegistry());

        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // This checks if we are on the physical client and registers the renderer event if so.
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::registerRenderers);
        }

    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
    }

    // This method will be called on the client during setup to register our block entity renderer.
    private void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlocks.CURRENCY_MINER_BE.get(), CurrencyMinerRenderer::new);
    }



    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }
}