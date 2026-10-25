package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.NosuriItemRenderer;
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

public class NosuriItem extends BasePartsItem.WeaponShoulder {
   public NosuriItem(Properties properties) {
      super(properties);
   }

   public NosuriItemRenderer newRenderer() {
      return new NosuriItemRenderer();
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
               Vec3 muzzlPos = new Vec3(1.4 * mechInterface.isRight(), 6.0, 2.0);
               if (tick < 14) {
                  int i = tick / 2 - 1;
                  Vec3 worldMuzzlPos = muzzlPos.add(0.0, 0.0, -2.5 - 0.5 * (i % 6)).yRot((float)Math.toRadians(-1.0 * mechInterface.getYRot()));
                  MissileGenericEntity be = new MissileGenericEntity(
                     (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_GENERIC.get(),
                     world,
                     mechInterface.getMechEntity(),
                     (LivingEntity)target,
                     mechInterface.applySkillDamageModifier(this.getDamage(mechInterface.getItemStack()), this.getWeaponCategory()),
                     BattleBalance.MECH_MISSILE_GENERIC_SPEED2
                  );
                  be.setSwitchTick(24);
                  be.setPos(offset.add(worldMuzzlPos));
                  be.shootFromRotation(be, -80.0F, mechInterface.getYRot(), mechInterface.getFallFlyingTicks(), BattleBalance.MECH_MISSILE_GENERIC_SPEED2 * 3.0F, 0.0F);
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
      return "nosuri";
   }
}
