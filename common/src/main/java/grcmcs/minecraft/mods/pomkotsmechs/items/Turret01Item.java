package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.GenericPomkotsMonster;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.BaseTurretEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt01Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;

public class Turret01Item extends Item {
   private static final String USED_KEY = "pomkotsmechs:corestone:used";

   public Turret01Item(Properties properties) {
      super(properties);
   }

   public InteractionResultHolder<ItemStack> m_7203_(Level world, Player player, InteractionHand hand) {
      ItemStack stack = player.m_21120_(hand);
      if (!world.f_46443_) {
         this.summonRobot(stack, world, player);
      }

      return InteractionResultHolder.m_19092_(stack, world.m_5776_());
   }

   private void summonRobot(ItemStack stack, Level world, Player player) {
      double distance = 20.0;
      HitResult result = player.m_19907_(distance, 0.0F, false);
      if (result.m_6662_() == Type.BLOCK) {
         BlockHitResult blockHit = (BlockHitResult)result;
         BlockPos hitPos = blockHit.m_82425_();
         BlockState state = player.m_9236_().m_8055_(hitPos);
         Block block = state.m_60734_();
         if (block == Blocks.f_50721_) {
            Pmt01Entity robot = this.createInstance(world);
            robot.m_7678_(hitPos.m_123341_() + 0.5, hitPos.m_123342_() + 1, hitPos.m_123343_() + 0.5, player.m_146908_(), player.m_146909_());
            world.m_7967_(robot);
            robot.setTargetEntityType(BaseTurretEntity.TargetEntityType.POMKOTS_MONSTERS);
            stack.m_41774_(1);
         }
      }
   }

   protected Pmt01Entity createInstance(Level world) {
      return new Pmt01Entity((EntityType<? extends GenericPomkotsMonster>)PomkotsMechs.PMT01.get(), world);
   }
}
