package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.TenpouItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TenpouItem extends BasePartsItem.WeaponArm {
   public TenpouItem(Properties properties) {
      super(properties);
   }

   @Override
   public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
      Level world = mechInterface.getWorld();
      if (isOnFire) {
         Entity driver = mechInterface.getDrivingPassenger();
         Vec3 pilePos1 = new Vec3(6.5 * mechInterface.isRight(), 4.0, 18.0)
            .m_82524_((float)Math.toRadians(-1.0 * mechInterface.getYRot()))
            .m_82549_(mechInterface.position());
         Vec3 pilePos2 = new Vec3(-2 * mechInterface.isRight(), -4.0, -4.0)
            .m_82524_((float)Math.toRadians(-1.0 * mechInterface.getYRot()))
            .m_82549_(mechInterface.position());
         Vec3 kbVel = new Vec3(0.0, 0.0, -1.0).m_82524_((float)Math.toRadians(-1.0 * mechInterface.getYRot()));
         float damage = mechInterface.applySkillDamageModifier(this.getDamage(mechInterface.getItemStack()), this.getWeaponCategory());

         for (Entity ent : world.m_45933_(null, new AABB(pilePos1, pilePos2))) {
            if (!mechInterface.isSelf(ent) && ent instanceof LivingEntity le) {
               if (!world.m_5776_()) {
                  float sbModifier = mechInterface.isSuperBoost() ? 1.5F : 1.0F;
                  le.m_147240_(sbModifier * 2.0F, sbModifier * kbVel.f_82479_, sbModifier * kbVel.f_82481_);
                  DamageSource ds;
                  if (driver instanceof Player p) {
                     ds = mechInterface.damageSources().m_269075_(p);
                  } else {
                     ds = mechInterface.damageSources().m_269264_();
                  }

                  float baseDamage = damage;
                  if (le instanceof BossHitBoxEntity hit) {
                     hit.addStunPoint((int)(sbModifier * 50.0F));
                  } else if (le instanceof Pmvc01Entity) {
                     baseDamage *= 1.5F;
                  }

                  if (PomkotsMechs.CONFIG.debugModeEnabled) {
                     baseDamage = 100000.0F;
                  }

                  le.m_6469_(ds, sbModifier * baseDamage);
               } else {
                  mechInterface.addHitParticles(le);
               }
            }
         }

         if (world.f_46443_) {
            mechInterface.playSoundEffect((SoundEvent)PomkotsMechs.SE_PILEBUNKER_EVENT.get());
         } else if (Utils.isBlockDestructionAllowed(mechInterface.getMechEntity())) {
            mechInterface.breakBlocksCube(3, 6, new Vec3(0.0, 0.0, 3.0));
         }
      }
   }

   public TenpouItemRenderer newRenderer() {
      return new TenpouItemRenderer();
   }

   @Override
   public BasePartsItem.WeaponInterface.WeaponAttachPoint getWeaponAttachPoint() {
      return BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_HAND;
   }

   @Override
   public BasePartsItem.WeaponCategory getWeaponCategory() {
      return BasePartsItem.WeaponCategory.MELEE;
   }

   @Override
   public Motion getMotion() {
      return Motion.PUNCH;
   }

   @Override
   public int getCoolTime() {
      return 30;
   }

   @Override
   public String getPartsSeriesName() {
      return "tenpou";
   }
}
