package com.divnectar.createblockchain.block;

import com.divnectar.createblockchain.Config;
import com.divnectar.createblockchain.block.entity.PiggyBankBlockEntity;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import dev.ithundxr.createnumismatics.content.backend.Coin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public class PiggyBankBlock extends BaseEntityBlock implements IWrenchable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final MapCodec<PiggyBankBlock> CODEC = simpleCodec(PiggyBankBlock::new);
    // Add a property to store the block's horizontal facing direction
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public PiggyBankBlock(Properties pProperties) {
        super(pProperties);
        // Set the default state for our new property
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        // Add the FACING property to the list of valid states for this block
        builder.add(FACING);
    }

    // This method is called when the block is placed to determine its initial state
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new PiggyBankBlockEntity(pPos, pState);
    }

    private int getWeightedCoinAmount(RandomSource random) {
        float chance = random.nextFloat();

        if (chance < 0.01f) {
            return 250;
        } else if (chance < 0.1f) {
            return random.nextInt(151, 250);
        } else if (chance < 0.4f) {
            return random.nextInt(51, 151);
        } else {
            return random.nextInt(6, 51);
        }
    }
}