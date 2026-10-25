package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier;

import dev.architectury.registry.registries.RegistrySupplier;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidControllerEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidObjectiveEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonsterPercistant;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.goal.NearestEntityTargetGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.goal.StraightRushGoal;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.ExplosionEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Pmc01Entity extends GenericPomkotsMonsterPercistant implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final int MAX_LIVING_TICK = 600;
   private static final int MAX_LIVING_TICK_AFTER_DROPPED = 60;
   public static final float DEFAULT_SCALE = 1.0F;
   public RaidControllerEntity raidControllerEntity = null;
   public UUID raidControllerEntityUUID = null;
   private int countDownForDiscard = -1;
   private float dropDistance = 100.0F;
   private float healthModifier = 1.0F;
   protected Entity container = null;
   public String containerType = "";
   protected UUID containerUUID = null;
   private List<String> spawnTargetMobs = new ArrayList<>();

   public Pmc01Entity(EntityType<? extends Monster> entityType, Level level) {
      super(entityType, level);
      this.setNoGravity(true);
      this.alwaysLookAtTarget = false;
   }

   @Override
   protected void registerGoals() {
      super.registerGoals();
      this.goalSelector.addGoal(1, new StraightRushGoal(this, 2.0));
      this.targetSelector.addGoal(1, new NearestEntityTargetGoal<RaidObjectiveEntity>(this, RaidObjectiveEntity.class, false));
      this.targetSelector.addGoal(2, new NearestEntityTargetGoal(this, Player.class, false));
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(
               this,
               "basic_move",
               0,
               event -> event.isMoving()
                  ? event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmc01.flying"))
                  : event.setAndContinue(RawAnimation.begin().thenLoop("animation.pmc01.idle"))
            )
         }
      );
   }

   protected void updateRotationBasedOnVelocity() {
      Vec3 vel = this.getDeltaMovement();
      if (vel.lengthSqr() > 1.0E-4) {
         float yaw = (float)Math.toDegrees(Math.atan2(vel.x, vel.z));
         this.setYRot(yaw);
         this.yRotO = yaw;
         this.setYBodyRot(yaw);
         this.yBodyRotO = yaw;
         this.setYHeadRot(yaw);
         this.yHeadRotO = yaw;
      }
   }

   @Override
   public void tick() {
      this.setNoGravity(true);
      super.tick();
      if (this.isServerSide()) {
         if (this.tickCount > 600) {
            if (this.container != null) {
               this.container.discard();
            }

            this.discard();
         }

         if (this.raidControllerEntityUUID != null && this.raidControllerEntity == null) {
            this.raidControllerEntity = (RaidControllerEntity)((ServerLevel)this.level()).getEntity(this.raidControllerEntityUUID);
         }

         if (!"".equals(this.containerType) && this.containerUUID == null) {
            RegistrySupplier<EntityType<?>> type = Utils.getEntityType(this.containerType);
            if (type != null) {
               Entity entity = ((EntityType)type.get()).create(this.level());
               entity.setPos(this.position());
               this.level().addFreshEntity(entity);
               this.containerUUID = entity.getUUID();
               this.container = entity;
               if (entity instanceof GenericPomkotsMonster pomkots) {
                  pomkots.setInRaid(this.isInRaid);
                  pomkots.setInEvent(this.isInEvent());
                  pomkots.setModifiers(this.healthModifier, 1.0F);
               }

               if (this.raidControllerEntity != null) {
                  this.raidControllerEntity.addSpawnedEntity(entity);
                  if (entity instanceof Pmc02Entity pmc02) {
                     pmc02.raidControllerEntityUUID = this.raidControllerEntity.getUUID();
                     pmc02.raidControllerEntity = this.raidControllerEntity;
                  }
               }
            }

            this.containerType = "";
         }

         if (this.containerUUID != null && this.container == null) {
            Entity c = ((ServerLevel)this.level()).getEntity(this.containerUUID);
            if (c != null) {
               this.container = c;
            }
         }

         if (this.container != null) {
            if (this.container instanceof BaseBossEntity boss) {
               boss.isCarrying = true;
            } else if (this.container instanceof Pmc02Entity cont) {
               cont.isCarrying = true;
               cont.setSpawnTargetMobs(this.spawnTargetMobs);
            }

            Vec3 pos = this.position();
            this.container.setPos(pos.x, pos.y - 10.0, pos.z);
            float yaw = this.getYRot();
            this.container.setYRot(yaw);
            this.container.setYHeadRot(yaw);
            this.container.yRotO = yaw;
            if (this.getTarget() != null
               && this.container != null
               && !isFartherThanXZ(this, this.getTarget(), this.dropDistance)
               && isBelowGroundNotWater(this.level(), this.blockPosition())) {
               if (this.container instanceof Mob mob) {
                  mob.setTarget(this.getTarget());
               }

               this.container = null;
               this.containerUUID = null;
               this.countDownForDiscard = 60;
            }
         }

         if (this.countDownForDiscard > 0) {
            this.countDownForDiscard--;
         } else if (this.countDownForDiscard == 0) {
            this.discard();
         }
      }
   }

   public static boolean isFartherThanXZ(Entity a, Entity b, double n) {
      double dx = a.getX() - b.getX();
      double dz = a.getZ() - b.getZ();
      double distSqr = dx * dx + dz * dz;
      return distSqr >= n * n;
   }

   public static boolean isBelowGroundNotWater(Level level, BlockPos startPos) {
      for (int y = startPos.getY(); y >= level.getMinBuildHeight(); y--) {
         BlockPos checkPos = new BlockPos(startPos.getX(), y, startPos.getZ());
         BlockState state = level.getBlockState(checkPos);
         if (!state.isAir()) {
            return !state.getFluidState().is(FluidTags.WATER);
         }
      }

      return false;
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putString(PomkotsMechs.nbtName("ContainerType"), this.containerType);
      if (this.containerUUID != null) {
         compound.putUUID(PomkotsMechs.nbtName("ContainerUUID"), this.containerUUID);
      } else {
         compound.remove(PomkotsMechs.nbtName("ContainerUUID"));
      }

      if (this.raidControllerEntityUUID != null) {
         compound.putUUID(PomkotsMechs.nbtName("RaidControllerEntityUUID"), this.raidControllerEntityUUID);
      } else {
         compound.remove(PomkotsMechs.nbtName("RaidControllerEntityUUID"));
      }

      ListTag list = new ListTag();

      for (String type : this.spawnTargetMobs) {
         list.add(StringTag.valueOf(type));
      }

      compound.put(PomkotsMechs.nbtName("SpawnTargetMobs"), list);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (compound.contains(PomkotsMechs.nbtName("ContainerType"))) {
         this.containerType = compound.getString(PomkotsMechs.nbtName("ContainerType"));
      } else {
         this.containerType = "";
      }

      if (compound.contains(PomkotsMechs.nbtName("ContainerUUID"))) {
         this.containerUUID = compound.getUUID(PomkotsMechs.nbtName("ContainerUUID"));
      } else {
         this.containerUUID = null;
      }

      if (compound.contains(PomkotsMechs.nbtName("RaidControllerEntityUUID"))) {
         this.raidControllerEntityUUID = compound.getUUID(PomkotsMechs.nbtName("RaidControllerEntityUUID"));
      } else {
         this.raidControllerEntityUUID = null;
      }

      if (compound.contains(PomkotsMechs.nbtName("SpawnTargetMobs"))) {
         this.spawnTargetMobs.clear();
         ListTag list = (ListTag)compound.get(PomkotsMechs.nbtName("SpawnTargetMobs"));
         if (list != null) {
            for (Tag t : list) {
               this.spawnTargetMobs.add(t.getAsString());
            }
         }
      }

      if (compound.contains(PomkotsMechs.nbtName("DropDistance"))) {
         this.dropDistance = compound.getFloat(PomkotsMechs.nbtName("DropDistance"));
      } else {
         this.dropDistance = 100.0F;
      }

      if (compound.contains(PomkotsMechs.nbtName("MonsterHealthModifier"))) {
         this.healthModifier = compound.getFloat(PomkotsMechs.nbtName("MonsterHealthModifier"));
      } else {
         this.healthModifier = 1.0F;
      }
   }

   @Override
   protected void tickDeath() {
      this.deathTime++;
      if (this.deathTime == 10) {
         if (this.isServerSide() && RemovalReason.KILLED.equals(this.getRemovalReason())) {
            Level level = this.level();
            ExplosionEntity e = new ExplosionEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.EXPLOSION.get(), level);
            e.setPos(this.position());
            level.addFreshEntity(e);
            if (this.container != null) {
               this.container.discard();
            }
         }

         this.remove(RemovalReason.KILLED);
      }
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }

   @Override
   public String getMechName() {
      return "pmc01";
   }

   @Override
   protected void doAttack() {
   }

   @Override
   protected int getMaxAttackCooltime() {
      return 20;
   }
}
