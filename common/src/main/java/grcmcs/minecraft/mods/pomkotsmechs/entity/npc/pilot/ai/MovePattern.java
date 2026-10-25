package grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.ai;

import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public abstract class MovePattern {
   protected final LivingEntity pilot;
   protected final Pmvc01Entity mech;
   protected final RandomSource random;
   protected LivingEntity target;
   protected int tick;
   protected int maxTicks;
   protected float preferredDistance;
   protected MechAutoController controller;

   protected MovePattern(LivingEntity pilot, Pmvc01Entity mech, MechAutoController controller, float preferredDistance) {
      this.pilot = pilot;
      this.mech = mech;
      this.preferredDistance = preferredDistance;
      this.random = RandomSource.m_216327_();
      this.controller = controller;
   }

   public void start(LivingEntity target) {
      this.tick = 0;
      this.target = target;
   }

   public abstract void tick(DriverInput var1);

   protected void lookAtYaw(float yaw) {
      this.pilot.m_146922_(yaw);
      this.pilot.m_5616_(yaw);
      this.pilot.f_19859_ = yaw;
   }

   public boolean shouldExit() {
      return this.tick >= this.maxTicks;
   }

   protected double distanceToTarget() {
      return this.pilot.m_20270_(this.target);
   }

   protected void setMove(float xxa, float zza, DriverInput input) {
      this.pilot.f_20900_ = xxa;
      this.pilot.f_20902_ = zza;
      input.setForwardPressed(zza > 0.0F);
      input.setBackPressed(zza < 0.0F);
      input.setLeftPressed(xxa > 0.0F);
      input.setRightPressed(xxa < 0.0F);
   }

   public static class AirBoostEvasionPattern extends MovePattern {
      private float moveX;
      private float moveZ;

      public AirBoostEvasionPattern(LivingEntity pilot, Pmvc01Entity mech, MechAutoController controller, float preferredDistance) {
         super(pilot, mech, controller, preferredDistance);
      }

      private void initMovement() {
         int dir = this.random.m_188503_(13);
         switch (dir) {
            case 0:
            case 1:
               this.moveX = 0.0F;
               this.moveZ = 1.0F;
               break;
            case 2:
            case 3:
               this.moveX = 1.0F;
               this.moveZ = 1.0F;
               break;
            case 4:
            case 5:
               this.moveX = -1.0F;
               this.moveZ = 1.0F;
               break;
            case 6:
            case 7:
               this.moveX = 1.0F;
               this.moveZ = 0.0F;
               break;
            case 8:
            case 9:
               this.moveX = -1.0F;
               this.moveZ = 0.0F;
               break;
            case 10:
               this.moveX = 0.0F;
               this.moveZ = -1.0F;
               break;
            case 11:
               this.moveX = 1.0F;
               this.moveZ = -1.0F;
               break;
            default:
               this.moveX = -1.0F;
               this.moveZ = -1.0F;
         }
      }

      @Override
      public void start(LivingEntity target) {
         super.start(target);
         this.maxTicks = 60;
         this.initMovement();
      }

      @Override
      public void tick(DriverInput input) {
         this.tick++;
         this.setMove(this.moveX, this.moveZ, input);
         if (this.tick < 5) {
            input.setJumpPressed(true);
         } else if (this.tick < 15) {
            input.setJumpPressed(true);
         } else if (this.tick < 25) {
            input.setJumpPressed(false);
            input.setEvasionPressed(true);
         } else {
            input.setJumpPressed(false);
            input.setEvasionPressed(false);
         }
      }

      @Override
      public boolean shouldExit() {
         return (this.tick <= 10 || !this.mech.m_20096_()) && this.mech.getEnergy() != 0 ? super.shouldExit() : true;
      }
   }

   public static class ChargePattern extends MovePattern {
      private final float chargeDistance;

      public ChargePattern(LivingEntity pilot, Pmvc01Entity mech, MechAutoController controller, float preferredDistance, float chargeDistance) {
         super(pilot, mech, controller, preferredDistance);
         this.chargeDistance = chargeDistance;
      }

      @Override
      public void start(LivingEntity target) {
         super.start(target);
         this.maxTicks = 60;
      }

      @Override
      public void tick(DriverInput input) {
         if (this.tick == 0) {
            input.setEvasionPressed(true);
         }

         this.tick++;
         this.setMove(0.0F, 1.0F, input);
      }

      @Override
      public boolean shouldExit() {
         return this.distanceToTarget() <= this.chargeDistance ? true : super.shouldExit();
      }
   }

   public static class FollowPlayerPattern extends MovePattern {
      private Player followTarget;

      public FollowPlayerPattern(LivingEntity pilot, Pmvc01Entity mech, MechAutoController controller, float preferredDistance) {
         super(pilot, mech, controller, preferredDistance);
      }

      @Override
      public void start(LivingEntity target) {
         super.start(target);
         this.maxTicks = 100;
         this.followTarget = this.findNearestPlayer();
      }

      private Player findNearestPlayer() {
         List<? extends Player> players = this.mech.m_9236_().m_6907_();
         double bestDistance = Double.MAX_VALUE;
         Player best = null;

         for (Player player : players) {
            double distance = this.mech.m_20280_(player);
            if (distance < bestDistance) {
               bestDistance = distance;
               best = player;
            }
         }

         return best;
      }

      @Override
      public void tick(DriverInput input) {
         this.tick++;
         if (this.followTarget != null) {
            this.controller.updateRotation(this.followTarget);
            double distance = this.mech.m_20270_(this.followTarget);
            if (this.followTarget.m_20182_().f_82480_ - this.mech.m_20182_().f_82480_ > 10.0) {
               input.setJumpPressed(true);
            }

            if (distance > 15.0) {
               if (distance > 100.0) {
                  input.setEvasionPressed(true);
               } else if (distance > 40.0) {
                  this.controller.startBoosting();
               }

               this.setMove(0.0F, 1.0F, input);
            } else {
               this.controller.stopBoosting();
               this.setMove(0.0F, 0.0F, input);
            }
         }
      }
   }

   public static class IdlePattern extends MovePattern {
      public IdlePattern(LivingEntity pilot, Pmvc01Entity mech, MechAutoController controller, float preferredDistance) {
         super(pilot, mech, controller, preferredDistance);
      }

      @Override
      public void start(LivingEntity target) {
         super.start(target);
         this.maxTicks = 100;
      }

      @Override
      public void tick(DriverInput input) {
         this.tick++;
         this.setMove(0.0F, 0.0F, input);
      }
   }

   public static class KitePattern extends MovePattern {
      private final boolean leftOrbit = this.random.m_188499_();

      public KitePattern(LivingEntity pilot, Pmvc01Entity mech, MechAutoController controller, float preferredDistance) {
         super(pilot, mech, controller, preferredDistance);
      }

      @Override
      public void start(LivingEntity target) {
         super.start(target);
         this.maxTicks = 80 + this.random.m_188503_(80);
      }

      @Override
      public void tick(DriverInput input) {
         this.tick++;
         float xxa = this.leftOrbit ? 0.6F : -0.6F;
         double distance = this.distanceToTarget();
         float zza;
         if (distance < this.preferredDistance) {
            zza = -1.0F;
         } else {
            zza = -0.2F;
         }

         this.setMove(xxa, zza, input);
      }
   }

   public record MovePatternEntry(int baseWeight, Class<? extends MovePattern> patternClass, Supplier<MovePattern> factory) {
   }

   public static class OrbitLeftPattern extends MovePattern {
      public OrbitLeftPattern(LivingEntity pilot, Pmvc01Entity mech, MechAutoController controller, float preferredDistance) {
         super(pilot, mech, controller, preferredDistance);
      }

      @Override
      public void start(LivingEntity target) {
         super.start(target);
         this.maxTicks = 20 + this.random.m_188503_(40);
      }

      @Override
      public void tick(DriverInput input) {
         this.tick++;
         double distance = this.distanceToTarget();
         float zza;
         if (distance < this.preferredDistance * 0.8F) {
            zza = -0.4F;
         } else if (distance > this.preferredDistance * 1.2F) {
            zza = 1.0F;
         } else {
            zza = 0.2F;
         }

         this.setMove(1.0F, zza, input);
      }
   }

   public static class OrbitRightPattern extends MovePattern {
      public OrbitRightPattern(LivingEntity pilot, Pmvc01Entity mech, MechAutoController controller, float preferredDistance) {
         super(pilot, mech, controller, preferredDistance);
      }

      @Override
      public void start(LivingEntity target) {
         super.start(target);
         this.maxTicks = 20 + this.random.m_188503_(40);
      }

      @Override
      public void tick(DriverInput input) {
         if (this.tick == 0) {
            input.setEvasionPressed(true);
         }

         this.tick++;
         double distance = this.distanceToTarget();
         float zza;
         if (distance < this.preferredDistance * 0.8F) {
            zza = -0.4F;
         } else if (distance > this.preferredDistance * 1.2F) {
            zza = 1.0F;
         } else {
            zza = 0.2F;
         }

         this.setMove(-1.0F, zza, input);
      }
   }

   public static class PatrolPattern extends MovePattern {
      private float moveX;
      private float moveZ;
      private float moveYaw;

      public PatrolPattern(LivingEntity pilot, Pmvc01Entity mech, MechAutoController controller, float preferredDistance) {
         super(pilot, mech, controller, preferredDistance);
      }

      @Override
      public void start(LivingEntity target) {
         super.start(target);
         this.controller.stopBoosting();
         this.maxTicks = 20 + this.random.m_188503_(40);
         this.chooseDirection();
      }

      private void chooseDirection() {
         this.moveYaw = this.random.m_188501_() * 360.0F;
         float rad = this.moveYaw * (float) (Math.PI / 180.0);
         this.moveX = -Mth.m_14031_(rad);
         this.moveZ = Mth.m_14089_(rad);
      }

      @Override
      public void tick(DriverInput input) {
         this.tick++;
         if (this.tick % 10 == 0) {
            this.moveYaw = this.moveYaw + (this.random.m_188501_() * 20.0F - 10.0F);
         }

         this.lookAtYaw(this.moveYaw);
         this.setMove(0.0F, 1.0F, input);
         if (this.tick % 40 == 0 && this.random.m_188501_() < 0.3F) {
            this.chooseDirection();
         }
      }
   }

   public static class RetreatPattern extends MovePattern {
      public RetreatPattern(LivingEntity pilot, Pmvc01Entity mech, MechAutoController controller, float preferredDistance) {
         super(pilot, mech, controller, preferredDistance);
      }

      @Override
      public void start(LivingEntity target) {
         super.start(target);
         this.maxTicks = 20 + this.random.m_188503_(40);
      }

      @Override
      public void tick(DriverInput input) {
         if (this.tick == 0) {
            input.setEvasionPressed(true);
         }

         this.tick++;
         this.setMove(0.0F, -1.0F, input);
      }

      @Override
      public boolean shouldExit() {
         return this.distanceToTarget() > this.preferredDistance ? true : super.shouldExit();
      }
   }

   public static class VerticalApproachPattern extends MovePattern {
      public VerticalApproachPattern(LivingEntity pilot, Pmvc01Entity mech, MechAutoController controller, float preferredDistance) {
         super(pilot, mech, controller, preferredDistance);
      }

      @Override
      public void start(LivingEntity target) {
         super.start(target);
         this.maxTicks = 80 + this.random.m_188503_(40);
      }

      @Override
      public void tick(DriverInput input) {
         if (this.tick == 0) {
            input.setEvasionPressed(true);
         }

         this.tick++;
         this.setMove(0.0F, 1.0F, input);
         input.setJumpPressed(true);
      }

      @Override
      public boolean shouldExit() {
         if (this.target == null) {
            return true;
         }

         double yDiff = Math.abs(this.target.m_20186_() - this.mech.m_20186_());
         return yDiff < 10.0 ? true : super.shouldExit();
      }
   }
}
