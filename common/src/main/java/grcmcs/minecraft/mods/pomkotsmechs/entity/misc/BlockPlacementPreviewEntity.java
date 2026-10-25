package grcmcs.minecraft.mods.pomkotsmechs.entity.misc;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BlockPlacementPreviewEntity extends Entity {
   protected static final EntityDataAccessor<Integer> PATTERN_INDEX = SynchedEntityData.defineId(
      BlockPlacementPreviewEntity.class, EntityDataSerializers.INT
   );
   public static List<BlockPos> LINE_5X1X1 = new ArrayList<>();
   public static List<BlockPos> PILLAR_1X5X1;
   public static List<BlockPos> PILLAR_3X5X3;
   public static List<BlockPos> WALL_5X5X1;
   public static List<BlockPos> WALL_NANAME;
   public static List<BlockPos> PLATE_5X1X5;
   public static List<BlockPos> STAIR_3X3X3;
   public static List<BlockPos> CUBE_3X3X3;
   public static List<BlockPos>[] BLOCK_TEMPLATES;

   public BlockPlacementPreviewEntity(EntityType<?> type, Level level) {
      super(type, level);
      this.noPhysics = true;
      this.noCulling = true;
   }

   public BlockPlacementPreviewEntity(Level level, Vec3 position) {
      this((EntityType<?>)PomkotsMechs.BLOCK_PLACEMENT_PREVIEW.get(), level);
      this.setPos(position);
   }

   public void tick() {
      this.setNoGravity(true);
      super.tick();
      this.hasImpulse = true;
   }

   public void placeBlocks(ServerLevel level, ServerPlayer player) {
      ItemStack stack = player.getMainHandItem();
      if (stack.getItem() instanceof BlockItem blockItem) {
         List var14 = Utils.getItemStacks(player.getMainHandItem(), player, this.getCurrentPattern().size());
         if (!PomkotsMechs.CONFIG.consumeBlocksWhenPlacing || player.getAbilities().instabuild || var14 != null) {
            BlockState state = blockItem.getBlock().defaultBlockState();
            int q = this.yawToQuarter(this.getYRot());
            BlockPos offset = this.blockPosition();
            int consumedBlockNum = 0;

            for (BlockPos bPos : this.getCurrentPattern()) {
               BlockPos localPos = this.rotate(bPos, q);
               BlockPos worldPos = new BlockPos(
                  offset.getX() + localPos.getX(), offset.getY() + localPos.getY(), offset.getZ() + localPos.getZ()
               );
               if (level.getBlockState(worldPos).canBeReplaced() && level.setBlock(worldPos, state, 3)) {
                  consumedBlockNum++;
               }
            }

            if (PomkotsMechs.CONFIG.consumeBlocksWhenPlacing && !player.getAbilities().instabuild && var14 != null) {
               Utils.consumeItemStackFromTop(var14, consumedBlockNum);
            }
         }
      }
   }

   protected void defineSynchedData() {
      this.entityData.define(PATTERN_INDEX, 0);
   }

   public void incrementPatternIndex() {
      int val = (this.getPatternIndex() + 1) % BLOCK_TEMPLATES.length;
      this.setPatternIndex(val);
   }

   public void setPatternIndex(int num) {
      this.entityData.set(PATTERN_INDEX, num);
   }

   public int getPatternIndex() {
      return (Integer)this.entityData.get(PATTERN_INDEX);
   }

   public List<BlockPos> getCurrentPattern() {
      return BLOCK_TEMPLATES[this.getPatternIndex()];
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
   }

   public boolean shouldRenderAtSqrDistance(double distance) {
      return true;
   }

   public boolean shouldBeSaved() {
      return false;
   }

   public int yawToQuarter(float yaw) {
      float rot = (yaw % 360.0F + 360.0F) % 360.0F;
      return Mth.floor((rot + 45.0F) / 90.0F) & 3;
   }

   public BlockPos rotate(BlockPos pos, int quarter) {
      int x = pos.getX();
      int y = pos.getY();
      int z = pos.getZ();

      return switch (quarter & 3) {
         case 1 -> new BlockPos(-z, y, x);
         case 2 -> new BlockPos(-x, y, -z);
         case 3 -> new BlockPos(z, y, -x);
         default -> pos;
      };
   }

   static {
      Direction right = Direction.EAST;
      Direction back = Direction.SOUTH;
      Direction up = Direction.UP;
      BlockPos center = new BlockPos(0, 0, 0);

      for (int x = -2; x <= 2; x++) {
         BlockPos pos = center.relative(right, x).relative(back, 0).relative(up, 0);
         LINE_5X1X1.add(pos);
      }

      PILLAR_1X5X1 = new ArrayList<>();
      right = Direction.EAST;
      back = Direction.SOUTH;
      up = Direction.UP;
      center = new BlockPos(0, 0, 0);

      for (int y = 0; y <= 4; y++) {
         BlockPos pos = center.relative(right, 0).relative(back, 0).relative(up, y);
         PILLAR_1X5X1.add(pos);
      }

      PILLAR_3X5X3 = new ArrayList<>();
      right = Direction.EAST;
      back = Direction.SOUTH;
      up = Direction.UP;
      center = new BlockPos(0, 0, 0);

      for (int x = -1; x <= 1; x++) {
         for (int z = -1; z <= 1; z++) {
            for (int y = 0; y <= 4; y++) {
               BlockPos pos = center.relative(right, x).relative(back, z).relative(up, y);
               PILLAR_3X5X3.add(pos);
            }
         }
      }

      WALL_5X5X1 = new ArrayList<>();
      right = Direction.EAST;
      back = Direction.UP;
      BlockPos centerx = new BlockPos(0, 0, 0);

      for (int x = -2; x <= 2; x++) {
         for (int y = 0; y <= 4; y++) {
            BlockPos pos = centerx.relative(right, x).relative(back, y);
            WALL_5X5X1.add(pos);
         }
      }

      WALL_NANAME = new ArrayList<>();
      right = Direction.EAST;
      back = Direction.SOUTH;
      up = Direction.UP;
      center = new BlockPos(0, 0, 0);

      for (int x = -2; x <= 2; x++) {
         for (int z = -2; z <= 2; z++) {
            for (int y = 0; y <= 4; y++) {
               if (x == z) {
                  BlockPos pos = center.relative(right, x).relative(back, z).relative(up, y);
                  WALL_NANAME.add(pos);
               }
            }
         }
      }

      PLATE_5X1X5 = new ArrayList<>();
      right = Direction.EAST;
      back = Direction.SOUTH;
      up = Direction.UP;
      center = new BlockPos(0, 0, 0);

      for (int x = -2; x <= 2; x++) {
         for (int z = -2; z <= 2; z++) {
            for (int y = 0; y <= 0; y++) {
               BlockPos pos = center.relative(right, x).relative(back, z).relative(up, y);
               PLATE_5X1X5.add(pos);
            }
         }
      }

      STAIR_3X3X3 = new ArrayList<>();
      right = Direction.EAST;
      back = Direction.SOUTH;
      up = Direction.UP;
      center = new BlockPos(0, 0, 0);

      for (int y = 0; y <= 2; y++) {
         for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1 - y; z++) {
               BlockPos pos = center.relative(right, x).relative(back, z).relative(up, y);
               STAIR_3X3X3.add(pos);
            }
         }
      }

      CUBE_3X3X3 = new ArrayList<>();
      right = Direction.EAST;
      back = Direction.SOUTH;
      up = Direction.UP;
      center = new BlockPos(0, 0, 0);

      for (int x = -1; x <= 1; x++) {
         for (int z = -1; z <= 1; z++) {
            for (int y = 0; y <= 2; y++) {
               BlockPos pos = center.relative(right, x).relative(back, z).relative(up, y);
               CUBE_3X3X3.add(pos);
            }
         }
      }

      BLOCK_TEMPLATES = new List[]{LINE_5X1X1, PILLAR_1X5X1, PILLAR_3X5X3, WALL_5X5X1, WALL_NANAME, PLATE_5X1X5, STAIR_3X3X3, CUBE_3X3X3};
   }
}
