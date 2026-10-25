package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.core;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class ArenaCameraEntity extends LivingEntity {
   private static final int MAX_LIFE_TICKS = 300;
   private int targetEntityId = -1;
   private int lifeTicks = 300;
   private Vec3 startPos = Vec3.f_82478_;
   private Vec3 orbitStartPos = null;
   private float startYaw;
   private float startPitch;
   private Vec3 orbitCenter;
   private Vec3 lookTarget;
   private double orbitRadius;
   private double orbitStartAngle;

   public static Builder createMobAttributes() {
      return LivingEntity.m_21183_().m_22266_(Attributes.f_22282_).m_22268_(Attributes.f_22278_, 1.0).m_22268_(Attributes.f_22276_, 50.0);
   }

   public ArenaCameraEntity(EntityType<? extends LivingEntity> type, Level level) {
      super(type, level);
      this.m_20242_(true);
      this.m_20331_(true);
   }

   public void initialize(ServerPlayer player, Entity target) {
      this.targetEntityId = target.m_19879_();
      this.startPos = player.m_146892_();
      this.startYaw = player.m_146908_();
      this.startPitch = player.m_146909_();
      this.m_7678_(this.startPos.f_82479_, this.startPos.f_82480_, this.startPos.f_82481_, this.startYaw, this.startPitch);
   }

   public Iterable<ItemStack> m_6168_() {
      return NonNullList.m_122780_(4, ItemStack.f_41583_);
   }

   @NotNull
   public ItemStack m_6844_(EquipmentSlot equipmentSlot) {
      return ItemStack.f_41583_;
   }

   public void m_8061_(EquipmentSlot equipmentSlot, ItemStack itemStack) {
   }

   public void m_8119_() {
      this.m_6842_(false);
      this.m_20242_(true);
      super.m_8119_();
      if (!this.m_9236_().m_5776_()) {
         Entity target = this.m_9236_().m_6815_(this.targetEntityId);
         if (target == null) {
            this.m_146870_();
         } else {
            this.tickOrbit(target);
            this.lifeTicks--;
            this.f_19812_ = true;
            if (this.lifeTicks <= 0) {
               this.m_146870_();
            }
         }
      }
   }

   public HumanoidArm m_5737_() {
      return null;
   }

   private void tickOrbit(Entity target) {
      int elapsed = 300 - this.lifeTicks;
      Vec3 targetEye = target.m_20191_().m_82399_();
      Vec3 forward = target.m_20154_().m_82541_();
      Vec3 cameraPos;
      if (elapsed < 20) {
         float t = elapsed / 20.0F;
         Vec3 nearPos = targetEye.m_82549_(forward.m_82490_(2.5)).m_82520_(0.0, 0.5, 0.0);
         cameraPos = this.startPos.m_165921_(nearPos, easeOutCubic(t));
      } else if (elapsed < 40) {
         float t = (elapsed - 20) / 20.0F;
         Vec3 from = targetEye.m_82549_(forward.m_82490_(2.5)).m_82520_(0.0, 0.5, 0.0);
         Vec3 to = targetEye.m_82549_(forward.m_82490_(6.0)).m_82520_(0.0, 2.0, 0.0);
         cameraPos = from.m_165921_(to, easeInOut(t));
         this.orbitStartPos = cameraPos;
      } else {
         if (this.orbitStartPos == null) {
            this.orbitStartPos = targetEye.m_82549_(forward.m_82490_(6.0)).m_82520_(0.0, 2.0, 0.0);
         }

         float t = (elapsed - 40) / 60.0F;
         double dx = this.orbitStartPos.f_82479_ - target.m_20185_();
         double dz = this.orbitStartPos.f_82481_ - target.m_20189_();
         double radius = Math.sqrt(dx * dx + dz * dz);
         double startAngle = Math.atan2(dz, dx);
         double angle = startAngle + Math.toRadians(t * 120.0);
         double x = target.m_20185_() + Math.cos(angle) * radius;
         double z = target.m_20189_() + Math.sin(angle) * radius;
         double y = this.orbitStartPos.f_82480_;
         cameraPos = new Vec3(x, y, z);
      }

      this.m_6027_(cameraPos.f_82479_, cameraPos.f_82480_, cameraPos.f_82481_);
      this.m_7618_(Anchor.EYES, targetEye);
   }

   private void tickOrbit3(Entity target) {
      int elapsed = 300 - this.lifeTicks;
      Vec3 targetEye = target.m_20191_().m_82399_();
      Vec3 cameraPos;
      if (elapsed < 20) {
         float t = elapsed / 20.0F;
         Vec3 nearPos = targetEye.m_82520_(-2.5, 0.5, 0.0);
         cameraPos = this.startPos.m_165921_(nearPos, easeOutCubic(t));
      } else if (elapsed < 40) {
         float t = (elapsed - 20) / 20.0F;
         Vec3 from = targetEye.m_82520_(-2.5, 0.5, 0.0);
         Vec3 to = targetEye.m_82520_(-6.0, 2.0, 0.0);
         cameraPos = from.m_165921_(to, easeInOut(t));
         this.orbitStartPos = cameraPos;
      } else {
         if (this.orbitStartPos == null) {
            this.orbitStartPos = targetEye.m_82520_(-6.0, 2.0, 0.0);
         }

         float t = (elapsed - 40) / 60.0F;
         double dx = this.orbitStartPos.f_82479_ - target.m_20185_();
         double dz = this.orbitStartPos.f_82481_ - target.m_20189_();
         double radius = Math.sqrt(dx * dx + dz * dz);
         double startAngle = Math.atan2(dz, dx);
         double angle = startAngle + Math.toRadians(t * 120.0);
         double x = target.m_20185_() + Math.cos(angle) * radius;
         double z = target.m_20189_() + Math.sin(angle) * radius;
         double y = this.orbitStartPos.f_82480_;
         cameraPos = new Vec3(x, y, z);
      }

      this.m_6027_(cameraPos.f_82479_, cameraPos.f_82480_, cameraPos.f_82481_);
      this.m_7618_(Anchor.EYES, targetEye);
   }

   private void tickOrbit2(Entity target) {
      int elapsed = 300 - this.lifeTicks;
      if (this.lookTarget == null) {
         this.lookTarget = target.m_20182_().m_82520_(0.0, target.m_20206_() * 0.6, 0.0);
      }

      Vec3 cameraPos;
      if (elapsed < 20) {
         float t = elapsed / 20.0F;
         Vec3 nearPos = this.lookTarget.m_82520_(-2.5, 0.5, 0.0);
         cameraPos = this.startPos.m_165921_(nearPos, easeOutCubic(t));
      } else if (elapsed < 40) {
         float t = (elapsed - 20) / 20.0F;
         Vec3 from = this.lookTarget.m_82520_(-2.5, 0.5, 0.0);
         Vec3 to = this.lookTarget.m_82520_(-6.0, 2.0, 0.0);
         cameraPos = from.m_165921_(to, easeInOut(t));
         this.orbitStartPos = cameraPos;
         this.orbitCenter = this.lookTarget;
         this.orbitRadius = Math.sqrt(
            Math.pow(this.orbitStartPos.f_82479_ - this.orbitCenter.f_82479_, 2.0) + Math.pow(this.orbitStartPos.f_82481_ - this.orbitCenter.f_82481_, 2.0)
         );
         this.orbitStartAngle = Math.atan2(this.orbitStartPos.f_82481_ - this.orbitCenter.f_82481_, this.orbitStartPos.f_82479_ - this.orbitCenter.f_82479_);
      } else {
         if (this.orbitStartPos == null) {
            this.orbitStartPos = this.lookTarget.m_82520_(-6.0, 2.0, 0.0);
            this.orbitCenter = this.lookTarget;
            this.orbitRadius = Math.sqrt(
               Math.pow(this.orbitStartPos.f_82479_ - this.orbitCenter.f_82479_, 2.0) + Math.pow(this.orbitStartPos.f_82481_ - this.orbitCenter.f_82481_, 2.0)
            );
            this.orbitStartAngle = Math.atan2(this.orbitStartPos.f_82481_ - this.orbitCenter.f_82481_, this.orbitStartPos.f_82479_ - this.orbitCenter.f_82479_);
         }

         float t = (elapsed - 40) / 60.0F;
         double angle = this.orbitStartAngle + Math.toRadians(t * 120.0);
         double x = this.orbitCenter.f_82479_ + Math.cos(angle) * this.orbitRadius;
         double z = this.orbitCenter.f_82481_ + Math.sin(angle) * this.orbitRadius;
         double y = this.orbitStartPos.f_82480_;
         cameraPos = new Vec3(x, y, z);
      }

      this.m_6027_(cameraPos.f_82479_, cameraPos.f_82480_, cameraPos.f_82481_);
      this.m_7618_(Anchor.EYES, this.lookTarget);
   }

   private static float easeOutCubic(float t) {
      return 1.0F - (float)Math.pow(1.0F - t, 3.0);
   }

   private static float easeInOut(float t) {
      return t < 0.5F ? 2.0F * t * t : 1.0F - (float)Math.pow(-2.0F * t + 2.0F, 2.0) / 2.0F;
   }

   public boolean m_142391_() {
      return false;
   }

   public boolean m_20223_(CompoundTag tag) {
      return false;
   }
}
