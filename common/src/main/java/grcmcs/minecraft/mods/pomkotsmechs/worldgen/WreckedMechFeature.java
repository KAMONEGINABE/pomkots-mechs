package grcmcs.minecraft.mods.pomkotsmechs.worldgen;

import com.mojang.serialization.Codec;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.MechCapsuleItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.extension.SBUnitProtoTypeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class WreckedMechFeature extends Feature<NoneFeatureConfiguration> {
   public WreckedMechFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   @Override
   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      WorldGenLevel level = context.level();
      BlockPos origin = context.origin();
      RandomSource random = context.random();

      BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, origin);
      BlockPos groundPos = surfacePos.below();
      BlockState groundState = level.getBlockState(groundPos);

      // 地面が流体（水・溶岩）でないこと、およびしっかりとしたブロックであることを確認
      if (!groundState.getFluidState().isEmpty() || !level.getFluidState(surfacePos).isEmpty()) {
         return false;
      }
      if (!groundState.isFaceSturdy(level, groundPos, Direction.UP)) {
         return false;
      }

      // 配置空間（高さ2ブロック分）がブロックで埋まっていないこと
      BlockState atSurface = level.getBlockState(surfacePos);
      BlockState aboveSurface = level.getBlockState(surfacePos.above());
      if (!atSurface.canBeReplaced() || !aboveSurface.canBeReplaced()) {
         return false;
      }

      Pmvc01Entity mech = (Pmvc01Entity)PomkotsMechs.PMVC01.get().create(level.getLevel());
      if (mech == null) {
         return false;
      }

      // 1. 機種決定（Rusty 50%, 赤・青・黄・緑・白 各10%）
      int roll = random.nextInt(100);
      String presetName;
      boolean isRusty = false;
      if (roll < 50) {
         presetName = "Rusty";
         isRusty = true;
      } else if (roll < 60) {
         presetName = "Altair"; // 赤
      } else if (roll < 70) {
         presetName = "Deneb"; // 青
      } else if (roll < 80) {
         presetName = "Vega"; // 黄
      } else if (roll < 90) {
         presetName = "Aldebaran"; // 緑
      } else {
         presetName = "Sirius"; // 白
      }

      // 2. プリセットからフル装備を一旦適用
      ItemStack templateCapsule = new ItemStack(PomkotsMechs.MECH_CAPSULE_ITEM.get());
      MechCapsuleItem.buildPreset(presetName, templateCapsule);
      CompoundTag rootTag = templateCapsule.getTag();
      if (rootTag != null && rootTag.contains(PomkotsMechs.nbtName("MechPreset"))) {
         CompoundTag presetTag = rootTag.getCompound(PomkotsMechs.nbtName("MechPreset"));
         MechCapsuleItem.applyPresetToCustomMech(presetTag, mech);
      }

      // 3. SuperBooster は確定で除外（スロット10および他スロット判定）
      mech.setItem(10, ItemStack.EMPTY);
      for (int i = 0; i < 12; i++) {
         if (mech.getItem(i).getItem() instanceof SBUnitProtoTypeItem) {
            mech.setItem(i, ItemStack.EMPTY);
         }
      }

      // 4. 稼働状態判定（5%の低確率）vs 大破状態（95%）
      boolean isAlive = random.nextFloat() < 0.05F;

      if (isAlive) {
         // 稼働維持状態: 全部位揃った状態を保証（欠損させない）
         mech.setBroken(false);
         mech.setHealth(Math.max(1.0F, mech.getMaxHealth() * 0.2F));
         mech.setEnergy(mech.getMaxEnergy() * 0.4F);
      } else {
         // 大破状態: パーツ欠損処理
         float keepChance = isRusty ? 0.50F : 0.25F;
         int[] partSlots = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 11};
         for (int slot : partSlots) {
            if (random.nextFloat() >= keepChance) {
               mech.setItem(slot, ItemStack.EMPTY);
            }
         }

         mech.setBroken(true);
         mech.setHealth(1.0F);
         mech.setEnergy(0.0F);

      }

      // 5. 弾薬・燃料スロット（スロット 12〜16）の残存判定（全機種共通: 20%の確率で1〜3個）
      for (int slot = 12; slot <= 16; slot++) {
         ItemStack stack = mech.getItem(slot);
         if (!stack.isEmpty()) {
            if (random.nextFloat() < 0.20F) {
               int count = random.nextInt(3) + 1;
               stack.setCount(Math.min(count, stack.getCount()));
            } else {
               mech.setItem(slot, ItemStack.EMPTY);
            }
         }
      }

      mech.setChanged();

      // 6. 配置と向きの設定
      float yaw = random.nextFloat() * 360.0F;
      mech.moveTo(surfacePos.getX() + 0.5, surfacePos.getY(), surfacePos.getZ() + 0.5, yaw, 0.0F);
      mech.setYBodyRot(yaw);
      mech.setYHeadRot(yaw);

      level.addFreshEntity(mech);
      return true;
   }
}
