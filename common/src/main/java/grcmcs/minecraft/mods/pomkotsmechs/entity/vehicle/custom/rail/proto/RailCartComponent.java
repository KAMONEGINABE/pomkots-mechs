package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.rail.proto;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

public class RailCartComponent {
   private LivingEntity ent;
   private boolean onRails;
   private boolean canUseRail = true;
   private static final Map<RailShape, Pair<Vec3i, Vec3i>> EXITS = (Map<RailShape, Pair<Vec3i, Vec3i>>)Util.m_137469_(
      Maps.newEnumMap(RailShape.class), enumMap -> {
         Vec3i vec3i = Direction.WEST.m_122436_();
         Vec3i vec3i1 = Direction.EAST.m_122436_();
         Vec3i vec3i2 = Direction.NORTH.m_122436_();
         Vec3i vec3i3 = Direction.SOUTH.m_122436_();
         Vec3i vec3i4 = vec3i.m_7495_();
         Vec3i vec3i5 = vec3i1.m_7495_();
         Vec3i vec3i6 = vec3i2.m_7495_();
         Vec3i vec3i7 = vec3i3.m_7495_();
         enumMap.put(RailShape.NORTH_SOUTH, Pair.of(vec3i2, vec3i3));
         enumMap.put(RailShape.EAST_WEST, Pair.of(vec3i, vec3i1));
         enumMap.put(RailShape.ASCENDING_EAST, Pair.of(vec3i4, vec3i1));
         enumMap.put(RailShape.ASCENDING_WEST, Pair.of(vec3i, vec3i5));
         enumMap.put(RailShape.ASCENDING_NORTH, Pair.of(vec3i2, vec3i7));
         enumMap.put(RailShape.ASCENDING_SOUTH, Pair.of(vec3i6, vec3i3));
         enumMap.put(RailShape.SOUTH_EAST, Pair.of(vec3i3, vec3i1));
         enumMap.put(RailShape.SOUTH_WEST, Pair.of(vec3i3, vec3i));
         enumMap.put(RailShape.NORTH_WEST, Pair.of(vec3i2, vec3i));
         enumMap.put(RailShape.NORTH_EAST, Pair.of(vec3i2, vec3i1));
      }
   );

   public RailCartComponent(LivingEntity ent) {
      this.ent = ent;
   }

   public boolean canUseRail() {
      return this.canUseRail;
   }

   public void tick() {
      if (!this.ent.m_9236_().f_46443_) {
         int k = Mth.m_14107_(this.ent.m_20185_());
         int i = Mth.m_14107_(this.ent.m_20186_());
         int j = Mth.m_14107_(this.ent.m_20189_());
         if (this.ent.m_9236_().m_8055_(new BlockPos(k, i - 1, j)).m_204336_(BlockTags.f_13034_)) {
            i--;
         }

         BlockPos blockpos = new BlockPos(k, i, j);
         BlockState blockstate = this.ent.m_9236_().m_8055_(blockpos);
         this.onRails = BaseRailBlock.m_49416_(blockstate);
         if (this.canUseRail() && this.onRails) {
            this.moveAlongTrack(blockpos, blockstate);
         }
      }
   }

   protected void comeOffTrack() {
      double d0 = this.ent.m_20096_() ? this.getMaxSpeed() : this.getMaxSpeedAirLateral();
      Vec3 vec3 = this.ent.m_20184_();
      this.ent.m_20334_(Mth.m_14008_(vec3.f_82479_, -d0, d0), vec3.f_82480_, Mth.m_14008_(vec3.f_82481_, -d0, d0));
      if (this.ent.m_20096_()) {
         this.ent.m_20256_(this.ent.m_20184_().m_82490_(0.5));
      }

      if (this.getMaxSpeedAirVertical() > 0.0F && this.ent.m_20184_().f_82480_ > this.getMaxSpeedAirVertical()) {
         if (Math.abs(this.ent.m_20184_().f_82479_) < 0.3F && Math.abs(this.ent.m_20184_().f_82481_) < 0.3F) {
            this.ent.m_20256_(new Vec3(this.ent.m_20184_().f_82479_, 0.15F, this.ent.m_20184_().f_82481_));
         } else {
            this.ent.m_20256_(new Vec3(this.ent.m_20184_().f_82479_, this.getMaxSpeedAirVertical(), this.ent.m_20184_().f_82481_));
         }
      }

      this.ent.m_6478_(MoverType.SELF, this.ent.m_20184_());
      if (!this.ent.m_20096_()) {
         this.ent.m_20256_(this.ent.m_20184_().m_82490_(this.getDragAir()));
      }
   }

