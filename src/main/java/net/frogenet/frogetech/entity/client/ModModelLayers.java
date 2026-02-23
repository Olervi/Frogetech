package net.frogenet.frogetech.entity.client;

import net.frogenet.frogetech.Frogetech;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;

public class ModModelLayers {
    public static final ModelLayerLocation FROG_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Frogetech.MODID, "frog"), "main");

}
