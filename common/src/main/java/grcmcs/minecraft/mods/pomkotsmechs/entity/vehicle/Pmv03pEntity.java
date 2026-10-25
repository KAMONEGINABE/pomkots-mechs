package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.config.BattleBalance;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BulletEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.MissileEnemyEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.Action;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.ActionController;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;

public class Pmv03pEntity extends PomkotsVehicleBase {
   public static final float DEFAULT_SCALE = 1.0F;
   protected static final int ACT_GATLING = 2;
   protected static final int ACT_MISSILE = 3;
   protected static final int ACT_HUMMER = 4;
   protected static final int ACT_ROLLER = 5;
   protected static final int ACT_BLOCKINJECT = 6;
   protected static final int ACT_VACUME = 7;
   protected static final int ACT_PLACE = 8;
   protected static final int ACT_LIFT_BLOCK = 9;
   protected static final int ACT_THROW = 10;
   private Vec3 seatPos1 = Vec3.f_82478_;
   private Vec3 seatPos2 = Vec3.f_82478_;

   @Override
   protected String getMechName() {
      return "pmv02";
   }

   public Pmv03pEntity(EntityType<? extends LivingEntity> entityType, Level world) {
      super(entityType, world);
   }

   @Override
   protected void registerActions() {
      super.registerActions();
      this.actionController.registerAction(2, new Action(0, 7, 2), ActionController.ActionType.R_ARM_MAIN);
      this.actionController.registerAction(3, new Action(60, 2, 18), ActionController.ActionType.L_ARM_MAIN);
      this.actionController.registerAction(4, new Action(20, 40, 20), ActionController.ActionType.R_SHL_MAIN);
      this.actionController.registerAction(5, new Action(0, 20, 2), ActionController.ActionType.L_SHL_MAIN);
      this.actionController.registerAction(6, new Action(0, 5, 5), ActionController.ActionType.R_ARM_SUB);
      this.actionController.registerAction(7, new Action(0, 5, 5), ActionController.ActionType.L_ARM_SUB);
      this.actionController.registerAction(9, new Action(20, 8, 2), ActionController.ActionType.R_SHL_SUB);
      this.actionController.registerAction(10, new Action(20, 3, 7), ActionController.ActionType.R_SHL_SUB);
      this.actionController.registerAction(8, new Action(0, 20, 2), ActionController.ActionType.L_SHL_SUB);
   }

   @Override
   protected boolean useEnergy(int dec) {
      return true;
   }

   @Override
   public void m_8119_() {
      if (this.isMainMode()) {
         super.m_8119_();
         if (this.m_20160_()) {
            this.m_20242_(true);
            this.moveForward();
         } else {
            this.m_20242_(false);
         }

         if (this.m_20069_()) {
            this.applyBuoyancy();
         }
      } else {
         super.m_8119_();
      }
   }

   private void applyBuoyancy() {
      double waterSurfaceY = this.m_204036_(FluidTags.f_13131_);
      double targetY = waterSurfaceY + 0.5;
      double currentY = this.m_20186_();
      double deltaY = targetY - currentY;
      Vec3 motion = this.m_20184_();
      this.m_20334_(motion.f_82479_, Mth.m_14008_(deltaY * 0.1, 0.0, 0.1), motion.f_82481_);
      this.m_20256_(this.m_20184_().m_82490_(0.9));
   }

   private void moveForward() {
      double xRotRadians = Math.toRadians(this.m_146909_());
      double yRotRadians = Math.toRadians(this.m_146908_());
      double speed = this.m_6113_();
      double vx = -speed * Math.cos(xRotRadians) * Math.sin(yRotRadians);
      double vy = -speed * Math.sin(xRotRadians);
      double vz = speed * Math.cos(xRotRadians) * Math.cos(yRotRadians);
      this.m_20334_(vx, vy, vz);
      this.m_6478_(MoverType.SELF, this.m_20184_());
   }

