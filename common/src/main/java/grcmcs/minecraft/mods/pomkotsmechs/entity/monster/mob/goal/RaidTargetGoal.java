package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.goal;

import java.util.List;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RaidTargetGoal<T extends LivingEntity> extends TargetGoal {
   private final Class<T> baseTargetClass;
   private final double targetRange;
   private final int playerRetargetTicks;
   @Nullable
   private LivingEntity currentTarget;
   private final Supplier<Boolean> raidActiveSupplier;

   public RaidTargetGoal(Mob mob, Class<T> baseTargetClass, double targetRange, int playerRetargetTicks, Supplier<Boolean> raidActiveSupplier) {
      super(mob, false);
      this.baseTargetClass = baseTargetClass;
      this.targetRange = targetRange;
      this.playerRetargetTicks = playerRetargetTicks;
      this.raidActiveSupplier = raidActiveSupplier;
   }

   public boolean canUse() {
      if (!this.raidActiveSupplier.get()) {
         return false;
      }

      this.currentTarget = this.findTarget();
      return this.currentTarget != null;
   }

   protected LivingEntity findTarget() {
      long time = this.mob.tickCount;
      long lastHurtTime = this.mob.getLastHurtByMobTimestamp();
      return (LivingEntity)(time - lastHurtTime < this.playerRetargetTicks && this.mob.getLastHurtByMob() instanceof Player attacker
         ? attacker
         : this.findBaseTarget());
   }

   public void start() {
      if (this.currentTarget != null) {
         this.mob.setTarget(this.currentTarget);
         super.start();
      }
   }

   public boolean canContinueToUse() {
      if (!this.raidActiveSupplier.get()) {
         return false;
      }

      LivingEntity target = this.findTarget();
      this.mob.setTarget(target);
      return target != null && target.isAlive();
   }

   public void stop() {
      super.stop();
      this.mob.setTarget(null);
      this.currentTarget = null;
   }

   @Nullable
   private T findBaseTarget() {
      if (this.mob.level() instanceof ServerLevel level) {
         Vec3 var5 = this.mob.position();
         AABB area = new AABB(
            var5.x - this.targetRange,
            var5.y - 100.0,
            var5.z - this.targetRange,
            var5.x + this.targetRange,
            var5.y + 100.0,
            var5.z + this.targetRange
         );
         List<T> list = level.getEntitiesOfClass(this.baseTargetClass, area, e -> e.isAlive() && !e.isRemoved());
         return list.isEmpty() ? null : list.get(0);
      } else {
         return null;
      }
   }
}
