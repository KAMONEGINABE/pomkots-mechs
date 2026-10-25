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
   private Minecraft minecraft = Minecraft.m_91087_();

   TargetFinder() {
      this.maxDistance = 150.0;
      this.viewAngle = 10.0;
      this.viewAngleCosineThreshold = Mth.m_14089_((float)Math.toRadians(this.viewAngle));
      this.targetPriority = new TargetPriority(1.0, 1.0, 2.0);
   }

   public LivingEntity findTargetEntity(Player player) {
      Camera cam = this.minecraft.f_91063_.m_109153_();
      List<LivingEntity> candidates = this.getEntitiesInViewDirection(cam, player);
      return candidates.stream()
         .filter(entity -> this.isValidTarget(cam, entity))
         .max((a, b) -> Double.compare(this.targetPriority.calculatePriority(player, a), this.targetPriority.calculatePriority(player, b)))
         .orElse(null);
   }

   private List<LivingEntity> getEntitiesInViewDirection(Camera cam, Player player) {
      Level level = player.m_9236_();
      Vec3 eyePos = cam.m_90583_();
      Vec3 lookDirection = new Vec3(cam.m_253058_());
      double searchWidth = this.maxDistance * Math.tan(Math.toRadians(this.viewAngle / 2.0)) * 2.2;
      AABB roughBox = this.createDirectionalAABB(eyePos, lookDirection, this.maxDistance, searchWidth);
      return level.m_6443_(
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
      if (candidate.m_9236_() != src.m_9236_()) {
         return false;
      }

      Vec3 vec3 = new Vec3(src.m_20185_(), src.m_20188_(), src.m_20189_());
      Vec3 vec32 = new Vec3(candidate.m_20185_(), candidate.m_20188_(), candidate.m_20189_());
      return vec32.m_82554_(vec3) > this.maxDistance
         ? false
         : src.m_9236_().m_45547_(new ClipContext(vec3, vec32, Block.COLLIDER, Fluid.NONE, src)).m_6662_() == Type.MISS;
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
      Vec3 playerToEnemy = ent.m_20182_().m_82546_(cam.m_90583_()).m_82541_();
      Vec3 playerLookDirection = new Vec3(cam.m_253058_().normalize());
      double dotProduct = playerToEnemy.m_82526_(playerLookDirection);
      return dotProduct >= cosineThreshold;
   }

   private AABB createDirectionalAABB(Vec3 eyePos, Vec3 lookDirection, double maxDistance, double width) {
      Vec3 direction = lookDirection.m_82541_();
      Vec3 endPoint = eyePos.m_82549_(direction.m_82490_(maxDistance));
      Vec3 right = direction.m_82537_(new Vec3(0.0, 1.0, 0.0)).m_82541_();
      Vec3 up = right.m_82537_(direction).m_82541_();
      double halfWidth = width / 2.0;
      Vec3 min = new Vec3(
         Math.min(eyePos.f_82479_, endPoint.f_82479_) - halfWidth,
         Math.min(eyePos.f_82480_, endPoint.f_82480_) - halfWidth,
         Math.min(eyePos.f_82481_, endPoint.f_82481_) - halfWidth
      );
      Vec3 max = new Vec3(
         Math.max(eyePos.f_82479_, endPoint.f_82479_) + halfWidth,
         Math.max(eyePos.f_82480_, endPoint.f_82480_) + halfWidth,
         Math.max(eyePos.f_82481_, endPoint.f_82481_) + halfWidth
      );
      return new AABB(min, max);
   }
}
