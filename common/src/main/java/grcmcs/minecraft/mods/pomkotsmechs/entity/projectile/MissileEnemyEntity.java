package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MissileEnemyEntity extends MissileBaseEntity {
   private float damage;
   private float speed;

   public MissileEnemyEntity(EntityType<? extends ThrowableProjectile> entityType, Level world) {
      this(entityType, world, null, 20.0F, 3.0F);
   }

   public MissileEnemyEntity(EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, float damage, float speed) {
      super(entityType, world, shooter, null);
      this.damage = damage;
      this.speed = speed;
   }

   public MissileEnemyEntity(
      EntityType<? extends ThrowableProjectile> entityType, Level world, LivingEntity shooter, LivingEntity target, float damage, float speed
   ) {
      super(entityType, world, shooter, target);
      this.damage = damage;
      this.speed = speed;
   }

   @Override
   protected int getSwitchTick() {
      return 4;
   }

   @Override
   protected double getSpeed() {
      return this.speed;
   }

   @Override
   protected int getSeekRange() {
      return 40;
   }

   @Override
   protected Class getTargetClass() {
      return Player.class;
   }

   @Override
   protected Vec3 getNonTargetVelocity() {
      return this.m_20184_();
   }

   @Override
   protected float getMaxRotationAnglePerTick() {
      return 7.0F;
   }

   @Override
   protected float getDamage() {
      return this.damage;
   }

   @Override
   protected int getMaxLifeTicks() {
      return 120;
   }
}
