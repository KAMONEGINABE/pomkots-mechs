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
      this.m_20242_(true);
      this.alwaysLookAtTarget = false;
   }

   @Override
   protected void m_8099_() {
      super.m_8099_();
      this.f_21345_.m_25352_(1, new StraightRushGoal(this, 2.0));
      this.f_21346_.m_25352_(1, new NearestEntityTargetGoal<RaidObjectiveEntity>(this, RaidObjectiveEntity.class, false));
      this.f_21346_.m_25352_(2, new NearestEntityTargetGoal(this, Player.class, false));
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
      Vec3 vel = this.m_20184_();
      if (vel.m_82556_() > 1.0E-4) {
         float yaw = (float)Math.toDegrees(Math.atan2(vel.f_82479_, vel.f_82481_));
         this.m_146922_(yaw);
         this.f_19859_ = yaw;
         this.m_5618_(yaw);
         this.f_20884_ = yaw;
         this.m_5616_(yaw);
         this.f_20886_ = yaw;
      }
   }

   @Override
   public void m_8119_() {
      this.m_20242_(true);
      super.m_8119_();
      if (this.isServerSide()) {
         if (this.f_19797_ > 600) {
            if (this.container != null) {
               this.container.m_146870_();
            }

            this.m_146870_();
         }

         if (this.raidControllerEntityUUID != null && this.raidControllerEntity == null) {
            this.raidControllerEntity = (RaidControllerEntity)((ServerLevel)this.m_9236_()).m_8791_(this.raidControllerEntityUUID);
         }

         if (!"".equals(this.containerType) && this.containerUUID == null) {
            RegistrySupplier<EntityType<?>> type = Utils.getEntityType(this.containerType);
            if (type != null) {
               Entity entity = ((EntityType)type.get()).m_20615_(this.m_9236_());
               entity.m_146884_(this.m_20182_());
               this.m_9236_().m_7967_(entity);
               this.containerUUID = entity.m_20148_();
               this.container = entity;
               if (entity instanceof GenericPomkotsMonster pomkots) {
                  pomkots.setInRaid(this.isInRaid);
                  pomkots.setInEvent(this.isInEvent());
                  pomkots.setModifiers(this.healthModifier, 1.0F);
               }

               if (this.raidControllerEntity != null) {
                  this.raidControllerEntity.addSpawnedEntity(entity);
                  if (entity instanceof Pmc02Entity pmc02) {
                     pmc02.raidControllerEntityUUID = this.raidControllerEntity.m_20148_();
                     pmc02.raidControllerEntity = this.raidControllerEntity;
                  }
               }
            }

            this.containerType = "";
         }

         if (this.containerUUID != null && this.container == null) {
            Entity c = ((ServerLevel)this.m_9236_()).m_8791_(this.containerUUID);
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

            Vec3 pos = this.m_20182_();
            this.container.m_6034_(pos.f_82479_, pos.f_82480_ - 10.0, pos.f_82481_);
            float yaw = this.m_146908_();
            this.container.m_146922_(yaw);
            this.container.m_5616_(yaw);
            this.container.f_19859_ = yaw;
            if (this.m_5448_() != null
               && this.container != null
               && !isFartherThanXZ(this, this.m_5448_(), this.dropDistance)
               && isBelowGroundNotWater(this.m_9236_(), this.m_20183_())) {
               if (this.container instanceof Mob mob) {
                  mob.m_6710_(this.m_5448_());
               }

               this.container = null;
               this.containerUUID = null;
               this.countDownForDiscard = 60;
            }
         }

         if (this.countDownForDiscard > 0) {
            this.countDownForDiscard--;
         } else if (this.countDownForDiscard == 0) {
            this.m_146870_();
         }
      }
   }

   public static boolean isFartherThanXZ(Entity a, Entity b, double n) {
      double dx = a.m_20185_() - b.m_20185_();
      double dz = a.m_20189_() - b.m_20189_();
      double distSqr = dx * dx + dz * dz;
      return distSqr >= n * n;
   }

   public static boolean isBelowGroundNotWater(Level level, BlockPos startPos) {
      for (int y = startPos.m_123342_(); y >= level.m_141937_(); y--) {
         BlockPos checkPos = new BlockPos(startPos.m_123341_(), y, startPos.m_123343_());
         BlockState state = level.m_8055_(checkPos);
         if (!state.m_60795_()) {
            return !state.m_60819_().m_205070_(FluidTags.f_13131_);
         }
      }

      return false;
   }

   @Override
   public void m_7380_(CompoundTag compound) {
      super.m_7380_(compound);
      compound.m_128359_(PomkotsMechs.nbtName("ContainerType"), this.containerType);
      if (this.containerUUID != null) {
         compound.m_128362_(PomkotsMechs.nbtName("ContainerUUID"), this.containerUUID);
      } else {
         compound.m_128473_(PomkotsMechs.nbtName("ContainerUUID"));
      }

      if (this.raidControllerEntityUUID != null) {
         compound.m_128362_(PomkotsMechs.nbtName("RaidControllerEntityUUID"), this.raidControllerEntityUUID);
      } else {
         compound.m_128473_(PomkotsMechs.nbtName("RaidControllerEntityUUID"));
      }

      ListTag list = new ListTag();

      for (String type : this.spawnTargetMobs) {
         list.add(StringTag.m_129297_(type));
      }

      compound.m_128365_(PomkotsMechs.nbtName("SpawnTargetMobs"), list);
   }

   @Override
   public void m_7378_(CompoundTag compound) {
      super.m_7378_(compound);
      if (compound.m_128441_(PomkotsMechs.nbtName("ContainerType"))) {
         this.containerType = compound.m_128461_(PomkotsMechs.nbtName("ContainerType"));
      } else {
         this.containerType = "";
      }

      if (compound.m_128441_(PomkotsMechs.nbtName("ContainerUUID"))) {
         this.containerUUID = compound.m_128342_(PomkotsMechs.nbtName("ContainerUUID"));
      } else {
         this.containerUUID = null;
      }

      if (compound.m_128441_(PomkotsMechs.nbtName("RaidControllerEntityUUID"))) {
         this.raidControllerEntityUUID = compound.m_128342_(PomkotsMechs.nbtName("RaidControllerEntityUUID"));
      } else {
         this.raidControllerEntityUUID = null;
      }

      if (compound.m_128441_(PomkotsMechs.nbtName("SpawnTargetMobs"))) {
         this.spawnTargetMobs.clear();
         ListTag list = (ListTag)compound.m_128423_(PomkotsMechs.nbtName("SpawnTargetMobs"));
         if (list != null) {
            for (Tag t : list) {
               this.spawnTargetMobs.add(t.m_7916_());
            }
         }
      }

      if (compound.m_128441_(PomkotsMechs.nbtName("DropDistance"))) {
         this.dropDistance = compound.m_128457_(PomkotsMechs.nbtName("DropDistance"));
      } else {
         this.dropDistance = 100.0F;
      }

      if (compound.m_128441_(PomkotsMechs.nbtName("MonsterHealthModifier"))) {
         this.healthModifier = compound.m_128457_(PomkotsMechs.nbtName("MonsterHealthModifier"));
      } else {
         this.healthModifier = 1.0F;
      }
   }

   @Override
   protected void m_6153_() {
      this.f_20919_++;
      if (this.f_20919_ == 10) {
         if (this.isServerSide() && RemovalReason.KILLED.equals(this.m_146911_())) {
            Level level = this.m_9236_();
            ExplosionEntity e = new ExplosionEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.EXPLOSION.get(), level);
            e.m_146884_(this.m_20182_());
            level.m_7967_(e);
            if (this.container != null) {
               this.container.m_146870_();
            }
         }

         this.m_142687_(RemovalReason.KILLED);
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
