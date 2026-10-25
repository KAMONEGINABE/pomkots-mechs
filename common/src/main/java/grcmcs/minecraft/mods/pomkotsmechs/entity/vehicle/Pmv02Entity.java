package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.DriverInput;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BlockMassEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.projectile.BlockProjectileEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.Action;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.ActionController;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.keyframe.event.SoundKeyframeEvent;
import software.bernie.geckolib.core.object.PlayState;

public class Pmv02Entity extends PomkotsVehicleBase {
   protected static final int ACT_NEEDLE = 2;
   protected static final int ACT_DRILL = 3;
   protected static final int ACT_HUMMER = 4;
   protected static final int ACT_ROLLER = 5;
   protected static final int ACT_BLOCKINJECT = 6;
   protected static final int ACT_VACUME = 7;
   protected static final int ACT_PLACE = 8;
   protected static final int ACT_LIFT_BLOCK = 9;
   protected static final int ACT_THROW = 10;
   private int hummerChargeNum = 0;
   private boolean airialHummer = false;
   private BlockMassEntity heldBlockMassEntity = null;
   private static final double BLOCK_OFFSET_Y = 6.0;
   private boolean isHoldingBlockMassEntity = false;

   @Override
   protected String getMechName() {
      return "pmv02";
   }

   public Pmv02Entity(EntityType<? extends LivingEntity> entityType, Level world) {
      super(entityType, world);
   }

   @Override
   protected void registerActions() {
      super.registerActions();
      this.actionController.registerAction(2, new Action(0, 7, 2), ActionController.ActionType.R_ARM_MAIN);
      this.actionController.registerAction(3, new Action(0, 20, 2), ActionController.ActionType.L_ARM_MAIN);
      this.actionController.registerAction(4, new Action(20, 40, 20), ActionController.ActionType.R_SHL_MAIN);
      this.actionController.registerAction(5, new Action(0, 20, 2), ActionController.ActionType.L_SHL_MAIN);
      this.actionController.registerAction(6, new Action(0, 5, 5), ActionController.ActionType.R_ARM_SUB);
      this.actionController.registerAction(7, new Action(0, 5, 5), ActionController.ActionType.L_ARM_SUB);
      this.actionController.registerAction(9, new Action(20, 8, 2), ActionController.ActionType.R_SHL_SUB);
      this.actionController.registerAction(10, new Action(20, 3, 7), ActionController.ActionType.R_SHL_SUB);
      this.actionController.registerAction(8, new Action(0, 20, 2), ActionController.ActionType.L_SHL_SUB);
   }

   @Override
   public void tick() {
      super.tick();
      if (this.isServerSide() && this.heldBlockMassEntity != null) {
         Vec3 offset = this.position();
         this.heldBlockMassEntity.setPos(offset.x, offset.y + 6.0, offset.z);
         this.heldBlockMassEntity.hasImpulse = true;
      }
   }

   @Override
   protected void applyPlayerInputWeapons(DriverInput driverInput) {
      if (this.isMainMode()) {
         if (!driverInput.isWeaponRightShoulderPressed() && this.actionController.getAction(4).isCharging()) {
            this.actionController.getAction(4).fireAction();
            this.airialHummer = false;
         }

         if (driverInput.isWeaponRightHandPressed()) {
            this.actionController.getAction(2).startAction();
         } else if (driverInput.isWeaponLeftHandPressed()) {
            this.actionController.getAction(3).startAction();
         } else if (driverInput.isWeaponRightShoulderPressed()) {
            Action hAction = this.actionController.getAction(4);
            hAction.startAction();
            if (hAction.isCharging()) {
               this.hummerChargeNum = hAction.currentChargeTime;
               if (this.justLanded(this)) {
                  this.airialHummer = true;
                  hAction.fireAction();
               } else {
                  this.airialHummer = false;
               }
            }
         } else if (driverInput.isWeaponLeftShoulderPressed()) {
            this.actionController.getAction(5).startAction();
         }
      } else if (driverInput.isWeaponRightHandPressed()) {
         this.actionController.getAction(6).startAction();
      } else if (driverInput.isWeaponLeftHandPressed()) {
         this.actionController.getAction(7).startAction();
      } else if (driverInput.isWeaponRightShoulderPressed()) {
         if (!this.isHoldingBlockMassEntity) {
            this.actionController.getAction(9).startAction();
         } else {
            this.actionController.getAction(10).startAction();
            this.fireThrowBlock(this.level(), false);
            this.isHoldingBlockMassEntity = false;
            this.actionController.getAction(9).currentCoolTime = 20;
         }
      } else if (driverInput.isWeaponLeftShoulderPressed()) {
         if (this.isHoldingBlockMassEntity) {
            this.actionController.getAction(10).startAction();
            this.fireThrowBlock(this.level(), true);
            this.isHoldingBlockMassEntity = false;
            this.actionController.getAction(9).currentCoolTime = 20;
         } else {
            this.actionController.getAction(8).startAction();
         }
      }
   }

