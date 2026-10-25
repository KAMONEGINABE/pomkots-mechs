package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class RepairKitItem extends Item {
   public static final int HEAL_AMOUNT = 100;

   public RepairKitItem(Properties properties) {
      super(properties);
   }

   public InteractionResult m_6880_(ItemStack itemStack, Player player, LivingEntity target, InteractionHand interactionHand) {
      Level level = player.m_9236_();
      if (level instanceof ServerLevel serverLevel && this.canRepair(target)) {
         float healed = Math.min(target.m_21233_(), target.m_21223_() + 100.0F);
         target.m_21153_(healed);
         this.healEffect(serverLevel, target);
         itemStack.m_41774_(1);
      }

      return InteractionResult.m_19078_(level.m_5776_());
   }

   protected boolean canRepair(LivingEntity target) {
      return target.m_21233_() > target.m_21223_() && target instanceof Pmvc01Entity;
   }

   public void healEffect(ServerLevel level, LivingEntity target) {
      for (int i = 0; i < 10; i++) {
         double dx = target.m_20185_() + (level.f_46441_.m_188500_() - 0.5) * target.m_20205_();
         double dy = target.m_20186_() + level.f_46441_.m_188500_() * target.m_20206_();
         double dz = target.m_20189_() + (level.f_46441_.m_188500_() - 0.5) * target.m_20205_();
         level.m_8767_(ParticleTypes.f_123748_, dx, dy, dz, 1, 0.0, 0.1, 0.0, 0.1);
      }

      level.m_5594_(null, target.m_20183_(), SoundEvents.f_12275_, SoundSource.PLAYERS, 0.5F, 1.2F);
   }
}
