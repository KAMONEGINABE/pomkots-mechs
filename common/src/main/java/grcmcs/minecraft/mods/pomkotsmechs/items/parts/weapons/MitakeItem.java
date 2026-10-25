package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.MitakeItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MitakeItem extends BasePartsItem.WeaponShoulder {
   public MitakeItem(Properties properties) {
      super(properties);
   }

   @Override
   public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
      Level world = mechInterface.getWorld();
      if (isOnFire) {
         Entity driver = mechInterface.getDrivingPassenger();
         int radius = 5;
         Vec3 pos = mechInterface.position();
         Vec3 aabbPos1 = new Vec3(-radius / 2.0F + mechInterface.isRight(), -radius / 2.0F, 0.0)
            .yRot((float)Math.toRadians(-1.0 * mechInterface.getYRot()))
            .add(mechInterface.position());
         Vec3 aabbPos2 = new Vec3(radius / 2.0F + mechInterface.isRight(), radius, radius * 2)
            .yRot((float)Math.toRadians(-1.0 * mechInterface.getYRot()))
            .add(mechInterface.position());
         AABB box = new AABB(aabbPos1, aabbPos2);
         float damage = mechInterface.applySkillDamageModifier(this.getDamage(mechInterface.getItemStack()), this.getWeaponCategory());

         for (Entity ent : world.getEntities(null, box)) {
            if (!mechInterface.isSelf(ent) && ent instanceof LivingEntity le) {
               if (!world.isClientSide()) {
                  Vec3 kbVel = ent.position().subtract(pos).normalize();
                  le.knockback(4.0, -kbVel.x * 2.0, -kbVel.z * 2.0);
                  DamageSource ds;
                  if (driver instanceof Player p) {
                     ds = mechInterface.damageSources().playerAttack(p);
                  } else {
                     ds = mechInterface.damageSources().generic();
                  }

                  float baseDamage = damage;
                  if (le instanceof BossHitBoxEntity hit) {
                     hit.addStunPoint(30);
                  } else if (le instanceof Pmvc01Entity) {
                     baseDamage *= 1.5F;
                  }

                  le.hurt(ds, mechInterface.isSuperBoost() ? baseDamage * 2.0F : baseDamage);
               } else {
                  mechInterface.addHitParticles(le);
               }
            }
         }

         if (world.isClientSide) {
            mechInterface.playSoundEffect((SoundEvent)PomkotsMechs.SE_PILEBUNKER_EVENT.get());
         }
      }
   }

   public MitakeItemRenderer newRenderer() {
      return new MitakeItemRenderer();
   }

   @Override
   public BasePartsItem.WeaponInterface.WeaponAttachPoint getWeaponAttachPoint() {
      return BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_SHOULDER;
   }

   @Override
   public BasePartsItem.WeaponCategory getWeaponCategory() {
      return BasePartsItem.WeaponCategory.MELEE;
   }

   @Override
   public Motion getMotion() {
      return Motion.SHOULDER_BLADE;
   }

   @Override
   public int getCoolTime() {
      return 40;
   }

   @Override
   public String getPartsSeriesName() {
      return "mitake";
   }
}