   @Override
   protected void fireWeapons() {
      Level level = this.level();
      if (this.actionController.getAction(2).isOnFire()) {
         this.fireNeedle(level);
      } else if (this.actionController.getAction(3).isOnFire()) {
         this.fireDrill(level);
      } else if (this.actionController.getAction(4).currentFireTime == 5) {
         this.fireHummer(level);
      } else if (this.actionController.getAction(5).isOnFire()) {
         this.fireRollerPlace(level, false);
      } else if (this.actionController.getAction(6).isOnFire()) {
         this.fireBlockInjection(level);
      } else if (this.actionController.getAction(7).isOnFire()) {
         this.fireVacume(level);
      } else if (this.actionController.getAction(9).isOnFire()) {
         this.fireLiftBlock(level);
      } else if (this.actionController.getAction(9).isOnEnd) {
         this.isHoldingBlockMassEntity = true;
      } else if (this.actionController.getAction(8).isOnFire()) {
         this.fireRollerPlace(level, true);
      }
   }

   public void fireBlockInjection(Level level) {
      if (this.isServerSide() && this.getDrivingPassenger() instanceof Player player) {
         ItemStack mainHand = player.getMainHandItem();
         if (mainHand.getItem() instanceof BlockItem blockItem) {
            if (!this.isAvailableBlock(blockItem.getBlock().defaultBlockState())) {
               return;
            }

            Block block = blockItem.getBlock();
            BlockProjectileEntity blockEntity = new BlockProjectileEntity(
               (EntityType<? extends BlockProjectileEntity>)PomkotsMechs.BLOCK_PROJECTILE.get(), player.level()
            );
            blockEntity.setBlock(block);
            Vec3 offset = this.posHistory.getFirst();
            Vec3 muzzlPos = new Vec3(-1.65, 2.0, 3.5);
            muzzlPos = muzzlPos.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
            blockEntity.setPos(offset.add(muzzlPos));
            float[] angle = this.getShootAngleNolock(player);
            blockEntity.shootFromRotation(blockEntity, angle[0], angle[1], this.getFallFlyingTicks(), 1.2F, 0.0F);
            player.level().addFreshEntity(blockEntity);
            if (PomkotsMechs.CONFIG.consumeBlocksWhenPlacing) {
               mainHand.shrink(1);
            }
         }
      }
   }

   public void fireVacume(Level level) {
      if (this.isServerSide() && this.getDrivingPassenger() instanceof Player player) {
         BlockHitResult hitResult = this.raycastBlock(player, true);
         if (hitResult.getType() == Type.BLOCK
            && level.getBlockState(hitResult.getBlockPos()).getBlock() instanceof LiquidBlock liquidBlock
            && (liquidBlock == Blocks.WATER || liquidBlock == Blocks.LAVA)) {
            BlockPos offset = hitResult.getBlockPos();

            for (int x = 0; x < 3; x++) {
               for (int z = 0; z < 3; z++) {
                  BlockPos pos = new BlockPos(offset.getX() + x - 1, offset.getY(), offset.getZ() + z - 1);
                  BlockState bs = level.getBlockState(pos);
                  Block b = bs.getBlock();
                  if (b == Blocks.WATER || b == Blocks.LAVA) {
                     level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                  }
               }
            }
         }
      }
   }

   private void fireLiftBlock(Level level) {
      if (this.isServerSide() && this.heldBlockMassEntity == null && this.getDrivingPassenger() instanceof Player player) {
         BlockState blockState = this.getMainHandBlock(player);
         if (blockState != null && this.isAvailableBlock(blockState)) {
            List<ItemStack> stacks = this.getItemStacks(player.getMainHandItem(), player, 64);
            if (!PomkotsMechs.CONFIG.consumeBlocksWhenPlacing || stacks != null) {
               BlockMassEntity blockMass = new BlockMassEntity((EntityType<? extends BlockMassEntity>)PomkotsMechs.BLOCK_MASS.get(), this.level());
               blockMass.initializeBlocksServer(blockState);
               blockMass.setPos(this.getX(), this.getY() + 6.0, this.getZ());
               blockMass.setNoGravity(true);
               this.level().addFreshEntity(blockMass);
               this.heldBlockMassEntity = blockMass;
               if (PomkotsMechs.CONFIG.consumeBlocksWhenPlacing) {
                  this.consumeItemStackFromTop(stacks, 64);
               }
            }
         }
      }
   }

