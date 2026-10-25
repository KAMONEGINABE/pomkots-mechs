package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;

public class FlyingMobGoal extends Goal {
   private final BaseSmallMonsterEntity mob;
   private LivingEntity target;
   private float angle = 0.0F;
   private boolean clockwise = true;
   private int switchCooldown = 0;
   private final double minRadius;
   private final double maxRadius;
   private double currentRadius;
   private int radiusCooldown = 0;
   private final double heightMin;
   private final double heightMax;
   private final double speed;
   private int ticksUntilNextSwitch = 0;

   public FlyingMobGoal(BaseSmallMonsterEntity mob, double speed, double minRadius, double maxRadius, double heightMin, double heightMax) {
      this.mob = mob;
      this.speed = speed;
      this.minRadius = minRadius;
      this.maxRadius = maxRadius;
      this.heightMin = heightMin;
      this.heightMax = heightMax;
      this.setFlags(EnumSet.of(Flag.MOVE));
      this.currentRadius = this.getRandomRadius();
   }

   private double getRandomRadius() {
      return this.minRadius + this.mob.getRandom().nextDouble() * (this.maxRadius - this.minRadius);
   }

   public boolean canUse() {
      this.target = this.mob.getTarget();
      return this.target != null && this.target.isAlive() && this.mob.getRandom().nextFloat() < 0.2F;
   }

   public boolean canContinueToUse() {
      return --this.ticksUntilNextSwitch > 0;
   }

   public void start() {
      this.ticksUntilNextSwitch = this.mob.getRandom().nextInt(30) + 30;
   }

   public void stop() {
      this.target = null;
   }

   public void tick() {
      if (this.target != null) {
         this.mob.rotateToTarget(this.target);
         if (--this.switchCooldown <= 0) {
            this.clockwise = this.mob.getRandom().nextBoolean();
            this.switchCooldown = 100 + this.mob.getRandom().nextInt(60);
         }

         if (--this.radiusCooldown <= 0) {
            this.currentRadius = this.getRandomRadius();
            this.radiusCooldown = 60 + this.mob.getRandom().nextInt(60);
         }

         this.angle = this.angle + (this.clockwise ? 1 : -1) * 20.0F;
         double rad = Math.toRadians(this.angle);
         double x = this.target.getX() + this.currentRadius * Math.cos(rad);
         double z = this.target.getZ() + this.currentRadius * Math.sin(rad);
         Level level = this.mob.level();
         BlockPos ground = level.getHeightmapPos(Types.WORLD_SURFACE, this.mob.blockPosition());
         double baseY = ground.getY() + this.heightMin;
         double y = baseY + this.mob.getRandom().nextDouble() * (this.heightMax - this.heightMin);
         Vec3 currentPos = this.mob.position();
         Vec3 targetVec = new Vec3(x, y, z);
         Vec3 motion = targetVec.subtract(currentPos).normalize().scale(0.6);
         this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(motion).scale(this.speed));
         this.mob.hurtMarked = true;
      }
   }
}
