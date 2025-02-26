package com.st0x0ef.beyond_earth.client.renderers.entities.rocket.big;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import com.st0x0ef.beyond_earth.BeyondEarth;
import com.st0x0ef.beyond_earth.client.renderers.entities.VehicleRenderer;
import com.st0x0ef.beyond_earth.common.entities.RocketEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BigRocketRenderer extends VehicleRenderer<RocketEntity, BigRocketModel<RocketEntity>> {
    public ResourceLocation TEXTURE = new ResourceLocation(BeyondEarth.MODID, "textures/vehicle/rocket_skin/big/standard.png");

    public BigRocketRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new BigRocketModel<>(renderManagerIn.bakeLayer(BigRocketModel.LAYER_LOCATION)), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(RocketEntity rocket) {
        return new ResourceLocation(BeyondEarth.MODID, rocket.getEntityData().get(RocketEntity.SKIN_TEXTURE_PATH));
     }

    @Override
    protected boolean isShaking(RocketEntity rocket) {
        return rocket.getEntityData().get(RocketEntity.ROCKET_START);
    }
}