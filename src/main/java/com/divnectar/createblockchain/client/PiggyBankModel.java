// In src/main/java/com/divnectar/createblockchain/client/PiggyBankModel.java

package com.divnectar.createblockchain.client;

import com.divnectar.createblockchain.CreateBlockchain;
import com.divnectar.createblockchain.block.entity.PiggyBankBlockEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PiggyBankModel extends GeoModel<PiggyBankBlockEntity> {
    @Override
    public ResourceLocation getModelResource(PiggyBankBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(CreateBlockchain.MODID, "geo/piggy_bank.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(PiggyBankBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(CreateBlockchain.MODID, "textures/block/piggy_bank.png");
    }

    @Override
    public ResourceLocation getAnimationResource(PiggyBankBlockEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(CreateBlockchain.MODID, "animations/piggy_bank.animation.json");
    }
}