package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.SuwaItemRenderer;
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

public class SuwaItem extends BasePartsItem.WeaponShoulder {
   public SuwaItem(Properties properties) {
      super(properties);
      SingletonGeoAnimatable.registerSyncedAnimatable(this);
   }

   public SuwaItemRenderer newRenderer() {
      return new SuwaItemRenderer();
   }

   @Override
   public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
      Level world = mechInterface.getWorld();
      if (isOnFire) {
         if (!mechInterface.consumeAmmo(1)) {
            return;
         }

         if (!world.isClientSide()) {
            BulletMachineEntity be = new BulletMachineEntity(
               (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_MACHINE_LARGE.get(),
               world,
               mechInterface.getMechEntity(),
               mechInterface.applySkillDamageModifier(this.getDamage(mechInterface.getItemStack()), this.getWeaponCategory())
            );
            Vec3 offset = mechInterface.getOffset();
            Vec3 muzzlPos = new Vec3(1.6 * mechInterface.isRight(), 5.7, 4.0);
            muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * mechInterface.getYRot()));
            be.setPos(offset.add(muzzlPos));
            float[] angle = mechInterface.getShootingAngle(be, true);
            be.shootFromRotation(be, angle[0], angle[1], mechInterface.getFallFlyingTicks(), 6.0F, 2.0F);
            world.addFreshEntity(be);
         } else {
            ParticleUtil.spawnAttachedMuzzleFlash(
               (ClientLevel)world, mechInterface.getMechEntity(), mechInterface.getAttachPoint(), 12.0, new Vec3(1.6 * mechInterface.isRight(), 5.7, 4.0)
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
      if (!context.getWorld().isClientSide && context.getPlayer() != null && context.getItemStack() != null) {
         this.triggerAnim(context.getPlayer(), GeoItem.getOrAssignId(context.getItemStack(), (ServerLevel)context.getWorld()), "Activation", "use");
      }
   }

   @Override
   public void endUsing(ActionWeapon.WeaponMechInterface context) {
      if (!context.getWorld().isClientSide && context.getPlayer() != null && context.getItemStack() != null) {
         this.triggerAnim(context.getPlayer(), GeoItem.getOrAssignId(context.getItemStack(), (ServerLevel)context.getWorld()), "Activation", "stop");
      }
   }

   public void inventoryTick(ItemStack stack, Level level, Entity ent, int slotIndex, boolean bl) {
      super.inventoryTick(stack, level, ent, slotIndex, bl);
      if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
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
      return BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_SHOULDER;
   }

   @Override
   public BasePartsItem.WeaponCategory getWeaponCategory() {
      return BasePartsItem.WeaponCategory.MACHINE_GUN;
   }

   @Override
   public Motion getMotion() {
      return Motion.SHOULDER_MACHINE_GUN;
   }

   @Override
   public int getCoolTime() {
      return 20;
   }

   @Override
   public String getPartsSeriesName() {
      return "suwa";
   }
}
