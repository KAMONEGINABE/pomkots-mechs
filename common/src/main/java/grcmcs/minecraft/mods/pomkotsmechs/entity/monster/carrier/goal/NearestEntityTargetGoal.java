package grcmcs.minecraft.mods.pomkotsmechs.entity.monster.carrier.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class NearestEntityTargetGoal<T extends LivingEntity> extends TargetGoal {
   private static final int DEFAULT_RANDOM_INTERVAL = 10;
   protected final Class<T> targetType;
   protected final int randomInterval;
   @Nullable
   protected LivingEntity target;
   protected TargetingConditions targetConditions;

   public NearestEntityTargetGoal(Mob mob, Class<T> class_, boolean bl) {
      this(mob, class_, 10, bl, false, (Predicate<LivingEntity>)null);
   }

   public NearestEntityTargetGoal(Mob mob, Class<T> class_, boolean bl, Predicate<LivingEntity> predicate) {
      this(mob, class_, 10, bl, false, predicate);
   }

   public NearestEntityTargetGoal(Mob mob, Class<T> class_, boolean bl, boolean bl2) {
      this(mob, class_, 10, bl, bl2, (Predicate<LivingEntity>)null);
   }

   public NearestEntityTargetGoal(Mob mob, Class<T> class_, int i, boolean bl, boolean bl2, @Nullable Predicate<LivingEntity> predicate) {
      super(mob, bl, bl2);
      this.targetType = class_;
      this.randomInterval = m_186073_(i);
      this.m_7021_(EnumSet.of(Flag.TARGET));
      this.targetConditions = TargetingConditions.m_148353_().m_26883_(300.0).m_148355_().m_26888_(predicate);
   }

   public boolean m_8036_() {
      if (this.randomInterval > 0 && this.f_26135_.m_217043_().m_188503_(this.randomInterval) != 0) {
         return false;
      }

      this.findTarget();
      return this.target != null;
   }

   protected AABB getTargetSearchArea(double d) {
      return this.f_26135_.m_20191_().m_82377_(d, 4.0, d);
   }

   protected void findTarget() {
      if (this.targetType != Player.class && this.targetType != ServerPlayer.class) {
         this.target = this.f_26135_
            .m_9236_()
            .m_45982_(
               this.f_26135_.m_9236_().m_6443_(this.targetType, this.getTargetSearchArea(this.m_7623_()), livingEntity -> true),
               this.targetConditions,
               this.f_26135_,
               this.f_26135_.m_20185_(),
               this.f_26135_.m_20188_(),
               this.f_26135_.m_20189_()
            );
      } else {
         this.target = this.f_26135_
            .m_9236_()
            .m_45949_(this.targetConditions, this.f_26135_, this.f_26135_.m_20185_(), this.f_26135_.m_20188_(), this.f_26135_.m_20189_());
         if (this.target != null && ((Player)this.target).m_7500_()) {
            this.target = null;
         }
      }
   }

   public void m_8056_() {
      this.f_26135_.m_6710_(this.target);
      super.m_8056_();
   }

   public void setTarget(@Nullable LivingEntity livingEntity) {
      this.target = livingEntity;
   }
}
