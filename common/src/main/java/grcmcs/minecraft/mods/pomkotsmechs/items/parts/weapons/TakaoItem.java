package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.TakaoItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
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
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;

public class TakaoItem extends BasePartsItem.WeaponArm {
   public TakaoItem(Properties properties) {
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
         Vec3 pilePos2 = new Vec3(-6.5 * mechInterface.isRight(), -4.0, -4.0)
            .m_82524_((float)Math.toRadians(-1.0 * mechInterface.getYRot()))
            .m_82549_(mechInterface.position());
         Vec3 kbVel = new Vec3(0.0, 0.0, -1.0).m_82524_((float)Math.toRadians(-1.0 * mechInterface.getYRot()));
         float baseDamage = mechInterface.applySkillDamageModifier(this.getDamage(mechInterface.getItemStack()), this.getWeaponCategory());

         for (Entity ent : world.m_45933_(null, new AABB(pilePos1, pilePos2))) {
            if (!mechInterface.isSelf(ent) && ent instanceof LivingEntity le) {
               if (!world.m_5776_()) {
                  int sbModifier = mechInterface.isSuperBoost() ? 2 : 1;
                  le.m_147240_(sbModifier * 4, sbModifier * kbVel.f_82479_, sbModifier * kbVel.f_82481_);
                  DamageSource ds;
                  if (driver instanceof Player p) {
                     ds = mechInterface.damageSources().m_269075_(p);
                  } else {
                     ds = mechInterface.damageSources().m_269264_();
                  }

                  int chargeModifier = 1;
                  if (tick > 60) {
                     chargeModifier = 3;
                  } else if (tick > 40) {
                     chargeModifier = 2;
                  }

                  if (le instanceof BossHitBoxEntity hit) {
                     hit.addStunPoint(10 * chargeModifier);
                  }

                  le.m_6469_(ds, baseDamage * chargeModifier * sbModifier);
               } else {
                  mechInterface.addHitParticles(le);
               }
            }
         }

         if (world.f_46443_) {
            mechInterface.playSoundEffect((SoundEvent)PomkotsMechs.SE_PILEBUNKER_EVENT.get());
         } else if (Utils.isBlockDestructionAllowed(mechInterface.getMechEntity())) {
            int rad = 5;
            if (tick > 60) {
               rad = 15;
            } else if (tick > 40) {
               rad = 10;
            }

            if (mechInterface.onGround()) {
               mechInterface.breakBlocksCube(rad);
            } else {
               mechInterface.breakBlockSphere(rad, new Vec3(0.0, 0.0, 5.0));
            }
         }
      }
   }

   public TakaoItemRenderer newRenderer() {
      return new TakaoItemRenderer();
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
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
      return Motion.CHARGE_HUMMER;
   }

   @Override
   public int getCoolTime() {
      return 60;
   }

   @Override
   public String getPartsSeriesName() {
      return "takao";
   }
}
