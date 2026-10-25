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
      this.m_7021_(EnumSet.of(Flag.MOVE, Flag.LOOK));
      this.currentVelocity = Vec3.f_82478_;
   }

   @Override
   public boolean m_8036_() {
      return true;
   }

   public boolean m_8045_() {
      return true;
   }

   public void m_8056_() {
   }

   public void m_8037_() {
      Level level = this.mob.m_9236_();
      BlockPos ground = level.m_5452_(Types.WORLD_SURFACE, this.mob.m_20183_());
      double baseY = ground.m_123342_() + 20;
      Vec3 currentPos = this.mob.m_20182_();
      if (baseY > currentPos.f_82480_) {
         double y = baseY + this.mob.m_217043_().m_188500_() * 10.0;
         Vec3 targetVec = new Vec3(currentPos.f_82479_, y, currentPos.f_82481_);
         Vec3 motion = targetVec.m_82546_(currentPos).m_82541_().m_82490_(0.6);
         this.mob.m_20256_(this.mob.m_20184_().m_82549_(motion).m_82490_(this.baseSpeed));
      } else {
         Vec3 wobble = new Vec3(
            (this.mob.m_217043_().m_188500_() - 0.5) * 0.1, (this.mob.m_217043_().m_188500_() - 0.5) * 0.1, (this.mob.m_217043_().m_188500_() - 0.5) * 0.1
         );
         this.targetVelocity = this.currentVelocity.m_82490_(0.1).m_82549_(wobble);
         this.applySmoothedMovement();
      }
   }

   private void applySmoothedMovement() {
      this.currentVelocity = this.currentVelocity.m_165921_(this.targetVelocity, 0.15);
      if (this.mob.m_20068_()) {
         this.mob.m_20256_(this.currentVelocity);
      } else {
         Vec3 currentMovement = this.mob.m_20184_();
         this.mob.m_20334_(this.currentVelocity.f_82479_, currentMovement.f_82480_, this.currentVelocity.f_82481_);
      }
   }

   public void m_8041_() {
      this.target = null;
      this.currentVelocity = Vec3.f_82478_;
      this.targetVelocity = Vec3.f_82478_;
   }
}
