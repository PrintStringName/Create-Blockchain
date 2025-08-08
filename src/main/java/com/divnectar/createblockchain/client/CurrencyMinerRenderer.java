package com.divnectar.createblockchain.client;

import com.divnectar.createblockchain.CreateBlockchain;
import com.divnectar.createblockchain.block.entity.CurrencyMinerBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultGeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

public class CurrencyMinerRenderer extends GeoBlockRenderer<CurrencyMinerBlockEntity> {
    public CurrencyMinerRenderer(BlockEntityRendererProvider.Context context) {
        // The model resource location points to your .geo.json file
        super(new DefaultGeoModel<>(ResourceLocation.fromNamespaceAndPath(CreateBlockchain.MODID, "geo/currency_miner.geo.json")));
    }
}