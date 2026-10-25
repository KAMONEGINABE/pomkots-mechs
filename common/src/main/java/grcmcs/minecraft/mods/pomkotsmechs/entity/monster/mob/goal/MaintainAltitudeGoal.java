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

   public void m_8037_() {
      BlockPos currentPosition = this.mob.m_20183_();
      double groundY = this.mob.m_9236_().m_5452_(Types.MOTION_BLOCKING, currentPosition).m_123342_();
      double currentAltitude = this.mob.m_20186_() - groundY;
      if (this.isInRangeLower(currentAltitude)) {
         this.mob.m_20256_(this.mob.m_20184_().m_82520_(0.0, this.speedModifier, 0.0));
      } else if (this.isInRangeUpper(currentAltitude)) {
         this.mob.m_20256_(this.mob.m_20184_().m_82520_(0.0, -this.speedModifier, 0.0));
      }
   }

   private boolean isInRangeLower(double currentAltitude) {
      return currentAltitude < this.targetAltitude - 10.0;
   }

   private boolean isInRangeUpper(double currentAltitude) {
      return currentAltitude > this.targetAltitude + 10.0;
   }
}
