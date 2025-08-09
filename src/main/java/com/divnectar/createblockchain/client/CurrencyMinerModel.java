package com.divnectar.createblockchain.client;

import com.divnectar.createblockchain.CreateBlockchain;
import com.divnectar.createblockchain.block.entity.CurrencyMinerBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CurrencyMinerModel extends GeoModel<CurrencyMinerBlockEntity> {
    @Override
    public ResourceLocation getModelResource(CurrencyMinerBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(CreateBlockchain.MODID, "geo/currency_miner.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CurrencyMinerBlockEntity animatable) {
        // Since you only have one texture for all states, we always return it.
        // This will fix the missing texture issue when the block is powered.
        return ResourceLocation.fromNamespaceAndPath(CreateBlockchain.MODID, "textures/block/currency_miner.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CurrencyMinerBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(CreateBlockchain.MODID, "animations/currency_miner.animation.json");
    }
}