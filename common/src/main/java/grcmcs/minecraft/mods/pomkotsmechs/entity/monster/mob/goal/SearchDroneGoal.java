package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;

public class SearchDroneGoal extends SmallMobGoalBase {
   private LivingEntity target;
   private final double baseSpeed;
   private Vec3 currentVelocity;
   private Vec3 targetVelocity;
   private static final double VELOCITY_SMOOTHING = 0.15;

   public SearchDroneGoal(BaseSmallMonsterEntity mob, double baseSpeed, float longRange, float midRange, float closeRange, int attackCooldown) {
      super(mob, (float)baseSpeed);
      this.mob = mob;
      this.baseSpeed = baseSpeed;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
      this.currentVelocity = Vec3.ZERO;
   }

   @Override
   public boolean canUse() {
      return true;
   }

   public boolean canContinueToUse() {
      return true;
   }

   public void start() {
   }

   public void tick() {
      Level level = this.mob.level();
      BlockPos ground = level.getHeightmapPos(Types.WORLD_SURFACE, this.mob.blockPosition());
      double baseY = ground.getY() + 20;
      Vec3 currentPos = this.mob.position();
      if (baseY > currentPos.y) {
         double y = baseY + this.mob.getRandom().nextDouble() * 10.0;
         Vec3 targetVec = new Vec3(currentPos.x, y, currentPos.z);
         Vec3 motion = targetVec.subtract(currentPos).normalize().scale(0.6);
         this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(motion).scale(this.baseSpeed));
      } else {
         Vec3 wobble = new Vec3(
            (this.mob.getRandom().nextDouble() - 0.5) * 0.1, (this.mob.getRandom().nextDouble() - 0.5) * 0.1, (this.mob.getRandom().nextDouble() - 0.5) * 0.1
         );
         this.targetVelocity = this.currentVelocity.scale(0.1).add(wobble);
         this.applySmoothedMovement();
      }
   }

   private void applySmoothedMovement() {
      this.currentVelocity = this.currentVelocity.lerp(this.targetVelocity, 0.15);
      if (this.mob.isNoGravity()) {
         this.mob.setDeltaMovement(this.currentVelocity);
      } else {
         Vec3 currentMovement = this.mob.getDeltaMovement();
         this.mob.setDeltaMovement(this.currentVelocity.x, currentMovement.y, this.currentVelocity.z);
      }
   }

   public void stop() {
      this.target = null;
      this.currentVelocity = Vec3.ZERO;
      this.targetVelocity = Vec3.ZERO;
   }
}
