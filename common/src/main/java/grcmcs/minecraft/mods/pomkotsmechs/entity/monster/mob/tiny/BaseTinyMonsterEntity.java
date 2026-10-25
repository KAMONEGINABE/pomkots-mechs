package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.tiny;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;

public abstract class BaseTinyMonsterEntity extends BaseSmallMonsterEntity {
   protected int attackingTime = 0;

   public static Builder createMobAttributes() {
      return LivingEntity.createLivingAttributes()
         .add(Attributes.ATTACK_KNOCKBACK)
         .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
         .add(Attributes.ATTACK_DAMAGE, 1.0)
         .add(Attributes.FOLLOW_RANGE, 100.0);
   }

   public BaseTinyMonsterEntity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
   }

   public boolean isAttacking() {
      return this.attackingTime > 0;
   }

   @Override
   public void tick() {
      super.tick();
      this.tickAttack();
      if (this.isClientSide() && this.isDeadOrDying() && this.deathTime == 10) {
         this.generateDestroyedExplosionEffect();
      }
   }

   protected void choiMove() {
      this.addDeltaMovement(new Vec3(0.0, 0.0, 0.001).yRot((float)Math.toRadians(-1.0 * this.getYRot())));
   }

   protected void tickAttack() {
      if (this.attackingTime > 0) {
         this.attackingTime--;
      }
   }

   @Override
   public boolean tryAttack() {
      if (this.attackCooltime == 0 && !this.isAttacking()) {
         this.startAttack();
         this.attackCooltime = this.getMaxAttackCooltime();
         return true;
      } else {
         return false;
      }
   }

   protected void startAttack() {
      this.rotateToTarget(this.getTarget());
      this.attackingTime = this.getMaxAttackTicks();
   }

   protected abstract int getMaxAttackTicks();

   @Override
   public void doAttack() {
   }

   public static boolean canSpawn(EntityType<Pmss01Entity> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
      return GenericPomkotsMonster.canSpawnCommon(type, world, reason, pos, random);
   }

   @Override
   protected void generateDestroyedExplosionEffect() {
      if (this.isClientSide()) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_EXPLOSION_EVENT.get());
         this.generateLava();
      }
   }

   private void generateLava() {
      if (this.level().isClientSide) {
         RandomSource random = this.level().getRandom();

         for (int i = 0; i < 50; i++) {
            double velocityX = random.nextDouble() * 3.0 - 1.0;
            double velocityY = random.nextDouble() * 3.0 - 1.0;
            double velocityZ = random.nextDouble() * 3.0 - 1.0;
            this.level().addAlwaysVisibleParticle(ParticleTypes.LAVA, true, this.getX(), this.getY(), this.getZ(), velocityX, velocityY, velocityZ);
         }
      }
   }

   protected void playSounds(SoundKeyframeEvent event) {
      if ("se_step".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_EARTHRAISE_EVENT.get(), 0.4F, 32.0);
      } else if ("se_machine".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_MACHINE.get(), 1.0F, 32.0);
      } else if ("se_impact".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_GASHAN.get(), 1.0F, 32.0);
      } else if ("se_shoot".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_RIFLE.get(), 1.0F, 32.0);
      } else if ("se_boot".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_MACHINE.get(), 1.0F, 32.0);
      }
   }
}
