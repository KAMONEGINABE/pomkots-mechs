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
   public void m_8119_() {
      super.m_8119_();
      if (this.isServerSide() && this.heldBlockMassEntity != null) {
         Vec3 offset = this.m_20182_();
         this.heldBlockMassEntity.m_6034_(offset.f_82479_, offset.f_82480_ + 6.0, offset.f_82481_);
         this.heldBlockMassEntity.f_19812_ = true;
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
            this.fireThrowBlock(this.m_9236_(), false);
            this.isHoldingBlockMassEntity = false;
            this.actionController.getAction(9).currentCoolTime = 20;
         }
      } else if (driverInput.isWeaponLeftShoulderPressed()) {
         if (this.isHoldingBlockMassEntity) {
            this.actionController.getAction(10).startAction();
            this.fireThrowBlock(this.m_9236_(), true);
            this.isHoldingBlockMassEntity = false;
            this.actionController.getAction(9).currentCoolTime = 20;
         } else {
            this.actionController.getAction(8).startAction();
         }
      }
   }

   @Override
   protected void fireWeapons() {
      Level level = this.m_9236_();
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
         ItemStack mainHand = player.m_21205_();
         if (mainHand.m_41720_() instanceof BlockItem blockItem) {
            if (!this.isAvailableBlock(blockItem.m_40614_().m_49966_())) {
               return;
            }

            Block block = blockItem.m_40614_();
            BlockProjectileEntity blockEntity = new BlockProjectileEntity(
               (EntityType<? extends BlockProjectileEntity>)PomkotsMechs.BLOCK_PROJECTILE.get(), player.m_9236_()
            );
            blockEntity.setBlock(block);
            Vec3 offset = this.posHistory.getFirst();
            Vec3 muzzlPos = new Vec3(-1.65, 2.0, 3.5);
            muzzlPos = muzzlPos.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
            blockEntity.m_146884_(offset.m_82549_(muzzlPos));
            float[] angle = this.getShootAngleNolock(player);
            blockEntity.m_37251_(blockEntity, angle[0], angle[1], this.m_21256_(), 1.2F, 0.0F);
            player.m_9236_().m_7967_(blockEntity);
            if (PomkotsMechs.CONFIG.consumeBlocksWhenPlacing) {
               mainHand.m_41774_(1);
            }
         }
      }
   }

   public void fireVacume(Level level) {
      if (this.isServerSide() && this.getDrivingPassenger() instanceof Player player) {
         BlockHitResult hitResult = this.raycastBlock(player, true);
         if (hitResult.m_6662_() == Type.BLOCK
            && level.m_8055_(hitResult.m_82425_()).m_60734_() instanceof LiquidBlock liquidBlock
            && (liquidBlock == Blocks.f_49990_ || liquidBlock == Blocks.f_49991_)) {
            BlockPos offset = hitResult.m_82425_();

            for (int x = 0; x < 3; x++) {
               for (int z = 0; z < 3; z++) {
                  BlockPos pos = new BlockPos(offset.m_123341_() + x - 1, offset.m_123342_(), offset.m_123343_() + z - 1);
                  BlockState bs = level.m_8055_(pos);
                  Block b = bs.m_60734_();
                  if (b == Blocks.f_49990_ || b == Blocks.f_49991_) {
                     level.m_7731_(pos, Blocks.f_50016_.m_49966_(), 3);
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
            List<ItemStack> stacks = this.getItemStacks(player.m_21205_(), player, 64);
            if (!PomkotsMechs.CONFIG.consumeBlocksWhenPlacing || stacks != null) {
               BlockMassEntity blockMass = new BlockMassEntity((EntityType<? extends BlockMassEntity>)PomkotsMechs.BLOCK_MASS.get(), this.m_9236_());
               blockMass.initializeBlocksServer(blockState);
               blockMass.m_6034_(this.m_20185_(), this.m_20186_() + 6.0, this.m_20189_());
               blockMass.m_20242_(true);
               this.m_9236_().m_7967_(blockMass);
               this.heldBlockMassEntity = blockMass;
               if (PomkotsMechs.CONFIG.consumeBlocksWhenPlacing) {
                  this.consumeItemStackFromTop(stacks, 64);
               }
            }
         }
      }
   }

   private BlockState getMainHandBlock(LivingEntity player) {
      return player != null && player.m_21205_() != null && player.m_21205_().m_41720_() instanceof BlockItem blockItem
         ? blockItem.m_40614_().m_49966_()
         : null;
   }

   private List<ItemStack> getItemStacks(ItemStack mainHandStack, Player player, int totalAmountToConsume) {
      List<ItemStack> stacks = new LinkedList<>();
      int remainingAmount = totalAmountToConsume;
      Item mainHandItem = mainHandStack.m_41720_();

      for (ItemStack stack : player.m_150109_().f_35974_) {
         if (stack != mainHandStack && stack.m_41720_() == mainHandItem) {
            stacks.add(stack);
            int stackCount = stack.m_41613_();
            if (stackCount > remainingAmount) {
               return stacks;
            }

            remainingAmount -= stackCount;
         }
      }

      if (remainingAmount > 0) {
         stacks.add(mainHandStack);
         int mainHandCount = mainHandStack.m_41613_();
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
         if (stack.m_41613_() > num) {
            stack.m_41764_(stack.m_41613_() - num);
            return;
         }

         num -= stack.m_41613_();
         stack.m_41764_(0);
      }
   }

   private void fireThrowBlock(Level level, boolean vertical) {
      if (this.heldBlockMassEntity != null && this.isServerSide()) {
         if (vertical) {
            this.heldBlockMassEntity.m_20256_(new Vec3(0.0, -1.0, 0.0));
         } else {
            this.heldBlockMassEntity.m_20256_(this.m_20154_().m_82490_(1.5));
         }

         this.heldBlockMassEntity.m_20242_(false);
         this.heldBlockMassEntity = null;
      }
   }

   private float[] getShootAngleNolock(LivingEntity driver) {
      Vec3 lookAngle = driver.m_20154_();
      double xRot = -Math.toDegrees(Math.asin(lookAngle.f_82480_)) - 6.5;
      double yRot = Math.toDegrees(Math.atan2(lookAngle.f_82481_, lookAngle.f_82479_)) - 95.0;
      return new float[]{(float)xRot, (float)yRot};
   }

   public void fireNeedle(Level level) {
      LivingEntity player = this.getDrivingPassenger();
      if (this.isServerSide() && player != null) {
         BlockHitResult hitResult = this.raycastBlock(player, false);
         if (hitResult.m_6662_() == Type.BLOCK) {
            BlockPos blockPos = hitResult.m_82425_();
            Utils.destroyBlock(level, blockPos, PomkotsMechs.CONFIG.dropItemsWhenDestroyBlock);
         }
      }
   }

   private BlockHitResult raycastBlock(LivingEntity player, boolean targetFluid) {
      Vec3 eyePosition = player.m_20299_(1.0F);
      eyePosition = new Vec3(eyePosition.f_82479_, eyePosition.f_82480_ + 1.0, eyePosition.f_82481_);
      Vec3 lookDirection = player.m_20154_();
      Vec3 rayEnd = eyePosition.m_82549_(lookDirection.m_82490_(40.0));
      return this.m_9236_()
         .m_45547_(
            new ClipContext(eyePosition, rayEnd, net.minecraft.world.level.ClipContext.Block.OUTLINE, targetFluid ? Fluid.SOURCE_ONLY : Fluid.NONE, player)
         );
   }

   public void fireDrill(Level level) {
      if (this.isServerSide() && this.f_19797_ % 10 == 0 && this.getDrivingPassenger() != null) {
         this.breakBlockSphere(5, this.getOffsetByLookingDirection(this.getDrivingPassenger(), 5));
      }
   }

   public boolean isDrilling() {
      return this.actionController.getAction(3).isInAction();
   }

   public Vec3 getOffsetByLookingDirection(LivingEntity entity, int distance) {
      Vec3 lookDirection = entity.m_20154_();
      Vec3 playerPosition = new Vec3(0.0, 4.0, 0.0);
      return playerPosition.m_82520_(0.0, lookDirection.f_82480_ * distance, (1.0 - Math.abs(lookDirection.f_82480_)) * distance);
   }

   public void fireRollerPlace(Level level, boolean place) {
      if (this.isServerSide() && this.f_19797_ % 4 == 0) {
         double forwardX = -Math.sin(Math.toRadians(this.m_146908_()));
         double forwardZ = Math.cos(Math.toRadians(this.m_146908_()));
         BlockPos origin = this.m_20183_().m_7918_((int)(forwardX * 5.0), 0, (int)(forwardZ * 5.0));
         int rangeX = 7;
         int rangeY = 15;
         int rangeZ = 7;
         if (!place) {
            for (int x = -rangeX / 2; x <= rangeX / 2; x++) {
               for (int y = 0; y <= rangeY; y++) {
                  for (int z = -rangeZ / 2; z <= rangeZ / 2; z++) {
                     BlockPos targetPos = origin.m_7918_(x, y, z);
                     if (!level.m_46859_(targetPos)) {
                        Utils.destroyBlock(level, targetPos, PomkotsMechs.CONFIG.dropItemsWhenDestroyBlock);
                     }
                  }
               }
            }
         } else if (this.getDrivingPassenger() instanceof Player player) {
            BlockState blockState = this.getMainHandBlock(player);
            if (blockState != null && this.isAvailableBlock(blockState)) {
               Block blockToPlace = blockState.m_60734_();
               List<ItemStack> stacks = this.getItemStacks(player.m_21205_(), player, rangeX * rangeZ);
               if (!PomkotsMechs.CONFIG.consumeBlocksWhenPlacing || stacks != null) {
                  int consumedBlocks = 0;

                  for (int x = -rangeX / 2; x <= rangeX / 2; x++) {
                     for (int z = -rangeZ / 2; z <= rangeZ / 2; z++) {
                        BlockPos targetPos = origin.m_7918_(x, -1, z);
                        BlockPos a = new BlockPos(targetPos.m_123341_(), targetPos.m_123342_(), targetPos.m_123343_());
                        BlockState bs = this.m_9236_().m_8055_(a);
                        if (!blockState.equals(bs)) {
                           Utils.setBlock(level, a, blockToPlace.m_49966_(), 3);
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
      if (!world.m_5776_()) {
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
      BlockPos curBP = this.m_20183_();
      Vec3 vec = new Vec3(0.0, 0.0, 5.0);
      vec = vec.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
      BlockPos pos = new BlockPos(curBP.m_123341_() + (int)vec.f_82479_, curBP.m_123342_() + (int)vec.f_82480_, curBP.m_123343_() + (int)vec.f_82481_);
      int height = 8;

      for (int y = 0; y < height; y++) {
         for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
               BlockPos blockPos = pos.m_7918_(x, y, z);
               BlockState state = this.m_9236_().m_8055_(blockPos);
               if (!state.m_60795_() && x * x + z * z <= radius * radius) {
                  Utils.destroyBlock(this.m_9236_(), blockPos, PomkotsMechs.CONFIG.dropItemsWhenDestroyBlock);
               }
            }
         }
      }
   }

   private void breakBlockSphere(int radius, Vec3 offset) {
      int radiusSquared = radius * radius;
      BlockPos curBP = this.m_20183_();
      Vec3 vec = offset;
      vec = vec.m_82524_((float)Math.toRadians(-1.0 * this.m_146908_()));
      BlockPos pos = new BlockPos(curBP.m_123341_() + (int)vec.f_82479_, curBP.m_123342_() + (int)vec.f_82480_, curBP.m_123343_() + (int)vec.f_82481_);

      for (int x = -radius; x <= radius; x++) {
         for (int y = -radius; y <= radius; y++) {
            for (int z = -radius; z <= radius; z++) {
               if (x * x + y * y + z * z <= radiusSquared) {
                  BlockPos targetPos = pos.m_7918_(x, y, z);
                  BlockState state = this.m_9236_().m_8055_(targetPos);
                  if (!state.m_60795_()) {
                     Utils.destroyBlock(this.m_9236_(), targetPos, PomkotsMechs.CONFIG.dropItemsWhenDestroyBlock);
                  }
               }
            }
         }
      }
   }

   private boolean isAvailableBlock(BlockState bs) {
      return !(bs.m_60734_() instanceof ShulkerBoxBlock);
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
