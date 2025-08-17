package com.divnectar.createblockchain.fluid;

import com.divnectar.createblockchain.CreateBlockchain;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Consumer;

public class ModFluidTypes {
    public static final ResourceLocation COOLANT_STILL_RL = ResourceLocation.fromNamespaceAndPath(CreateBlockchain.MODID, "block/cryotheum_coolant_still");
    public static final ResourceLocation COOLANT_FLOWING_RL = ResourceLocation.fromNamespaceAndPath(CreateBlockchain.MODID, "block/cryotheum_coolant_flow");

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, CreateBlockchain.MODID);

    public static final DeferredHolder<FluidType, FluidType> CRYOTHEUM_COOLANT_TYPE = FLUID_TYPES.register("cryotheum_coolant",
            () -> new FluidType(FluidType.Properties.create()
                    .lightLevel(2)
                    .density(15)
                    .viscosity(5)
                    .temperature(200)
                    .canSwim(true)
                    .canDrown(true)
                    .canPushEntity(true)
                    .supportsBoating(true)
                    .canHydrate(true)
                    .motionScale(0.014D)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)) {

                @Override
                public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
                    consumer.accept(new IClientFluidTypeExtensions() {
                        @Override
                        public ResourceLocation getStillTexture() {
                            return COOLANT_STILL_RL;
                        }

                        @Override
                        public ResourceLocation getFlowingTexture() {
                            return COOLANT_FLOWING_RL;
                        }
                    });
                }
            }
    );

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }
}