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
            if (world.f_46443_) {
               mechInterface.playSoundEffect((SoundEvent)PomkotsMechs.SE_LIFT.get());
            } else {
               Entity p = findFirstEntityInFront3x3x3(mech);
               if (p != null && this.isTargetClass(p)) {
                  Vec3 pos = mech.m_20182_();
                  p.m_6034_(pos.f_82479_, pos.f_82480_ + 7.5, pos.f_82481_);
                  p.m_20242_(true);
                  mech.setHavingEntity(Optional.of(p.m_20148_()));
               } else {
                  mech.setHavingEntity(Optional.empty());
               }
            }
         } else if (world.f_46443_) {
            mechInterface.playSoundEffect((SoundEvent)PomkotsMechs.SE_THROW.get());
         } else {
            Entity p = ((ServerLevel)world).m_8791_(mech.havingEntity().get());
            if (p != null) {
               p.f_19802_ = 20;
               Vec3 muzzlPos = new Vec3(0.0, 0.0, 6.0);
               muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * mechInterface.getYRot()));
               p.m_146884_(mech.m_20182_().m_82549_(muzzlPos));
               p.m_20242_(false);
            }

            mech.setHavingEntity(Optional.empty());
         }
      }
   }

   public static void shootFromRotation(Entity ent, Entity entity, float f, float g, float h, float i, float j) {
      float k = -Mth.m_14031_(g * (float) (Math.PI / 180.0)) * Mth.m_14089_(f * (float) (Math.PI / 180.0));
      float l = -Mth.m_14031_((f + h) * (float) (Math.PI / 180.0));
      float m = Mth.m_14089_(g * (float) (Math.PI / 180.0)) * Mth.m_14089_(f * (float) (Math.PI / 180.0));
      shoot(ent, k, l, m, i, j);
      Vec3 vec3 = entity.m_20184_();
      ent.m_20256_(ent.m_20184_().m_82520_(vec3.f_82479_, entity.m_20096_() ? 0.0 : vec3.f_82480_, vec3.f_82481_));
   }

   public static void shoot(Entity ent, double d, double e, double f, float g, float h) {
      Vec3 vec3 = new Vec3(d, e, f).m_82541_().m_82490_(g);
      ent.m_20256_(vec3);
      double i = vec3.m_165924_();
      ent.m_146922_((float)(Mth.m_14136_(vec3.f_82479_, vec3.f_82481_) * 180.0F / (float)Math.PI));
      ent.m_146926_((float)(Mth.m_14136_(vec3.f_82480_, i) * 180.0F / (float)Math.PI));
      ent.f_19859_ = ent.m_146908_();
      ent.f_19860_ = ent.m_146909_();
   }

   public static Entity findFirstEntityInFront3x3x3(Entity self) {
      Level level = self.m_9236_();
      Vec3 forward = Vec3.m_82498_(0.0F, self.m_146908_());
      Vec3 center = self.m_20182_().m_82520_(0.0, 0.0, 0.0).m_82549_(forward.m_82490_(5.0));
      AABB box = new AABB(
         center.f_82479_ - 1.5, center.f_82480_ - 0.0, center.f_82481_ - 1.5, center.f_82479_ + 1.5, center.f_82480_ + 3.0, center.f_82481_ + 1.5
      );
      List<Entity> list = level.m_6249_(self, box, e -> e.m_6084_() && e != self);
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
