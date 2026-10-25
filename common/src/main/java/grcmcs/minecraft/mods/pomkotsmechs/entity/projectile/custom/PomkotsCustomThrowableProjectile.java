package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.custom;

import grcmcs.minecraft.mods.pomkotsmechs.client.particles.ParticleUtil;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BossHitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.PomkotsThrowableProjectile;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ProjectileUtil;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
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

public abstract class PomkotsCustomThrowableProjectile extends PomkotsThrowableProjectile {
   protected int maxLifeTicks = 20;
   protected int lifeTicks = 0;
   protected float damage;
   protected int stun;
   protected double initialSpeed = 0.0;
   protected boolean toDiscard = false;
   private static final int SUBSTEPS = 1;

   public PomkotsCustomThrowableProjectile(
      EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, int maxLifeTicks, float damage, int stun
   ) {
      super(entityType, shooter, world);
      this.maxLifeTicks = maxLifeTicks;
      this.damage = damage;
      this.stun = stun;
      if (shooter instanceof Pmvc01Entity pmvc01 && pmvc01.getDrivingPassenger() != null) {
         this.setOwner(pmvc01.getDrivingPassenger());
      }
   }

   @Override
   public void tick() {
      if (this.toDiscard) {
         this.discard();
      }

      this.setOldPosAndRot();
      if (!this.level().isClientSide) {
         if (this.lifeTicks++ >= this.maxLifeTicks) {
            this.discard();
         } else {
            this.checkInsideBlocks();
            if (this.performSubStepMovement()) {
               ;
            }
         }
      }
   }

   protected boolean performSubStepMovement() {
      if (this.firstTick) {
         this.initialSpeed = this.getDeltaMovement().distanceTo(Vec3.ZERO);
      }

      Vec3 totalMovement = this.getDeltaMovement();
      Vec3 subStepMovement = totalMovement;

      for (int step = 0; step < 1; step++) {
         Vec3 start = this.position();
         Vec3 end = start.add(subStepMovement);
         BlockHitResult blockHit = this.level().clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, this));
         AABB searchBox = new AABB(start, end).inflate(0.5);
         EntityHitResult entityHit = this.getEntityHitResult(
            this.level(),
            this,
            start,
            end,
            searchBox,
            entity -> entity != this.getShooter()
               && entity != this
               && entity.isAlive()
               && entity.isPickable()
               && !entity.isSpectator()
               && !(entity instanceof BossHitBoxEntity hb && hb.isRelatedShooter(this))
         );
         HitResult hit = this.selectCloserHit(start, blockHit, entityHit);
         if (hit != null && hit.getType() != Type.MISS) {
            this.setPosInSubStep(hit.getLocation());
            this.onHit(hit);
            return false;
         }

         this.setPosInSubStep(end);
      }

      return true;
   }

   protected void setPosInSubStep(Vec3 pos) {
      this.setPos(pos);
   }

   protected EntityHitResult getEntityHitResult(Level level, Entity projectile, Vec3 start, Vec3 end, AABB searchBox, Predicate<Entity> filter) {
      Entity closestEntity = null;
      Vec3 closestHitPos = null;
      double closestDist = Double.MAX_VALUE;

      for (Entity candidate : level.getEntities(projectile, searchBox, filter)) {
         AABB candidateBox = candidate.getBoundingBox().inflate(candidate.getPickRadius());
         Optional<Vec3> hitPos = candidateBox.clip(start, end);
         if (hitPos.isPresent()) {
            double dist = start.distanceToSqr(hitPos.get());
            if (dist < closestDist) {
               closestDist = dist;
               closestEntity = candidate;
               closestHitPos = hitPos.get();
            }
         }
      }

      return closestEntity != null ? new EntityHitResult(closestEntity, closestHitPos) : null;
   }

   protected HitResult selectCloserHit(Vec3 origin, BlockHitResult blockHit, EntityHitResult entityHit) {
      boolean blockMiss = blockHit.getType() == Type.MISS;
      if (entityHit == null && blockMiss) {
         return null;
      }

      if (entityHit == null) {
         return blockHit;
      }

      if (blockMiss) {
         return entityHit;
      }

      double blockDist = origin.distanceToSqr(blockHit.getLocation());
      double entityDist = origin.distanceToSqr(entityHit.getLocation());
      return (HitResult)(entityDist <= blockDist ? entityHit : blockHit);
   }

   protected void onHitEntity(EntityHitResult entityHitResult) {
      Entity entity = entityHitResult.getEntity();
      if (!(entity instanceof BossHitBoxEntity hb && hb.isRelatedShooter(this))) {
         float finalDamage = this.calcFinalDamage(this.damage);
         if (entity instanceof BaseBossEntity boss) {
            boss.addStunPoint(this.stun);
         } else if (entity instanceof BossHitBoxEntity hitbox) {
            hitbox.addStunPoint(this.stun);
         }

         ProjectileUtil.hurt(entity, this, this.getOwner(), finalDamage);
         entity.invulnerableTime = 0;
         this.toDiscard = true;
      }
   }

   protected float calcFinalDamage(float baseDamage) {
      return baseDamage;
   }

   public int getStunPoint() {
      return this.stun;
   }

   public void setStunPoint(int stunPoint) {
      this.stun = stunPoint;
   }

   protected void onHitBlock(BlockHitResult blockHitResult) {
      this.toDiscard = true;
   }

   public void onClientRemoval() {
      if (this.level().isClientSide) {
         ParticleUtil.addSparkParticles(this.position(), this.level());
      }
   }

   protected float getGravity() {
      return 0.0F;
   }

   public enum RangeCategory {
      OPTIMAL((short)0),
      EFFECTIVE((short)1),
      MAXIMUM((short)2),
      OUT((short)3),
      SUPER((short)4);

      private final short category;

      RangeCategory(final short category) {
         this.category = category;
      }

      public short category() {
         return this.category;
      }
   }
}
