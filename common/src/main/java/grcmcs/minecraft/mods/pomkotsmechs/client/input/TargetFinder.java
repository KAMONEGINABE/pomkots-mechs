package grcmcs.minecraft.mods.pomkotsmechs.client.input;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PlateEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;

public class TargetFinder {
   private final double maxDistance;
   private final double viewAngle;
   private final float viewAngleCosineThreshold;
   private final TargetPriority targetPriority;
   private Minecraft minecraft = Minecraft.getInstance();

   TargetFinder() {
      this.maxDistance = 150.0;
      this.viewAngle = 10.0;
      this.viewAngleCosineThreshold = Mth.cos((float)Math.toRadians(this.viewAngle));
      this.targetPriority = new TargetPriority(1.0, 1.0, 2.0);
   }

   public LivingEntity findTargetEntity(Player player) {
      Camera cam = this.minecraft.gameRenderer.getMainCamera();
      List<LivingEntity> candidates = this.getEntitiesInViewDirection(cam, player);
      return candidates.stream()
         .filter(entity -> this.isValidTarget(cam, entity))
         .max((a, b) -> Double.compare(this.targetPriority.calculatePriority(player, a), this.targetPriority.calculatePriority(player, b)))
         .orElse(null);
   }

   private List<LivingEntity> getEntitiesInViewDirection(Camera cam, Player player) {
      Level level = player.level();
      Vec3 eyePos = cam.getPosition();
      Vec3 lookDirection = new Vec3(cam.getLookVector());
      double searchWidth = this.maxDistance * Math.tan(Math.toRadians(this.viewAngle / 2.0)) * 2.2;
      AABB roughBox = this.createDirectionalAABB(eyePos, lookDirection, this.maxDistance, searchWidth);
      return level.getEntitiesOfClass(
         LivingEntity.class,
         roughBox,
         entity -> entity != player && !this.isSelf(entity, player) && this.isTargetClass(entity) && this.hasLineOfSight(player, entity)
      );
   }

   private boolean isTargetClass(LivingEntity entity) {
      return entity instanceof PomkotsVehicleBase && PomkotsMechs.CONFIG.targetLockPomkotsVehicles
         || entity instanceof GenericPomkotsMonster
         || entity instanceof BossHitBoxEntity
         || entity instanceof Player && PomkotsMechs.CONFIG.targetLockPlayers
         || entity instanceof PlateEntity
         || PomkotsMechs.CONFIG.targetLockNonPomkotsMobs && entity != null;
   }

   private boolean hasLineOfSight(Entity src, Entity candidate) {
      if (candidate.level() != src.level()) {
         return false;
      }

      Vec3 vec3 = new Vec3(src.getX(), src.getEyeY(), src.getZ());
      Vec3 vec32 = new Vec3(candidate.getX(), candidate.getEyeY(), candidate.getZ());
      return vec32.distanceTo(vec3) > this.maxDistance
         ? false
         : src.level().clip(new ClipContext(vec3, vec32, Block.COLLIDER, Fluid.NONE, src)).getType() == Type.MISS;
   }

   private boolean isSelf(LivingEntity entity, LivingEntity player) {
      if (entity instanceof PomkotsVehicleBase) {
         LivingEntity driver = ((PomkotsVehicleBase)entity).getDrivingPassenger();
         return driver == null ? false : driver.equals(player);
      } else {
         return false;
      }
   }

   private boolean isValidTarget(Camera cam, LivingEntity entity) {
      return this.isInLockonTraceRange(cam, entity, this.viewAngleCosineThreshold);
   }

   private boolean isInLockonTraceRange(Camera cam, Entity ent, float cosineThreshold) {
      Vec3 playerToEnemy = ent.position().subtract(cam.getPosition()).normalize();
      Vec3 playerLookDirection = new Vec3(cam.getLookVector().normalize());
      double dotProduct = playerToEnemy.dot(playerLookDirection);
      return dotProduct >= cosineThreshold;
   }

   private AABB createDirectionalAABB(Vec3 eyePos, Vec3 lookDirection, double maxDistance, double width) {
      Vec3 direction = lookDirection.normalize();
      Vec3 endPoint = eyePos.add(direction.scale(maxDistance));
      Vec3 right = direction.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
      Vec3 up = right.cross(direction).normalize();
      double halfWidth = width / 2.0;
      Vec3 min = new Vec3(
         Math.min(eyePos.x, endPoint.x) - halfWidth,
         Math.min(eyePos.y, endPoint.y) - halfWidth,
         Math.min(eyePos.z, endPoint.z) - halfWidth
      );
      Vec3 max = new Vec3(
         Math.max(eyePos.x, endPoint.x) + halfWidth,
         Math.max(eyePos.y, endPoint.y) + halfWidth,
         Math.max(eyePos.z, endPoint.z) + halfWidth
      );
      return new AABB(min, max);
   }
}
