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
   private Vec3 seatPos1 = Vec3.ZERO;
   private Vec3 seatPos2 = Vec3.ZERO;

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
   public void tick() {
      if (this.isMainMode()) {
         super.tick();
         if (this.isVehicle()) {
            this.setNoGravity(true);
            this.moveForward();
         } else {
            this.setNoGravity(false);
         }

         if (this.isInWater()) {
            this.applyBuoyancy();
         }
      } else {
         super.tick();
      }
   }

   private void applyBuoyancy() {
      double waterSurfaceY = this.getFluidHeight(FluidTags.WATER);
      double targetY = waterSurfaceY + 0.5;
      double currentY = this.getY();
      double deltaY = targetY - currentY;
      Vec3 motion = this.getDeltaMovement();
      this.setDeltaMovement(motion.x, Mth.clamp(deltaY * 0.1, 0.0, 0.1), motion.z);
      this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
   }

   private void moveForward() {
      double xRotRadians = Math.toRadians(this.getXRot());
      double yRotRadians = Math.toRadians(this.getYRot());
      double speed = this.getSpeed();
      double vx = -speed * Math.cos(xRotRadians) * Math.sin(yRotRadians);
      double vy = -speed * Math.sin(xRotRadians);
      double vz = speed * Math.cos(xRotRadians) * Math.cos(yRotRadians);
      this.setDeltaMovement(vx, vy, vz);
      this.move(MoverType.SELF, this.getDeltaMovement());
   }

   @Override
   protected void applyPlayerInput(DriverInput driverInput) {
      if (this.isMainMode()) {
         this.getUserIntentionForDirectionFromKey(driverInput);
         this.applyPlayerInputWeapons(driverInput);
         if (driverInput.isWeaponRightShoulderPressed()) {
            this.setSpeed(this.getSpeed() - 0.01F);
         }

         if (driverInput.isWeaponLeftShoulderPressed()) {
            this.setSpeed(this.getSpeed() + 0.01F);
         }

         if (this.getSpeed() <= 0.0F) {
            this.setSpeed(0.1F);
         } else if (this.getSpeed() > 2.0F) {
            this.setSpeed(2.0F);
         }

         if (driverInput.isForwardPressed()) {
            this.setXRot(this.getXRot() - 2.0F);
         }

         if (driverInput.isBackPressed()) {
            this.setXRot(this.getXRot() + 2.0F);
         }

         if (driverInput.isLeftPressed()) {
            this.setYRot(this.getYRot() - 2.0F);
         }

         if (driverInput.isRightPressed()) {
            this.setYRot(this.getYRot() + 2.0F);
         }

         this.setXRot(Mth.wrapDegrees(this.getXRot()));
         this.hasImpulse = true;
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
      Level level = this.level();
      if (this.actionController.getAction(2).isOnFire()) {
         this.fireGatling(level);
      } else if (this.actionController.getAction(3).isOnFire()) {
         this.fireMissile(level);
      }
   }

   public void fireGatling(Level world) {
      if (!world.isClientSide()) {
         for (int i = 0; i < 2; i++) {
            BulletEntity be = new BulletEntity((EntityType<? extends ThrowableProjectile>)PomkotsMechs.BULLET.get(), world, this);
            Vec3 offset = this.posHistory.getFirst();
            Vec3 muzzlPos = new Vec3(0.5 - i, 3.3F, 6.5);
            muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
            be.setPos(offset.add(muzzlPos));
            be.shootFromRotation(be, this.getXRot(), this.getYRot(), this.getFallFlyingTicks(), 2.5F, 2.0F);
            world.addFreshEntity(be);
         }
      } else if (this.tickCount % 7 == 0) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_GATLING_EVENT.get());
      }
   }

   private void fireMissile(Level world) {
      if (!world.isClientSide()) {
         Vec3 offset = this.posHistory.getFirst();
         Vec3 muzzlPos = new Vec3(-1.0, 3.5, 0.8F);
         Vec3 worldMuzzlPos = muzzlPos.add(0.0, 0.0, 3.0).yRot((float)Math.toRadians(-1.0 * this.getYRot()));
         MissileEnemyEntity be = new MissileEnemyEntity(
            (EntityType<? extends ThrowableProjectile>)PomkotsMechs.MISSILE_HORIZONTAL.get(),
            world,
            this,
            BattleBalance.MECH_MISSILE_DAMAGE,
            BattleBalance.MECH_MISSILE_GENERIC_SPEED
         );
         be.setPos(offset.add(worldMuzzlPos));
         be.shootFromRotation(be, this.getXRot(), this.getYRot(), this.getFallFlyingTicks(), 2.5F, 0.0F);
         world.addFreshEntity(be);
      } else {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_MISSILE_EVENT.get());
         this.lockTargets.clearLockTargetsMulti();
      }
   }

   @Override
   public void travel(Vec3 pos) {
      if (this.isMainMode()) {
         this.travel2(pos);
      } else {
         super.travel(pos);
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
                  event -> this.isVehicle()
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
                  event -> event.isMoving() && ((Pmv03pEntity)event.getAnimatable()).onGround()
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
      if (this.isControlledByLocalInstance()) {
         double d = 0.08;
         boolean bl = this.getDeltaMovement().y <= 0.0;
         if (bl && this.hasEffect(MobEffects.SLOW_FALLING)) {
            d = 0.01;
         }

         FluidState fluidState = this.level().getFluidState(this.blockPosition());
         if (this.isInWater() && this.isAffectedByFluids() && !this.canStandOnFluid(fluidState)) {
            double e = this.getY();
            float f = this.isSprinting() ? 0.9F : this.getWaterSlowDown();
            float g = 0.02F;
            float h = EnchantmentHelper.getDepthStrider(this);
            if (h > 3.0F) {
               h = 3.0F;
            }

            if (!this.onGround()) {
               h *= 0.5F;
            }

            if (h > 0.0F) {
               f += (0.54600006F - f) * h / 3.0F;
               g += (this.getSpeed() - g) * h / 3.0F;
            }

            if (this.hasEffect(MobEffects.DOLPHINS_GRACE)) {
               f = 0.96F;
            }

            this.moveRelative(g, vec3);
            this.move(MoverType.SELF, this.getDeltaMovement());
            Vec3 vec32 = this.getDeltaMovement();
            if (this.horizontalCollision && this.onClimbable()) {
               vec32 = new Vec3(vec32.x, 0.2, vec32.z);
            }

            this.setDeltaMovement(vec32.multiply(f, 0.8F, f));
            Vec3 vec33 = this.getFluidFallingAdjustedMovement(d, bl, this.getDeltaMovement());
            this.setDeltaMovement(vec33);
            if (this.horizontalCollision && this.isFree(vec33.x, vec33.y + 0.6F - this.getY() + e, vec33.z)) {
               this.setDeltaMovement(vec33.x, 0.3F, vec33.z);
            }
         } else if (this.isInLava() && this.isAffectedByFluids() && !this.canStandOnFluid(fluidState)) {
            double e = this.getY();
            this.moveRelative(0.02F, vec3);
            this.move(MoverType.SELF, this.getDeltaMovement());
            if (this.getFluidHeight(FluidTags.LAVA) <= this.getFluidJumpThreshold()) {
               this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.8F, 0.5));
               Vec3 vec34 = this.getFluidFallingAdjustedMovement(d, bl, this.getDeltaMovement());
               this.setDeltaMovement(vec34);
            } else {
               this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
            }

            if (!this.isNoGravity()) {
               this.setDeltaMovement(this.getDeltaMovement().add(0.0, -d / 4.0, 0.0));
            }

            Vec3 vec34 = this.getDeltaMovement();
            if (this.horizontalCollision && this.isFree(vec34.x, vec34.y + 0.6F - this.getY() + e, vec34.z)) {
               this.setDeltaMovement(vec34.x, 0.3F, vec34.z);
            }
         } else if (this.isFallFlying()) {
            this.checkSlowFallDistance();
            Vec3 vec35 = this.getDeltaMovement();
            Vec3 vec36 = this.getLookAngle();
            float f = this.getXRot() * (float) (Math.PI / 180.0);
            double i = Math.sqrt(vec36.x * vec36.x + vec36.z * vec36.z);
            double j = vec35.horizontalDistance();
            double k = vec36.length();
            double l = Math.cos(f);
            l = l * l * Math.min(1.0, k / 0.4);
            vec35 = this.getDeltaMovement().add(0.0, d * (-1.0 + l * 0.75), 0.0);
            if (vec35.y < 0.0 && i > 0.0) {
               double m = vec35.y * -0.1 * l;
               vec35 = vec35.add(vec36.x * m / i, m, vec36.z * m / i);
            }

            if (f < 0.0F && i > 0.0) {
               double m = j * -Mth.sin(f) * 0.04;
               vec35 = vec35.add(-vec36.x * m / i, m * 3.2, -vec36.z * m / i);
            }

            if (i > 0.0) {
               vec35 = vec35.add((vec36.x / i * j - vec35.x) * 0.1, 0.0, (vec36.z / i * j - vec35.z) * 0.1);
            }

            this.setDeltaMovement(vec35.multiply(0.99F, 0.98F, 0.99F));
            this.move(MoverType.SELF, this.getDeltaMovement());
            if (this.horizontalCollision && !this.level().isClientSide) {
               double m = this.getDeltaMovement().horizontalDistance();
               double n = j - m;
               float o = (float)(n * 10.0 - 3.0);
               if (o > 0.0F) {
                  this.hurt(this.damageSources().flyIntoWall(), o);
               }
            }

            if (this.onGround() && !this.level().isClientSide) {
               this.setSharedFlag(7, false);
            }
         } else {
            BlockPos blockPos = this.getBlockPosBelowThatAffectsMyMovement();
            float p = this.level().getBlockState(blockPos).getBlock().getFriction();
            float f = this.onGround() ? p * 0.91F : 0.91F;
            Vec3 vec37 = this.handleRelativeFrictionAndCalculateMovement(vec3, p);
            double q = vec37.y;
            if (this.hasEffect(MobEffects.LEVITATION)) {
               q += (0.05 * (this.getEffect(MobEffects.LEVITATION).getAmplifier() + 1) - vec37.y) * 0.2;
            } else if (this.level().isClientSide && !this.level().hasChunkAt(blockPos)) {
               if (this.getY() > this.level().getMinBuildHeight()) {
                  q = -0.1;
               } else {
                  q = 0.0;
               }
            } else if (!this.isNoGravity()) {
               q -= d;
            }

            if (this.shouldDiscardFriction()) {
               this.setDeltaMovement(vec37.x, q, vec37.z);
            } else {
               this.setDeltaMovement(vec37.x * f, q * 0.98F, vec37.z * f);
            }
         }
      }

      this.calculateEntityAnimation(this instanceof FlyingAnimal);
   }

   public void setClientSeatPos1(Vec3 seatPos) {
      this.seatPos1 = seatPos;
   }

   public void setClientSeatPos2(Vec3 seatPos) {
      this.seatPos2 = seatPos;
   }

   @Override
   public Vec3 getClientSeatPos(Entity passenger) {
      List<Entity> ps = this.getPassengers();
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

      return Vec3.ZERO;
   }

   @Override
   public InteractionResult interact(Player player, InteractionHand hand) {
      if (this.isServerSide()) {
         player.setYRot(this.getYRot());
         player.setXRot(this.getXRot());
         player.startRiding(this);
      }

      this.rideCoolTick = 3;
      this.actionController.reset();
      return InteractionResult.sidedSuccess(this.level().isClientSide);
   }

   @Override
   protected boolean canAddPassenger(Entity passenger) {
      return this.getPassengers().size() < 2;
   }
}
