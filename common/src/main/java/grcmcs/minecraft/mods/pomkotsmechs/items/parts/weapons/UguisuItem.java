package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.UguisuItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom.MissileGenericEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineMissileItem;
import java.util.List;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;

public class UguisuItem extends BasePartsItem.WeaponArm {
   public UguisuItem(Properties properties) {
      super(properties);
   }

   public UguisuItemRenderer newRenderer() {
      return new UguisuItemRenderer();
   }

   @Override
   public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
      Level world = mechInterface.getWorld();
      if (isOnFire) {
         if (!world.isClientSide) {
            Vec3 offset = mechInterface.getOffset();
            Vec3 muzzlPos = new Vec3(1.8 * mechInterface.isRight(), 3.0, 5.0);
            List<Entity> targets = mechInterface.consumeMultiLockTargets();
            float damage = mechInterface.applySkillDamageModifier(this.getDamage(mechInterface.getItemStack()), this.getWeaponCategory());

            for (int i = 0; i < targets.size(); i++) {
               if (mechInterface.consumeAmmoFromServerSide(1)) {
                  Vec3 worldMuzzlPos = muzzlPos.add(1.0 * (i / 2 - 0.5), 1 * (i % 2), 0.0).yRot((float)Math.toRadians(-1.0 * mechInterface.getYRot()));
                  MissileGenericEntity be = new MissileGenericEntity(
                     (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_GENERIC.get(),
                     world,
                     mechInterface.getMechEntity(),
                     (LivingEntity)targets.get(i),
                     damage,
                     BattleBalance.MECH_MISSILE_GENERIC_SPEED2
                  );
                  be.setPos(offset.add(worldMuzzlPos));
                  be.setMaxRotationAnglePerTick(1.0F);
                  be.shootFromRotation(be, 0.0F, mechInterface.getYRot(), mechInterface.getFallFlyingTicks(), BattleBalance.MECH_MISSILE_GENERIC_SPEED2, 0.0F);
                  world.addFreshEntity(be);
               }
            }
         } else if (tick == 2) {
            mechInterface.playSoundEffect((SoundEvent)PomkotsMechs.SE_MISSILE_EVENT.get());
         }
      }
   }

   @Override
   public boolean isMatchAmmo(BasePartsItem.Magazine mag) {
      return mag instanceof MagazineMissileItem;
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
      return BasePartsItem.WeaponCategory.MISSILE;
   }

   @Override
   public int maxMultiLockNum() {
      return 4;
   }

   @Override
   public Motion getMotion() {
      return Motion.HAND_MISSILE;
   }

   @Override
   public int getCoolTime() {
      return 60;
   }

   @Override
   public String getPartsSeriesName() {
      return "uguisu";
   }
}
