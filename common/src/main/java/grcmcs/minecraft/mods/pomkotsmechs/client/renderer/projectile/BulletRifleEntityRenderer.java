package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile.SimpleProjectileModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletRifleEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class BulletRifleEntityRenderer extends SimpleProjectileRenderer<BulletRifleEntity> {
    public BulletRifleEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new SimpleProjectileModel<>(SimpleProjectileModel.rifleLayer().bakeRoot()),
                PomkotsMechs.id("textures/entity/projectile/bulletrifle.png"), 1.2F, true, true);
    }
}