   private BlockState getMainHandBlock(LivingEntity player) {
      return player != null && player.getMainHandItem() != null && player.getMainHandItem().getItem() instanceof BlockItem blockItem
         ? blockItem.getBlock().defaultBlockState()
         : null;
   }

   private List<ItemStack> getItemStacks(ItemStack mainHandStack, Player player, int totalAmountToConsume) {
      List<ItemStack> stacks = new LinkedList<>();
      int remainingAmount = totalAmountToConsume;
      Item mainHandItem = mainHandStack.getItem();

      for (ItemStack stack : player.getInventory().items) {
         if (stack != mainHandStack && stack.getItem() == mainHandItem) {
            stacks.add(stack);
            int stackCount = stack.getCount();
            if (stackCount > remainingAmount) {
               return stacks;
            }

            remainingAmount -= stackCount;
         }
      }

      if (remainingAmount > 0) {
         stacks.add(mainHandStack);
         int mainHandCount = mainHandStack.getCount();
         if (mainHandCount <= remainingAmount) {
            remainingAmount -= mainHandCount;
         } else {
            remainingAmount = 0;
         }
      }

      return remainingAmount > 0 ? null : stacks;
   }

   private void consumeItemStackFromTop(List<ItemStack> stacks, int num) {
      for (ItemStack stack : stacks) {
         if (stack.getCount() > num) {
            stack.setCount(stack.getCount() - num);
            return;
         }

         num -= stack.getCount();
         stack.setCount(0);
      }
   }

   private void fireThrowBlock(Level level, boolean vertical) {
      if (this.heldBlockMassEntity != null && this.isServerSide()) {
         if (vertical) {
            this.heldBlockMassEntity.setDeltaMovement(new Vec3(0.0, -1.0, 0.0));
         } else {
            this.heldBlockMassEntity.setDeltaMovement(this.getLookAngle().scale(1.5));
         }

         this.heldBlockMassEntity.setNoGravity(false);
         this.heldBlockMassEntity = null;
      }
   }

   private float[] getShootAngleNolock(LivingEntity driver) {
      Vec3 lookAngle = driver.getLookAngle();
      double xRot = -Math.toDegrees(Math.asin(lookAngle.y)) - 6.5;
      double yRot = Math.toDegrees(Math.atan2(lookAngle.z, lookAngle.x)) - 95.0;
      return new float[]{(float)xRot, (float)yRot};
   }

   public void fireNeedle(Level level) {
      LivingEntity player = this.getDrivingPassenger();
      if (this.isServerSide() && player != null) {
         BlockHitResult hitResult = this.raycastBlock(player, false);
         if (hitResult.getType() == Type.BLOCK) {
            BlockPos blockPos = hitResult.getBlockPos();
            Utils.destroyBlock(level, blockPos, PomkotsMechs.CONFIG.dropItemsWhenDestroyBlock);
         }
      }
   }

   private BlockHitResult raycastBlock(LivingEntity player, boolean targetFluid) {
      Vec3 eyePosition = player.getEyePosition(1.0F);
      eyePosition = new Vec3(eyePosition.x, eyePosition.y + 1.0, eyePosition.z);
      Vec3 lookDirection = player.getLookAngle();
      Vec3 rayEnd = eyePosition.add(lookDirection.scale(40.0));
      return this.level()
         .clip(
            new ClipContext(eyePosition, rayEnd, net.minecraft.world.level.ClipContext.Block.OUTLINE, targetFluid ? Fluid.SOURCE_ONLY : Fluid.NONE, player)
         );
   }

   public void fireDrill(Level level) {
      if (this.isServerSide() && this.tickCount % 10 == 0 && this.getDrivingPassenger() != null) {
         this.breakBlockSphere(5, this.getOffsetByLookingDirection(this.getDrivingPassenger(), 5));
      }
   }

   public boolean isDrilling() {
      return this.actionController.getAction(3).isInAction();
   }

   public Vec3 getOffsetByLookingDirection(LivingEntity entity, int distance) {
      Vec3 lookDirection = entity.getLookAngle();
      Vec3 playerPosition = new Vec3(0.0, 4.0, 0.0);
      return playerPosition.add(0.0, lookDirection.y * distance, (1.0 - Math.abs(lookDirection.y)) * distance);
   }

