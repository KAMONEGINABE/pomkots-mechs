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

   public boolean m_8036_() {
      if (!this.raidActiveSupplier.get()) {
         return false;
      }

      this.currentTarget = this.findTarget();
      return this.currentTarget != null;
   }

   protected LivingEntity findTarget() {
      long time = this.f_26135_.f_19797_;
      long lastHurtTime = this.f_26135_.m_21213_();
      return (LivingEntity)(time - lastHurtTime < this.playerRetargetTicks && this.f_26135_.m_21188_() instanceof Player attacker
         ? attacker
         : this.findBaseTarget());
   }

   public void m_8056_() {
      if (this.currentTarget != null) {
         this.f_26135_.m_6710_(this.currentTarget);
         super.m_8056_();
      }
   }

   public boolean m_8045_() {
      if (!this.raidActiveSupplier.get()) {
         return false;
      }

      LivingEntity target = this.findTarget();
      this.f_26135_.m_6710_(target);
      return target != null && target.m_6084_();
   }

   public void m_8041_() {
      super.m_8041_();
      this.f_26135_.m_6710_(null);
      this.currentTarget = null;
   }

   @Nullable
   private T findBaseTarget() {
      if (this.f_26135_.m_9236_() instanceof ServerLevel level) {
         Vec3 var5 = this.f_26135_.m_20182_();
         AABB area = new AABB(
            var5.f_82479_ - this.targetRange,
            var5.f_82480_ - 100.0,
            var5.f_82481_ - this.targetRange,
            var5.f_82479_ + this.targetRange,
            var5.f_82480_ + 100.0,
            var5.f_82481_ + this.targetRange
         );
         List<T> list = level.m_6443_(this.baseTargetClass, area, e -> e.m_6084_() && !e.m_213877_());
         return list.isEmpty() ? null : list.get(0);
      } else {
         return null;
      }
   }
}
