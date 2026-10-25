package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.SenzokuItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletMachineEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.PomkotsCustomThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineShotGunItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;

public class SenzokuItem extends BasePartsItem.WeaponArm {
   public SenzokuItem(Properties properties) {
      super(properties);
   }

   public SenzokuItemRenderer newRenderer() {
      return new SenzokuItemRenderer();
   }

   @Override
   public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
      Level world = mechInterface.getWorld();
      if (isOnFire) {
         int bulletNum = 10;

         for (int i = 0; i < bulletNum; i++) {
            if (mechInterface.consumeAmmo(1)) {
               if (!world.m_5776_()) {
                  BulletMachineEntity be = new BulletMachineEntity(
                     (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_MACHINE.get(),
                     world,
                     mechInterface.getMechEntity(),
                     mechInterface.applySkillDamageModifier(this.getDamage(mechInterface.getItemStack()), this.getWeaponCategory())
                  );
                  Vec3 offset = mechInterface.getOffset();
                  Vec3 muzzlPos = new Vec3(1.8 * mechInterface.isRight(), 3.0, 5.0);
                  muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * mechInterface.getYRot()));
                  be.m_146884_(offset.m_82549_(muzzlPos));
                  float[] angle = this.randomizeShootingAngle(mechInterface.getShootingAngle(be, true), 30.0F, mechInterface.getMechEntity().m_217043_());
                  be.m_37251_(be, angle[0], angle[1], mechInterface.getFallFlyingTicks(), 7.0F, 1.0F);
                  world.m_7967_(be);
               } else if (i == 0) {
                  ParticleUtil.spawnAttachedMuzzleFlash(
                     (ClientLevel)world, mechInterface.getMechEntity(), mechInterface.getAttachPoint(), 16.0, new Vec3(1.8 * mechInterface.isRight(), 3.0, 5.0)
                  );
                  mechInterface.playSoundEffect((SoundEvent)PomkotsMechs.SE_GUN_3.get());
               }
            }
         }
      }
   }

   @Override
   public PomkotsCustomThrowableProjectile.RangeCategory getCurrentRange(int distance) {
      return BulletMachineEntity.calcRangeCategoryStatic(distance);
   }

   private float[] randomizeShootingAngle(float[] baseShootingAngle, float spread, RandomSource random) {
      float newPitch = baseShootingAngle[0] + (random.m_188501_() - 0.5F) * spread;
      float newYaw = baseShootingAngle[1] + (random.m_188501_() - 0.5F) * spread;
      return new float[]{newPitch, newYaw};
   }

   @Override
   public boolean isMatchAmmo(BasePartsItem.Magazine mag) {
      return mag instanceof MagazineShotGunItem;
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
      return BasePartsItem.WeaponCategory.SHOT_GUN;
   }

   @Override
   public Motion getMotion() {
      return Motion.RIFLE;
   }

   @Override
   public int getCoolTime() {
      return 40;
   }

   @Override
   public String getPartsSeriesName() {
      return "senzoku";
   }
}