   public void fireRollerPlace(Level level, boolean place) {
      if (this.isServerSide() && this.tickCount % 4 == 0) {
         double forwardX = -Math.sin(Math.toRadians(this.getYRot()));
         double forwardZ = Math.cos(Math.toRadians(this.getYRot()));
         BlockPos origin = this.blockPosition().offset((int)(forwardX * 5.0), 0, (int)(forwardZ * 5.0));
         int rangeX = 7;
         int rangeY = 15;
         int rangeZ = 7;
         if (!place) {
            for (int x = -rangeX / 2; x <= rangeX / 2; x++) {
               for (int y = 0; y <= rangeY; y++) {
                  for (int z = -rangeZ / 2; z <= rangeZ / 2; z++) {
                     BlockPos targetPos = origin.offset(x, y, z);
                     if (!level.isEmptyBlock(targetPos)) {
                        Utils.destroyBlock(level, targetPos, PomkotsMechs.CONFIG.dropItemsWhenDestroyBlock);
                     }
                  }
               }
            }
         } else if (this.getDrivingPassenger() instanceof Player player) {
            BlockState blockState = this.getMainHandBlock(player);
            if (blockState != null && this.isAvailableBlock(blockState)) {
               Block blockToPlace = blockState.getBlock();
               List<ItemStack> stacks = this.getItemStacks(player.getMainHandItem(), player, rangeX * rangeZ);
               if (!PomkotsMechs.CONFIG.consumeBlocksWhenPlacing || stacks != null) {
                  int consumedBlocks = 0;

                  for (int x = -rangeX / 2; x <= rangeX / 2; x++) {
                     for (int z = -rangeZ / 2; z <= rangeZ / 2; z++) {
                        BlockPos targetPos = origin.offset(x, -1, z);
                        BlockPos a = new BlockPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
                        BlockState bs = this.level().getBlockState(a);
                        if (!blockState.equals(bs)) {
                           Utils.setBlock(level, a, blockToPlace.defaultBlockState(), 3);
                           consumedBlocks++;
                        }
                     }
                  }

                  if (PomkotsMechs.CONFIG.consumeBlocksWhenPlacing) {
                     this.consumeItemStackFromTop(stacks, consumedBlocks);
                  }
               }
            }
         }
      }
   }

   private void fireHummer(Level world) {
      if (!world.isClientSide()) {
         int rad = 0;
         byte var3;
         if (this.hummerChargeNum < 10) {
            var3 = 5;
         } else if (this.hummerChargeNum < 39) {
            var3 = 10;
         } else {
            var3 = 15;
         }

         if (!this.airialHummer) {
            this.breakBlocksCube(var3);
         } else {
            this.breakBlockSphere(var3, new Vec3(0.0, 0.0, 5.0));
         }
      }
   }

