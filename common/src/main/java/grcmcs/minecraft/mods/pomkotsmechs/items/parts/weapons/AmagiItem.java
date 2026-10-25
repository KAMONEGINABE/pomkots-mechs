package grcmcs.minecraft.mods.pomkotsmechs.items.parts.weapons;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.weapons.AmagiItemRenderer;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.ActionWeapon;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom.Motion;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;

public class AmagiItem extends BasePartsItem.WeaponArm {
   public AmagiItem(Properties properties) {
      super(properties);
      SingletonGeoAnimatable.registerSyncedAnimatable(this);
   }

   @Override
   public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
      Level level = mechInterface.getWorld();
      if (isOnFire && !level.f_46443_) {
         Pmvc01Entity mech = mechInterface.getMechEntity();
         double forwardX = -Math.sin(Math.toRadians(mech.m_146908_()));
         double forwardZ = Math.cos(Math.toRadians(mech.m_146908_()));
         BlockPos origin = mech.m_20183_().m_7918_((int)(forwardX * 5.0), 0, (int)(forwardZ * 5.0));
         int rangeX = 5;
         int rangeY = 15;
         int rangeZ = 5;
         if (mech.getDrivingPassenger() instanceof Player player) {
            BlockState blockState = this.getMainHandBlock(player);
            if (blockState != null && this.isAvailableBlock(blockState)) {
               Block blockToPlace = blockState.m_60734_();
               List<ItemStack> stacks = Utils.getItemStacks(player.m_21205_(), player, rangeX * rangeZ);
               if (!PomkotsMechs.CONFIG.consumeBlocksWhenPlacing || stacks != null || player.m_150110_().f_35937_) {
                  int consumedBlocks = 0;

                  for (int x = -rangeX / 2; x <= rangeX / 2; x++) {
                     for (int z = -rangeZ / 2; z <= rangeZ / 2; z++) {
                        BlockPos targetPos = origin.m_7918_(x, -1, z);
                        BlockPos a = new BlockPos(targetPos.m_123341_(), targetPos.m_123342_(), targetPos.m_123343_());
                        BlockState bs = level.m_8055_(a);
                        if (!blockState.equals(bs)) {
                           Utils.setBlock(level, a, blockToPlace.m_49966_(), 3);
                           consumedBlocks++;
                        }
                     }
                  }

                  if (PomkotsMechs.CONFIG.consumeBlocksWhenPlacing && !player.m_150110_().f_35937_ && stacks != null) {
                     Utils.consumeItemStackFromTop(stacks, consumedBlocks);
                  }
               }
            }
         }
      }
   }

   private BlockState getMainHandBlock(LivingEntity player) {
      return player != null && player.m_21205_().m_41720_() instanceof BlockItem blockItem ? blockItem.m_40614_().m_49966_() : null;
   }

   private boolean isAvailableBlock(BlockState bs) {
      return !(bs.m_60734_() instanceof ShulkerBoxBlock);
   }

   @Override
   public void startUsing(ActionWeapon.WeaponMechInterface context) {
      if (!context.getWorld().f_46443_ && context.getPlayer() != null && context.getItemStack() != null) {
         this.triggerAnim(context.getPlayer(), GeoItem.getOrAssignId(context.getItemStack(), (ServerLevel)context.getWorld()), "Activation", "use");
      }
   }

   @Override
   public void endUsing(ActionWeapon.WeaponMechInterface context) {
      if (!context.getWorld().f_46443_ && context.getPlayer() != null && context.getItemStack() != null) {
         this.triggerAnim(context.getPlayer(), GeoItem.getOrAssignId(context.getItemStack(), (ServerLevel)context.getWorld()), "Activation", "stop");
      }
   }

   public void m_6883_(ItemStack stack, Level level, Entity ent, int slotIndex, boolean bl) {
      super.m_6883_(stack, level, ent, slotIndex, bl);
      if (!level.f_46443_ && level instanceof ServerLevel serverLevel) {
         GeoItem.getOrAssignId(stack, serverLevel);
      }
   }

   @Override
   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "Activation", 0, state -> PlayState.STOP)
               .triggerableAnim("use", RawAnimation.begin().thenPlay("animation.weapon.use").thenLoop("animation.weapon.use_loop"))
               .triggerableAnim("stop", RawAnimation.begin().thenPlay("animation.weapon.idle"))
         }
      );
   }

   public AmagiItemRenderer newRenderer() {
      return new AmagiItemRenderer();
   }

   @Override
   public BasePartsItem.WeaponInterface.WeaponAttachPoint getWeaponAttachPoint() {
      return BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_ARM;
   }

   @Override
   public BasePartsItem.WeaponCategory getWeaponCategory() {
      return BasePartsItem.WeaponCategory.MISC;
   }

   @Override
   public Motion getMotion() {
      return Motion.ROAD_ROLLER;
   }

   @Override
   public int getCoolTime() {
      return 60;
   }

   @Override
   public String getPartsSeriesName() {
      return "amagi";
   }
}
