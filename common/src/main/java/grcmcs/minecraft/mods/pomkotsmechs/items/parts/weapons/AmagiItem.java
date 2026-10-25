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
      if (isOnFire && !level.isClientSide) {
         Pmvc01Entity mech = mechInterface.getMechEntity();
         double forwardX = -Math.sin(Math.toRadians(mech.getYRot()));
         double forwardZ = Math.cos(Math.toRadians(mech.getYRot()));
         BlockPos origin = mech.blockPosition().offset((int)(forwardX * 5.0), 0, (int)(forwardZ * 5.0));
         int rangeX = 5;
         int rangeY = 15;
         int rangeZ = 5;
         if (mech.getDrivingPassenger() instanceof Player player) {
            BlockState blockState = this.getMainHandBlock(player);
            if (blockState != null && this.isAvailableBlock(blockState)) {
               Block blockToPlace = blockState.getBlock();
               List<ItemStack> stacks = Utils.getItemStacks(player.getMainHandItem(), player, rangeX * rangeZ);
               if (!PomkotsMechs.CONFIG.consumeBlocksWhenPlacing || stacks != null || player.getAbilities().instabuild) {
                  int consumedBlocks = 0;

                  for (int x = -rangeX / 2; x <= rangeX / 2; x++) {
                     for (int z = -rangeZ / 2; z <= rangeZ / 2; z++) {
                        BlockPos targetPos = origin.offset(x, -1, z);
                        BlockPos a = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                        BlockState bs = level.getBlockState(a);
                        if (!blockState.equals(bs)) {
                           Utils.setBlock(level, a, blockToPlace.defaultBlockState(), 3);
                           consumedBlocks++;
                        }
                     }
                  }

                  if (PomkotsMechs.CONFIG.consumeBlocksWhenPlacing && !player.getAbilities().instabuild && stacks != null) {
                     Utils.consumeItemStackFromTop(stacks, consumedBlocks);
                  }
               }
            }
         }
      }
   }

   private BlockState getMainHandBlock(LivingEntity player) {
      return player != null && player.getMainHandItem().getItem() instanceof BlockItem blockItem ? blockItem.getBlock().defaultBlockState() : null;
   }

   private boolean isAvailableBlock(BlockState bs) {
      return !(bs.getBlock() instanceof ShulkerBoxBlock);
   }

   @Override
   public void startUsing(ActionWeapon.WeaponMechInterface context) {
      if (!context.getWorld().isClientSide && context.getPlayer() != null && context.getItemStack() != null) {
         this.triggerAnim(context.getPlayer(), GeoItem.getOrAssignId(context.getItemStack(), (ServerLevel)context.getWorld()), "Activation", "use");
      }
   }

   @Override
   public void endUsing(ActionWeapon.WeaponMechInterface context) {
      if (!context.getWorld().isClientSide && context.getPlayer() != null && context.getItemStack() != null) {
         this.triggerAnim(context.getPlayer(), GeoItem.getOrAssignId(context.getItemStack(), (ServerLevel)context.getWorld()), "Activation", "stop");
      }
   }

   public void inventoryTick(ItemStack stack, Level level, Entity ent, int slotIndex, boolean bl) {
      super.inventoryTick(stack, level, ent, slotIndex, bl);
      if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
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
