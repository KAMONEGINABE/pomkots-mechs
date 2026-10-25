package grcmcs.minecraft.mods.pomkotsmechs.client.input;

import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicle;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TargetLocker {
   private static final Logger LOGGER = LoggerFactory.getLogger("pomkotsmechs");
   private static final float COSINE_THRESHOLD = Mth.m_14089_((float)Math.toRadians(30.0));
   private static final float COSINE_THRESHOLD2 = Mth.m_14089_((float)Math.toRadians(10.0));
   private static final TargetLocker singleton = new TargetLocker();
   private Minecraft minecraft = Minecraft.m_91087_();
   private TargetFinder finder = new TargetFinder();
   private Entity targetSoft = null;
   private Entity targetHard = null;
   public Map<Integer, Entity> targetMulti = new HashMap<>();
   public Map<Integer, Entity> targetMultiRA = new HashMap<>();
   public Map<Integer, Entity> targetMultiLA = new HashMap<>();
   public Map<Integer, Entity> targetMultiRS = new HashMap<>();
   public Map<Integer, Entity> targetMultiLS = new HashMap<>();
   private int lockOnMultiCooltime = 0;
   public static final int SOFT = 0;
   public static final int HARD = 1;
   public static final int MULTI = 2;
   public static final int NONE = 3;

   public static TargetLocker getInstance() {
      return singleton;
   }

   public void clearLockTargets() {
      this.targetSoft = null;
      this.targetHard = null;
      if (this.targetMulti != null) {
         this.targetMulti.clear();
      }

      if (this.targetMultiRA != null) {
         this.targetMultiRA.clear();
      }

      if (this.targetMultiLA != null) {
         this.targetMultiLA.clear();
      }

      if (this.targetMultiRS != null) {
         this.targetMultiRS.clear();
      }

      if (this.targetMultiLS != null) {
         this.targetMultiLS.clear();
      }
   }

   public void tick(DriverInput driverInput, PomkotsVehicle bot) {
      if (this.targetSoft != null && !this.targetSoft.m_6084_()) {
         this.unlockTargetSoft();
      }

      if (this.targetHard != null && !this.targetHard.m_6084_()) {
         this.unlockTargetHard();
      }

      if (!this.targetMulti.isEmpty()) {
         this.unlockTargetMulti(this.targetMulti);
      }

      if (!this.targetMultiRA.isEmpty()) {
         this.unlockTargetMulti(this.targetMultiRA);
      }

      if (!this.targetMultiLA.isEmpty()) {
         this.unlockTargetMulti(this.targetMultiLA);
      }

      if (!this.targetMultiRS.isEmpty()) {
         this.unlockTargetMulti(this.targetMultiRS);
      }

      if (!this.targetMultiLS.isEmpty()) {
         this.unlockTargetMulti(this.targetMultiLS);
      }

      if (this.isLockingHard()) {
         if (bot.shouldLockStrong(driverInput)) {
            this.unlockTargetHard();
         }
      } else {
         if (bot.shouldLockWeak(driverInput)) {
            if (this.targetSoft == null) {
               this.lockOnTargetSoft(this.getCrossHairTarget2());
            } else if (!this.isInLockonTraceRange(this.targetSoft, COSINE_THRESHOLD)) {
               this.unlockTargetSoft();
            }
         } else {
            this.unlockTargetSoft();
         }

         if (bot.shouldLockStrong(driverInput)) {
            Entity tgt = this.findTargetHard();
            if (tgt != null) {
               this.lockOnTargetHard(tgt);
            }
         }
      }

      this.tickMultiLock(bot, driverInput);
   }

   public Entity getHardLockTarget() {
      return this.targetHard;
   }

   public Entity getSoftLockTarget() {
      return this.targetSoft;
   }

   public Entity getCrossHairTarget2() {
      return this.finder.findTargetEntity(this.minecraft.f_91074_);
   }

   public Entity getCrossHairTarget() {
      for (LivingEntity ent : this.getEntitiesAroundPlayer(this.minecraft.f_91074_, 150.0)) {
         if (this.isInLockonTraceRange(ent, COSINE_THRESHOLD2)) {
            return ent;
         }
      }

      return null;
   }

   private void tickMultiLock(PomkotsVehicle bot, DriverInput driverInput) {
      if (bot instanceof Pmvc01Entity customMech) {
         if (this.lockOnMultiCooltime > 0) {
            this.lockOnMultiCooltime--;
         } else {
            this.addTargetMultiCustom(customMech, driverInput);
         }

         this.releaseTargetMultiCustomAll(customMech, driverInput);
      } else {
         if (this.lockOnMultiCooltime > 0) {
            this.lockOnMultiCooltime--;
         } else if (bot.shouldLockMulti(driverInput)) {
            Entity target = this.findMultiLockTarget();
            if (target != null) {
               this.addTargetMulti(target);
            }
         }

         if (!driverInput.isWeaponRightShoulderPressed() && !this.targetMulti.isEmpty()) {
            this.releaseTargetMulti(bot);
         }
      }
   }

   private Entity findMultiLockTarget() {
      Entity target = null;
      if (this.targetHard != null) {
         target = this.targetHard;
      } else if (this.targetSoft != null) {
         target = this.targetSoft;
      } else {
         Entity ent = this.getCrossHairTarget2();
         if (ent != null) {
            target = ent;
         }
      }

      return target;
   }

   private void addTargetMultiCustom(Pmvc01Entity mech, DriverInput driverInput) {
      boolean added = false;
      if (mech.shouldLockMulti(driverInput)) {
         Entity target = this.findMultiLockTarget();
         if (target != null) {
            if (driverInput.isWeaponRightHandPressed()) {
               added |= this.addTargetMultiCustom(this.targetMultiRA, 6, target, mech.getRightArmWeapon(), mech);
            }

            if (driverInput.isWeaponLeftHandPressed()) {
               added |= this.addTargetMultiCustom(this.targetMultiLA, 7, target, mech.getLeftArmWeapon(), mech);
            }

            if (driverInput.isWeaponRightShoulderPressed()) {
               added |= this.addTargetMultiCustom(this.targetMultiRS, 8, target, mech.getRightShoulderWeapon(), mech);
            }

            if (driverInput.isWeaponLeftShoulderPressed()) {
               added |= this.addTargetMultiCustom(this.targetMultiLS, 9, target, mech.getLeftShoulderWeapon(), mech);
            }
         }
      }

      if (added) {
         this.minecraft.f_91074_.m_5496_((SoundEvent)PomkotsMechs.SE_TARGET_EVENT.get(), 1.0F, 1.0F);
      }
   }

   private boolean addTargetMultiCustom(Map<Integer, Entity> map, int slot, Entity target, ItemStack item, Pmvc01Entity mech) {
      if (map.size() >= Pmvc01Entity.getMultiLockTargetNum(item) && !mech.isGattai()) {
         return false;
      }

      map.put(target.m_19879_(), target);
      this.lockOnMultiCooltime = 10;
      this.sendServerLockMultiCustom(target.m_19879_(), slot);
      return true;
   }

   private void releaseTargetMultiCustomAll(Pmvc01Entity mech, DriverInput driverInput) {
      this.releaseTargetMultiCustom(mech, driverInput.isWeaponRightHandPressed(), this.targetMultiRA, this.targetMultiLA, 6, 7);
      this.releaseTargetMultiCustom(mech, driverInput.isWeaponLeftHandPressed(), this.targetMultiLA, this.targetMultiRA, 7, 6);
      this.releaseTargetMultiCustom(mech, driverInput.isWeaponRightShoulderPressed(), this.targetMultiRS, this.targetMultiLS, 8, 9);
      this.releaseTargetMultiCustom(mech, driverInput.isWeaponLeftShoulderPressed(), this.targetMultiLS, this.targetMultiRS, 9, 8);
   }

   private void releaseTargetMultiCustom(
      Pmvc01Entity mech, boolean pressed, Map<Integer, Entity> target, Map<Integer, Entity> targetLinked, int inv, int invLinked
   ) {
      if (!pressed && !target.isEmpty()) {
         target.clear();
         if (mech.shouldLinkWeapon(inv, invLinked)) {
            targetLinked.clear();
         }

         mech.getLockTargets().unlockTargetMulti();
         this.sendServerUnlockMulti();
      }
   }

   private void addTargetMulti(Entity ent) {
      if (this.targetMulti.size() < 6) {
         this.targetMulti.put(ent.m_19879_(), ent);
         this.lockOnMultiCooltime = 10;
         this.sendServerLockMulti(ent.m_19879_());
         this.minecraft.f_91074_.m_5496_((SoundEvent)PomkotsMechs.SE_TARGET_EVENT.get(), 1.0F, 1.0F);
      }
   }

   private void releaseTargetMulti(PomkotsVehicle bot) {
      this.targetMulti.clear();
      bot.getLockTargets().unlockTargetMulti();
      this.sendServerUnlockMulti();
   }

   private void unlockTargetMulti(Map<Integer, Entity> map) {
      List<Integer> removeTarget = null;

      for (Entry<Integer, Entity> entry : map.entrySet()) {
         if (entry.getValue() != null && !entry.getValue().m_6084_()) {
            if (removeTarget == null) {
               removeTarget = new ArrayList<>();
            }

            removeTarget.add(entry.getKey());
         }
      }

      if (removeTarget != null) {
         for (Integer key : removeTarget) {
            map.remove(key);
         }
      }
   }

   private void sendServerLockMulti(int entityId) {
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.copyInt(entityId));
      NetworkManager.sendToServer(PomkotsMechs.id("lm"), buf);
   }

   private void sendServerUnlockMulti() {
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.EMPTY_BUFFER);
      NetworkManager.sendToServer(PomkotsMechs.id("ulm"), buf);
   }

   private void sendServerLockMultiCustom(int entityId, int slot) {
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.copyInt(entityId));
      buf.writeInt(slot);
      NetworkManager.sendToServer(PomkotsMechs.id("lmc"), buf);
   }

   private Entity findTargetHard() {
      Entity res;
      if (this.targetSoft != null) {
         res = this.targetSoft;
      } else {
         res = this.getCrossHairTarget();
         if (res == null) {
            Player p = this.minecraft.f_91074_;
            List<LivingEntity> list = this.getEntitiesAroundPlayer(p, 50.0);
            res = this.getClosestEntityInLookDirection(p, list);
         }
      }

      return res;
   }

   public List<LivingEntity> getEntitiesAroundPlayer(LivingEntity player, double range) {
      Level world = player.m_9236_();
      Vec3 playerPos = player.m_20182_();
      AABB searchBox = new AABB(
         playerPos.f_82479_ - range,
         playerPos.f_82480_ - range,
         playerPos.f_82481_ - range,
         playerPos.f_82479_ + range,
         playerPos.f_82480_ + range,
         playerPos.f_82481_ + range
      );
      return world.m_6443_(LivingEntity.class, searchBox, entity -> entity != player && player.m_142582_(entity) && !this.isSelf(entity, player));
   }

   private boolean isSelf(LivingEntity entity, LivingEntity player) {
      if (entity instanceof PomkotsVehicleBase) {
         LivingEntity driver = ((PomkotsVehicleBase)entity).getDrivingPassenger();
         return driver == null ? false : driver.equals(player);
      } else {
         return false;
      }
   }

   private Entity getClosestEntityInLookDirection(LivingEntity player, List<LivingEntity> entities) {
      double maxCosineSimilarity = -1.0;
      LivingEntity closestEntity = null;
      Vec3 lookVector = player.m_20154_().m_82541_();
      Vec3 playerPos = player.m_20182_();

      for (LivingEntity entity : entities) {
         Vec3 entityVector = entity.m_20182_().m_82546_(playerPos).m_82541_();
         double cosineSimilarity = lookVector.m_82526_(entityVector);
         if (cosineSimilarity > maxCosineSimilarity && !this.isSelf(entity, player)) {
            maxCosineSimilarity = cosineSimilarity;
            closestEntity = entity;
         }
      }

      return closestEntity;
   }

   public boolean isLockingHard() {
      return this.targetHard != null;
   }

   private void lockOnTargetHard(Entity ent) {
      if (ent != null) {
         this.sendServerLockHard(ent.m_19879_());
         this.targetHard = ent;
         this.minecraft.f_91074_.m_5496_((SoundEvent)PomkotsMechs.SE_TARGET_EVENT.get(), 1.0F, 1.0F);
      }
   }

   private void unlockTargetHard() {
      if (this.targetHard != null) {
         this.sendServerUnlockHard();
      }

      this.targetHard = null;
   }

   private void sendServerLockHard(int entityId) {
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.copyInt(entityId));
      NetworkManager.sendToServer(PomkotsMechs.id("lh"), buf);
   }

   private void sendServerUnlockHard() {
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.EMPTY_BUFFER);
      NetworkManager.sendToServer(PomkotsMechs.id("ulh"), buf);
   }

   public boolean turn(LocalPlayer player) {
      if (Utils.isRidingPomkotsVehicle(player)) {
         if (this.targetHard != null) {
            if (!this.targetHard.m_6084_()) {
               this.unlockTargetHard();
               return false;
            } else {
               Vec3 targetPos = this.targetHard.m_20182_().m_82520_(0.0, this.targetHard.m_20206_() - this.targetHard.m_20206_() / 2.0F, 0.0);
               Vec3 targetVec = targetPos.m_82546_(player.m_20182_().m_82520_(0.0, player.m_20192_(), 0.0)).m_82541_();
               double angleX = Mth.m_14175_(Math.atan2(-targetVec.f_82479_, targetVec.f_82481_) * 180.0 / Math.PI);
               double angleY = Math.atan2(targetVec.f_82480_, targetVec.m_165924_()) * 180.0 / Math.PI;
               double xRot = Mth.m_14177_(player.m_146909_());
               double yRot = Mth.m_14177_(player.m_146908_());
               double toTurnX = Mth.m_14175_(yRot - angleX);
               double toTurnY = Mth.m_14175_(xRot + angleY);
               player.m_19884_(-toTurnX, -toTurnY);
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public boolean isLockingSoft() {
      return this.targetHard != null;
   }

   private void lockOnTargetSoft(Entity ent) {
      if (ent != null) {
         this.sendServerLockSoft(ent.m_19879_());
         this.targetSoft = ent;
      }
   }

   private void unlockTargetSoft() {
      if (this.targetSoft != null) {
         this.sendServerUnlocksoft();
      }

      this.targetSoft = null;
   }

   private boolean isInLockonTraceRange(Entity ent, float cosineThreshold) {
      Camera cam = this.minecraft.f_91063_.m_109153_();
      Vec3 playerToEnemy = ent.m_20182_().m_82546_(cam.m_90583_()).m_82541_();
      Vec3 playerLookDirection = new Vec3(cam.m_253058_().normalize());
      double dotProduct = playerToEnemy.m_82526_(playerLookDirection);
      return dotProduct >= cosineThreshold;
   }

   private void sendServerLockSoft(int entityId) {
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.copyInt(entityId));
      NetworkManager.sendToServer(PomkotsMechs.id("ls"), buf);
   }

   private void sendServerUnlocksoft() {
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.EMPTY_BUFFER);
      NetworkManager.sendToServer(PomkotsMechs.id("uls"), buf);
   }

   public int isEntityLocked(Entity ent) {
      if (this.targetSoft == ent) {
         return 0;
      } else if (this.targetHard == ent) {
         return 1;
      } else if (this.targetMulti.containsKey(ent.m_19879_())) {
         return 2;
      } else {
         return !this.targetMultiRA.containsKey(ent.m_19879_())
               && !this.targetMultiLA.containsKey(ent.m_19879_())
               && !this.targetMultiRS.containsKey(ent.m_19879_())
               && !this.targetMultiLS.containsKey(ent.m_19879_())
            ? 3
            : 2;
      }
   }

   private Entity getCrossHairTargetFromPick() {
      HitResult hit = this.minecraft.f_91063_.m_109153_().m_90592_().m_19907_(1000.0, 1.0F, true);
      if (hit.m_6662_() == Type.ENTITY) {
         EntityHitResult entityHit = (EntityHitResult)hit;
         return entityHit.m_82443_();
      } else {
         return null;
      }
   }

   private Entity getCrossHairTargetFromCameraDirection() {
      Entity entity = this.minecraft.m_91288_();
      Entity crosshairPickEntity = null;
      double maxDistance = 1000.0;
      HitResult hitResult = entity.m_19907_(maxDistance, 1.0F, false);
      Vec3 vec3 = entity.m_20299_(1.0F);
      boolean bl = false;
      double e = 6.0;
      maxDistance = e;
      e *= e;
      if (hitResult != null) {
         e = hitResult.m_82450_().m_82557_(vec3);
      }

      Vec3 vec32 = entity.m_20252_(1.0F);
      Vec3 vec33 = vec3.m_82520_(vec32.f_82479_ * maxDistance, vec32.f_82480_ * maxDistance, vec32.f_82481_ * maxDistance);
      float g = 1.0F;
      AABB aABB = entity.m_20191_().m_82369_(vec32.m_82490_(maxDistance)).m_82377_(1.0, 1.0, 1.0);
      EntityHitResult entityHitResult = ProjectileUtil.m_37287_(entity, vec3, vec33, aABB, entityx -> !entityx.m_5833_() && entityx.m_6087_(), e);
      if (entityHitResult != null) {
         Entity entity2 = entityHitResult.m_82443_();
         Vec3 vec34 = entityHitResult.m_82450_();
         double h = vec3.m_82557_(vec34);
         if (bl && h > 9.0) {
            HitResult var20 = BlockHitResult.m_82426_(vec34, Direction.m_122366_(vec32.f_82479_, vec32.f_82480_, vec32.f_82481_), BlockPos.m_274446_(vec34));
         } else if ((h < e || hitResult == null) && (entity2 instanceof LivingEntity || entity2 instanceof ItemFrame)) {
            crosshairPickEntity = entity2;
         }
      }

      return crosshairPickEntity;
   }

   public Entity getCrossHairTargetFromLookAtDirection(Player player, double maxDistance) {
      Vec3 startVec = player.m_146892_();
      Vec3 lookVec = player.m_20252_(1.0F).m_82490_(maxDistance);
      Vec3 endVec = startVec.m_82549_(lookVec);
      ClipContext context = new ClipContext(startVec, endVec, Block.OUTLINE, Fluid.NONE, player);
      HitResult hitResult = player.m_9236_().m_45547_(context);
      if (hitResult.m_6662_() == Type.ENTITY) {
         EntityHitResult entityHitResult = (EntityHitResult)hitResult;
         if (entityHitResult != null && entityHitResult.m_82443_() != player.m_20202_()) {
            return entityHitResult.m_82443_();
         }
      }

      return null;
   }
}
