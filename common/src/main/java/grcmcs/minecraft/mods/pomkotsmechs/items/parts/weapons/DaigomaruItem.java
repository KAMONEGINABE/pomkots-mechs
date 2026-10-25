package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.DaigomaruItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;

public class DaigomaruItem extends BasePartsItem.WeaponArm {
   public DaigomaruItem(Properties properties) {
      super(properties);
   }

   public DaigomaruItemRenderer newRenderer() {
      return new DaigomaruItemRenderer();
   }

   @Override
   public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
      Level world = mechInterface.getWorld();
      if (isOnFire) {
         if (!world.m_5776_()) {
            Player player = mechInterface.getPlayer();
            if (player != null) {
               HitResult hit = player.m_19907_(100.0, 0.0F, true);
               if (hit.m_6662_() == Type.BLOCK) {
                  BlockHitResult bh = (BlockHitResult)hit;
                  Utils.destroyBlock(world, bh.m_82425_(), PomkotsMechs.CONFIG.dropItemsWhenDestroyBlock);
               }
            }
         } else {
            mechInterface.playSoundEffect((SoundEvent)PomkotsMechs.SE_NEEDLE.get());
         }
      }
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
      return "daigomaru";
   }
}
