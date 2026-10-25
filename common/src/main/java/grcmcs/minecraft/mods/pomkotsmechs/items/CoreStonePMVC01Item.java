package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;

public class CoreStonePMVC01Item extends Item {
   private static final String USED_KEY = "pomkotsmechs:corestone:used";

   public CoreStonePMVC01Item(Properties properties) {
      super(properties);
   }

   public InteractionResultHolder<ItemStack> m_7203_(Level world, Player player, InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      CompoundTag nbt = stack.m_41784_();
      if (!nbt.m_128471_("pomkotsmechs:corestone:used")) {
         if (!world.f_46443_) {
            this.summonRobot(world, player);
         }

         nbt.m_128379_("pomkotsmechs:corestone:used", true);
         return InteractionResultHolder.m_19092_(stack, world.m_5776_());
      } else {
         return InteractionResultHolder.m_19100_(stack);
      }
   }

   private void summonRobot(Level world, Player player) {
      Pmvc01Entity robot = this.createInstance(world);
      robot.m_7678_(player.m_20185_(), player.m_20186_(), player.m_20189_(), player.m_146908_(), player.m_146909_());
      world.m_7967_(robot);
   }

   protected Pmvc01Entity createInstance(Level world) {
      return new Pmvc01Entity((EntityType<? extends LivingEntity>)PomkotsMechs.PMVC01.get(), world);
   }
}
