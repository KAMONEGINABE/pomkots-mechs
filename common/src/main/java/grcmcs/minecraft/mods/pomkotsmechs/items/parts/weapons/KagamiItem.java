package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.KagamiItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.BulletGrenadeEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineGrenadeItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;

public class KagamiItem extends BasePartsItem.WeaponArm {
   public KagamiItem(Properties properties) {
      super(properties);
   }

   public KagamiItemRenderer newRenderer() {
      return new KagamiItemRenderer();
   }

   @Override
   public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
      Level world = mechInterface.getWorld();
      if (isOnFire) {
         if (!mechInterface.consumeAmmo(1)) {
            return;
         }

         if (!world.isClientSide()) {
            BulletGrenadeEntity be = new BulletGrenadeEntity(
               (EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET_GRENADE.get(),
               world,
               mechInterface.getMechEntity(),
               mechInterface.applySkillDamageModifier(this.getDamage(mechInterface.getItemStack()), this.getWeaponCategory())
            );
            Vec3 offset = mechInterface.getOffset();
            Vec3 muzzlPos = new Vec3(1.6 * mechInterface.isRight(), 5.2, 4.0);
            muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * mechInterface.getYRot()));
            be.setPos(offset.add(muzzlPos));
            float[] angle = mechInterface.getShootingAngle(be, true);
            be.shootFromRotation(be, angle[0], angle[1], mechInterface.getFallFlyingTicks(), 5.0F, 0.0F);
            world.addFreshEntity(be);
            mechInterface.knockBack(5.0F);
         } else {
            ParticleUtil.spawnAttachedMuzzleFlash(
               (ClientLevel)world, mechInterface.getMechEntity(), mechInterface.getAttachPoint(), 20.0, new Vec3(1.6 * mechInterface.isRight(), 5.2, 4.0)
            );
            mechInterface.playSoundEffect((SoundEvent)PomkotsMechs.SE_GRENADE_EVENT.get());
         }
      }
   }

   @Override
   public boolean isMatchAmmo(BasePartsItem.Magazine mag) {
      return mag instanceof MagazineGrenadeItem;
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
      return BasePartsItem.WeaponCategory.GRENADE;
   }

   @Override
   public Motion getMotion() {
      return Motion.GRENADE_HAND;
   }

   @Override
   public int getCoolTime() {
      return 100;
   }

   @Override
   public String getPartsSeriesName() {
      return "kagami";
   }
}
