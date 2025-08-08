package com.divnectar.createblockchain.fluid;

import com.divnectar.createblockchain.CreateBlockchain;
import com.divnectar.createblockchain.block.ModBlocks;
import com.divnectar.createblockchain.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, CreateBlockchain.MODID);

    public static final DeferredHolder<Fluid, FlowingFluid> SOURCE_CRYOTHEUM_COOLANT = FLUIDS.register("cryotheum_coolant",
            () -> new BaseFlowingFluid.Source(ModFluids.CRYOTHEUM_COOLANT_PROPERTIES));

    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_CRYOTHEUM_COOLANT = FLUIDS.register("flowing_cryotheum_coolant",
            () -> new BaseFlowingFluid.Flowing(ModFluids.CRYOTHEUM_COOLANT_PROPERTIES));

    public static final BaseFlowingFluid.Properties CRYOTHEUM_COOLANT_PROPERTIES = new BaseFlowingFluid.Properties(
            ModFluidTypes.CRYOTHEUM_COOLANT_TYPE, SOURCE_CRYOTHEUM_COOLANT, FLOWING_CRYOTHEUM_COOLANT)
            .slopeFindDistance(2).levelDecreasePerBlock(2).block(ModBlocks.CRYOTHEUM_COOLANT_BLOCK)
            .bucket(ModItems.CRYOTHEUM_COOLANT_BUCKET);

    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }
}
