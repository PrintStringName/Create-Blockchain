package com.divnectar.createblockchain.client;

import com.divnectar.createblockchain.block.entity.CurrencyMinerBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class CurrencyMinerRenderer extends GeoBlockRenderer<CurrencyMinerBlockEntity> {
    public CurrencyMinerRenderer(BlockEntityRendererProvider.Context context) {
        // The renderer now takes an instance of our new model class.
        super(new CurrencyMinerModel());
    }
}