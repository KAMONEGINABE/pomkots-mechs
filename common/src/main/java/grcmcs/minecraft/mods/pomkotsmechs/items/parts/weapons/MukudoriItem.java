package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.MukudoriItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineMissileItem;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;

public class MukudoriItem extends BasePartsItem.WeaponShoulder {
   public MukudoriItem(Properties properties) {
      super(properties);
   }

   public MukudoriItemRenderer newRenderer() {
      return new MukudoriItemRenderer();
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
   }

   @Override
   public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
      Level world = mechInterface.getWorld();
      if (isOnFire) {
         if (!world.isClientSide()) {
            Entity target = mechInterface.consumeMultiLockTargetsSingle();
            if (target != null && mechInterface.consumeAmmoFromServerSide(1)) {
               Vec3 offset = mechInterface.getOffset();
               Vec3 muzzlPos = new Vec3(1.4 * mechInterface.isRight(), 4.8F, 2.0);
               int i = tick / 2 - 1;
               if (i < 3) {
                  Vec3 worldMuzzlPos = muzzlPos.add(mechInterface.isRight() * 1.4, 1.0 - 0.5 * (i % 3), 0.0)
                     .yRot((float)Math.toRadians(-1.0 * mechInterface.getYRot()));
                  MissileGenericEntity be = new MissileGenericEntity(
                     (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_GENERIC.get(),
                     world,
                     mechInterface.getMechEntity(),
                     (LivingEntity)target,
                     mechInterface.applySkillDamageModifier(this.getDamage(mechInterface.getItemStack()), this.getWeaponCategory()),
                     BattleBalance.MECH_MISSILE_GENERIC_SPEED2
                  );
                  be.setPos(offset.add(worldMuzzlPos));
                  be.shootFromRotation(
                     be,
                     0.0F,
                     mechInterface.getYRot() - 45 * mechInterface.isRight(),
                     mechInterface.getFallFlyingTicks(),
                     BattleBalance.MECH_MISSILE_GENERIC_SPEED2,
                     0.0F
                  );
                  world.addFreshEntity(be);
               } else if (i < 6) {
                  Vec3 worldMuzzlPos = muzzlPos.add(0.5 * mechInterface.isRight() * (i % 3), 0.0, 0.0)
                     .yRot((float)Math.toRadians(-1.0 * mechInterface.getYRot()));
                  MissileGenericEntity be = new MissileGenericEntity(
                     (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_GENERIC.get(),
                     world,
                     mechInterface.getMechEntity(),
                     (LivingEntity)target,
                     this.getDamage(mechInterface.getItemStack()),
                     BattleBalance.MECH_MISSILE_GENERIC_SPEED2
                  );
                  be.setPos(offset.add(worldMuzzlPos));
                  be.shootFromRotation(be, -45.0F, mechInterface.getYRot(), mechInterface.getFallFlyingTicks(), BattleBalance.MECH_MISSILE_GENERIC_SPEED2, 0.0F);
                  world.addFreshEntity(be);
               }
            }
         } else {
            int i = tick / 2 - 1;
            if (i == 0) {
               mechInterface.playSoundEffect((SoundEvent)PomkotsMechs.SE_MISSILE_EVENT.get());
            }
         }
      }
   }

   @Override
   public boolean isMatchAmmo(BasePartsItem.Magazine mag) {
      return mag instanceof MagazineMissileItem;
   }

   @Override
   public BasePartsItem.WeaponInterface.WeaponAttachPoint getWeaponAttachPoint() {
      return BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_SHOULDER;
   }

   @Override
   public BasePartsItem.WeaponCategory getWeaponCategory() {
      return BasePartsItem.WeaponCategory.MISSILE;
   }

   @Override
   public Motion getMotion() {
      return Motion.MULTI_MISSILE;
   }

   @Override
   public int maxMultiLockNum() {
      return 6;
   }

   @Override
   public int getCoolTime() {
      return 100;
   }

   @Override
   public String getPartsSeriesName() {
      return "mukudori";
   }
}
