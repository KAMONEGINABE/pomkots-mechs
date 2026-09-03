package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile.SimpleProjectileModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeLargeEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class BulletGrenadeLargeEntityRenderer extends SimpleProjectileRenderer<BulletGrenadeLargeEntity> {
    public BulletGrenadeLargeEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new SimpleProjectileModel<>(SimpleProjectileModel.grenadeLayer().bakeRoot()),
                PomkotsMechs.id("textures/entity/grenade.png"), BulletGrenadeLargeEntity.scale, false);
    }
}