   private void breakBlocksCube(int radius) {
      BlockPos curBP = this.blockPosition();
      Vec3 vec = new Vec3(0.0, 0.0, 5.0);
      vec = vec.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
      BlockPos pos = new BlockPos(curBP.getX() + (int)vec.x, curBP.getY() + (int)vec.y, curBP.getZ() + (int)vec.z);
      int height = 8;

      for (int y = 0; y < height; y++) {
         for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
               BlockPos blockPos = pos.offset(x, y, z);
               BlockState state = this.level().getBlockState(blockPos);
               if (!state.isAir() && x * x + z * z <= radius * radius) {
                  Utils.destroyBlock(this.level(), blockPos, PomkotsMechs.CONFIG.dropItemsWhenDestroyBlock);
               }
            }
         }
      }
   }

   private void breakBlockSphere(int radius, Vec3 offset) {
      int radiusSquared = radius * radius;
      BlockPos curBP = this.blockPosition();
      Vec3 vec = offset;
      vec = vec.yRot((float)Math.toRadians(-1.0 * this.getYRot()));
      BlockPos pos = new BlockPos(curBP.getX() + (int)vec.x, curBP.getY() + (int)vec.y, curBP.getZ() + (int)vec.z);

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
               if (x * x + y * y + z * z <= radiusSquared) {
                  BlockPos targetPos = pos.offset(x, y, z);
                  BlockState state = this.level().getBlockState(targetPos);
                  if (!state.isAir()) {
                     Utils.destroyBlock(this.level(), targetPos, PomkotsMechs.CONFIG.dropItemsWhenDestroyBlock);
                  }
               }
            }
         }
      }
   }

   private boolean isAvailableBlock(BlockState bs) {
      return !(bs.getBlock() instanceof ShulkerBoxBlock);
   }

   @Override
   protected PlayState controllAnimationWeapons(AnimationState<PomkotsVehicleBase> event) {
      if (this.actionController.getAction(9).isInAction()) {
         if (this.actionController.getAction(9).isOnStart()) {
            event.getController().forceAnimationReset();
         }

         return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".raise"));
      } else if (this.actionController.getAction(10).isInAction()) {
         if (this.actionController.getAction(10).isOnStart()) {
            event.getController().forceAnimationReset();
         }

         return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".throw"));
      } else {
         return null;
      }
   }

   @Override
   protected void addExtraAnimationController(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(
                  this,
                  "continuous",
                  0,
                  event -> {
                     if (this.isHoldingBlockMassEntity) {
                        return event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".keep"));
                     }

                     if (this.actionController.getAction(2).isInAction()) {
                        if (this.actionController.getAction(2).isOnStart()) {
                           event.getController().forceAnimationReset();
                        }

                        return !this.actionController.getAction(2).isInFire()
                           ? event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".gatringgun1"))
                           : event.setAndContinue(RawAnimation.begin().thenLoop("animation." + this.getMechName() + ".gatringgun2"));
                     } else if (this.actionController.getAction(3).isInAction()) {
                        if (this.actionController.getAction(3).isOnStart()) {
                           event.getController().forceAnimationReset();
                        }

                        return !this.actionController.getAction(3).isInFire()
                           ? event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".drill1"))
                           : event.setAndContinue(RawAnimation.begin().thenLoop("animation." + this.getMechName() + ".drill2"));
                     } else if (this.actionController.getAction(4).isInAction()) {
                        if (this.actionController.getAction(4).isOnStart()) {
                           event.getController().forceAnimationReset();
                        }

                        return !this.actionController.getAction(4).isInFire()
                           ? event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".hummer1"))
                           : event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".hummer2"));
                     } else if (this.actionController.getAction(5).isInAction()) {
                        if (this.actionController.getAction(5).isOnStart()) {
                           event.getController().forceAnimationReset();
                        }

                        return !this.actionController.getAction(5).isInFire()
                           ? event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".roller1"))
                           : event.setAndContinue(RawAnimation.begin().thenLoop("animation." + this.getMechName() + ".roller2"));
                     } else if (this.actionController.getAction(8).isInAction()) {
                        if (this.actionController.getAction(8).isOnStart()) {
                           event.getController().forceAnimationReset();
                        }

                        return !this.actionController.getAction(8).isInFire()
                           ? event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".roller1"))
                           : event.setAndContinue(RawAnimation.begin().thenLoop("animation." + this.getMechName() + ".roller2"));
                     } else if (this.actionController.getAction(6).isInAction()) {
                        if (this.actionController.getAction(6).isOnStart()) {
                           event.getController().forceAnimationReset();
                        }

                        return !this.actionController.getAction(6).isInFire()
                           ? event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".blockinjection"))
                           : event.setAndContinue(RawAnimation.begin().thenLoop("animation." + this.getMechName() + ".blockinjection_keep"));
                     } else if (this.actionController.getAction(7).isInAction()) {
                        if (this.actionController.getAction(7).isOnStart()) {
                           event.getController().forceAnimationReset();
                        }

                        return !this.actionController.getAction(7).isInFire()
                           ? event.setAndContinue(RawAnimation.begin().thenPlay("animation." + this.getMechName() + ".vacum"))
                           : event.setAndContinue(RawAnimation.begin().thenLoop("animation." + this.getMechName() + ".vacum_keep"));
                     } else {
                        return event.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation." + this.getMechName() + ".nop"));
                     }
                  }
               )
               .setSoundKeyframeHandler(soundKeyframeEvent -> this.registerAnimationSoundHandlers(soundKeyframeEvent))
         }
      );
   }

   @Override
   protected void registerAnimationSoundHandlers(SoundKeyframeEvent event) {
      if ("se_jump".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_JUMP_EVENT.get());
      } else if ("se_booster".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_BOOSTER_EVENT.get());
      } else if ("se_onground".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_JUMP_EVENT.get());
      } else if ("se_drill1".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_DRILL1.get());
      } else if ("se_drill2".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_DRILL2.get());
      } else if ("se_hummer1".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_HUMMER1.get());
      } else if ("se_hummer2".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_HUMMER2.get());
      } else if ("se_lift".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_LIFT.get());
      } else if ("se_needle".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_NEEDLE.get());
      } else if ("se_roller1".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_ROLLER1.get());
      } else if ("se_roller2".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_ROLLER2.get());
      } else if ("se_step".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_STEP.get());
      } else if ("se_throw".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_THROW.get());
      } else if ("se_water".equals(event.getKeyframeData().getSound())) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_WATER.get());
      }
   }

   @Override
   public boolean shouldRenderDefaultHud(String hudName) {
      return "renderHotbar".equals(hudName);
   }
}
