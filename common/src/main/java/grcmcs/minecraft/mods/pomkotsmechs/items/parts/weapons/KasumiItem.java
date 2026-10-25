package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.KasumiItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletMachineEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.PomkotsCustomThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineGatlingItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;

public class KasumiItem extends BasePartsItem.WeaponArm {
   public KasumiItem(Properties properties) {
      super(properties);
      SingletonGeoAnimatable.registerSyncedAnimatable(this);
   }

   public KasumiItemRenderer newRenderer() {
      return new KasumiItemRenderer();
   }

   @Override
   public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
      Level world = mechInterface.getWorld();
      if (isOnFire) {
         if (!mechInterface.consumeAmmo(1)) {
            return;
         }

         if (!world.m_5776_()) {
            BulletMachineEntity be = new BulletMachineEntity(
               (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_MACHINE_LARGE.get(),
               world,
               mechInterface.getMechEntity(),
               mechInterface.applySkillDamageModifier(this.getDamage(mechInterface.getItemStack()), this.getWeaponCategory())
            );
            Vec3 offset = mechInterface.getOffset();
            Vec3 muzzlPos = new Vec3(1.8 * mechInterface.isRight(), 3.0, 5.0);
            muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * mechInterface.getYRot()));
            be.m_146884_(offset.m_82549_(muzzlPos));
            float[] angle = mechInterface.getShootingAngle(be, true);
            be.m_37251_(be, angle[0], angle[1], mechInterface.getFallFlyingTicks(), 6.0F, 2.0F);
            world.m_7967_(be);
         } else {
            ParticleUtil.spawnAttachedMuzzleFlash(
               (ClientLevel)world, mechInterface.getMechEntity(), mechInterface.getAttachPoint(), 12.0, new Vec3(1.8 * mechInterface.isRight(), 3.0, 5.0)
            );
            if (tick % 5 == 1) {
               mechInterface.playSoundEffect((SoundEvent)PomkotsMechs.SE_GATLING_EVENT.get());
            }
         }
      }
   }

   @Override
   public PomkotsCustomThrowableProjectile.RangeCategory getCurrentRange(int distance) {
      return BulletMachineEntity.calcRangeCategoryStatic(distance);
   }

   @Override
   public void startUsing(ActionWeapon.WeaponMechInterface context) {
      if (!context.getWorld().f_46443_ && context.getPlayer() != null && context.getItemStack() != null) {
         this.triggerAnim(context.getPlayer(), GeoItem.getOrAssignId(context.getItemStack(), (ServerLevel)context.getWorld()), "Activation", "use");
      }
   }

   @Override
   public void endUsing(ActionWeapon.WeaponMechInterface context) {
      if (!context.getWorld().f_46443_ && context.getPlayer() != null && context.getItemStack() != null) {
         this.triggerAnim(context.getPlayer(), GeoItem.getOrAssignId(context.getItemStack(), (ServerLevel)context.getWorld()), "Activation", "stop");
      }
   }

   public void m_6883_(ItemStack stack, Level level, Entity ent, int slotIndex, boolean bl) {
      super.m_6883_(stack, level, ent, slotIndex, bl);
      if (!level.f_46443_ && level instanceof ServerLevel serverLevel) {
         GeoItem.getOrAssignId(stack, serverLevel);
      }
   }

   @Override
   public boolean isMatchAmmo(BasePartsItem.Magazine mag) {
      return mag instanceof MagazineGatlingItem;
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

   @Override
   public BasePartsItem.WeaponInterface.WeaponAttachPoint getWeaponAttachPoint() {
      return BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_ARM;
   }

   @Override
   public BasePartsItem.WeaponCategory getWeaponCategory() {
      return BasePartsItem.WeaponCategory.MACHINE_GUN;
   }

   @Override
   public Motion getMotion() {
      return Motion.GATLING_GUN;
   }

   @Override
   public int getCoolTime() {
      return 20;
   }

   @Override
   public String getPartsSeriesName() {
      return "kasumi";
   }
}
