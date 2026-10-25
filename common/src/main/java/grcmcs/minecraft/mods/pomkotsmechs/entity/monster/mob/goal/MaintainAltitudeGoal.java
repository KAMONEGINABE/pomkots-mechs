package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.BaseSmallMonsterEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap.Types;

public class MaintainAltitudeGoal extends SmallMobGoalBase {
   private final double targetAltitude;
   private static final int RANGE = 10;

   public MaintainAltitudeGoal(BaseSmallMonsterEntity mob, double targetAltitude, float movementSpeed) {
      super(mob, movementSpeed);
      this.targetAltitude = targetAltitude;
   }

   public void tick() {
      BlockPos currentPosition = this.mob.blockPosition();
      double groundY = this.mob.level().getHeightmapPos(Types.MOTION_BLOCKING, currentPosition).getY();
      double currentAltitude = this.mob.getY() - groundY;
      if (this.isInRangeLower(currentAltitude)) {
         this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0.0, this.speedModifier, 0.0));
      } else if (this.isInRangeUpper(currentAltitude)) {
         this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0.0, -this.speedModifier, 0.0));
      }
   }

   private boolean isInRangeLower(double currentAltitude) {
      return currentAltitude < this.targetAltitude - 10.0;
   }

   private boolean isInRangeUpper(double currentAltitude) {
      return currentAltitude > this.targetAltitude + 10.0;
   }
}
