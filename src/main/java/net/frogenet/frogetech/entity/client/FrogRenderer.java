package net.frogenet.frogetech.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.frogenet.frogetech.Frogetech;
import net.frogenet.frogetech.entity.custom.FrogEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class FrogRenderer extends GeoEntityRenderer<FrogEntity> {
    public FrogRenderer(EntityRendererProvider.Context context) {
        super(context, new FrogModel());
    }


    @Override
    public ResourceLocation getTextureLocation(FrogEntity frogEntity) {
        return ResourceLocation.fromNamespaceAndPath(Frogetech.MODID, "textures/entity/frog.png");
    }

    @Override
    public void render(FrogEntity entity, float entityYaw, float partialTicks,
                         PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
