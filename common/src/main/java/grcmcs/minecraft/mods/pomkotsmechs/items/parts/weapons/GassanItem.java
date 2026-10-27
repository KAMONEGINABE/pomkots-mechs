package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.GassanItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class GassanItem extends BasePartsItem.WeaponArm {
    public GassanItem(Properties properties) {
        super(properties);

    }

    public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
        var world = mechInterface.getWorld();

        if (isOnFire) {
            Entity driver = mechInterface.getDrivingPassenger();
            var pilePos1 = new Vec3(6.5 * mechInterface.isRight(), 4.0F, 36F).yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot())).add(mechInterface.position());
            var pilePos2 = new Vec3(-2 * mechInterface.isRight(), -4F, 28F).yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot())).add(mechInterface.position());

            var kbVel = new Vec3(0, 0, -1F).yRot((float) Math.toRadians((-1.0) * mechInterface.getYRot()));
            DamageSource ds;
            if (driver instanceof Player p) {
                ds = mechInterface.damageSources().source(PomkotsMechs.ANTI_ARMOR_DAMAGE_TYPE, mechInterface.getMechEntity(), p);
            } else {
                ds = mechInterface.damageSources().source(PomkotsMechs.ANTI_ARMOR_DAMAGE_TYPE, mechInterface.getMechEntity());
            }

            var baseDamage = mechInterface.applySkillDamageModifier(this.getDamage(mechInterface.getItemStack()), this.getWeaponCategory());
            var sbModifier = mechInterface.isSuperBoost() ? 2F : 1F;

            for (var ent : world.getEntities(null, new AABB(pilePos1, pilePos2))) {
                if (mechInterface.isSelf(ent)) {
                    continue;
                }

                if (ent instanceof LivingEntity le) {
                    if (!world.isClientSide()) {
                        le.knockback(2 * sbModifier, kbVel.x * sbModifier, kbVel.z * sbModifier);
                        var targetDamage = baseDamage;
                        if (le instanceof Pmvc01Entity) {
                            targetDamage *= 1.5F;
                        }
                        le.hurt(ds, targetDamage * sbModifier);
                    } else {
                        mechInterface.addHitParticles(le);
                    }
                } else if (ent.isAttackable()) {
                    if (!world.isClientSide()) {
                        ent.hurt(ds, baseDamage * sbModifier);
                    } else {
                        mechInterface.addHitParticles(ent);
                    }
                }
            }

            if (world.isClientSide) {
                mechInterface.playSoundEffect(PomkotsMechs.SE_PILEBUNKER_EVENT.get());
            }
        }
    }

    @Override
    public GassanItemRenderer newRenderer() {
        return new GassanItemRenderer();
    }

    @Override
    public WeaponAttachPoint getWeaponAttachPoint() {
        return WeaponAttachPoint.ATTACH_POINT_HAND;
    }

    @Override
    public WeaponCategory getWeaponCategory() {
        return WeaponCategory.MELEE;
    }

    @Override
    public Motion getMotion() {
        return Motion.YARI;
    }

    @Override
    public int getCoolTime() {
        return 20;
    }

    @Override
    public String getPartsSeriesName() {
        return "gassan";
    }
}
