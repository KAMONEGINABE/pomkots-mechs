package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class MissileHorizontalEntity extends MissileBaseEntity {
   public MissileHorizontalEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
      super(entityType, world);
      this.setNoGravity(true);
   }

   public MissileHorizontalEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, LivingEntity target) {
      this(entityType, world);
      this.shooter = shooter;
      this.target = target;
   }

   @Override
   protected LivingEntity findTarget() {
      int range = this.getSeekRange();
      List<Entity> candidates = this.level()
         .getEntitiesOfClass(
            this.getTargetClass(),
            new AABB(
               this.getX() - range,
               this.getY() - range,
               this.getZ() - range,
               this.getX() + range,
               this.getY() + range,
               this.getZ() + range
            ),
            new MissileHorizontalEntity.MissileTargetPredicate()
         );
      return (LivingEntity)this.getBestMatch(candidates, this);
   }

   public Entity getBestMatch(List<Entity> entities, Entity currentEntity) {
      Entity bestMatch = null;
      double bestScore = Double.MAX_VALUE;

      for (Entity entity : entities) {
         if (entity != this.shooter && entity.getVehicle() != this.shooter && entity != currentEntity) {
            double heightDifference = Math.abs(entity.getY() - currentEntity.getY());
            double distance = entity.distanceTo(currentEntity);
            double score = heightDifference * 100.0 + distance;
            if (score < bestScore) {
               bestScore = score;
               bestMatch = entity;
            }
         }
      }

      return bestMatch;
   }

   @Override
   protected void updateHomingMovement() {
      Vec3 currentPosition = this.position();
      Vec3 targetPosition = this.target.position().add(0.0, this.target.getBbHeight() / 2.0F, 0.0);
      Vec3 currentVelocity = this.getDeltaMovement().normalize();
      Vec3 directionToTarget = targetPosition.subtract(currentPosition).normalize();
      double angleBetween = Math.acos(currentVelocity.dot(directionToTarget));
      double maxRotationRadians = Math.toRadians(this.getMaxRotationAnglePerTick());
      Vec3 newVelocity;
      if (angleBetween > maxRotationRadians) {
         newVelocity = this.rotateTowards(currentVelocity, directionToTarget, maxRotationRadians);
      } else {
         newVelocity = directionToTarget;
      }

      this.setDeltaMovement(newVelocity.scale(this.getSpeed()));
   }

   @Override
   protected int getSwitchTick() {
      return 7;
   }

   @Override
   protected double getSpeed() {
      return 2.0;
   }

   @Override
   protected int getSeekRange() {
      return 40;
   }

   @Override
   protected float getMaxRotationAnglePerTick() {
      return 10.0F;
   }

   @Override
   protected float getDamage() {
      return 60.0F;
   }

   @Override
   protected int getMaxLifeTicks() {
      return 120;
   }

   @Override
   protected Vec3 getNonTargetVelocity() {
      return this.getDeltaMovement();
   }

   @Override
   protected Class getTargetClass() {
      return LivingEntity.class;
   }

   private class MissileTargetPredicate<LivingEntity> implements Predicate<LivingEntity> {
      @Override
      public boolean test(LivingEntity entity) {
         return true;
      }
   }
}
