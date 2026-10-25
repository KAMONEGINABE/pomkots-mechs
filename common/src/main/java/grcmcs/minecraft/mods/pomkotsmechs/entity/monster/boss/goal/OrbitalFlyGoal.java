package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.goal;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.BaseBossEntity;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.phys.Vec3;

public class OrbitalFlyGoal extends BaseBossGoal {
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

   public OrbitalFlyGoal(BaseBossEntity mob, double speed, double minRadius, double maxRadius, double heightMin, double heightMax) {
      super(mob);
      this.speed = speed;
      this.minRadius = minRadius;
      this.maxRadius = maxRadius;
      this.heightMin = heightMin;
      this.heightMax = heightMax;
      this.m_7021_(EnumSet.of(Flag.MOVE));
      this.currentRadius = this.getRandomRadius();
   }

   private double getRandomRadius() {
      return this.minRadius + this.mob.m_217043_().m_188500_() * (this.maxRadius - this.minRadius);
   }

   @Override
   public boolean canUseInternal() {
      return true;
   }

   public boolean m_8045_() {
      return --this.ticksUntilNextSwitch > 0;
   }

   public void m_8056_() {
      this.target = this.mob.m_5448_();
      this.ticksUntilNextSwitch = this.mob.m_217043_().m_188503_(30) + 30;
   }

   public void m_8041_() {
      this.target = null;
   }

   public void m_8037_() {
      if (this.target != null) {
         this.mob.rotateToTarget(this.target);
         if (--this.switchCooldown <= 0) {
            this.clockwise = this.mob.m_217043_().m_188499_();
            this.switchCooldown = 100 + this.mob.m_217043_().m_188503_(60);
         }

         if (--this.radiusCooldown <= 0) {
            this.currentRadius = this.getRandomRadius();
            this.radiusCooldown = 60 + this.mob.m_217043_().m_188503_(60);
         }

         this.angle = this.angle + (this.clockwise ? 1 : -1) * 20.0F;
         double rad = Math.toRadians(this.angle);
         double x = this.target.m_20185_() + this.currentRadius * Math.cos(rad);
         double z = this.target.m_20189_() + this.currentRadius * Math.sin(rad);
         Level level = this.mob.m_9236_();
         BlockPos ground = level.m_5452_(Types.WORLD_SURFACE, this.mob.m_20183_());
         double baseY = ground.m_123342_() + this.heightMin;
         double y = baseY + this.mob.m_217043_().m_188500_() * (this.heightMax - this.heightMin);
         Vec3 currentPos = this.mob.m_20182_();
         Vec3 targetVec = new Vec3(x, y, z);
         Vec3 motion = targetVec.m_82546_(currentPos).m_82541_().m_82490_(0.6);
         this.mob.m_20256_(this.mob.m_20184_().m_82549_(motion).m_82490_(this.speed));
         this.mob.f_19864_ = true;
      }
   }
}
