package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile.BeamProjectileModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletBeamEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/** Lightweight, full-bright renderer for Mashu's beam projectile. */
public class BulletBeamEntityRenderer extends SimpleProjectileRenderer<BulletBeamEntity> {
    public BulletBeamEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new BeamProjectileModel(),
                PomkotsMechs.id("textures/entity/projectile/bulletbeam.png"), 6.0F, true, true);
    }
}
