package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.HitBoxEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class MissileEnemyLargeEntity extends MissileBaseEntity {
   public final float defaultScale = 10.0F;

   public MissileEnemyLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
      super(entityType, world);
   }

   public MissileEnemyLargeEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter) {
      super(entityType, world, shooter, null);
   }

   @Override
   protected void onHitEntity(EntityHitResult entityHitResult) {
      Entity entity = entityHitResult.getEntity();
      if (entity instanceof LivingEntity) {
         if (entity.equals(this.shooter) || entity instanceof HitBoxEntity) {
            return;
         }

         entity.hurt(entity.damageSources().thrown(this, (Entity)(this.getOwner() != null ? this.getOwner() : this)), this.getDamage());
         entity.invulnerableTime = 0;
      }

      this.createExplosion(this.position());
      this.discard();
   }

   @Override
   protected int getSwitchTick() {
      return 5;
   }

   @Override
   protected double getSpeed() {
      return 2.0;
   }

   @Override
   protected int getSeekRange() {
      return 100;
   }

   @Override
   protected Class getTargetClass() {
      return Player.class;
   }

   @Override
   protected Vec3 getNonTargetVelocity() {
      return this.getDeltaMovement();
   }

   @Override
   protected float getMaxRotationAnglePerTick() {
      return 5.0F;
   }

   @Override
   protected float getDamage() {
      return 10.0F;
   }

   @Override
   public float getScale() {
      return 10.0F;
   }

   @Override
   protected int getMaxLifeTicks() {
      return 300;
   }

   @Override
   protected void createExplosion(Vec3 pos) {
      Level world = this.level();
      if (!world.isClientSide) {
         ExplosionInteraction exp = ProjectileUtil.isDestructionAllowed(this) ? ExplosionInteraction.BLOCK : ExplosionInteraction.NONE;
         Utils.explode(this, pos.x, pos.y, pos.z, 8.0F, false, exp, this.level());
         ExplosionEntity e = new ExplosionEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.EXPLOSION.get(), world);
         e.setPos(this.position());
         world.addFreshEntity(e);
      }
   }
}
