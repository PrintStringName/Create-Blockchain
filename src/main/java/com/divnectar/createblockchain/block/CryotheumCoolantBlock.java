package com.divnectar.createblockchain.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;

public class CryotheumCoolantBlock extends LiquidBlock {
    // CORRECTED: The constructor now takes the fluid object directly.
    // This simplifies the class and changes where the fluid is resolved.
    public CryotheumCoolantBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    // Optional: You can add custom behavior here if you want the block to do something special on random ticks.
    // For example, you could have it very slowly melt ice around it or have a small chance to create snow.
    // By default, it will just behave like a standard fluid source block.
    @Override
    public void randomTick(BlockState pState, net.minecraft.server.level.ServerLevel pLevel, BlockPos pPos, net.minecraft.util.RandomSource pRandom) {
        super.randomTick(pState, pLevel, pPos, pRandom);
    }
}