package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.KawasemiItemRenderer;
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

public class KawasemiItem extends BasePartsItem.WeaponShoulder {
   public KawasemiItem(Properties properties) {
      super(properties);
   }

   public KawasemiItemRenderer newRenderer() {
      return new KawasemiItemRenderer();
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
   }

   @Override
   public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
      Level world = mechInterface.getWorld();
      if (isOnFire) {
         if (!world.m_5776_()) {
            Entity target = mechInterface.consumeMultiLockTargetsSingle();
            if (target != null && mechInterface.consumeAmmoFromServerSide(1)) {
               Vec3 offset = mechInterface.getOffset();
               Vec3 muzzlPos = new Vec3(1.8 * mechInterface.isRight(), 5.0, 2.0);
               if (tick < 10) {
                  int i = tick / 2 - 1;
                  Vec3 worldMuzzlPos = muzzlPos.m_82520_(1.0 * (i / 2 - 0.5), 1 * (i % 2), 0.0).m_82524_((float)Math.toRadians(-1.0 * mechInterface.getYRot()));
                  MissileGenericEntity be = new MissileGenericEntity(
                     (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_GENERIC.get(),
                     world,
                     mechInterface.getMechEntity(),
                     (LivingEntity)target,
                     mechInterface.applySkillDamageModifier(this.getDamage(mechInterface.getItemStack()), this.getWeaponCategory()),
                     BattleBalance.MECH_MISSILE_GENERIC_SPEED2
                  );
                  be.m_146884_(offset.m_82549_(worldMuzzlPos));
                  be.m_37251_(be, -10.0F, mechInterface.getYRot(), mechInterface.getFallFlyingTicks(), BattleBalance.MECH_MISSILE_GENERIC_SPEED2, 0.0F);
                  world.m_7967_(be);
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
      return 4;
   }

   @Override
   public int getCoolTime() {
      return 100;
   }

   @Override
   public String getPartsSeriesName() {
      return "kawasemi";
   }
}
