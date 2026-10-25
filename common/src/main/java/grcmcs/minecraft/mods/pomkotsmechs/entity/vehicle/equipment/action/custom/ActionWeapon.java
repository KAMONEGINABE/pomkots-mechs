package grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.custom;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.Action;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats.MechStats;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ActionWeapon extends Action {
   private BasePartsItem.WeaponInterface weapon;
   private Motion motion;
   private int maxActionTick;
   private int fireStartTick;
   private int currentActionTick = -1;
   private Pmvc01Entity owner;
   private int weaponItemSlot;
   private ItemStack itemStack;
   private boolean isToggleOnStart = false;

   public ActionWeapon(Pmvc01Entity owner, int weaponItemSlot) {
      super(1, 1, 1);
      this.owner = owner;
      this.weaponItemSlot = weaponItemSlot;
   }

   public void setWeapon(BasePartsItem.WeaponInterface w, ItemStack itemStack) {
      this.weapon = w;
      this.motion = w.getMotion();
      this.maxActionTick = this.motion.getMaxActionTick();
      this.fireStartTick = this.getSmallest(this.motion.getActualFireTick());
      this.maxCoolTime = w.getCoolTime();
      this.itemStack = itemStack;
      this.isToggleOnStart = false;
      this.reset();
   }

   private int getSmallest(Set<Integer> set) {
      int val = Integer.MAX_VALUE;

      for (int i : set) {
         if (i < val) {
            val = i;
         }
      }

      return val;
   }

   public Motion getMotion() {
      return this.motion;
   }

   @Override
   public void tick() {
      if (this.currentCoolTime > 0) {
         this.currentCoolTime--;
      }

      if (this.isInAction()) {
         this.weapon.tickWeaponInAction(new ActionWeapon.WeaponMechInterface(this.owner, this), this.currentActionTick, this.isOnFire());
         if (this.isOnFire() && this.getMotion() == Motion.PUNCH || this.getMotion() == Motion.SABER) {
            this.owner.setSuperBoost(false);
         }

         if (this.canContinue()) {
            this.currentActionTick++;
         } else {
            this.reset();
         }
      }
   }

   public int getFireStartTick() {
      return this.fireStartTick;
   }

   public int getCurrentActionTick() {
      return this.currentActionTick;
   }

   public int getWeaponItemSlot() {
      return this.weaponItemSlot;
   }

   protected boolean concurrentAvailable() {
      return this.motion.concurrentAvailable();
   }

   protected boolean canContinue() {
      if (this.motion.getType().equals(Motion.MotionType.CONTINUOUS)) {
         return this.owner.canWork(false);
      } else {
         return this.motion.getType().equals(Motion.MotionType.CHARGE)
            ? this.currentActionTick <= this.fireStartTick + this.maxActionTick
            : this.currentActionTick <= this.maxActionTick;
      }
   }

   public boolean canStartAction() {
      return !this.isInCooltime() && !this.isInAction();
   }

   @Override
   public boolean startAction() {
      if (this.getMotion().getType() == Motion.MotionType.TOGGLE) {
         this.isToggleOnStart = !this.isToggleOn();
      }

      this.currentActionTick = 0;
      this.currentCoolTime = this.maxCoolTime;
      this.weapon.startUsing(new ActionWeapon.WeaponMechInterface(this.owner, this));
      return true;
   }

   @Override
   public void fireAction() {
      if (this.motion.getType().equals(Motion.MotionType.CHARGE)) {
         this.fireStartTick = this.currentActionTick;
      }
   }

   @Override
   public boolean isOnStart() {
      return this.currentActionTick == 0;
   }

   @Override
   public boolean isInAction() {
      return this.currentActionTick >= 0;
   }

   @Override
   public boolean isInCooltime() {
      return this.currentCoolTime > 0;
   }

   @Override
   public boolean isCharging() {
      return this.currentActionTick < this.fireStartTick;
   }

   @Override
   public boolean isInFire() {
      return this.currentActionTick >= this.fireStartTick;
   }

   @Override
   public boolean isOnFire() {
      if (this.motion.getType().equals(Motion.MotionType.CONTINUOUS)
         && this.isInFire()
         && (this.currentActionTick - this.fireStartTick) % this.motion.getInterval() == 0) {
         return true;
      } else {
         return this.motion.getType().equals(Motion.MotionType.CHARGE)
            ? this.currentActionTick == this.fireStartTick + 5
            : this.motion.getActualFireTick().contains(this.currentActionTick);
      }
   }

   public boolean isToggleOnStart() {
      return this.isToggleOnStart;
   }

   public boolean isToggleOn() {
      return this.weapon.isToggleOn(new ActionWeapon.WeaponMechInterface(this.owner, this));
   }

   @Override
   public void reset() {
      this.currentChargeTime = 0;
      this.currentFireTime = 0;
      this.currentActionTick = -1;
      if (this.motion.getType().equals(Motion.MotionType.CHARGE)) {
         this.fireStartTick = this.getSmallest(this.motion.getActualFireTick());
      }

      this.weapon.endUsing(new ActionWeapon.WeaponMechInterface(this.owner, this));
      this.isToggleOnStart = false;
   }

   public Pmvc01Entity getOwner() {
      return this.owner;
   }

   public ItemStack getItemStack() {
      return this.itemStack;
   }

   public static class DefaultDummyWeapon implements BasePartsItem.WeaponInterface {
      @Override
      public void tickWeaponInAction(ActionWeapon.WeaponMechInterface mechInterface, int tick, boolean isOnFire) {
         Level world = mechInterface.getWorld();
         if (isOnFire) {
            Entity driver = mechInterface.getDrivingPassenger();
            Vec3 pilePos1 = new Vec3(4.5 * mechInterface.isRight(), 4.0, 10.0)
               .m_82524_((float)Math.toRadians(-1.0 * mechInterface.getYRot()))
               .m_82549_(mechInterface.position());
            Vec3 pilePos2 = new Vec3(-2 * mechInterface.isRight(), -4.0, -4.0)
               .m_82524_((float)Math.toRadians(-1.0 * mechInterface.getYRot()))
               .m_82549_(mechInterface.position());
            Vec3 kbVel = new Vec3(0.0, 0.0, -1.0).m_82524_((float)Math.toRadians(-1.0 * mechInterface.getYRot()));

            for (Entity ent : world.m_45933_(null, new AABB(pilePos1, pilePos2))) {
               if (!mechInterface.isSelf(ent) && ent instanceof LivingEntity le) {
                  if (!world.m_5776_()) {
                     le.m_147240_(2.0, kbVel.f_82479_, kbVel.f_82481_);
                     DamageSource ds;
                     if (driver instanceof Player p) {
                        ds = mechInterface.damageSources().m_269075_(p);
                     } else {
                        ds = mechInterface.damageSources().m_269264_();
                     }

                     le.m_6469_(ds, 20.0F);
                  } else {
                     mechInterface.addHitParticles(le);
                  }
               }
            }

            if (world.f_46443_) {
               mechInterface.playSoundEffect((SoundEvent)PomkotsMechs.SE_PILEBUNKER_EVENT.get());
            }
         }
      }

      @Override
      public BasePartsItem.WeaponInterface.WeaponAttachPoint getWeaponAttachPoint() {
         return BasePartsItem.WeaponInterface.WeaponAttachPoint.ATTACH_POINT_ALL;
      }

      @Override
      public Motion getMotion() {
         return Motion.PUNCH;
      }

      @Override
      public int maxMultiLockNum() {
         return 0;
      }

      @Override
      public boolean isSoftLockEnabled() {
         return false;
      }

      @Override
      public int getCoolTime() {
         return 100;
      }
   }

   public static class WeaponMechInterface {
      private Pmvc01Entity owner;
      private ActionWeapon action;

      private WeaponMechInterface(Pmvc01Entity owner, ActionWeapon action) {
         this.owner = owner;
         this.action = action;
      }

      public Level getWorld() {
         return this.owner.m_9236_();
      }

      public Vec3 getOffset() {
         return this.owner.getShootingOffset();
      }

      public float getYRot() {
         return this.owner.m_146908_();
      }

      public Player getPlayer() {
         return this.owner.getDrivingPassenger() instanceof Player p ? p : null;
      }

      public ItemStack getItemStack() {
         return this.action.itemStack;
      }

      public int isRight() {
         return this.action.weaponItemSlot != 6 && this.action.weaponItemSlot != 8 ? 1 : -1;
      }

      public int getAttachPoint() {
         return switch (this.action.weaponItemSlot) {
            case 6 -> 0;
            case 7 -> 1;
            case 8 -> 2;
            case 9 -> 3;
            default -> 0;
         };
      }

      public boolean consumeEnergy(int num) {
         return this.owner.consumeEnergy(num);
      }

      public boolean consumeAmmo(int num) {
         return this.owner.consumeBullet(num, this.action.itemStack, this.getWeaponItemSlot());
      }

      public boolean consumeAmmoFromServerSide(int num) {
         return this.owner.consumeBulletFromServerSide(num, this.action.itemStack, this.getWeaponItemSlot());
      }

      public int getWeaponItemSlot() {
         return this.action.weaponItemSlot;
      }

      public Pmvc01Entity getMechEntity() {
         return this.owner;
      }

      public boolean isToggleOnStart() {
         return this.action.isToggleOnStart();
      }

      public boolean isSuperBoost() {
         return this.owner.isSuperBoost();
      }

      public float[] getShootingAngle(Entity ent, boolean useTarget) {
         return this.owner.getShootingAngle(ent, useTarget, true);
      }

      public float getFallFlyingTicks() {
         return this.owner.m_21256_();
      }

      public List<Entity> getMultiLockTargets() {
         return this.owner.getLockTargets().getLockTargetMulti(this.action.getWeaponItemSlot());
      }

      public List<Entity> consumeMultiLockTargets() {
         return this.owner.getLockTargets().consumeTargetMulti(this.action.getWeaponItemSlot());
      }

      public Entity consumeMultiLockTargetsSingle() {
         return this.owner.getLockTargets().consumeTargetMultiSingle(this.action.getWeaponItemSlot());
      }

      public void playSoundEffect(SoundEvent event) {
         this.owner.playSoundPublic(event);
      }

      public void addHitParticles(Entity e) {
         this.owner.addHitParticles(e);
      }

      public Vec3 position() {
         return this.owner.m_20182_();
      }

      public DamageSources damageSources() {
         return this.owner.m_269291_();
      }

      public LivingEntity getDrivingPassenger() {
         return this.owner.getDrivingPassenger();
      }

      public boolean isSelf(Entity ent) {
         return this.owner.isSelfPublic(ent);
      }

      public boolean onGround() {
         return this.owner.m_20096_();
      }

      public float applySkillDamageModifier(float baseDamage, BasePartsItem.WeaponCategory category) {
         MechStats stats = this.owner.getModifiedMechStatus().stats();

         double modifier = switch (category) {
            case RIFLE -> stats.rifleDamage();
            case MACHINE_GUN -> stats.machineGunDamage();
            case SHOT_GUN -> stats.shotgunDamage();
            case GRENADE -> stats.grenadeDamage();
            case MELEE -> stats.meleeDamage();
            case MISSILE -> stats.missileDamage();
            case MISC, NONE -> 1.0;
         };
         modifier += stats.damageAll() - 1.0;
         return (float)(baseDamage * modifier);
      }

      public void knockBack(float strength) {
         Vec3 knockBackvel = new Vec3(0.0, 0.0, -strength).m_82524_((float)Math.toRadians(-1.0 * this.owner.m_146908_()));
         this.owner.m_246865_(knockBackvel);
      }

      public void addVelocity(float pitch, float yaw, float strength) {
         float x = -Mth.m_14031_(yaw * (float) (Math.PI / 180.0)) * Mth.m_14089_(pitch * (float) (Math.PI / 180.0));
         float y = -Mth.m_14031_(pitch * (float) (Math.PI / 180.0));
         float z = Mth.m_14089_(yaw * (float) (Math.PI / 180.0)) * Mth.m_14089_(pitch * (float) (Math.PI / 180.0));
         if (!this.owner.m_20096_()) {
            strength *= 0.5F;
         }

         Vec3 acceleration = new Vec3(x, y, z).m_82490_(strength);
         this.owner.m_20256_(acceleration);
      }

      public Vec3 getOffsetByLookingDirection(int distance) {
         LivingEntity entity = this.owner.getDrivingPassenger();
         if (entity != null) {
            Vec3 lookDirection = entity.m_20154_();
            Vec3 playerPosition = new Vec3(0.0, 6.0, 0.0);
            return playerPosition.m_82520_(0.0, lookDirection.f_82480_ * distance, (1.0 - Math.abs(lookDirection.f_82480_)) * distance);
         } else {
            return null;
         }
      }

      public void breakBlocksCube(int radius) {
         this.breakBlocksCube(radius, 8, new Vec3(0.0, 0.0, 5.0));
      }

      public void breakBlocksCube(int radius, int height, Vec3 offset) {
         BlockPos curBP = this.owner.m_20183_();
         Vec3 vec = offset;
         vec = vec.m_82524_((float)Math.toRadians(-1.0 * this.getYRot()));
         BlockPos pos = new BlockPos(curBP.m_123341_() + (int)vec.f_82479_, curBP.m_123342_() + (int)vec.f_82480_, curBP.m_123343_() + (int)vec.f_82481_);

         for (int y = 0; y < height; y++) {
            for (int x = -radius; x <= radius; x++) {
               for (int z = -radius; z <= radius; z++) {
                  BlockPos blockPos = pos.m_7918_(x, y, z);
                  BlockState state = this.owner.m_9236_().m_8055_(blockPos);
                  if (!state.m_60795_() && x * x + z * z <= radius * radius) {
                     Utils.destroyBlock(this.owner.m_9236_(), blockPos, PomkotsMechs.CONFIG.dropItemsWhenDestroyBlock);
                  }
               }
            }
         }
      }

      public void breakBlockSphere(int radius, Vec3 offset) {
         int radiusSquared = radius * radius;
         BlockPos curBP = this.owner.m_20183_();
         Vec3 vec = offset;
         vec = vec.m_82524_((float)Math.toRadians(-1.0 * this.getYRot()));
         BlockPos pos = new BlockPos(curBP.m_123341_() + (int)vec.f_82479_, curBP.m_123342_() + (int)vec.f_82480_, curBP.m_123343_() + (int)vec.f_82481_);

         for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
               for (int z = -radius; z <= radius; z++) {
                  if (x * x + y * y + z * z <= radiusSquared) {
                     BlockPos targetPos = pos.m_7918_(x, y, z);
                     BlockState state = this.owner.m_9236_().m_8055_(targetPos);
                     if (!state.m_60795_()) {
                        Utils.destroyBlock(this.owner.m_9236_(), targetPos, PomkotsMechs.CONFIG.dropItemsWhenDestroyBlock);
                     }
                  }
               }
            }
         }
      }
   }
}
