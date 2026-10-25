package grcmcs.minecraft.mods.pomkotsmechs.items.pilot;

import grcmcs.minecraft.mods.pomkotsmechs.client.gui.pilot.PilotMenuProvider;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class PilotConfiguratorItem extends Item {
   public PilotConfiguratorItem(Properties properties) {
      super(properties);
   }

   public InteractionResult m_6880_(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
      if (!(entity instanceof Mob mob) && !(entity instanceof Pmvc01Entity mech)) {
         return InteractionResult.PASS;
      } else {
         if (!player.m_9236_().m_5776_()) {
            if (entity instanceof Pmvc01Entity mech && mech.getDrivingPassenger() instanceof Mob mob) {
               mob.m_8127_();
               mob.m_21557_(false);
            } else if (entity instanceof Mob mob) {
               player.m_5893_(new PilotMenuProvider(mob));
            }
         }

         return InteractionResult.m_19078_(player.m_9236_().m_5776_());
      }
   }
}
