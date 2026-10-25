package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.WadaItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class WadaItem extends BasePartsItem.WeaponArm {
   public WadaItem(Properties properties) {
      super(properties);
   }

   @Override
   public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
      Level world = mechInterface.getWorld();
      if (isOnFire) {
         Pmvc01Entity mech = mechInterface.getMechEntity();
         if (mechInterface.isToggleOnStart()) {
            if (world.isClientSide) {
               mechInterface.playSoundEffect((SoundEvent)PomkotsMechs.SE_LIFT.get());
            } else {
               Entity p = findFirstEntityInFront3x3x3(mech);
               if (p != null && this.isTargetClass(p)) {
                  Vec3 pos = mech.position();
                  p.setPos(pos.x, pos.y + 7.5, pos.z);
                  p.setNoGravity(true);
                  mech.setHavingEntity(Optional.of(p.getUUID()));
               } else {
                  mech.setHavingEntity(Optional.empty());
               }
            }
         } else if (world.isClientSide) {
            mechInterface.playSoundEffect((SoundEvent)PomkotsMechs.SE_THROW.get());
         } else {
            Entity p = ((ServerLevel)world).getEntity(mech.havingEntity().get());
            if (p != null) {
               p.invulnerableTime = 20;
               Vec3 muzzlPos = new Vec3(0.0, 0.0, 6.0);
               muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * mechInterface.getYRot()));
               p.setPos(mech.position().add(muzzlPos));
               p.setNoGravity(false);
            }

            mech.setHavingEntity(Optional.empty());
         }
      }
   }

   public static void shootFromRotation(Entity ent, Entity entity, float f, float g, float h, float i, float j) {
      float k = -Mth.sin(g * (float) (Math.PI / 180.0)) * Mth.cos(f * (float) (Math.PI / 180.0));
      float l = -Mth.sin((f + h) * (float) (Math.PI / 180.0));
      float m = Mth.cos(g * (float) (Math.PI / 180.0)) * Mth.cos(f * (float) (Math.PI / 180.0));
      shoot(ent, k, l, m, i, j);
      Vec3 vec3 = entity.getDeltaMovement();
      ent.setDeltaMovement(ent.getDeltaMovement().add(vec3.x, entity.onGround() ? 0.0 : vec3.y, vec3.z));
   }

   public static void shoot(Entity ent, double d, double e, double f, float g, float h) {
      Vec3 vec3 = new Vec3(d, e, f).normalize().scale(g);
      ent.setDeltaMovement(vec3);
      double i = vec3.horizontalDistance();
      ent.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * 180.0F / (float)Math.PI));
      ent.setXRot((float)(Mth.atan2(vec3.y, i) * 180.0F / (float)Math.PI));
      ent.yRotO = ent.getYRot();
      ent.xRotO = ent.getXRot();
   }

   public static Entity findFirstEntityInFront3x3x3(Entity self) {
      Level level = self.level();
      Vec3 forward = Vec3.directionFromRotation(0.0F, self.getYRot());
      Vec3 center = self.position().add(0.0, 0.0, 0.0).add(forward.scale(5.0));
      AABB box = new AABB(
         center.x - 1.5, center.y - 0.0, center.z - 1.5, center.x + 1.5, center.y + 3.0, center.z + 1.5
      );
      List<Entity> list = level.getEntities(self, box, e -> e.isAlive() && e != self);
      return list.isEmpty() ? null : list.get(0);
   }

   private boolean isTargetClass(Entity e) {
      boolean onBlackList = e instanceof BossHitBoxEntity || e instanceof EnderDragon || e instanceof EnderDragonPart;
      return !onBlackList;
   }

   @Override
   public boolean isToggleOn(ActionWeapon.WeaponMechInterface context) {
      return context.getMechEntity() != null && context.getMechEntity().havingEntity().isPresent();
   }

   public WadaItemRenderer newRenderer() {
      return new WadaItemRenderer();
   }

   @Override
   public BasePartsItem.WeaponInterface.WeaponAttachPoint getWeaponAttachPoint() {
      return BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_HAND;
   }

   @Override
   public BasePartsItem.WeaponCategory getWeaponCategory() {
      return BasePartsItem.WeaponCategory.MELEE;
   }

   @Override
   public Motion getMotion() {
      return Motion.RAISE_THROW;
   }

   @Override
   public int getCoolTime() {
      return 20;
   }

   @Override
   public String getPartsSeriesName() {
      return "wada";
   }
}
