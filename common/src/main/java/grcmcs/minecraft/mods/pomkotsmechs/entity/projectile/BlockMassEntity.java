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
   private static final EntityDataAccessor<String> BLOCK_REGISTRY_NAME = SynchedEntityData.m_135353_(BlockMassEntity.class, EntityDataSerializers.f_135030_);
   private BlockState cachedBlockState;

   public static Builder createMobAttributes() {
      return LivingEntity.m_21183_().m_22266_(Attributes.f_22282_).m_22268_(Attributes.f_22276_, 50.0);
   }

   public BlockMassEntity(EntityType<? extends BlockMassEntity> type, Level level) {
      super(type, level);
      this.m_20242_(false);
   }

   public void m_8119_() {
      super.m_8119_();
      if (!this.m_20068_()) {
         this.m_246865_(new Vec3(0.0, -0.08, 0.0));
         this.m_6478_(MoverType.SELF, this.m_20184_());
         this.m_20256_(this.m_20184_().m_82542_(0.98, 0.98, 0.98));
      }

      if (this.m_20096_()) {
         if (!this.m_9236_().f_46443_) {
            this.placeBlocks();
            this.m_146870_();
         } else {
            this.m_9236_()
               .m_7785_(this.m_20185_(), this.m_20186_(), this.m_20189_(), (SoundEvent)PomkotsMechs.SE_PLACE.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
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
      Level serverLevel = this.m_9236_();
      BlockPos basePos = this.m_20183_();

      for (int x = 0; x < 4; x++) {
         for (int y = 0; y < 4; y++) {
            for (int z = 0; z < 4; z++) {
               BlockState state = this.blockStates[x][y][z];
               if (state != null) {
                  BlockPos pos = basePos.m_7918_(x - 2, y, z - 2);
                  serverLevel.m_7731_(pos, state, 3);
               }
            }
         }
      }
   }

   public void initializeBlocksServer(BlockState blockState) {
      this.initializeBlocks(blockState);
      this.setBlock(blockState.m_60734_());
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

   protected void m_8097_() {
      super.m_8097_();
      this.f_19804_.m_135372_(BLOCK_REGISTRY_NAME, "minecraft:air");
   }

   public void setBlock(Block block) {
      this.f_19804_.m_135381_(BLOCK_REGISTRY_NAME, BuiltInRegistries.f_256975_.m_7981_(block).toString());
      this.cachedBlockState = block.m_49966_();
   }

   public BlockState getBlockState() {
      if (this.cachedBlockState == null) {
         String registryName = (String)this.f_19804_.m_135370_(BLOCK_REGISTRY_NAME);
         Block block = (Block)BuiltInRegistries.f_256975_.m_7745_(new ResourceLocation(registryName));
         this.cachedBlockState = block != null ? block.m_49966_() : Blocks.f_50016_.m_49966_();
      }

      return this.cachedBlockState;
   }

   public Iterable<ItemStack> m_6168_() {
      return NonNullList.m_122780_(4, ItemStack.f_41583_);
   }

   public ItemStack m_6844_(EquipmentSlot equipmentSlot) {
      return ItemStack.f_41583_;
   }

   public void m_8061_(EquipmentSlot equipmentSlot, ItemStack itemStack) {
   }

   public HumanoidArm m_5737_() {
      return null;
   }
}