   public float getMaxSpeed() {
      return 4.0F;
   }

   public float getMaxSpeedAirLateral() {
      return 4.0F;
   }

   public float getMaxSpeedAirVertical() {
      return 4.0F;
   }

   public double getDragAir() {
      return 0.95F;
   }

   public double getMaxSpeedWithRail() {
      return 4.0;
   }

   protected void moveAlongTrack(BlockPos curRailPos, BlockState curRailState) {
      this.ent.m_183634_();
      double entPosX = this.ent.m_20185_();
      double entPosY = this.ent.m_20186_();
      double entPosZ = this.ent.m_20189_();
      Vec3 entityPosAdjustedByRailPos = this.getPos(entPosX, entPosY, entPosZ);
      entPosY = curRailPos.m_123342_();
      boolean isPoweredRail = false;
      boolean notPoweredRail = false;
      BaseRailBlock baserailblock = (BaseRailBlock)curRailState.m_60734_();
      if (baserailblock instanceof PoweredRailBlock) {
         isPoweredRail = (Boolean)curRailState.m_61143_(PoweredRailBlock.f_55215_);
         notPoweredRail = !isPoweredRail;
      }

      double slopeAdj = this.getSlopeAdjustment();
      if (this.ent.m_20069_()) {
         slopeAdj *= 0.2;
      }

      Vec3 entVel = this.ent.m_20184_();
      RailShape railshape = this.getRailDirection(curRailState);
      switch (railshape) {
         case ASCENDING_EAST:
            this.ent.m_20256_(entVel.m_82520_(-slopeAdj, 0.0, 0.0));
            entPosY++;
            break;
         case ASCENDING_WEST:
            this.ent.m_20256_(entVel.m_82520_(slopeAdj, 0.0, 0.0));
            entPosY++;
            break;
         case ASCENDING_NORTH:
            this.ent.m_20256_(entVel.m_82520_(0.0, 0.0, slopeAdj));
            entPosY++;
            break;
         case ASCENDING_SOUTH:
            this.ent.m_20256_(entVel.m_82520_(0.0, 0.0, -slopeAdj));
            entPosY++;
      }

      entVel = this.ent.m_20184_();
      Pair<Vec3i, Vec3i> pair = exits(railshape);
      Vec3i railDir1 = (Vec3i)pair.getFirst();
      Vec3i railDir2 = (Vec3i)pair.getSecond();
      double d4 = railDir2.m_123341_() - railDir1.m_123341_();
      double d5 = railDir2.m_123343_() - railDir1.m_123343_();
      double d6 = Math.sqrt(d4 * d4 + d5 * d5);
      double d7 = entVel.f_82479_ * d4 + entVel.f_82481_ * d5;
      if (d7 < 0.0) {
         d4 = -d4;
         d5 = -d5;
      }

      double d8 = Math.min(2.0, entVel.m_165924_());
      entVel = new Vec3(d8 * d4 / d6, entVel.f_82480_, d8 * d5 / d6);
      this.ent.m_20256_(entVel);
      Entity entity = this.ent.m_146895_();
      if (entity instanceof Player) {
         Vec3 vec32 = entity.m_20184_();
         double d9 = vec32.m_165925_();
         double d11 = this.ent.m_20184_().m_165925_();
         if (d9 > 1.0E-4 && d11 < 0.01) {
            this.ent.m_20256_(this.ent.m_20184_().m_82520_(vec32.f_82479_ * 0.1, 0.0, vec32.f_82481_ * 0.1));
            notPoweredRail = false;
         }
      }

      if (notPoweredRail) {
         double entVelHor = this.ent.m_20184_().m_165924_();
         if (entVelHor < 0.03) {
            this.ent.m_20256_(Vec3.f_82478_);
         } else {
            this.ent.m_20256_(this.ent.m_20184_().m_82542_(0.5, 0.0, 0.5));
         }
      }

      double entVelHor = curRailPos.m_123341_() + 0.5 + railDir1.m_123341_() * 0.5;
      double d10 = curRailPos.m_123343_() + 0.5 + railDir1.m_123343_() * 0.5;
      double d12 = curRailPos.m_123341_() + 0.5 + railDir2.m_123341_() * 0.5;
      double d13 = curRailPos.m_123343_() + 0.5 + railDir2.m_123343_() * 0.5;
      d4 = d12 - entVelHor;
      d5 = d13 - d10;
      double d14;
      if (d4 == 0.0) {
         d14 = entPosZ - curRailPos.m_123343_();
      } else if (d5 == 0.0) {
         d14 = entPosX - curRailPos.m_123341_();
      } else {
         double d15 = entPosX - entVelHor;
         double d16 = entPosZ - d10;
         d14 = (d15 * d4 + d16 * d5) * 2.0;
      }

      entPosX = entVelHor + d4 * d14;
      entPosZ = d10 + d5 * d14;
      this.ent.m_6034_(entPosX, entPosY, entPosZ);
      this.moveMinecartOnRail(curRailPos);
      if (railDir1.m_123342_() != 0
         && Mth.m_14107_(this.ent.m_20185_()) - curRailPos.m_123341_() == railDir1.m_123341_()
         && Mth.m_14107_(this.ent.m_20189_()) - curRailPos.m_123343_() == railDir1.m_123343_()) {
         this.ent.m_6034_(this.ent.m_20185_(), this.ent.m_20186_() + railDir1.m_123342_(), this.ent.m_20189_());
      } else if (railDir2.m_123342_() != 0
         && Mth.m_14107_(this.ent.m_20185_()) - curRailPos.m_123341_() == railDir2.m_123341_()
         && Mth.m_14107_(this.ent.m_20189_()) - curRailPos.m_123343_() == railDir2.m_123343_()) {
         this.ent.m_6034_(this.ent.m_20185_(), this.ent.m_20186_() + railDir2.m_123342_(), this.ent.m_20189_());
      }

      this.applyNaturalSlowdown();
      Vec3 vec33 = this.getPos(this.ent.m_20185_(), this.ent.m_20186_(), this.ent.m_20189_());
      if (vec33 != null && entityPosAdjustedByRailPos != null) {
         double d17 = (entityPosAdjustedByRailPos.f_82480_ - vec33.f_82480_) * 0.05;
         Vec3 vec36 = this.ent.m_20184_();
         double d27 = vec36.m_165924_();
         if (d27 > 0.0) {
            this.ent.m_20256_(vec36.m_82542_((d27 + d17) / d27, 1.0, (d27 + d17) / d27));
         }

         this.ent.m_6034_(this.ent.m_20185_(), vec33.f_82480_, this.ent.m_20189_());
      }

      int j = Mth.m_14107_(this.ent.m_20185_());
      int i = Mth.m_14107_(this.ent.m_20189_());
      if (j != curRailPos.m_123341_() || i != curRailPos.m_123343_()) {
         Vec3 vec36 = this.ent.m_20184_();
         double d27 = vec36.m_165924_();
         this.ent.m_20334_(d27 * (j - curRailPos.m_123341_()), vec36.f_82480_, d27 * (i - curRailPos.m_123343_()));
      }

      if (isPoweredRail) {
         Vec3 vec36 = this.ent.m_20184_();
         double d27 = vec36.m_165924_();
         if (d27 > 0.01) {
            double d19 = 0.06;
            this.ent.m_20256_(vec36.m_82520_(vec36.f_82479_ / d27 * 0.06, 0.0, vec36.f_82481_ / d27 * 0.06));
         } else {
            Vec3 vec37 = this.ent.m_20184_();
            double d20 = vec37.f_82479_;
            double d21 = vec37.f_82481_;
            this.ent.m_20334_(d20, vec37.f_82480_, d21);
         }
      }
   }

