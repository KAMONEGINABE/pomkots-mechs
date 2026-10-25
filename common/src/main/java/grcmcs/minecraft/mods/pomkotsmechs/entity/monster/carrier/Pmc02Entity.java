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
      this.f_19811_ = true;
      this.alwaysLookAtTarget = false;
   }

   @Override
   public void m_8119_() {
      this.m_20242_(false);
      super.m_8119_();
      if (this.isServerSide()) {
         if (this.raidControllerEntityUUID != null && this.raidControllerEntity == null) {
            this.raidControllerEntity = (RaidControllerEntity)((ServerLevel)this.m_9236_()).m_8791_(this.raidControllerEntityUUID);
         }

         if (this.lifeTicks++ >= 4000) {
            this.m_146870_();
         } else {
            if (this.isCarrying && this.m_20096_()) {
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
                     Entity ent = ((EntityType)type.get()).m_20615_(this.m_9236_());
                     if (ent != null) {
                        Vec3 pos = this.m_20182_();
                        ent.m_6034_(pos.f_82479_ + this.f_19796_.m_188503_(8) - 4.0, pos.f_82480_ + 5.0, pos.f_82481_ + this.f_19796_.m_188503_(8) - 4.0);
                        if (ent instanceof BaseSmallMonsterEntity mons) {
                           mons.m_21530_();
                           if (this.m_5448_() != null) {
                              mons.m_6710_(this.m_5448_());
                           }

                           mons.setInRaid(this.isInRaid);
                           mons.setInEvent(this.isInEvent());
                        }

                        if (this.raidControllerEntity != null) {
                           this.raidControllerEntity.addSpawnedEntity(ent);
                        }

                        this.m_9236_().m_7967_(ent);
                     }
                  }
               }

               this.spawnTargetMobs.clear();
            } else if (this.ongroundTicks < 0) {
               this.m_146870_();
            }
         }
      }
   }

   public void setSpawnTargetMobs(@NotNull List<String> list) {
      this.spawnTargetMobs = list;
   }

   @Override
   public void m_7380_(CompoundTag compound) {
      super.m_7380_(compound);
      compound.m_128379_(PomkotsMechs.nbtName("IsCarrying"), this.isCarrying);
      ListTag list = new ListTag();

      for (String type : this.spawnTargetMobs) {
         list.add(StringTag.m_129297_(type));
      }

      compound.m_128365_(PomkotsMechs.nbtName("SpawnTargetMobs"), list);
      if (this.raidControllerEntityUUID != null) {
         compound.m_128362_(PomkotsMechs.nbtName("RaidControllerEntityUUID"), this.raidControllerEntityUUID);
      } else {
         compound.m_128473_(PomkotsMechs.nbtName("RaidControllerEntityUUID"));
      }
   }

   @Override
   public void m_7378_(CompoundTag compound) {
      super.m_7378_(compound);
      if (compound.m_128441_(PomkotsMechs.nbtName("IsCarrying"))) {
         this.isCarrying = compound.m_128471_(PomkotsMechs.nbtName("IsCarrying"));
      } else {
         this.isCarrying = false;
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

      if (compound.m_128441_(PomkotsMechs.nbtName("RaidControllerEntityUUID"))) {
         this.raidControllerEntityUUID = compound.m_128342_(PomkotsMechs.nbtName("RaidControllerEntityUUID"));
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
         this.spawnDirtParticlesSquare(this.m_9236_(), (int)(1.0F * this.scale), this.m_20183_());
      } else if ("raise2".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_EARTHBREAK_EVENT.get());
         this.spawnDirtParticlesSquare(this.m_9236_(), (int)(10.0F * this.scale), this.m_20183_());
      } else if ("close".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_BOSSBOXOPEN.get());
      } else if ("se_bossdown".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_BOSSDOWN_EVENT.get());
      }
   }

   public void spawnDirtParticlesSquare(Level level, int ypos, BlockPos centerPos) {
      BlockState dirt = Blocks.f_50493_.m_49966_();
      RandomSource random = level.m_213780_();
      int range = (int)(7.0F * this.scale);

      for (int dx = -range; dx < range; dx++) {
         for (int dz = -range; dz < range; dz++) {
            double px = centerPos.m_123341_() + 0.5 + dx;
            double py = centerPos.m_123342_() + 1.0 + random.m_188500_() * 2.0 + ypos;
            double pz = centerPos.m_123343_() + 0.5 + dz;
            double vx = (random.m_188500_() - 0.5) * 0.1;
            double vy = random.m_188500_() * 0.1;
            double vz = (random.m_188500_() - 0.5) * 0.1;
            this.m_9236_().m_7106_(new BlockParticleOption(ParticleTypes.f_123794_, dirt), px, py, pz, vx, vy, vz);
         }
      }
   }

   @Override
   protected void playSoundEffect(SoundEvent event) {
      this.m_9236_().m_7785_(this.m_20185_(), this.m_20186_(), this.m_20189_(), event, SoundSource.PLAYERS, 1.0F, 1.0F, false);
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }

   public boolean m_6000_(double a, double b, double c) {
      return true;
   }

   @Override
   public boolean m_6783_(double e) {
      return true;
   }

   @Override
   protected void doAttack() {
   }

   @Override
   protected int getMaxAttackCooltime() {
      return 0;
   }

   public boolean m_142535_(float f1, float f2, DamageSource damageSource) {
      return false;
   }
}
