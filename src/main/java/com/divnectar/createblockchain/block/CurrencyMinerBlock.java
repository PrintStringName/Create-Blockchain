package com.divnectar.createblockchain.block;

import com.divnectar.createblockchain.block.entity.CurrencyMinerBlockEntity;
import com.divnectar.createblockchain.item.ModItems;
import com.mojang.serialization.MapCodec;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CurrencyMinerBlock extends BaseEntityBlock implements IWrenchable {
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final MapCodec<CurrencyMinerBlock> CODEC = simpleCodec(CurrencyMinerBlock::new);

    public CurrencyMinerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(POWERED, false)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);
            if (blockEntity instanceof CurrencyMinerBlockEntity minerEntity) {
                ItemStack heldItem = pPlayer.getItemInHand(pHand);

                // Logic for shift-right-clicking with an empty hand to remove the core
                if (pPlayer.isShiftKeyDown() && heldItem.isEmpty()) {
                    // Call the new, unrestricted method for player interaction
                    ItemStack coreStack = minerEntity.removeCoreForPlayer();
                    if (!coreStack.isEmpty()) {
                        pPlayer.setItemInHand(pHand, coreStack);
//                        pLevel.playSound(null, pPos, SoundEvents.ARMOR_EQUIP_IRON, SoundSource.BLOCKS, 1.0f, 0.8f);
                        return ItemInteractionResult.SUCCESS;
                    }
                }

                // Logic for right-clicking with a core to insert it
                else if (heldItem.is(ModItems.MINING_CORE.get())) {
                    ItemStack remainder = minerEntity.getItemHandler().insertItem(1, heldItem, false);
                    if (remainder.getCount() < heldItem.getCount()) {
                        pPlayer.setItemInHand(pHand, remainder);
//                        pLevel.playSound(null, pPos, SoundEvents.ARMOR_EQUIP_IRON, SoundSource.BLOCKS, 1.0f, 1.0f);
                        return ItemInteractionResult.SUCCESS;
                    }
                }
            }
        }
        return ItemInteractionResult.FAIL;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CurrencyMinerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlocks.CURRENCY_MINER_BE.get(), CurrencyMinerBlockEntity::tick);
    }
}