   public RailShape getRailDirection(BlockState state) {
      BaseRailBlock railBlock = (BaseRailBlock)state.m_60734_();
      return (RailShape)state.m_61143_(railBlock.m_7978_());
   }

   public Vec3 getPos(double d, double e, double f) {
      int i = Mth.m_14107_(d);
      int j = Mth.m_14107_(e);
      int k = Mth.m_14107_(f);
      if (this.ent.m_9236_().m_8055_(new BlockPos(i, j - 1, k)).m_204336_(BlockTags.f_13034_)) {
         j--;
      }

      BlockState blockstate = this.ent.m_9236_().m_8055_(new BlockPos(i, j, k));
      if (BaseRailBlock.m_49416_(blockstate)) {
         RailShape railshape = this.getRailDirection(blockstate);
         Pair<Vec3i, Vec3i> pair = exits(railshape);
         Vec3i vec3i = (Vec3i)pair.getFirst();
         Vec3i vec3i1 = (Vec3i)pair.getSecond();
         double d0 = i + 0.5 + vec3i.m_123341_() * 0.5;
         double d1 = j + 0.0625 + vec3i.m_123342_() * 0.5;
         double d2 = k + 0.5 + vec3i.m_123343_() * 0.5;
         double d3 = i + 0.5 + vec3i1.m_123341_() * 0.5;
         double d4 = j + 0.0625 + vec3i1.m_123342_() * 0.5;
         double d5 = k + 0.5 + vec3i1.m_123343_() * 0.5;
         double d6 = d3 - d0;
         double d7 = (d4 - d1) * 2.0;
         double d8 = d5 - d2;
         double d9;
         if (d6 == 0.0) {
            d9 = f - k;
         } else if (d8 == 0.0) {
            d9 = d - i;
         } else {
            double d10 = d - d0;
            double d11 = f - d2;
            d9 = (d10 * d6 + d11 * d8) * 2.0;
         }

         d = d0 + d6 * d9;
         e = d1 + d7 * d9;
         f = d2 + d8 * d9;
         if (d7 < 0.0) {
            e++;
         } else if (d7 > 0.0) {
            e += 0.5;
         }

         return new Vec3(d, e, f);
      } else {
         return null;
      }
   }

   private double getSlopeAdjustment() {
      return 0.0078125;
   }

   protected void applyNaturalSlowdown() {
      double d0 = this.ent.m_20160_() ? 0.997 : 0.96;
      Vec3 vec3 = this.ent.m_20184_();
      vec3 = vec3.m_82542_(d0, 0.0, d0);
      if (this.ent.m_20069_()) {
         vec3 = vec3.m_82490_(0.95F);
      }

      this.ent.m_20256_(vec3);
   }

   public void moveMinecartOnRail(BlockPos pos) {
      double d24 = this.ent.m_20160_() ? 0.75 : 1.0;
      double d25 = this.getMaxSpeedWithRail();
      Vec3 vec3d1 = this.ent.m_20184_();
      this.ent.m_6478_(MoverType.SELF, new Vec3(Mth.m_14008_(d24 * vec3d1.f_82479_, -d25, d25), 0.0, Mth.m_14008_(d24 * vec3d1.f_82481_, -d25, d25)));
   }

   private static Pair<Vec3i, Vec3i> exits(RailShape arg) {
      return EXITS.get(arg);
   }
}
