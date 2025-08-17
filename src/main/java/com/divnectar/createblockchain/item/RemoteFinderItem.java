package com.divnectar.createblockchain.item;

import com.divnectar.createblockchain.block.ModBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.ComponentEnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.IEventBus;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;


import javax.annotation.Nullable;

public class RemoteFinderItem extends Item {

    private static final String MODID = "createblockchain";
    private static final int ENERGY_CAPACITY = 10000;
    private static final int MAX_RECEIVE = 500;
    private static final int MAX_EXTRACT = 500;
    private static final int ENERGY_PER_SCAN = 150;
    private static final int SEARCH_RADIUS = 128;
    private static final float MIN_PITCH = 0.5f;
    private static final float MAX_PITCH = 2.0f;

    // DataComponent registration using DeferredHolder (2-type-args version)
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE, MODID);

    // THIS IS THE CORRECTED LINE
    public static DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ENERGY_COMPONENT;

    public RemoteFinderItem(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energy == null) return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);

        if (energy.extractEnergy(ENERGY_PER_SCAN, true) < ENERGY_PER_SCAN) {
            if (!level.isClientSide)
                player.displayClientMessage(Component.literal("Remote Finder is out of power!").withStyle(ChatFormatting.RED), true);
            playBeep(level, player, MIN_PITCH);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        energy.extractEnergy(ENERGY_PER_SCAN, false);

        if (!level.isClientSide) {
            BlockPos start = player.blockPosition();
            BlockPos nearest = findNearestSpiralingOutwards(level, start, SEARCH_RADIUS);

            if (nearest != null) {
                double dist = Math.sqrt(start.distSqr(nearest));
                float norm = (float) Mth.clamp(dist / SEARCH_RADIUS, 0.0, 1.0);
                float pitch = Mth.clamp(MAX_PITCH - (MAX_PITCH - MIN_PITCH) * norm, MIN_PITCH, MAX_PITCH);
                playBeep(level, player, pitch);

                player.displayClientMessage(Component.literal("Mining core detected: " + (int) dist + " blocks")
                        .withStyle(ChatFormatting.GREEN), true);
            } else {
                playBeep(level, player, MIN_PITCH + 0.2f);
                player.displayClientMessage(Component.literal("No mining core in range.").withStyle(ChatFormatting.GRAY), true);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energy == null) {
            return 0;
        }
        // Calculate the width of the bar (0-13) based on energy percentage
        return Math.round(13.0F * energy.getEnergyStored() / energy.getMaxEnergyStored());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energy == null) {
            return super.getBarColor(stack);
        }

        // Calculate a color gradient from red (empty) to green (full)
        float fraction = (float) energy.getEnergyStored() / (float) energy.getMaxEnergyStored();
        return Mth.hsvToRgb(Math.max(0.0F, fraction) / 3.0F, 1.0F, 1.0F);
    }

    // get rid of this in favor of the spiraling search
//    @Nullable
//    private static BlockPos findNearest(Level level, BlockPos origin, int radius) {
////        if (ModBlocks.MINING_CORE_GEODE == null) return null;
//        int bestDist2 = Integer.MAX_VALUE;
//        BlockPos best = null;
//        BlockPos min = origin.offset(-radius, -radius, -radius);
//        BlockPos max = origin.offset(radius, radius, radius);
//        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
//            BlockState state = level.getBlockState(pos);
//            if (state.is(ModBlocks.MINING_CORE_GEODE)) {
//                int d2 = (int) origin.distSqr(pos);
//                if (d2 < bestDist2) {
//                    bestDist2 = d2;
//                    best = pos.immutable();
//                }
//            }
//        }
//        return best;
//    }

@Nullable
private static BlockPos findNearestSpiralingOutwards(Level level, BlockPos origin, int radius) {
    BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

    int minY = level.getMinBuildHeight();
    int maxY = origin.getY();

    // Check the origin block first using the correct block reference
    if (level.getBlockState(origin).is(ModBlocks.MINING_CORE_GEODE.get())) {
        return origin;
    }

    // Search in expanding square rings outwards from the origin
    for (int r = 1; r <= radius; r++) {
        for (int y = maxY; y >= minY; y--) { // Scan from top to bottom
            for (int x = -r; x <= r; x++) {
                for (int z = -r; z <= r; z++) {
                    // Only check the outer ring of the current radius 'r'
                    if (Math.abs(x) != r && Math.abs(z) != r) {
                        continue;
                    }

                    mutablePos.set(origin.getX() + x, y, origin.getZ() + z);
                    // Also check here using the correct block reference
                    if (level.getBlockState(mutablePos).is(ModBlocks.MINING_CORE_GEODE.get())) {
                        return mutablePos.immutable(); // Found the nearest one!
                    }
                }
            }
        }
    }

    return null; // Nothing found in the entire radius
}

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        IEnergyStorage energy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energy != null) {
            // Add the energy status text to the tooltip
            tooltip.add(Component.literal("Energy: " + energy.getEnergyStored() + " / " + energy.getMaxEnergyStored() + " FE")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private static void playBeep(Level level, Player player, float pitch) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 1.0f, pitch);
    }

    // --- Capability registration ---
    public static class CapabilityHooks {
        @SubscribeEvent
        public static void registerCaps(RegisterCapabilitiesEvent event) {
            event.registerItem(
                    Capabilities.EnergyStorage.ITEM,
                    (stack, ctx) -> new ComponentEnergyStorage(stack, ENERGY_COMPONENT.value(), ENERGY_CAPACITY, MAX_RECEIVE, MAX_EXTRACT),
                    ModItems.REMOTE_FINDER.get()
            );
        }
    }

    public static void registerDataComponents(IEventBus modBus) {
        ENERGY_COMPONENT = DATA_COMPONENTS.register("remote_finder_energy",
                () -> DataComponentType.<Integer>builder()
                        .persistent(Codec.INT)
                        .build()
        );

        DATA_COMPONENTS.register(modBus);
    }
}