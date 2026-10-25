package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BlockMassEntity extends LivingEntity {
   private static final int SIZE = 4;
   private BlockState[][][] blockStates = new BlockState[4][4][4];
   private boolean isInitialized = false;
   private static final EntityDataAccessor<String> BLOCK_REGISTRY_NAME = SynchedEntityData.defineId(BlockMassEntity.class, EntityDataSerializers.STRING);
   private BlockState cachedBlockState;

   public static Builder createMobAttributes() {
      return LivingEntity.createLivingAttributes().add(Attributes.ATTACK_KNOCKBACK).add(Attributes.MAX_HEALTH, 50.0);
   }

   public BlockMassEntity(EntityType<? extends BlockMassEntity> type, Level level) {
      super(type, level);
      this.setNoGravity(false);
   }

   public void tick() {
      super.tick();
      if (!this.isNoGravity()) {
         this.addDeltaMovement(new Vec3(0.0, -0.08, 0.0));
         this.move(MoverType.SELF, this.getDeltaMovement());
         this.setDeltaMovement(this.getDeltaMovement().multiply(0.98, 0.98, 0.98));
      }

      if (this.onGround()) {
         if (!this.level().isClientSide) {
            this.placeBlocks();
            this.discard();
         } else {
            this.level()
               .playLocalSound(this.getX(), this.getY(), this.getZ(), (SoundEvent)PomkotsMechs.SE_PLACE.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
         }
      }
   }

   public BlockState[][][] getBlockStates() {
      if (!this.isInitialized) {
         this.initializeBlocks(this.getBlockState());
      }

      return this.blockStates;
   }

   private void placeBlocks() {
      Level serverLevel = this.level();
      BlockPos basePos = this.blockPosition();

      for (int x = 0; x < 4; x++) {
         for (int y = 0; y < 4; y++) {
            for (int z = 0; z < 4; z++) {
               BlockState state = this.blockStates[x][y][z];
               if (state != null) {
                  BlockPos pos = basePos.offset(x - 2, y, z - 2);
                  serverLevel.setBlock(pos, state, 3);
               }
            }
         }
      }
   }

   public void initializeBlocksServer(BlockState blockState) {
      this.initializeBlocks(blockState);
      this.setBlock(blockState.getBlock());
   }

   public void initializeBlocks(BlockState blockState) {
      for (int x = 0; x < 4; x++) {
         for (int y = 0; y < 4; y++) {
            for (int z = 0; z < 4; z++) {
               this.blockStates[x][y][z] = blockState;
            }
         }
      }

      this.isInitialized = true;
   }

   protected void defineSynchedData() {
      super.defineSynchedData();
      this.entityData.define(BLOCK_REGISTRY_NAME, "minecraft:air");
   }

   public void setBlock(Block block) {
      this.entityData.set(BLOCK_REGISTRY_NAME, BuiltInRegistries.BLOCK.getKey(block).toString());
      this.cachedBlockState = block.defaultBlockState();
   }

   public BlockState getBlockState() {
      if (this.cachedBlockState == null) {
         String registryName = (String)this.entityData.get(BLOCK_REGISTRY_NAME);
         Block block = (Block)BuiltInRegistries.BLOCK.get(new ResourceLocation(registryName));
         this.cachedBlockState = block != null ? block.defaultBlockState() : Blocks.AIR.defaultBlockState();
      }

      return this.cachedBlockState;
   }

   public Iterable<ItemStack> getArmorSlots() {
      return NonNullList.withSize(4, ItemStack.EMPTY);
   }

   public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
      return ItemStack.EMPTY;
   }

   public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
   }

   public HumanoidArm getMainArm() {
      return null;
   }
}
