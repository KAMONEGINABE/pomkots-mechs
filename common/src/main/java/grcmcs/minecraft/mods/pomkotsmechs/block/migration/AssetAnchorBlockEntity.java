package grcmcs.minecraft.mods.pomkotsmechs.block.migration;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.misc.migration.AssetMigrationManager;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class AssetAnchorBlockEntity extends BlockEntity {
   private static final String NBT_ID = PomkotsMechs.nbtName("AssetId");
   private static final String NBT_VERSION = PomkotsMechs.nbtName("AssetVersion");
   private String assetId = "";
   private int assetVersion = 0;
   private boolean migrationChecked = false;

   public AssetAnchorBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)PomkotsMechs.ASSET_ANCHOR_BE.get(), pos, state);
   }

   public static void tick(Level level, BlockPos pos, BlockState state, AssetAnchorBlockEntity be) {
      if (!level.isClientSide) {
         if (!be.migrationChecked && !Utils.isMapEditingMode(level)) {
            be.migrationChecked = true;
            AssetMigrationManager.queueCheck(be);
         }
      }
   }

   public String getAssetId() {
      return this.assetId;
   }

   public void setAssetId(String assetId) {
      this.assetId = assetId;
      this.setChanged();
   }

   public int getAssetVersion() {
      return this.assetVersion;
   }

   public void setAssetVersion(int assetVersion) {
      this.assetVersion = assetVersion;
      this.setChanged();
   }

   protected void saveAdditional(CompoundTag tag) {
      super.saveAdditional(tag);
      tag.putString(NBT_ID, this.assetId);
      tag.putInt(NBT_VERSION, this.assetVersion);
   }

   public void load(CompoundTag tag) {
      super.load(tag);
      this.assetId = tag.getString(NBT_ID);
      this.assetVersion = tag.getInt(NBT_VERSION);
   }
}
