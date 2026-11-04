package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.ReusableLaserEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public final class ReusableLaserEntityModel extends GeoModel<ReusableLaserEntity> {
    @Override public ResourceLocation getModelResource(ReusableLaserEntity entity) {
        return PomkotsMechs.id("geo/reusable_laser.geo.json");
    }

    @Override public ResourceLocation getTextureResource(ReusableLaserEntity entity) {
        return PomkotsMechs.id("textures/entity/effects/reusable_laser.png");
    }

    @Override public ResourceLocation getAnimationResource(ReusableLaserEntity entity) {
        return PomkotsMechs.id("animations/reusable_laser.animation.json");
    }
}
