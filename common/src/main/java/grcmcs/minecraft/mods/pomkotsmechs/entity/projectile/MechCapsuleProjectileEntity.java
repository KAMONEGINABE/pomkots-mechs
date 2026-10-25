package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.items.MechCapsuleItem;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MechCapsuleProjectileEntity extends ThrowableItemProjectile {
   public MechCapsuleProjectileEntity(EntityType<? extends MechCapsuleProjectileEntity> type, Level level) {
      super(type, level);
   }

   public MechCapsuleProjectileEntity(Level level, LivingEntity owner) {
      super((EntityType)PomkotsMechs.MECH_CAPSULE_PROJECTILE.get(), owner, level);
   }

   protected Item m_7881_() {
      return (Item)PomkotsMechs.MECH_CAPSULE_ITEM.get();
   }

   protected void m_6532_(HitResult result) {
      super.m_6532_(result);
      if (!this.m_9236_().f_46443_) {
         LivingEntity ent = MechCapsuleItem.generateEntity(this.m_9236_(), this.m_7846_());
         if (ent != null) {
            Vec3 safePos = resolveSpawnCollision(this.m_9236_(), this.m_20182_(), ent.m_20191_(), this.m_20184_().m_82541_().m_82548_());
            ent.m_20219_(safePos);
            this.m_9236_().m_7967_(ent);
         }

         this.m_146870_();
      } else {
         playBasicExplosion(this.m_9236_(), this.m_20182_());
      }
   }

   public static Vec3 resolveSpawnCollision(Level level, Vec3 desiredPos, AABB baseAabb, Vec3 incomingDir) {
      AABB aabb = baseAabb.m_82383_(desiredPos.m_82546_(baseAabb.m_82399_()));
      Iterable<VoxelShape> collisions = level.m_186434_(null, aabb);
      double dx = incomingDir.f_82479_;
      double dy = incomingDir.f_82480_;
      double dz = incomingDir.f_82481_;
      Direction hitDir = Direction.m_122366_(incomingDir.f_82479_, incomingDir.f_82480_, incomingDir.f_82481_);
      switch (hitDir.m_122434_()) {
         case X:
            dx = Shapes.m_193135_(Axis.X, aabb, collisions, dx);
            break;
         case Z:
            dz = Shapes.m_193135_(Axis.Z, aabb, collisions, dz);
            break;
         case Y:
            dy = Shapes.m_193135_(Axis.Y, aabb, collisions, dy);
      }

      return desiredPos.m_82520_(dx, dy, dz);
   }

   public void m_142036_() {
      if (this.m_9236_().f_46443_) {
         playBasicExplosion(this.m_9236_(), this.m_20182_());
      }
   }

   public static void playBasicExplosion(Level level, Vec3 pos) {
      for (int i = 0; i < 50; i++) {
         double offsetX = (Math.random() - 0.5) * 2.0;
         double offsetY = Math.random() * 1.5;
         double offsetZ = (Math.random() - 0.5) * 2.0;
         double velocityX = offsetX * 0.1;
         double velocityY = 0.1 + Math.random() * 0.1;
         double velocityZ = offsetZ * 0.1;
         level.m_7106_(ParticleTypes.f_123759_, pos.f_82479_ + offsetX, pos.f_82480_ + offsetY, pos.f_82481_ + offsetZ, velocityX, velocityY, velocityZ);
      }

      for (int i = 0; i < 20; i++) {
         double angle = Math.random() * Math.PI * 2.0;
         double distance = Math.random() * 5.0;
         double offsetX = Math.cos(angle) * distance;
         double offsetZ = Math.sin(angle) * distance;
         double offsetY = Math.random() * distance;
         level.m_7106_(ParticleTypes.f_123813_, pos.f_82479_ + offsetX, pos.f_82480_ + offsetY, pos.f_82481_ + offsetZ, 0.0, 0.0, 0.0);
      }

      level.m_7785_(pos.f_82479_, pos.f_82480_, pos.f_82481_, SoundEvents.f_11913_, SoundSource.BLOCKS, 0.5F, 1.2F, false);
   }
}
