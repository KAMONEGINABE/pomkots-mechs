package grcmcs.minecraft.mods.pomkotsmechs.block;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MechWorkbenchBlockEntity extends BlockEntity {
   private List<UUID> repairingMechList = new ArrayList<>();
   private long currentTick = 0L;

   public MechWorkbenchBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType)PomkotsMechs.MECH_WORKBENCH_BLOCK_ENTITY.get(), pos, state);
   }

   public static void serverTick(Level level, BlockPos pos, BlockState state, MechWorkbenchBlockEntity be) {
      be.tick();
   }

   public void tick() {
      if (this.level instanceof ServerLevel sLevel && !this.repairingMechList.isEmpty() && this.currentTick++ % 20L == 0L) {
         BlockPos pos = this.getBlockPos();
         this.repairingMechList
            .removeIf(
               uuid -> {
                  Pmvc01Entity mech = (Pmvc01Entity)sLevel.getEntity(uuid);
                  boolean flag = mech == null
                     || !mech.isAlive()
                     || mech.getMaxHealth() == mech.getHealth()
                     || mech.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > 50.0;
                  if (!flag) {
                     mech.healWithEffect(sLevel, 10.0F);
                  }

                  return flag;
               }
            );
      }
   }

   public void startMechRepair(Pmvc01Entity mech) {
      if (this.level instanceof ServerLevel sLevel) {
         this.repairingMechList.add(mech.getUUID());
      }
   }

   public void saveAdditional(@NotNull CompoundTag tag) {
      super.saveAdditional(tag);
      ListTag list = new ListTag();

      for (UUID uuid : this.repairingMechList) {
         list.add(NbtUtils.createUUID(uuid));
      }

      tag.put(PomkotsMechs.nbtName("RepairingMechList"), list);
   }

   public void load(@NotNull CompoundTag tag) {
      super.load(tag);
      this.repairingMechList.clear();
      ListTag list = (ListTag)tag.get(PomkotsMechs.nbtName("RepairingMechList"));
      if (list != null) {
         for (Tag t : list) {
            this.repairingMechList.add(NbtUtils.loadUUID(t));
         }
      }
   }
}
