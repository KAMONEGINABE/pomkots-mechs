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

   public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand interactionHand) {
      Level level = player.level();
      if (level instanceof ServerLevel serverLevel && this.canRepair(target)) {
         float healed = Math.min(target.getMaxHealth(), target.getHealth() + 100.0F);
         target.setHealth(healed);
         this.healEffect(serverLevel, target);
         itemStack.shrink(1);
      }

      return InteractionResult.sidedSuccess(level.isClientSide());
   }

   protected boolean canRepair(LivingEntity target) {
      return target.getMaxHealth() > target.getHealth() && target instanceof Pmvc01Entity;
   }

   public void healEffect(ServerLevel level, LivingEntity target) {
      for (int i = 0; i < 10; i++) {
         double dx = target.getX() + (level.random.nextDouble() - 0.5) * target.getBbWidth();
         double dy = target.getY() + level.random.nextDouble() * target.getBbHeight();
         double dz = target.getZ() + (level.random.nextDouble() - 0.5) * target.getBbWidth();
         level.sendParticles(ParticleTypes.HAPPY_VILLAGER, dx, dy, dz, 1, 0.0, 0.1, 0.0, 0.1);
      }

      level.playSound(null, target.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5F, 1.2F);
   }
}
