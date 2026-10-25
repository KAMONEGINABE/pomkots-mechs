package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonsterPercistant;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.goal.NearestEntityTargetGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;

public abstract class BaseTurretEntity extends GenericPomkotsMonsterPercistant implements GeoEntity, GeoAnimatable {
   protected BaseTurretEntity.TargetEntityType targetEntityType = BaseTurretEntity.TargetEntityType.PLAYER;

   public void setTargetEntityType(BaseTurretEntity.TargetEntityType type) {
      this.targetEntityType = type;
   }

   public BaseTurretEntity(EntityType<? extends GenericPomkotsMonster> entityType, Level world) {
      super(entityType, world);
      this.setMaxUpStep(this.getMechData().maxStepUp);
      this.setSpeed(this.getMechData().speed);
      this.setPersistenceRequired();
      this.setNoGravity(false);
      this.setYRot(0.0F);
   }

   @Override
   public void tick() {
      if (this.firstTick) {
         this.registerTargetSelectorGoals();
         if (this.targetEntityType == BaseTurretEntity.TargetEntityType.POMKOTS_MONSTERS) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(this.mechData.health * 3.0F);
            this.setHealth(this.mechData.health * 3.0F);
         }
      }

      super.tick();
   }

   protected void registerTargetSelectorGoals() {
      if (this.targetEntityType == BaseTurretEntity.TargetEntityType.POMKOTS_MONSTERS) {
         this.targetSelector
            .addGoal(1, new NearestEntityTargetGoal(this, GenericPomkotsMonster.class, false, livingEntity -> !(livingEntity instanceof BaseTurretEntity)));
      } else {
         this.targetSelector.addGoal(1, new NearestEntityTargetGoal(this, Player.class, false, false));
      }
   }

   public abstract void startActionAnimation();

   public abstract void stopActionAnimation();

   public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
      return false;
   }

   @Override
   public void travel(Vec3 pos) {
   }

   @Override
   public int getMaxAttackCooltime() {
      return 10;
   }

   public static boolean canSpawn(EntityType<BaseTurretEntity> type, ServerLevelAccessor world, MobSpawnType reason, BlockPos pos, RandomSource random) {
      return GenericPomkotsMonster.canSpawnCommon(type, world, reason, pos, random);
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      if (!player.level().isClientSide && player.isCreative() && player.isShiftKeyDown()) {
         this.rotateToTarget(player);
         return InteractionResult.SUCCESS;
      } else {
         return super.mobInteract(player, hand);
      }
   }

   protected void registerAnimationSoundHandlers(SoundKeyframeEvent event) {
      if ("small_canon".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_SHOTGUN.get());
      } else if ("missile".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_MISSILE_EVENT.get());
      } else if ("missile_large".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_MISSILE_EVENT.get());
      } else if ("large_canon".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_GRENADE_EVENT.get());
      }
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (compound.contains(PomkotsMechs.nbtName("TargetEntityType"))) {
         this.targetEntityType = BaseTurretEntity.TargetEntityType.of(compound.getInt(PomkotsMechs.nbtName("TargetEntityType")));
      }
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putInt(PomkotsMechs.nbtName("TargetEntityType"), this.targetEntityType.getNum());
   }

   public enum TargetEntityType {
      PLAYER(0),
      POMKOTS_MONSTERS(1),
      HOSTILE_MOBS(2);

      private final int num;

      TargetEntityType(int num) {
         this.num = num;
      }

      public static BaseTurretEntity.TargetEntityType of(int num) {
         if (num < 0) {
            return PLAYER;
         }

         for (BaseTurretEntity.TargetEntityType type : values()) {
            if (type.num == num) {
               return type;
            }
         }

         return PLAYER;
      }

      public int getNum() {
         return this.num;
      }
   }
}
