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
   private static final Map<RailShape, Pair<Vec3i, Vec3i>> EXITS = Util.make(
      new java.util.EnumMap<RailShape, Pair<Vec3i, Vec3i>>(RailShape.class), enumMap -> {
         Vec3i vec3i = Direction.WEST.getNormal();
         Vec3i vec3i1 = Direction.EAST.getNormal();
         Vec3i vec3i2 = Direction.NORTH.getNormal();
         Vec3i vec3i3 = Direction.SOUTH.getNormal();
         Vec3i vec3i4 = vec3i.below();
         Vec3i vec3i5 = vec3i1.below();
         Vec3i vec3i6 = vec3i2.below();
         Vec3i vec3i7 = vec3i3.below();
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
      if (!this.ent.level().isClientSide) {
         int k = Mth.floor(this.ent.getX());
         int i = Mth.floor(this.ent.getY());
         int j = Mth.floor(this.ent.getZ());
         if (this.ent.level().getBlockState(new BlockPos(k, i - 1, j)).is(BlockTags.RAILS)) {
            i--;
         }

         BlockPos blockpos = new BlockPos(k, i, j);
         BlockState blockstate = this.ent.level().getBlockState(blockpos);
         this.onRails = BaseRailBlock.isRail(blockstate);
         if (this.canUseRail() && this.onRails) {
            this.moveAlongTrack(blockpos, blockstate);
         }
      }
   }

   protected void comeOffTrack() {
      double d0 = this.ent.onGround() ? this.getMaxSpeed() : this.getMaxSpeedAirLateral();
      Vec3 vec3 = this.ent.getDeltaMovement();
      this.ent.setDeltaMovement(Mth.clamp(vec3.x, -d0, d0), vec3.y, Mth.clamp(vec3.z, -d0, d0));
      if (this.ent.onGround()) {
         this.ent.setDeltaMovement(this.ent.getDeltaMovement().scale(0.5));
      }

      if (this.getMaxSpeedAirVertical() > 0.0F && this.ent.getDeltaMovement().y > this.getMaxSpeedAirVertical()) {
         if (Math.abs(this.ent.getDeltaMovement().x) < 0.3F && Math.abs(this.ent.getDeltaMovement().z) < 0.3F) {
            this.ent.setDeltaMovement(new Vec3(this.ent.getDeltaMovement().x, 0.15F, this.ent.getDeltaMovement().z));
         } else {
            this.ent.setDeltaMovement(new Vec3(this.ent.getDeltaMovement().x, this.getMaxSpeedAirVertical(), this.ent.getDeltaMovement().z));
         }
      }

      this.ent.move(MoverType.SELF, this.ent.getDeltaMovement());
      if (!this.ent.onGround()) {
         this.ent.setDeltaMovement(this.ent.getDeltaMovement().scale(this.getDragAir()));
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
      this.ent.resetFallDistance();
      double entPosX = this.ent.getX();
      double entPosY = this.ent.getY();
      double entPosZ = this.ent.getZ();
      Vec3 entityPosAdjustedByRailPos = this.getPos(entPosX, entPosY, entPosZ);
      entPosY = curRailPos.getY();
      boolean isPoweredRail = false;
      boolean notPoweredRail = false;
      BaseRailBlock baserailblock = (BaseRailBlock)curRailState.getBlock();
      if (baserailblock instanceof PoweredRailBlock) {
         isPoweredRail = (Boolean)curRailState.getValue(PoweredRailBlock.POWERED);
         notPoweredRail = !isPoweredRail;
      }

      double slopeAdj = this.getSlopeAdjustment();
      if (this.ent.isInWater()) {
         slopeAdj *= 0.2;
      }

      Vec3 entVel = this.ent.getDeltaMovement();
      RailShape railshape = this.getRailDirection(curRailState);
      switch (railshape) {
         case ASCENDING_EAST:
            this.ent.setDeltaMovement(entVel.add(-slopeAdj, 0.0, 0.0));
            entPosY++;
            break;
         case ASCENDING_WEST:
            this.ent.setDeltaMovement(entVel.add(slopeAdj, 0.0, 0.0));
            entPosY++;
            break;
         case ASCENDING_NORTH:
            this.ent.setDeltaMovement(entVel.add(0.0, 0.0, slopeAdj));
            entPosY++;
            break;
         case ASCENDING_SOUTH:
            this.ent.setDeltaMovement(entVel.add(0.0, 0.0, -slopeAdj));
            entPosY++;
      }

      entVel = this.ent.getDeltaMovement();
      Pair<Vec3i, Vec3i> pair = exits(railshape);
      Vec3i railDir1 = (Vec3i)pair.getFirst();
      Vec3i railDir2 = (Vec3i)pair.getSecond();
      double d4 = railDir2.getX() - railDir1.getX();
      double d5 = railDir2.getZ() - railDir1.getZ();
      double d6 = Math.sqrt(d4 * d4 + d5 * d5);
      double d7 = entVel.x * d4 + entVel.z * d5;
      if (d7 < 0.0) {
         d4 = -d4;
         d5 = -d5;
      }

      double d8 = Math.min(2.0, entVel.horizontalDistance());
      entVel = new Vec3(d8 * d4 / d6, entVel.y, d8 * d5 / d6);
      this.ent.setDeltaMovement(entVel);
      Entity entity = this.ent.getFirstPassenger();
      if (entity instanceof Player) {
         Vec3 vec32 = entity.getDeltaMovement();
         double d9 = vec32.horizontalDistanceSqr();
         double d11 = this.ent.getDeltaMovement().horizontalDistanceSqr();
         if (d9 > 1.0E-4 && d11 < 0.01) {
            this.ent.setDeltaMovement(this.ent.getDeltaMovement().add(vec32.x * 0.1, 0.0, vec32.z * 0.1));
            notPoweredRail = false;
         }
      }

      if (notPoweredRail) {
         double entVelHor = this.ent.getDeltaMovement().horizontalDistance();
         if (entVelHor < 0.03) {
            this.ent.setDeltaMovement(Vec3.ZERO);
         } else {
            this.ent.setDeltaMovement(this.ent.getDeltaMovement().multiply(0.5, 0.0, 0.5));
         }
      }

      double entVelHor = curRailPos.getX() + 0.5 + railDir1.getX() * 0.5;
      double d10 = curRailPos.getZ() + 0.5 + railDir1.getZ() * 0.5;
      double d12 = curRailPos.getX() + 0.5 + railDir2.getX() * 0.5;
      double d13 = curRailPos.getZ() + 0.5 + railDir2.getZ() * 0.5;
      d4 = d12 - entVelHor;
      d5 = d13 - d10;
      double d14;
      if (d4 == 0.0) {
         d14 = entPosZ - curRailPos.getZ();
      } else if (d5 == 0.0) {
         d14 = entPosX - curRailPos.getX();
      } else {
         double d15 = entPosX - entVelHor;
         double d16 = entPosZ - d10;
         d14 = (d15 * d4 + d16 * d5) * 2.0;
      }

      entPosX = entVelHor + d4 * d14;
      entPosZ = d10 + d5 * d14;
      this.ent.setPos(entPosX, entPosY, entPosZ);
      this.moveMinecartOnRail(curRailPos);
      if (railDir1.getY() != 0
         && Mth.floor(this.ent.getX()) - curRailPos.getX() == railDir1.getX()
         && Mth.floor(this.ent.getZ()) - curRailPos.getZ() == railDir1.getZ()) {
         this.ent.setPos(this.ent.getX(), this.ent.getY() + railDir1.getY(), this.ent.getZ());
      } else if (railDir2.getY() != 0
         && Mth.floor(this.ent.getX()) - curRailPos.getX() == railDir2.getX()
         && Mth.floor(this.ent.getZ()) - curRailPos.getZ() == railDir2.getZ()) {
         this.ent.setPos(this.ent.getX(), this.ent.getY() + railDir2.getY(), this.ent.getZ());
      }

      this.applyNaturalSlowdown();
      Vec3 vec33 = this.getPos(this.ent.getX(), this.ent.getY(), this.ent.getZ());
      if (vec33 != null && entityPosAdjustedByRailPos != null) {
         double d17 = (entityPosAdjustedByRailPos.y - vec33.y) * 0.05;
         Vec3 vec36 = this.ent.getDeltaMovement();
         double d27 = vec36.horizontalDistance();
         if (d27 > 0.0) {
            this.ent.setDeltaMovement(vec36.multiply((d27 + d17) / d27, 1.0, (d27 + d17) / d27));
         }

         this.ent.setPos(this.ent.getX(), vec33.y, this.ent.getZ());
      }

      int j = Mth.floor(this.ent.getX());
      int i = Mth.floor(this.ent.getZ());
      if (j != curRailPos.getX() || i != curRailPos.getZ()) {
         Vec3 vec36 = this.ent.getDeltaMovement();
         double d27 = vec36.horizontalDistance();
         this.ent.setDeltaMovement(d27 * (j - curRailPos.getX()), vec36.y, d27 * (i - curRailPos.getZ()));
      }

      if (isPoweredRail) {
         Vec3 vec36 = this.ent.getDeltaMovement();
         double d27 = vec36.horizontalDistance();
         if (d27 > 0.01) {
            double d19 = 0.06;
            this.ent.setDeltaMovement(vec36.add(vec36.x / d27 * 0.06, 0.0, vec36.z / d27 * 0.06));
         } else {
            Vec3 vec37 = this.ent.getDeltaMovement();
            double d20 = vec37.x;
            double d21 = vec37.z;
            this.ent.setDeltaMovement(d20, vec37.y, d21);
         }
      }
   }

   public RailShape getRailDirection(BlockState state) {
      BaseRailBlock railBlock = (BaseRailBlock)state.getBlock();
      return (RailShape)state.getValue(railBlock.getShapeProperty());
   }

   public Vec3 getPos(double d, double e, double f) {
      int i = Mth.floor(d);
      int j = Mth.floor(e);
      int k = Mth.floor(f);
      if (this.ent.level().getBlockState(new BlockPos(i, j - 1, k)).is(BlockTags.RAILS)) {
         j--;
      }

      BlockState blockstate = this.ent.level().getBlockState(new BlockPos(i, j, k));
      if (BaseRailBlock.isRail(blockstate)) {
         RailShape railshape = this.getRailDirection(blockstate);
         Pair<Vec3i, Vec3i> pair = exits(railshape);
         Vec3i vec3i = (Vec3i)pair.getFirst();
         Vec3i vec3i1 = (Vec3i)pair.getSecond();
         double d0 = i + 0.5 + vec3i.getX() * 0.5;
         double d1 = j + 0.0625 + vec3i.getY() * 0.5;
         double d2 = k + 0.5 + vec3i.getZ() * 0.5;
         double d3 = i + 0.5 + vec3i1.getX() * 0.5;
         double d4 = j + 0.0625 + vec3i1.getY() * 0.5;
         double d5 = k + 0.5 + vec3i1.getZ() * 0.5;
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
      double d0 = this.ent.isVehicle() ? 0.997 : 0.96;
      Vec3 vec3 = this.ent.getDeltaMovement();
      vec3 = vec3.multiply(d0, 0.0, d0);
      if (this.ent.isInWater()) {
         vec3 = vec3.scale(0.95F);
      }

      this.ent.setDeltaMovement(vec3);
   }

   public void moveMinecartOnRail(BlockPos pos) {
      double d24 = this.ent.isVehicle() ? 0.75 : 1.0;
      double d25 = this.getMaxSpeedWithRail();
      Vec3 vec3d1 = this.ent.getDeltaMovement();
      this.ent.move(MoverType.SELF, new Vec3(Mth.clamp(d24 * vec3d1.x, -d25, d25), 0.0, Mth.clamp(d24 * vec3d1.z, -d25, d25)));
   }

   private static Pair<Vec3i, Vec3i> exits(RailShape arg) {
      return EXITS.get(arg);
   }
}
