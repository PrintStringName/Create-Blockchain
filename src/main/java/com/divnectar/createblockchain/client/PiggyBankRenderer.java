package com.divnectar.createblockchain.client;

import com.divnectar.createblockchain.block.entity.PiggyBankBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class PiggyBankRenderer extends GeoBlockRenderer<PiggyBankBlockEntity> {
    public PiggyBankRenderer(BlockEntityRendererProvider.Context context) {
        super(new PiggyBankModel());
    }
}