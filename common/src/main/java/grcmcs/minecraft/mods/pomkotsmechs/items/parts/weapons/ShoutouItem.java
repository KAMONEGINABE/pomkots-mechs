package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.ShoutouItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;

public class ShoutouItem extends BasePartsItem.WeaponArm {
   public ShoutouItem(Properties properties) {
      super(properties);
   }

   public ShoutouItemRenderer newRenderer() {
      return new ShoutouItemRenderer();
   }

   @Override
   public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
      Level world = mechInterface.getWorld();
      if (isOnFire) {
         if (!world.m_5776_()) {
            Player player = mechInterface.getPlayer();
            if (player != null) {
               BlockState blockState = this.getMainHandBlock(player);
               if (blockState != null) {
                  HitResult hit = player.m_19907_(100.0, 0.0F, true);
                  if (hit.m_6662_() == Type.BLOCK) {
                     BlockHitResult bh = (BlockHitResult)hit;
                     BlockPos pos = bh.m_82425_().m_121945_(bh.m_82434_());
                     Utils.setBlock(world, pos, blockState, 3);
                     if (PomkotsMechs.CONFIG.consumeBlocksWhenPlacing && !player.m_150110_().f_35937_) {
                        player.m_21205_().m_41774_(1);
                     }
                  }
               }
            }
         } else {
            mechInterface.playSoundEffect((SoundEvent)PomkotsMechs.SE_PLACE.get());
         }
      }
   }

   private BlockState getMainHandBlock(LivingEntity player) {
      return player != null && player.m_21205_().m_41720_() instanceof BlockItem blockItem ? blockItem.m_40614_().m_49966_() : null;
   }

   @Override
   public BasePartsItem.WeaponInterface.WeaponAttachPoint getWeaponAttachPoint() {
      return BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_ARM;
   }

   @Override
   public BasePartsItem.WeaponCategory getWeaponCategory() {
      return BasePartsItem.WeaponCategory.RIFLE;
   }

   @Override
   public Motion getMotion() {
      return Motion.RIFLE;
   }

   @Override
   public int getCoolTime() {
      return 11;
   }

   @Override
   public String getPartsSeriesName() {
      return "shoutou";
   }
}
