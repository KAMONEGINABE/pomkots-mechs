package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.Pmv01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
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

public class CoreStonePMV01Item extends Item {
   private static final String USED_KEY = "pomkotsmechs:corestone:used";

   public CoreStonePMV01Item(Properties properties) {
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

   public boolean m_7579_(ItemStack stack, LivingEntity target, LivingEntity attacker) {
      if (target instanceof PomkotsVehicleBase robot) {
         robot.m_5634_(20.0F);
         robot.m_7292_(new MobEffectInstance(MobEffects.f_19605_, 1, 3));
         return false;
      } else {
         return super.m_7579_(stack, target, attacker);
      }
   }

   private void summonRobot(Level world, Player player) {
      Pmv01Entity robot = this.createInstance(world);
      robot.m_7678_(player.m_20185_(), player.m_20186_(), player.m_20189_(), player.m_146908_(), player.m_146909_());
      world.m_7967_(robot);
   }

   protected Pmv01Entity createInstance(Level world) {
      return new Pmv01Entity((EntityType<? extends LivingEntity>)PomkotsMechs.PMV01.get(), world);
   }
}
