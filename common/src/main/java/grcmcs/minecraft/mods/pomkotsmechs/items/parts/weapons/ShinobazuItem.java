package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.ShinobazuItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletMachineEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletRifleEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.PomkotsCustomThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineMachineGunItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;

public class ShinobazuItem extends BasePartsItem.WeaponArm {
    @Override public boolean usesArmPitchAiming() { return true; }
    public ShinobazuItem(Properties properties) {
        super(properties);
    }

    @Override
    public ShinobazuItemRenderer newRenderer() {
        return new ShinobazuItemRenderer();
    }

    public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
        var world = mechInterface.getWorld();
        if (isOnFire) {
            if (!mechInterface.consumeAmmo(1)) {
                return;
            }

            if (!world.isClientSide()) {
                BulletMachineEntity be = new BulletMachineEntity(
                        PomkotsMechs.BULLET_MACHINE.get(), world,
                        mechInterface.getMechEntity(),
                        mechInterface.applySkillDamageModifier(this.getDamage(mechInterface.getItemStack()), this.getWeaponCategory())
                );

                var offset = mechInterface.getOffset();

                // オフセット位置から大体の銃口の座標を決める（モデル位置からとるとクラサバ同期がめんどい…）
                var muzzlPos = new Vec3(1.8 * mechInterface.isRight(), 3.0F, 6.5F);
                muzzlPos = mechInterface.getAimAdjustedMuzzleOffset(muzzlPos,
                        new Vec3(1.8 * mechInterface.isRight(), 3.0F, 0.0F));
                be.setPos(offset.add(muzzlPos));

                float[] angle = mechInterface.getShootingAngle(be, true);

                be.shootFromRotation(be, angle[0], angle[1], mechInterface.getFallFlyingTicks(), 6, 2F);

                world.addFreshEntity(be);
            } else {
                ParticleUtil.spawnAttachedMuzzleFlash(
                        (ClientLevel) world,
                        mechInterface.getMechEntity(),
                        mechInterface.getAttachPoint(),
                        10.0D,
                        mechInterface.getAimAdjustedLocalMuzzleOffset(
                                new Vec3(1.8 * mechInterface.isRight(), 3.0F, 6.5F),
                                new Vec3(1.8 * mechInterface.isRight(), 3.0F, 0.0F))
                );
                if (tick % 5 == 1) {
                    mechInterface.playSoundEffect(PomkotsMechs.SE_GATLING_EVENT.get());
                }
            }
        }
    }

    @Override
    public PomkotsCustomThrowableProjectile.RangeCategory getCurrentRange(int distance) {
        return BulletMachineEntity.calcRangeCategoryStatic(distance);
    }

    @Override
    public boolean isMatchAmmo(Magazine mag) {
        return mag instanceof MagazineMachineGunItem;
    }

    @Override
    public WeaponAttachPoint getWeaponAttachPoint() {
        return WeaponAttachPoint.ATTACH_POINT_HAND;
    }

    @Override
    public WeaponCategory getWeaponCategory() {
        return WeaponCategory.MACHINE_GUN;
    }

    @Override
    public Motion getMotion() {
        return Motion.SUB_MACHINE_GUN;
    }

    @Override
    public int getCoolTime() {
        return 20;
    }

    @Override
    public String getPartsSeriesName() {
        return "shinobazu";
    }
}
