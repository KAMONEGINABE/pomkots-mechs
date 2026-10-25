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
   private static final float COSINE_THRESHOLD = Mth.cos((float)Math.toRadians(30.0));
   private static final float COSINE_THRESHOLD2 = Mth.cos((float)Math.toRadians(10.0));
   private static final TargetLocker singleton = new TargetLocker();
   private Minecraft minecraft = Minecraft.getInstance();
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
      if (this.targetSoft != null && !this.targetSoft.isAlive()) {
         this.unlockTargetSoft();
      }

      if (this.targetHard != null && !this.targetHard.isAlive()) {
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
      return this.finder.findTargetEntity(this.minecraft.player);
   }

   public Entity getCrossHairTarget() {
      for (LivingEntity ent : this.getEntitiesAroundPlayer(this.minecraft.player, 150.0)) {
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
         this.minecraft.player.playSound((SoundEvent)PomkotsMechs.SE_TARGET_EVENT.get(), 1.0F, 1.0F);
      }
   }

   private boolean addTargetMultiCustom(Map<Integer, Entity> map, int slot, Entity target, ItemStack item, Pmvc01Entity mech) {
      if (map.size() >= Pmvc01Entity.getMultiLockTargetNum(item) && !mech.isGattai()) {
         return false;
      }

      map.put(target.getId(), target);
      this.lockOnMultiCooltime = 10;
      this.sendServerLockMultiCustom(target.getId(), slot);
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
         this.targetMulti.put(ent.getId(), ent);
         this.lockOnMultiCooltime = 10;
         this.sendServerLockMulti(ent.getId());
         this.minecraft.player.playSound((SoundEvent)PomkotsMechs.SE_TARGET_EVENT.get(), 1.0F, 1.0F);
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
         if (entry.getValue() != null && !entry.getValue().isAlive()) {
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
            Player p = this.minecraft.player;
            List<LivingEntity> list = this.getEntitiesAroundPlayer(p, 50.0);
            res = this.getClosestEntityInLookDirection(p, list);
         }
      }

      return res;
   }

   public List<LivingEntity> getEntitiesAroundPlayer(LivingEntity player, double range) {
      Level world = player.level();
      Vec3 playerPos = player.position();
      AABB searchBox = new AABB(
         playerPos.x - range,
         playerPos.y - range,
         playerPos.z - range,
         playerPos.x + range,
         playerPos.y + range,
         playerPos.z + range
      );
      return world.getEntitiesOfClass(LivingEntity.class, searchBox, entity -> entity != player && player.hasLineOfSight(entity) && !this.isSelf(entity, player));
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
      Vec3 lookVector = player.getLookAngle().normalize();
      Vec3 playerPos = player.position();

      for (LivingEntity entity : entities) {
         Vec3 entityVector = entity.position().subtract(playerPos).normalize();
         double cosineSimilarity = lookVector.dot(entityVector);
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
         this.sendServerLockHard(ent.getId());
         this.targetHard = ent;
         this.minecraft.player.playSound((SoundEvent)PomkotsMechs.SE_TARGET_EVENT.get(), 1.0F, 1.0F);
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
            if (!this.targetHard.isAlive()) {
               this.unlockTargetHard();
               return false;
            } else {
               Vec3 targetPos = this.targetHard.position().add(0.0, this.targetHard.getBbHeight() - this.targetHard.getBbHeight() / 2.0F, 0.0);
               Vec3 targetVec = targetPos.subtract(player.position().add(0.0, player.getEyeHeight(), 0.0)).normalize();
               double angleX = Mth.wrapDegrees(Math.atan2(-targetVec.x, targetVec.z) * 180.0 / Math.PI);
               double angleY = Math.atan2(targetVec.y, targetVec.horizontalDistance()) * 180.0 / Math.PI;
               double xRot = Mth.wrapDegrees(player.getXRot());
               double yRot = Mth.wrapDegrees(player.getYRot());
               double toTurnX = Mth.wrapDegrees(yRot - angleX);
               double toTurnY = Mth.wrapDegrees(xRot + angleY);
               player.turn(-toTurnX, -toTurnY);
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
         this.sendServerLockSoft(ent.getId());
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
      Camera cam = this.minecraft.gameRenderer.getMainCamera();
      Vec3 playerToEnemy = ent.position().subtract(cam.getPosition()).normalize();
      Vec3 playerLookDirection = new Vec3(cam.getLookVector().normalize());
      double dotProduct = playerToEnemy.dot(playerLookDirection);
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
      } else if (this.targetMulti.containsKey(ent.getId())) {
         return 2;
      } else {
         return !this.targetMultiRA.containsKey(ent.getId())
               && !this.targetMultiLA.containsKey(ent.getId())
               && !this.targetMultiRS.containsKey(ent.getId())
               && !this.targetMultiLS.containsKey(ent.getId())
            ? 3
            : 2;
      }
   }

   private Entity getCrossHairTargetFromPick() {
      HitResult hit = this.minecraft.gameRenderer.getMainCamera().getEntity().pick(1000.0, 1.0F, true);
      if (hit.getType() == Type.ENTITY) {
         EntityHitResult entityHit = (EntityHitResult)hit;
         return entityHit.getEntity();
      } else {
         return null;
      }
   }

   private Entity getCrossHairTargetFromCameraDirection() {
      Entity entity = this.minecraft.getCameraEntity();
      Entity crosshairPickEntity = null;
      double maxDistance = 1000.0;
      HitResult hitResult = entity.pick(maxDistance, 1.0F, false);
      Vec3 vec3 = entity.getEyePosition(1.0F);
      boolean bl = false;
      double e = 6.0;
      maxDistance = e;
      e *= e;
      if (hitResult != null) {
         e = hitResult.getLocation().distanceToSqr(vec3);
      }

      Vec3 vec32 = entity.getViewVector(1.0F);
      Vec3 vec33 = vec3.add(vec32.x * maxDistance, vec32.y * maxDistance, vec32.z * maxDistance);
      float g = 1.0F;
      AABB aABB = entity.getBoundingBox().expandTowards(vec32.scale(maxDistance)).inflate(1.0, 1.0, 1.0);
      EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(entity, vec3, vec33, aABB, entityx -> !entityx.isSpectator() && entityx.isPickable(), e);
      if (entityHitResult != null) {
         Entity entity2 = entityHitResult.getEntity();
         Vec3 vec34 = entityHitResult.getLocation();
         double h = vec3.distanceToSqr(vec34);
         if (bl && h > 9.0) {
            HitResult var20 = BlockHitResult.miss(vec34, Direction.getNearest(vec32.x, vec32.y, vec32.z), BlockPos.containing(vec34));
         } else if ((h < e || hitResult == null) && (entity2 instanceof LivingEntity || entity2 instanceof ItemFrame)) {
            crosshairPickEntity = entity2;
         }
      }

      return crosshairPickEntity;
   }

   public Entity getCrossHairTargetFromLookAtDirection(Player player, double maxDistance) {
      Vec3 startVec = player.getEyePosition();
      Vec3 lookVec = player.getViewVector(1.0F).scale(maxDistance);
      Vec3 endVec = startVec.add(lookVec);
      ClipContext context = new ClipContext(startVec, endVec, Block.OUTLINE, Fluid.NONE, player);
      HitResult hitResult = player.level().clip(context);
      if (hitResult.getType() == Type.ENTITY) {
         EntityHitResult entityHitResult = (EntityHitResult)hitResult;
         if (entityHitResult != null && entityHitResult.getEntity() != player.getVehicle()) {
            return entityHitResult.getEntity();
         }
      }

      return null;
   }
}
