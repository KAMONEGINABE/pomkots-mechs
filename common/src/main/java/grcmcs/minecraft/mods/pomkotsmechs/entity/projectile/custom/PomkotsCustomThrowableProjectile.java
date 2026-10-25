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
         this.m_5602_(pmvc01.getDrivingPassenger());
      }
   }

   @Override
   public void m_8119_() {
      if (this.toDiscard) {
         this.m_146870_();
      }

      this.m_146867_();
      if (!this.m_9236_().f_46443_) {
         if (this.lifeTicks++ >= this.maxLifeTicks) {
            this.m_146870_();
         } else {
            this.m_20101_();
            if (this.performSubStepMovement()) {
               ;
            }
         }
      }
   }

   protected boolean performSubStepMovement() {
      if (this.f_19803_) {
         this.initialSpeed = this.m_20184_().m_82554_(Vec3.f_82478_);
      }

      Vec3 totalMovement = this.m_20184_();
      Vec3 subStepMovement = totalMovement;

      for (int step = 0; step < 1; step++) {
         Vec3 start = this.m_20182_();
         Vec3 end = start.m_82549_(subStepMovement);
         BlockHitResult blockHit = this.m_9236_().m_45547_(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, this));
         AABB searchBox = new AABB(start, end).m_82400_(0.5);
         EntityHitResult entityHit = this.getEntityHitResult(
            this.m_9236_(),
            this,
            start,
            end,
            searchBox,
            entity -> entity != this.getShooter()
               && entity != this
               && entity.m_6084_()
               && entity.m_6087_()
               && !entity.m_5833_()
               && !(entity instanceof BossHitBoxEntity hb && hb.isRelatedShooter(this))
         );
         HitResult hit = this.selectCloserHit(start, blockHit, entityHit);
         if (hit != null && hit.m_6662_() != Type.MISS) {
            this.setPosInSubStep(hit.m_82450_());
            this.m_6532_(hit);
            return false;
         }

         this.setPosInSubStep(end);
      }

      return true;
   }

   protected void setPosInSubStep(Vec3 pos) {
      this.m_146884_(pos);
   }

   protected EntityHitResult getEntityHitResult(Level level, Entity projectile, Vec3 start, Vec3 end, AABB searchBox, Predicate<Entity> filter) {
      Entity closestEntity = null;
      Vec3 closestHitPos = null;
      double closestDist = Double.MAX_VALUE;

      for (Entity candidate : level.m_6249_(projectile, searchBox, filter)) {
         AABB candidateBox = candidate.m_20191_().m_82400_(candidate.m_6143_());
         Optional<Vec3> hitPos = candidateBox.m_82371_(start, end);
         if (hitPos.isPresent()) {
            double dist = start.m_82557_(hitPos.get());
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
      boolean blockMiss = blockHit.m_6662_() == Type.MISS;
      if (entityHit == null && blockMiss) {
         return null;
      }

      if (entityHit == null) {
         return blockHit;
      }

      if (blockMiss) {
         return entityHit;
      }

      double blockDist = origin.m_82557_(blockHit.m_82450_());
      double entityDist = origin.m_82557_(entityHit.m_82450_());
      return (HitResult)(entityDist <= blockDist ? entityHit : blockHit);
   }

   protected void m_5790_(EntityHitResult entityHitResult) {
      Entity entity = entityHitResult.m_82443_();
      if (!(entity instanceof BossHitBoxEntity hb && hb.isRelatedShooter(this))) {
         float finalDamage = this.calcFinalDamage(this.damage);
         if (entity instanceof BaseBossEntity boss) {
            boss.addStunPoint(this.stun);
         } else if (entity instanceof BossHitBoxEntity hitbox) {
            hitbox.addStunPoint(this.stun);
         }

         ProjectileUtil.hurt(entity, this, this.m_19749_(), finalDamage);
         entity.f_19802_ = 0;
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

   protected void m_8060_(BlockHitResult blockHitResult) {
      this.toDiscard = true;
   }

   public void m_142036_() {
      if (this.m_9236_().f_46443_) {
         ParticleUtil.addSparkParticles(this.m_20182_(), this.m_9236_());
      }
   }

   protected float m_7139_() {
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
