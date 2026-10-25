package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BlockProjectileEntity extends PomkotsThrowableProjectile {
   private static final int MAX_LIFE_TICKS = 40;
   private int lifeTicks = 0;
   private static final EntityDataAccessor<String> BLOCK_REGISTRY_NAME = SynchedEntityData.m_135353_(
      BlockProjectileEntity.class, EntityDataSerializers.f_135030_
   );
   private BlockState cachedBlockState;

   public BlockProjectileEntity(EntityType<? extends BlockProjectileEntity> type, Level world) {
      super(type, null, world);
      this.m_20242_(true);
   }

   @Override
   public void m_8119_() {
      super.m_8119_();
      if (this.lifeTicks++ >= 40) {
         this.m_146870_();
      }
   }

   protected void m_8060_(BlockHitResult hitResult) {
      Level world = this.m_9236_();
      BlockPos pos = hitResult.m_82425_().m_121945_(hitResult.m_82434_());
      BlockState bs = this.getBlockState();
      if (!world.m_5776_() && bs != null) {
         world.m_7731_(pos, bs, 3);
      }

      this.m_146870_();
   }

   protected void m_8097_() {
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
}