   @Override
   protected void applyPlayerInput(DriverInput driverInput) {
      if (this.isMainMode()) {
         this.getUserIntentionForDirectionFromKey(driverInput);
         this.applyPlayerInputWeapons(driverInput);
         if (driverInput.isWeaponRightShoulderPressed()) {
            this.m_7910_(this.m_6113_() - 0.01F);
         }

         if (driverInput.isWeaponLeftShoulderPressed()) {
            this.m_7910_(this.m_6113_() + 0.01F);
         }

         if (this.m_6113_() <= 0.0F) {
            this.m_7910_(0.1F);
         } else if (this.m_6113_() > 2.0F) {
            this.m_7910_(2.0F);
         }

         if (driverInput.isForwardPressed()) {
            this.m_146926_(this.m_146909_() - 2.0F);
         }

         if (driverInput.isBackPressed()) {
            this.m_146926_(this.m_146909_() + 2.0F);
         }

         if (driverInput.isLeftPressed()) {
            this.m_146922_(this.m_146908_() - 2.0F);
         }

         if (driverInput.isRightPressed()) {
            this.m_146922_(this.m_146908_() + 2.0F);
         }

         this.m_146926_(Mth.m_14177_(this.m_146909_()));
         this.f_19812_ = true;
      } else {
         this.getUserIntentionForDirectionFromKey(driverInput);
         this.applyPlayerInputWeapons(driverInput);
         this.applyPlayerInputBoost(driverInput);
         this.applyPlayerInputEvasion(driverInput);
         this.applyPlayerInputJump(driverInput);
         this.applyPlayerInputInAirActions(driverInput);
      }
   }

   @Override
   protected void applyPlayerInputWeapons(DriverInput driverInput) {
      if (driverInput.isWeaponRightHandPressed()) {
         this.actionController.getAction(2).startAction();
      } else if (driverInput.isWeaponLeftHandPressed()) {
         this.actionController.getAction(3).startAction();
      }
   }

   @Override
   protected void fireWeapons() {
      Level level = this.m_9236_();
      if (this.actionController.getAction(2).isOnFire()) {
         this.fireGatling(level);
      } else if (this.actionController.getAction(3).isOnFire()) {
         this.fireMissile(level);
      }
   }

