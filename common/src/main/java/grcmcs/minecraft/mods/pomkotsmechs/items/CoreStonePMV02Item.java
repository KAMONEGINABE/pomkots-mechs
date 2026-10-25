package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv02Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class CoreStonePMV02Item extends Item {
   private static final String USED_KEY = "pomkotsmechs:corestone:used";

   public CoreStonePMV02Item(Properties properties) {
      super(properties);
   }

   public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
      ItemStack stack = player.getItemInHand(hand);
      CompoundTag nbt = stack.getOrCreateTag();
      if (!nbt.getBoolean("pomkotsmechs:corestone:used")) {
         if (!world.isClientSide) {
            this.summonRobot(world, player);
         }

         nbt.putBoolean("pomkotsmechs:corestone:used", true);
         return InteractionResultHolder.sidedSuccess(stack, world.isClientSide());
      } else {
         return InteractionResultHolder.fail(stack);
      }
   }

   public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      if (target instanceof Pmv02Entity robot) {
         robot.heal(20.0F);
         robot.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1, 3));
         return false;
      } else {
         return super.hurtEnemy(stack, target, attacker);
      }
   }

   private void summonRobot(Level world, Player player) {
      Pmv02Entity robot = this.createInstance(world);
      robot.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
      world.addFreshEntity(robot);
   }

   protected Pmv02Entity createInstance(Level world) {
      return new Pmv02Entity((EntityType<? extends LivingEntity>)PomkotsMechs.PMV02.get(), world);
   }
}
