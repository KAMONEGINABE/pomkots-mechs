package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.TsurugiItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;

public class TsurugiItem extends BasePartsItem.WeaponArm {
   public TsurugiItem(Properties properties) {
      super(properties);
      SingletonGeoAnimatable.registerSyncedAnimatable(this);
   }

   @Override
   public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
      Level world = mechInterface.getWorld();
      if (tick == 2) {
         if (!world.f_46443_) {
            float[] angle = mechInterface.getShootingAngle(mechInterface.getMechEntity(), true);
            mechInterface.addVelocity(angle[0], angle[1], 5.0F);
         } else {
            mechInterface.playSoundEffect((SoundEvent)PomkotsMechs.SE_SABER.get());
         }
      }

      if (isOnFire) {
         if (!mechInterface.consumeEnergy(50)) {
            return;
         }

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
                  le.m_147240_(2.0, kbVel.f_82479_, kbVel.f_82481_);
                  DamageSource ds;
                  if (driver instanceof Player p) {
                     ds = mechInterface.damageSources().m_269075_(p);
                  } else {
                     ds = mechInterface.damageSources().m_269264_();
                  }

                  float baseDamage = damage;
                  if (le instanceof BossHitBoxEntity hit) {
                     hit.addStunPoint(30);
                  } else if (le instanceof Pmvc01Entity) {
                     baseDamage *= 1.5F;
                  }

                  le.m_6469_(ds, baseDamage);
               } else {
                  mechInterface.addHitParticles(le);
               }
            }
         }
      }
   }

   @Override
   public void startUsing(ActionWeapon.WeaponMechInterface mechInterface) {
      if (!mechInterface.getWorld().f_46443_ && mechInterface.getPlayer() != null && mechInterface.getItemStack() != null) {
         this.triggerAnim(
            mechInterface.getPlayer(), GeoItem.getOrAssignId(mechInterface.getItemStack(), (ServerLevel)mechInterface.getWorld()), "Activation", "use"
         );
      }
   }

   @Override
   public void endUsing(ActionWeapon.WeaponMechInterface mechInterface) {
      if (!mechInterface.getWorld().f_46443_ && mechInterface.getPlayer() != null && mechInterface.getItemStack() != null) {
         this.triggerAnim(
            mechInterface.getPlayer(), GeoItem.getOrAssignId(mechInterface.getItemStack(), (ServerLevel)mechInterface.getWorld()), "Activation", "stop"
         );
      }
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "Activation", 0, state -> PlayState.STOP)
               .triggerableAnim("use", RawAnimation.begin().thenPlay("animation.weapon.use"))
               .triggerableAnim("stop", RawAnimation.begin().thenPlay("animation.weapon.idle"))
         }
      );
   }

   public void m_6883_(ItemStack stack, Level level, Entity ent, int slotIndex, boolean bl) {
      super.m_6883_(stack, level, ent, slotIndex, bl);
      if (!level.f_46443_ && level instanceof ServerLevel serverLevel) {
         GeoItem.getOrAssignId(stack, serverLevel);
      }
   }

   public TsurugiItemRenderer newRenderer() {
      return new TsurugiItemRenderer();
   }

   @Override
   public BasePartsItem.WeaponInterface.WeaponAttachPoint getWeaponAttachPoint() {
      return BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_ARM;
   }

   @Override
   public BasePartsItem.WeaponCategory getWeaponCategory() {
      return BasePartsItem.WeaponCategory.MELEE;
   }

   @Override
   public Motion getMotion() {
      return Motion.SABER;
   }

   @Override
   public int getCoolTime() {
      return 60;
   }

   @Override
   public String getPartsSeriesName() {
      return "tsurugi";
   }
}
