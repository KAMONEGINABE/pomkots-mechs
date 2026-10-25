package grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class MechPilotEntity extends PathfinderMob implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final EntityDataAccessor<String> MODEL_LOCATION = SynchedEntityData.m_135353_(MechPilotEntity.class, EntityDataSerializers.f_135030_);
   private static final EntityDataAccessor<String> TEXTURE_LOCATION = SynchedEntityData.m_135353_(MechPilotEntity.class, EntityDataSerializers.f_135030_);
   private boolean shouldSave = true;
   private float soundEffectVolume = -1.0F;

   public static @NotNull Builder createMobAttributes() {
      return LivingEntity.m_21183_().m_22266_(Attributes.f_22282_).m_22268_(Attributes.f_22277_, 20.0).m_22268_(Attributes.f_22276_, 20.0);
   }

   public MechPilotEntity(EntityType<? extends PathfinderMob> entityType, Level level) {
      super(entityType, level);
      this.m_274367_(2.0F);
   }

   protected void m_8099_() {
      super.m_8099_();
      this.f_21345_.m_25352_(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
      this.f_21345_.m_25352_(4, new RandomLookAroundGoal(this));
   }

   public void m_8119_() {
      if (this.m_20202_() instanceof Pmvc01Entity) {
         this.m_21557_(true);
      } else {
         this.m_21557_(false);
      }

      super.m_8119_();
   }

   public void m_7023_(@NotNull Vec3 pos) {
      super.m_7023_(pos);
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(
               this,
               "basic_move",
               0,
               event -> {
                  if (this.m_20159_()) {
                     return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.mech_pilot.sit"));
                  } else if (event.isMoving()) {
                     return this.m_20184_().m_165925_() > 0.04
                        ? event.setAndContinue(RawAnimation.begin().thenLoop("animation.mech_pilot.run"))
                        : event.setAndContinue(RawAnimation.begin().thenLoop("animation.mech_pilot.walk"));
                  } else {
                     return PlayState.STOP;
                  }
               }
            )
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }

   public void m_7378_(@NotNull CompoundTag compound) {
      super.m_7378_(compound);
      if (compound.m_128441_(PomkotsMechs.nbtName("PilotTexture")) && !compound.m_128461_(PomkotsMechs.nbtName("PilotTexture")).isEmpty()) {
         this.setTextureLocation(compound.m_128461_(PomkotsMechs.nbtName("PilotTexture")));
      } else {
         this.setTextureLocation(PomkotsMechs.id("textures/entity/pilot/mech_pilot_wide_john.png").toString());
      }

      if (compound.m_128441_(PomkotsMechs.nbtName("PilotModel")) && !compound.m_128461_(PomkotsMechs.nbtName("PilotModel")).isEmpty()) {
         this.setModelLocation(compound.m_128461_(PomkotsMechs.nbtName("PilotModel")));
      } else {
         this.setModelLocation(PomkotsMechs.id("geo/mech_pilot_wide.geo.json").toString());
      }
   }

   public void m_7380_(@NotNull CompoundTag compound) {
      super.m_7380_(compound);
      compound.m_128359_(PomkotsMechs.nbtName("PilotTexture"), this.getTextureLocation());
      compound.m_128359_(PomkotsMechs.nbtName("PilotModel"), this.getModelLocation());
   }

   protected void m_8097_() {
      super.m_8097_();
      this.f_19804_.m_135372_(MODEL_LOCATION, PomkotsMechs.id("geo/mech_pilot_wide.geo.json").toString());
      this.f_19804_.m_135372_(TEXTURE_LOCATION, PomkotsMechs.id("textures/entity/pilot/mech_pilot_wide.png").toString());
   }

   public void setModelLocation(String s) {
      this.f_19804_.m_135381_(MODEL_LOCATION, s);
   }

   public String getModelLocation() {
      return (String)this.f_19804_.m_135370_(MODEL_LOCATION);
   }

   public void setTextureLocation(String s) {
      this.f_19804_.m_135381_(TEXTURE_LOCATION, s);
   }

   public String getTextureLocation() {
      return (String)this.f_19804_.m_135370_(TEXTURE_LOCATION);
   }

   public boolean isShouldSave() {
      return this.shouldSave;
   }

   public void setShouldSave(boolean shouldSave) {
      this.shouldSave = shouldSave;
   }

   public boolean m_142391_() {
      return this.shouldSave;
   }

   public boolean m_6469_(DamageSource damageSource, float f) {
      return this.m_20202_() instanceof Pmvc01Entity ? false : super.m_6469_(damageSource, f);
   }

   public boolean m_6129_() {
      return false;
   }

   public int m_7302_(int air) {
      return 0;
   }

   public boolean m_6785_(double distanceToClosestPlayer) {
      return false;
   }

   public boolean m_6094_() {
      return false;
   }

   public boolean m_6783_(double d) {
      return true;
   }

   protected boolean isServerSide() {
      return !this.isClientSide();
   }

   protected boolean isClientSide() {
      return this.m_9236_().m_5776_();
   }

   protected void playSoundEffect(SoundEvent event) {
      this.playSoundEffect(event, 1.0F);
   }

   protected void playSoundEffect(SoundEvent event, float volume) {
      if (this.soundEffectVolume < 0.0F) {
         this.soundEffectVolume = this.computeVolume(100.0);
      }

      if (this.soundEffectVolume > 0.0F) {
         this.m_9236_().m_7785_(this.m_20185_(), this.m_20186_(), this.m_20189_(), event, SoundSource.PLAYERS, volume * this.soundEffectVolume, 1.0F, false);
      }
   }

   private float computeVolume(double maxDistance) {
      Minecraft mc = Minecraft.m_91087_();
      LocalPlayer player = mc.f_91074_;
      if (player == null) {
         return 0.0F;
      }

      double distance = player.m_20270_(this);
      if (distance > maxDistance) {
         return 0.0F;
      }

      float volume = 1.0F - (float)(distance / maxDistance);
      return Mth.m_14036_(volume, 0.0F, 1.0F);
   }
}
