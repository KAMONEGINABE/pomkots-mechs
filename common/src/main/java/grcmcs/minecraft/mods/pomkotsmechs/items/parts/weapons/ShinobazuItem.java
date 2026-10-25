package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.ShinobazuItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletMachineEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.PomkotsCustomThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineMachineGunItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ShinobazuItem extends BasePartsItem.WeaponArm {
   public ShinobazuItem(Properties properties) {
      super(properties);
   }

   public ShinobazuItemRenderer newRenderer() {
      return new ShinobazuItemRenderer();
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
               (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_MACHINE.get(),
               world,
               mechInterface.getMechEntity(),
               mechInterface.applySkillDamageModifier(this.getDamage(mechInterface.getItemStack()), this.getWeaponCategory())
            );
            Vec3 offset = mechInterface.getOffset();
            Vec3 muzzlPos = new Vec3(1.8 * mechInterface.isRight(), 3.0, 6.5);
            muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * mechInterface.getYRot()));
            be.m_146884_(offset.m_82549_(muzzlPos));
            float[] angle = mechInterface.getShootingAngle(be, true);
            be.m_37251_(be, angle[0], angle[1], mechInterface.getFallFlyingTicks(), 6.0F, 2.0F);
            world.m_7967_(be);
         } else {
            ParticleUtil.spawnAttachedMuzzleFlash(
               (ClientLevel)world, mechInterface.getMechEntity(), mechInterface.getAttachPoint(), 10.0, new Vec3(1.8 * mechInterface.isRight(), 3.0, 6.5)
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
   public boolean isMatchAmmo(BasePartsItem.Magazine mag) {
      return mag instanceof MagazineMachineGunItem;
   }

   @Override
   public BasePartsItem.WeaponInterface.WeaponAttachPoint getWeaponAttachPoint() {
      return BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_HAND;
   }

   @Override
   public BasePartsItem.WeaponCategory getWeaponCategory() {
      return BasePartsItem.WeaponCategory.MACHINE_GUN;
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
