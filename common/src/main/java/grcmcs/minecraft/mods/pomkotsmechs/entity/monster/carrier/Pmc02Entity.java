package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier;

import dev.architectury.registry.registries.RegistrySupplier;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.event.RaidControllerEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonsterPercistant;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class Pmc02Entity extends GenericPomkotsMonsterPercistant implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final int MAX_LIFE_TICKS = 4000;
   private int lifeTicks = 0;
   private int ongroundTicks = 80;
   public float scale = 2.0F;
   public boolean isCarrying = false;
   private List<String> spawnTargetMobs = new ArrayList<>();
   public RaidControllerEntity raidControllerEntity = null;
   public UUID raidControllerEntityUUID = null;
   private final AnimationController<Pmc02Entity> trigger = new AnimationController(this, "action_controller", state -> PlayState.STOP)
      .triggerableAnim("appearFromGround", RawAnimation.begin().thenPlayAndHold("animation.bossbox.start"))
      .triggerableAnim("open", RawAnimation.begin().thenPlayAndHold("animation.bossbox.open"))
      .triggerableAnim("onground", RawAnimation.begin().thenPlayAndHold("animation.bossbox.onground"))
      .setSoundKeyframeHandler(this::registerAnimationSoundHandlers);

   @Override
   public String getMechName() {
      return "pmc02";
   }

   public Pmc02Entity(EntityType<? extends Monster> entityType, Level level) {
      super(entityType, level);
      this.noCulling = true;
      this.alwaysLookAtTarget = false;
   }

   @Override
   public void tick() {
      this.setNoGravity(false);
      super.tick();
      if (this.isServerSide()) {
         if (this.raidControllerEntityUUID != null && this.raidControllerEntity == null) {
            this.raidControllerEntity = (RaidControllerEntity)((ServerLevel)this.level()).getEntity(this.raidControllerEntityUUID);
         }

         if (this.lifeTicks++ >= 4000) {
            this.discard();
         } else {
            if (this.isCarrying && this.onGround()) {
               this.isCarrying = false;
               this.triggerAnim("action_controller", "onground");
            }

            if (!this.isCarrying) {
               this.ongroundTicks--;
            }

            if (this.ongroundTicks == 50) {
               this.triggerAnim("action_controller", "open");

               for (String typeString : this.spawnTargetMobs) {
                  RegistrySupplier<EntityType<?>> type = Utils.getEntityType(typeString);
                  if (type != null) {
                     Entity ent = ((EntityType)type.get()).create(this.level());
                     if (ent != null) {
                        Vec3 pos = this.position();
                        ent.setPos(pos.x + this.random.nextInt(8) - 4.0, pos.y + 5.0, pos.z + this.random.nextInt(8) - 4.0);
                        if (ent instanceof BaseSmallMonsterEntity mons) {
                           mons.setPersistenceRequired();
                           if (this.getTarget() != null) {
                              mons.setTarget(this.getTarget());
                           }

                           mons.setInRaid(this.isInRaid);
                           mons.setInEvent(this.isInEvent());
                        }

                        if (this.raidControllerEntity != null) {
                           this.raidControllerEntity.addSpawnedEntity(ent);
                        }

                        this.level().addFreshEntity(ent);
                     }
                  }
               }

               this.spawnTargetMobs.clear();
            } else if (this.ongroundTicks < 0) {
               this.discard();
            }
         }
      }
   }

   public void setSpawnTargetMobs(@NotNull List<String> list) {
      this.spawnTargetMobs = list;
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      super.addAdditionalSaveData(compound);
      compound.putBoolean(PomkotsMechs.nbtName("IsCarrying"), this.isCarrying);
      ListTag list = new ListTag();

      for (String type : this.spawnTargetMobs) {
         list.add(StringTag.valueOf(type));
      }

      compound.put(PomkotsMechs.nbtName("SpawnTargetMobs"), list);
      if (this.raidControllerEntityUUID != null) {
         compound.putUUID(PomkotsMechs.nbtName("RaidControllerEntityUUID"), this.raidControllerEntityUUID);
      } else {
         compound.remove(PomkotsMechs.nbtName("RaidControllerEntityUUID"));
      }
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      super.readAdditionalSaveData(compound);
      if (compound.contains(PomkotsMechs.nbtName("IsCarrying"))) {
         this.isCarrying = compound.getBoolean(PomkotsMechs.nbtName("IsCarrying"));
      } else {
         this.isCarrying = false;
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

      if (compound.contains(PomkotsMechs.nbtName("RaidControllerEntityUUID"))) {
         this.raidControllerEntityUUID = compound.getUUID(PomkotsMechs.nbtName("RaidControllerEntityUUID"));
      } else {
         this.raidControllerEntityUUID = null;
      }
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController[]{this.trigger});
   }

   protected void registerAnimationSoundHandlers(SoundKeyframeEvent event) {
      if ("raise1".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_PILEBUNKER_EVENT.get());
         this.spawnDirtParticlesSquare(this.level(), (int)(1.0F * this.scale), this.blockPosition());
      } else if ("raise2".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_EARTHBREAK_EVENT.get());
         this.spawnDirtParticlesSquare(this.level(), (int)(10.0F * this.scale), this.blockPosition());
      } else if ("close".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_BOSSBOXOPEN.get());
      } else if ("se_bossdown".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_BOSSDOWN_EVENT.get());
      }
   }

   public void spawnDirtParticlesSquare(Level level, int ypos, BlockPos centerPos) {
      BlockState dirt = Blocks.DIRT.defaultBlockState();
      RandomSource random = level.getRandom();
      int range = (int)(7.0F * this.scale);

      for (int dx = -range; dx < range; dx++) {
         for (int dz = -range; dz < range; dz++) {
            double px = centerPos.getX() + 0.5 + dx;
            double py = centerPos.getY() + 1.0 + random.nextDouble() * 2.0 + ypos;
            double pz = centerPos.getZ() + 0.5 + dz;
            double vx = (random.nextDouble() - 0.5) * 0.1;
            double vy = random.nextDouble() * 0.1;
            double vz = (random.nextDouble() - 0.5) * 0.1;
            this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, dirt), px, py, pz, vx, vy, vz);
         }
      }
   }

   @Override
   protected void playSoundEffect(SoundEvent event) {
      this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), event, SoundSource.PLAYERS, 1.0F, 1.0F, false);
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }

   public boolean shouldRender(double a, double b, double c) {
      return true;
   }

   @Override
   public boolean shouldRenderAtSqrDistance(double e) {
      return true;
   }

   @Override
   protected void doAttack() {
   }

   @Override
   protected int getMaxAttackCooltime() {
      return 0;
   }

   public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
      return false;
   }
}
