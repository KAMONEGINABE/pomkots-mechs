package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action;

public class Action {
   public int maxCoolTime;
   public int maxChargeTime;
   public int maxFireTime;
   public int currentCoolTime = 0;
   public int currentChargeTime = 0;
   public int currentFireTime = 0;
   public boolean isOnEnd = false;

   public Action(int maxCoolTime, int maxChargeTime, int maxFireTime) {
      this.maxCoolTime = maxCoolTime;
      this.maxChargeTime = maxChargeTime;
      this.maxFireTime = maxFireTime;
   }

   public void tick() {
      this.isOnEnd = false;
      if (this.currentCoolTime > 0) {
         this.currentCoolTime--;
      }

      if (this.currentChargeTime > 0 && this.maxChargeTime != 0) {
         this.currentChargeTime++;
         if (this.currentChargeTime == this.maxChargeTime) {
            this.fireAction();
         }
      } else if (this.currentFireTime > 0) {
         this.currentFireTime++;
         if (this.currentFireTime == this.maxFireTime) {
            this.currentFireTime = 0;
            this.isOnEnd = true;
         }
      }
   }

   public void fireAction() {
      this.currentChargeTime = 0;
      this.currentFireTime = 1;
   }

   public boolean isInAction() {
      return this.currentChargeTime > 0 || this.currentFireTime > 0;
   }

   public boolean isInFire() {
      return this.currentFireTime > 0;
   }

   public boolean isInCooltime() {
      return this.currentCoolTime > 0;
   }

   public boolean isCharging() {
      return this.currentChargeTime > 0;
   }

   public boolean isFiring() {
      return this.currentFireTime > 0;
   }

   public boolean isOnFire() {
      return this.currentFireTime == 1;
   }

   public boolean startAction() {
      if (this.maxCoolTime == 0 && this.isOnEnd) {
         this.currentChargeTime = 0;
         this.currentFireTime = 1;
         this.isOnEnd = false;
         return true;
      } else if (!this.isInAction() && !this.isInCooltime()) {
         this.startActionInternal();
         return true;
      } else {
         return false;
      }
   }

   public boolean isOnStart() {
      return this.maxChargeTime > 0 ? this.currentChargeTime == 1 : this.currentFireTime == 1;
   }

   private void startActionInternal() {
      if (this.maxChargeTime > 0) {
         this.currentChargeTime = 1;
      } else {
         this.currentFireTime = 1;
      }

      this.currentCoolTime = this.maxCoolTime;
   }

   public void reset() {
      this.currentCoolTime = 0;
      this.currentChargeTime = 0;
      this.currentFireTime = 0;
   }
}
