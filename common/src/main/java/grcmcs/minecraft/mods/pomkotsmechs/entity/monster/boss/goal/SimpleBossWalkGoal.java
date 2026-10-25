package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SimpleBossWalkGoal extends BaseBossGoal {
   private final double moveSpeed;
   private final double desiredDistance;
   private LivingEntity target;
   private SimpleBossWalkGoal.Mode mode = SimpleBossWalkGoal.Mode.APPROACH;
   private int circleDirection = 1;
   private int ticksRemaining = 0;
   private int modeSwitchCooldown = 0;
   private int airTicks = 0;
   private static final int MAX_AIR_TICKS = 10;
   private static final int MIN_DURATION = 20;
   private static final int MAX_DURATION = 30;
   private static final int MODE_SWITCH_INTERVAL = 15;
   private static final double CIRCLE_RADIUS = 2.0;

   public SimpleBossWalkGoal(BaseBossEntity mob, double speed, double desiredDistance) {
      super(mob);
      this.moveSpeed = speed;
      this.desiredDistance = desiredDistance;
      this.m_7021_(EnumSet.of(Flag.MOVE));
   }

   @Override
   public boolean canUseInternal() {
      return this.mob.m_5448_() != null && this.mob.m_5448_().m_6084_();
   }

   public boolean m_8045_() {
      return this.ticksRemaining > 0 && this.target != null && this.target.m_6084_() && this.airTicks < 10;
   }

   public void m_8056_() {
      this.target = this.mob.m_5448_();
      this.ticksRemaining = 20 + this.mob.m_217043_().m_188503_(10);
      this.modeSwitchCooldown = 15;
      this.mode = SimpleBossWalkGoal.Mode.APPROACH;
      this.circleDirection = this.mob.m_217043_().m_188499_() ? 1 : -1;
      this.airTicks = 0;
      this.mob.m_20242_(false);
   }

   public void m_8041_() {
      this.target = null;
      this.ticksRemaining = 0;
      Vec3 vel = this.mob.m_20184_();
      this.mob.m_20334_(0.0, vel.f_82480_, 0.0);
   }

   public void m_8037_() {
      this.ticksRemaining--;
      if (this.target != null) {
         this.updateAirTicks();
         this.mob.rotateToTarget(this.target);
         this.updateMode();
         this.performMovement();
      }
   }

   private void updateAirTicks() {
      if (!this.mob.m_20096_() && !this.mob.m_20069_() && !this.mob.m_20077_()) {
         this.airTicks++;
      } else {
         this.airTicks = 0;
      }
   }

   private void updateMode() {
      this.modeSwitchCooldown--;
      if (this.modeSwitchCooldown <= 0) {
         double distance = this.mob.m_20270_(this.target);
         if (this.mode == SimpleBossWalkGoal.Mode.APPROACH && distance <= this.desiredDistance) {
            this.mode = SimpleBossWalkGoal.Mode.CIRCLE;
            this.circleDirection *= -1;
         } else if (this.mode == SimpleBossWalkGoal.Mode.CIRCLE && distance > this.desiredDistance + 2.0) {
            this.mode = SimpleBossWalkGoal.Mode.APPROACH;
         } else {
            this.circleDirection *= -1;
         }

         this.modeSwitchCooldown = 15 + this.mob.m_217043_().m_188503_(20);
      }
   }

   private void performMovement() {
      Vec3 currentPos = this.mob.m_20182_();
      Vec3 toTarget = this.target.m_20182_().m_82546_(currentPos);
      Vec3 horizontalDir = this.calculateHorizontalDirection(toTarget);
      double nextX = currentPos.f_82479_ + horizontalDir.f_82479_;
      double nextZ = currentPos.f_82481_ + horizontalDir.f_82481_;
      if (!this.canMoveToPosition(nextX, currentPos.f_82480_, nextZ)) {
         this.applyGravityOnly();
         this.ticksRemaining = 0;
      } else {
         double yVelocity = this.calculateVerticalMovement(nextX, currentPos.f_82480_, nextZ);
         this.mob.m_20334_(horizontalDir.f_82479_, yVelocity, horizontalDir.f_82481_);
      }
   }

   private Vec3 calculateHorizontalDirection(Vec3 toTarget) {
      Vec3 horizontal = new Vec3(toTarget.f_82479_, 0.0, toTarget.f_82481_);

      return switch (this.mode) {
         case APPROACH -> horizontal.m_82556_() < 0.01 ? Vec3.f_82478_ : horizontal.m_82541_().m_82490_(this.moveSpeed);
         case CIRCLE -> {
            double angleToTarget = Math.atan2(horizontal.f_82481_, horizontal.f_82479_);
            double circleAngle = angleToTarget + this.circleDirection * Math.PI / 2.0;
            yield new Vec3(Math.cos(circleAngle) * this.moveSpeed, 0.0, Math.sin(circleAngle) * this.moveSpeed);
         }
      };
   }

   private boolean isStandingInFluid() {
      BlockPos startPos = this.mob.m_20183_().m_7494_();

      for (int y = startPos.m_123342_() + 5; y >= startPos.m_123342_() - 5; y--) {
         BlockPos pos = new BlockPos(startPos.m_123341_(), y, startPos.m_123343_());
         BlockState state = this.mob.m_9236_().m_8055_(pos);
         if (state.m_280296_() && !state.m_60795_()) {
            return false;
         }

         if (!state.m_60819_().m_76178_()) {
            return true;
         }
      }

      return false;
   }

   private boolean canMoveToPosition(double x, double y, double z) {
      Level level = this.mob.m_9236_();
      boolean currentlyInFluid = this.mob.m_20069_() || this.mob.m_20077_();
      double groundY = this.findGroundY(x, y, z);
      if (groundY == Double.MIN_VALUE) {
         return false;
      } else {
         BlockPos footPos = new BlockPos(Mth.m_14107_(x), Mth.m_14107_(groundY), Mth.m_14107_(z));
         BlockState footState = level.m_8055_(footPos);
         BlockPos footPosB = new BlockPos(Mth.m_14107_(x), Mth.m_14107_(groundY) + 1, Mth.m_14107_(z));
         BlockState footStateB = level.m_8055_(footPosB);
         BlockPos footPosC = new BlockPos(Mth.m_14107_(x), Mth.m_14107_(groundY) - 1, Mth.m_14107_(z));
         BlockState footStateC = level.m_8055_(footPosC);
         if (footState.m_60819_().m_76178_() && footStateB.m_60819_().m_76178_() && footStateC.m_60819_().m_76178_()) {
            double heightDiff = groundY - y;
            double maxStep = this.mob.m_274421_() + 1.0;
            return heightDiff > maxStep ? false : this.hasHeadroom(x, groundY, z);
         } else {
            return currentlyInFluid;
         }
      }
   }

   private double findGroundY(double x, double startY, double z) {
      Level level = this.mob.m_9236_();
      int searchUp = (int)Math.ceil(this.mob.m_274421_()) + 2;
      int searchDown = 30;
      int startYInt = Mth.m_14107_(startY);

      for (int y = startYInt + searchUp; y >= startYInt - searchDown; y--) {
         BlockPos pos = new BlockPos(Mth.m_14107_(x), y, Mth.m_14107_(z));
         BlockState state = level.m_8055_(pos);
         if (state.m_60819_().m_76178_() && state.m_280296_() && !state.m_60795_()) {
            return this.getBlockTopY(level, pos, state);
         }
      }

      return Double.MIN_VALUE;
   }

   private double getBlockTopY(Level level, BlockPos pos, BlockState state) {
      try {
         VoxelShape shape = state.m_60812_(level, pos);
         if (!shape.m_83281_()) {
            return pos.m_123342_() + shape.m_83297_(Axis.Y);
         }
      } catch (Exception var5) {
      }

      return pos.m_123342_() + 1.0;
   }

   private boolean hasHeadroom(double x, double groundY, double z) {
      Level level = this.mob.m_9236_();
      double height = this.mob.m_20206_();
      int checkHeight = Mth.m_14165_(height) + 1;

      for (int y = 0; y < checkHeight; y++) {
         BlockPos pos = new BlockPos(Mth.m_14107_(x), Mth.m_14107_(groundY) + y, Mth.m_14107_(z));
         BlockState state = level.m_8055_(pos);
         if (state.m_280296_() && !state.m_60795_()) {
            return false;
         }
      }

      return true;
   }

   private double calculateVerticalMovement(double nextX, double currentY, double nextZ) {
      double groundY = this.findGroundY(nextX, currentY, nextZ);
      if (groundY == Double.MIN_VALUE) {
         return this.applyGravity();
      } else {
         double heightDiff = groundY - currentY;
         if (Math.abs(heightDiff) < 0.1) {
            return 0.0;
         } else if (heightDiff > 0.0) {
            double maxStep = this.mob.m_274421_();
            return heightDiff <= maxStep ? heightDiff : maxStep;
         } else {
            return this.applyGravity();
         }
      }
   }

   private double applyGravity() {
      double currentVY = this.mob.m_20184_().f_82480_;
      double gravity = 0.08;
      return !this.mob.m_20096_() ? Math.max(currentVY - gravity, -3.0) : 0.0;
   }

   private void applyGravityOnly() {
      this.mob.m_20334_(0.0, this.applyGravity(), 0.0);
   }

   private enum Mode {
      APPROACH,
      CIRCLE;
   }
}
