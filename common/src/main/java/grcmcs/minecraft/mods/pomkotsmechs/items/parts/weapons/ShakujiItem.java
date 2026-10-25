package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.ShakujiItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletRifleEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.PomkotsCustomThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineRifleItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;

public class ShakujiItem extends BasePartsItem.WeaponArm {
   public ShakujiItem(Properties properties) {
      super(properties);
   }

   public ShakujiItemRenderer newRenderer() {
      return new ShakujiItemRenderer();
   }

   @Override
   public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
      Level world = mechInterface.getWorld();
      if (isOnFire) {
         if (!mechInterface.consumeAmmo(1)) {
            return;
         }

         if (!world.isClientSide()) {
            BulletRifleEntity be = new BulletRifleEntity(
               (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_RIFLE.get(),
               world,
               mechInterface.getMechEntity(),
               mechInterface.applySkillDamageModifier(this.getDamage(mechInterface.getItemStack()), this.getWeaponCategory())
            );
            Vec3 offset = mechInterface.getOffset();
            Vec3 muzzlPos = new Vec3(1.8 * mechInterface.isRight(), 3.0, 6.5);
            muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * mechInterface.getYRot()));
            be.setPos(offset.add(muzzlPos));
            float[] angle = mechInterface.getShootingAngle(be, true);
            be.shootFromRotation(be, angle[0], angle[1], mechInterface.getFallFlyingTicks(), 7.0F, 0.0F);
            world.addFreshEntity(be);
         } else {
            ParticleUtil.spawnAttachedMuzzleFlash(
               (ClientLevel)world, mechInterface.getMechEntity(), mechInterface.getAttachPoint(), 16.0, new Vec3(1.8 * mechInterface.isRight(), 3.0, 6.5)
            );
            mechInterface.playSoundEffect((SoundEvent)PomkotsMechs.SE_GUN_2.get());
         }
      }
   }

   @Override
   public PomkotsCustomThrowableProjectile.RangeCategory getCurrentRange(int distance) {
      return BulletRifleEntity.calcRangeCategoryStatic(distance);
   }

   @Override
   public boolean isMatchAmmo(BasePartsItem.Magazine mag) {
      return mag instanceof MagazineRifleItem;
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
      return BasePartsItem.WeaponCategory.RIFLE;
   }

   @Override
   public Motion getMotion() {
      return Motion.RIFLE;
   }

   @Override
   public int getCoolTime() {
      return 20;
   }

   @Override
   public String getPartsSeriesName() {
      return "shakuji";
   }
}
