package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.projectile.MachineProjectileModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletMachineEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class BulletMachineEntityRenderer extends SimpleProjectileRenderer<BulletMachineEntity> {
    public BulletMachineEntityRenderer(EntityRendererProvider.Context context, float scale) {
        super(context, new MachineProjectileModel<>(),
                PomkotsMechs.id("textures/entity/projectile/bulletmachine.png"), scale, true, true);
    }
}
