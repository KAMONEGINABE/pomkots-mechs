package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class PomkotsWrenchItem extends Item {
   public PomkotsWrenchItem(Properties properties) {
      super(properties);
   }

   public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      if (target instanceof PomkotsVehicleBase robot) {
         robot.heal(300.0F);
         robot.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1, 3));
         return false;
      } else {
         return super.hurtEnemy(stack, target, attacker);
      }
   }
}
