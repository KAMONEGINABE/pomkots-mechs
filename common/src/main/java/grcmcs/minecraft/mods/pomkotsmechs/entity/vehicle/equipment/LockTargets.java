package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class LockTargets {
   private Entity lockTargetS = null;
   private Entity lockTargetH = null;
   private boolean multiLockComplete = false;
   private List<Entity> lockTargetM = new ArrayList<>();
   private List<Entity> lockTargetMRA = new ArrayList<>();
   private List<Entity> lockTargetMLA = new ArrayList<>();
   private List<Entity> lockTargetMRS = new ArrayList<>();
   private List<Entity> lockTargetMLS = new ArrayList<>();

   public Entity getLockTargetSoft() {
      return this.lockTargetS;
   }

   public void lockTargetSoft(Entity ent) {
      this.lockTargetS = ent;
   }

   public void unlockTargetSoft() {
      this.lockTargetS = null;
   }

   public Entity getLockTargetHard() {
      return this.lockTargetH;
   }

   public void lockTargetHard(Entity ent) {
      this.lockTargetH = ent;
   }

   public void unlockTargetHard() {
      this.lockTargetH = null;
   }

   public List<Entity> getLockTargetMulti() {
      return this.lockTargetM;
   }

   public void lockTargetMulti(Entity ent) {
      if (this.lockTargetM.size() < 6) {
         this.lockTargetM.add(ent);
      }
   }

   public void clearLockTargetsMulti() {
      this.lockTargetM.clear();
   }

   public List<Entity> getLockTargetMulti(int slot) {
      if (slot == 6) {
         return this.lockTargetMRA;
      } else if (slot == 7) {
         return this.lockTargetMLA;
      } else if (slot == 8) {
         return this.lockTargetMRS;
      } else {
         return slot == 9 ? this.lockTargetMLS : Collections.emptyList();
      }
   }

   public void syncTargetMulti(int slotSrc, int slotDst) {
      List<Entity> src = this.getLockTargetMulti(slotSrc);
      List<Entity> dst = this.getLockTargetMulti(slotDst);
      dst.clear();
      dst.addAll(src);
   }

   public List<Entity> consumeTargetMulti(int slot) {
      List<Entity> tgt = this.getLockTargetMulti(slot);
      if (tgt.isEmpty()) {
         return tgt;
      }

      ArrayList<Entity> res = new ArrayList<>(tgt);
      tgt.clear();
      return res;
   }

   public Entity consumeTargetMultiSingle(int slot) {
      List<Entity> tgt = this.getLockTargetMulti(slot);
      return tgt.isEmpty() ? null : tgt.remove(0);
   }

   public void lockTargetMulti(Entity ent, int slot, Pmvc01Entity mech) {
      if (slot == 6) {
         this.lockTargetMultiInternal(ent, this.lockTargetMRA, mech.getRightArmWeapon(), mech);
      } else if (slot == 7) {
         this.lockTargetMultiInternal(ent, this.lockTargetMLA, mech.getLeftArmWeapon(), mech);
      } else if (slot == 8) {
         this.lockTargetMultiInternal(ent, this.lockTargetMRS, mech.getRightShoulderWeapon(), mech);
      } else if (slot == 9) {
         this.lockTargetMultiInternal(ent, this.lockTargetMLS, mech.getLeftShoulderWeapon(), mech);
      }
   }

   private void lockTargetMultiInternal(Entity ent, List<Entity> list, ItemStack stack, Pmvc01Entity mech) {
      if (list.size() < Pmvc01Entity.getMultiLockTargetNum(stack) || mech.isGattai()) {
         list.add(ent);
      }
   }

   public void clearLockTargetsMulti(int slot) {
      if (slot == 6) {
         this.lockTargetMRA.clear();
      } else if (slot == 7) {
         this.lockTargetMLA.clear();
      } else if (slot == 8) {
         this.lockTargetMRS.clear();
      } else if (slot == 9) {
         this.lockTargetMLS.clear();
      }
   }

   public void unlockTargetMulti() {
      this.multiLockComplete = true;
   }

   public boolean consumeMultiLockComplete() {
      if (this.multiLockComplete) {
         this.multiLockComplete = false;
         return true;
      } else {
         return false;
      }
   }

   public void clearLockTargets() {
      this.unlockTargetSoft();
      this.unlockTargetHard();
      if (!this.lockTargetM.isEmpty()) {
         this.lockTargetM.clear();
      }

      if (!this.lockTargetMRA.isEmpty()) {
         this.lockTargetMRA.clear();
      }

      if (!this.lockTargetMLA.isEmpty()) {
         this.lockTargetMLA.clear();
      }

      if (!this.lockTargetMRS.isEmpty()) {
         this.lockTargetMRS.clear();
      }

      if (!this.lockTargetMLS.isEmpty()) {
         this.lockTargetMLS.clear();
      }
   }
}
