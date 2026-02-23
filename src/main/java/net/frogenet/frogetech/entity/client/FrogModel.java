package net.frogenet.frogetech.entity.client;

import net.frogenet.frogetech.Frogetech;
import net.frogenet.frogetech.entity.custom.FrogEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FrogModel extends GeoModel<FrogEntity> {
    @Override
    public ResourceLocation getModelResource(FrogEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Frogetech.MODID,
                "geo/frog.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(FrogEntity animatable){
        return ResourceLocation.fromNamespaceAndPath(Frogetech.MODID,
                "textures/entity/frog.png");
    }

    @Override
    public ResourceLocation getAnimationResource(FrogEntity animatable) {
        return ResourceLocation.fromNamespaceAndPath(Frogetech.MODID,
                "animations/frog.animation.json");
    }
}