   public void fireGatling(Level world) {
      if (!world.m_5776_()) {
         for (int i = 0; i < 2; i++) {
            BulletEntity be = new BulletEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET.get(), world, this);
            Vec3 offset = this.posHistory.getFirst();
            Vec3 muzzlPos = new Vec3(0.5 - i, 3.3F, 6.5);
            muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
            be.m_146884_(offset.m_82549_(muzzlPos));
            be.m_37251_(be, this.m_146909_(), this.m_146908_(), this.m_21256_(), 2.5F, 2.0F);
            world.m_7967_(be);
         }
      } else if (this.f_19797_ % 7 == 0) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_GATLING_EVENT.get());
      }
   }

   private void fireMissile(Level world) {
      if (!world.m_5776_()) {
         Vec3 offset = this.posHistory.getFirst();
         Vec3 muzzlPos = new Vec3(-1.0, 3.5, 0.8F);
         Vec3 worldMuzzlPos = muzzlPos.m_82520_(0.0, 0.0, 3.0).m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
         MissileEnemyEntity be = new MissileEnemyEntity(
            (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_HORIZONTAL.get(),
            world,
            this,
            BattleBalance.MECH_MISSILE_DAMAGE,
            BattleBalance.MECH_MISSILE_GENERIC_SPEED
         );
         be.m_146884_(offset.m_82549_(worldMuzzlPos));
         be.m_37251_(be, this.m_146909_(), this.m_146908_(), this.m_21256_(), 2.5F, 0.0F);
         world.m_7967_(be);
      } else {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_MISSILE_EVENT.get());
         this.lockTargets.clearLockTargetsMulti();
      }
   }

   @Override
   public void m_7023_(Vec3 pos) {
      if (this.isMainMode()) {
         this.travel2(pos);
      } else {
         super.m_7023_(pos);
      }
   }

   @Override
   protected PlayState controllAnimationWeapons(AnimationState<PomkotsVehicleBase> event) {
      return null;
   }

   @Override
   protected PlayState controllAnimationBasicMove(AnimationState<PomkotsVehicleBase> event) {
      return this.isMainMode() ? null : super.controllAnimationBasicMove(event);
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController[]{new AnimationController(this, "rotation", 6, event -> {
         if (this.getDriverInput() != null) {
            if (this.getDriverInput().isRightPressed()) {
               return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".rightfly"));
            }

            if (this.getDriverInput().isLeftPressed()) {
               return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".leftfly"));
            }
         }

         return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".centerfly"));
      })});
      this.addExtraAnimationController(controllers);
   }

   @Override
   protected void addExtraAnimationController(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(
                  this,
                  "props",
                  6,
                  event -> this.m_20160_()
                     ? event.setAndContinue(RawAnimation.begin().thenLoop("animation." + this.getMechName() + ".prop"))
                     : event.setAndContinue(RawAnimation.begin().thenLoop("animation." + this.getMechName() + ".idle"))
               )
               .setSoundKeyframeHandler(soundKeyframeEvent -> this.registerAnimationSoundHandlers(soundKeyframeEvent))
         }
      );
      controllers.add(
         new AnimationController[]{
            new AnimationController(
                  this,
                  "tires",
                  6,
                  event -> event.isMoving() && ((Pmv03pEntity)event.getAnimatable()).m_20096_()
                     ? event.setAndContinue(RawAnimation.begin().thenLoop("animation." + this.getMechName() + ".tire"))
                     : event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".nop"))
               )
               .setSoundKeyframeHandler(soundKeyframeEvent -> this.registerAnimationSoundHandlers(soundKeyframeEvent))
         }
      );
   }

   @Override
   protected void registerAnimationSoundHandlers(SoundKeyframeEvent event) {
      if ("se_jump".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_JUMP_EVENT.get());
      } else if ("se_booster".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_BOOSTER_EVENT.get());
      } else if ("se_onground".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_JUMP_EVENT.get());
      } else if ("se_drill1".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_DRILL1.get());
      } else if ("se_drill2".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_DRILL2.get());
      } else if ("se_hummer1".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_HUMMER1.get());
      } else if ("se_hummer2".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_HUMMER2.get());
      } else if ("se_lift".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_LIFT.get());
      } else if ("se_needle".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_NEEDLE.get());
      } else if ("se_roller1".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_ROLLER1.get());
      } else if ("se_roller2".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_ROLLER2.get());
      } else if ("se_step".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_STEP.get());
      } else if ("se_throw".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_THROW.get());
      } else if ("se_water".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_WATER.get());
      }
   }

   @Override
   public boolean shouldRenderDefaultHud(String hudName) {
      return "renderHotbar".equals(hudName);
   }

   public void travel2(Vec3 vec3) {
      if (this.m_6109_()) {
         double d = 0.08;
         boolean bl = this.m_20184_().f_82480_ <= 0.0;
         if (bl && this.m_21023_(MobEffects.f_19591_)) {
            d = 0.01;
         }

         FluidState fluidState = this.m_9236_().m_6425_(this.m_20183_());
         if (this.m_20069_() && this.m_6129_() && !this.m_203441_(fluidState)) {
            double e = this.m_20186_();
            float f = this.m_20142_() ? 0.9F : this.m_6108_();
            float g = 0.02F;
            float h = EnchantmentHelper.m_44922_(this);
            if (h > 3.0F) {
               h = 3.0F;
            }

            if (!this.m_20096_()) {
               h *= 0.5F;
            }

            if (h > 0.0F) {
               f += (0.54600006F - f) * h / 3.0F;
               g += (this.m_6113_() - g) * h / 3.0F;
            }

            if (this.m_21023_(MobEffects.f_19593_)) {
               f = 0.96F;
            }

            this.m_19920_(g, vec3);
            this.m_6478_(MoverType.SELF, this.m_20184_());
            Vec3 vec32 = this.m_20184_();
            if (this.f_19862_ && this.m_6147_()) {
               vec32 = new Vec3(vec32.f_82479_, 0.2, vec32.f_82481_);
            }

            this.m_20256_(vec32.m_82542_(f, 0.8F, f));
            Vec3 vec33 = this.m_20994_(d, bl, this.m_20184_());
            this.m_20256_(vec33);
            if (this.f_19862_ && this.m_20229_(vec33.f_82479_, vec33.f_82480_ + 0.6F - this.m_20186_() + e, vec33.f_82481_)) {
               this.m_20334_(vec33.f_82479_, 0.3F, vec33.f_82481_);
            }
         } else if (this.m_20077_() && this.m_6129_() && !this.m_203441_(fluidState)) {
            double e = this.m_20186_();
            this.m_19920_(0.02F, vec3);
            this.m_6478_(MoverType.SELF, this.m_20184_());
            if (this.m_204036_(FluidTags.f_13132_) <= this.m_20204_()) {
               this.m_20256_(this.m_20184_().m_82542_(0.5, 0.8F, 0.5));
               Vec3 vec34 = this.m_20994_(d, bl, this.m_20184_());
               this.m_20256_(vec34);
            } else {
               this.m_20256_(this.m_20184_().m_82490_(0.5));
            }

            if (!this.m_20068_()) {
               this.m_20256_(this.m_20184_().m_82520_(0.0, -d / 4.0, 0.0));
            }

            Vec3 vec34 = this.m_20184_();
            if (this.f_19862_ && this.m_20229_(vec34.f_82479_, vec34.f_82480_ + 0.6F - this.m_20186_() + e, vec34.f_82481_)) {
               this.m_20334_(vec34.f_82479_, 0.3F, vec34.f_82481_);
            }
         } else if (this.m_21255_()) {
            this.m_245125_();
            Vec3 vec35 = this.m_20184_();
            Vec3 vec36 = this.m_20154_();
            float f = this.m_146909_() * (float) (Math.PI / 180.0);
            double i = Math.sqrt(vec36.f_82479_ * vec36.f_82479_ + vec36.f_82481_ * vec36.f_82481_);
            double j = vec35.m_165924_();
            double k = vec36.m_82553_();
            double l = Math.cos(f);
            l = l * l * Math.min(1.0, k / 0.4);
            vec35 = this.m_20184_().m_82520_(0.0, d * (-1.0 + l * 0.75), 0.0);
            if (vec35.f_82480_ < 0.0 && i > 0.0) {
               double m = vec35.f_82480_ * -0.1 * l;
               vec35 = vec35.m_82520_(vec36.f_82479_ * m / i, m, vec36.f_82481_ * m / i);
            }

            if (f < 0.0F && i > 0.0) {
               double m = j * -Mth.m_14031_(f) * 0.04;
               vec35 = vec35.m_82520_(-vec36.f_82479_ * m / i, m * 3.2, -vec36.f_82481_ * m / i);
            }

            if (i > 0.0) {
               vec35 = vec35.m_82520_((vec36.f_82479_ / i * j - vec35.f_82479_) * 0.1, 0.0, (vec36.f_82481_ / i * j - vec35.f_82481_) * 0.1);
            }

            this.m_20256_(vec35.m_82542_(0.99F, 0.98F, 0.99F));
            this.m_6478_(MoverType.SELF, this.m_20184_());
            if (this.f_19862_ && !this.m_9236_().f_46443_) {
               double m = this.m_20184_().m_165924_();
               double n = j - m;
               float o = (float)(n * 10.0 - 3.0);
               if (o > 0.0F) {
                  this.m_6469_(this.m_269291_().m_269515_(), o);
               }
            }

            if (this.m_20096_() && !this.m_9236_().f_46443_) {
               this.m_20115_(7, false);
            }
         } else {
            BlockPos blockPos = this.m_20099_();
            float p = this.m_9236_().m_8055_(blockPos).m_60734_().m_49958_();
            float f = this.m_20096_() ? p * 0.91F : 0.91F;
            Vec3 vec37 = this.m_21074_(vec3, p);
            double q = vec37.f_82480_;
            if (this.m_21023_(MobEffects.f_19620_)) {
               q += (0.05 * (this.m_21124_(MobEffects.f_19620_).m_19564_() + 1) - vec37.f_82480_) * 0.2;
            } else if (this.m_9236_().f_46443_ && !this.m_9236_().m_46805_(blockPos)) {
               if (this.m_20186_() > this.m_9236_().m_141937_()) {
                  q = -0.1;
               } else {
                  q = 0.0;
               }
            } else if (!this.m_20068_()) {
               q -= d;
            }

            if (this.m_147223_()) {
               this.m_20334_(vec37.f_82479_, q, vec37.f_82481_);
            } else {
               this.m_20334_(vec37.f_82479_ * f, q * 0.98F, vec37.f_82481_ * f);
            }
         }
      }

      this.m_267651_(this instanceof FlyingAnimal);
   }

   public void setClientSeatPos1(Vec3 seatPos) {
      this.seatPos1 = seatPos;
   }

   public void setClientSeatPos2(Vec3 seatPos) {
      this.seatPos2 = seatPos;
   }

   @Override
   public Vec3 getClientSeatPos(Entity passenger) {
      List<Entity> ps = this.m_20197_();
      int num = 1;

      for (Entity p : ps) {
         if (p == passenger) {
            if (num == 1) {
               return this.seatPos1;
            }

            if (num == 2) {
               return this.seatPos2;
            }
         }

         num++;
      }

      return Vec3.f_82478_;
   }

   @Override
   public InteractionResult m_6096_(Player player, InteractionHand hand) {
      if (this.isServerSide()) {
         player.m_146922_(this.m_146908_());
         player.m_146926_(this.m_146909_());
         player.m_20329_(this);
      }

      this.rideCoolTick = 3;
      this.actionController.reset();
      return InteractionResult.m_19078_(this.m_9236_().f_46443_);
   }

   @Override
   protected boolean m_7310_(Entity passenger) {
      return this.m_20197_().size() < 2;
   }
}
