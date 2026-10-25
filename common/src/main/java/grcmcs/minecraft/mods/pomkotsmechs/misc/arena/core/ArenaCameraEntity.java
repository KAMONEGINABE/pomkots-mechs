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
   private Vec3 startPos = Vec3.ZERO;
   private Vec3 orbitStartPos = null;
   private float startYaw;
   private float startPitch;
   private Vec3 orbitCenter;
   private Vec3 lookTarget;
   private double orbitRadius;
   private double orbitStartAngle;

   public static Builder createMobAttributes() {
      return LivingEntity.createLivingAttributes().add(Attributes.ATTACK_KNOCKBACK).add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.MAX_HEALTH, 50.0);
   }

   public ArenaCameraEntity(EntityType<? extends LivingEntity> type, Level level) {
      super(type, level);
      this.setNoGravity(true);
      this.setInvulnerable(true);
   }

   public void initialize(ServerPlayer player, Entity target) {
      this.targetEntityId = target.getId();
      this.startPos = player.getEyePosition();
      this.startYaw = player.getYRot();
      this.startPitch = player.getXRot();
      this.moveTo(this.startPos.x, this.startPos.y, this.startPos.z, this.startYaw, this.startPitch);
   }

   public Iterable<ItemStack> getArmorSlots() {
      return NonNullList.withSize(4, ItemStack.EMPTY);
   }

   @NotNull
   public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
      return ItemStack.EMPTY;
   }

   public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
   }

   public void tick() {
      this.setInvisible(false);
      this.setNoGravity(true);
      super.tick();
      if (!this.level().isClientSide()) {
         Entity target = this.level().getEntity(this.targetEntityId);
         if (target == null) {
            this.discard();
         } else {
            this.tickOrbit(target);
            this.lifeTicks--;
            this.hasImpulse = true;
            if (this.lifeTicks <= 0) {
               this.discard();
            }
         }
      }
   }

   public HumanoidArm getMainArm() {
      return null;
   }

   private void tickOrbit(Entity target) {
      int elapsed = 300 - this.lifeTicks;
      Vec3 targetEye = target.getBoundingBox().getCenter();
      Vec3 forward = target.getLookAngle().normalize();
      Vec3 cameraPos;
      if (elapsed < 20) {
         float t = elapsed / 20.0F;
         Vec3 nearPos = targetEye.add(forward.scale(2.5)).add(0.0, 0.5, 0.0);
         cameraPos = this.startPos.lerp(nearPos, easeOutCubic(t));
      } else if (elapsed < 40) {
         float t = (elapsed - 20) / 20.0F;
         Vec3 from = targetEye.add(forward.scale(2.5)).add(0.0, 0.5, 0.0);
         Vec3 to = targetEye.add(forward.scale(6.0)).add(0.0, 2.0, 0.0);
         cameraPos = from.lerp(to, easeInOut(t));
         this.orbitStartPos = cameraPos;
      } else {
         if (this.orbitStartPos == null) {
            this.orbitStartPos = targetEye.add(forward.scale(6.0)).add(0.0, 2.0, 0.0);
         }

         float t = (elapsed - 40) / 60.0F;
         double dx = this.orbitStartPos.x - target.getX();
         double dz = this.orbitStartPos.z - target.getZ();
         double radius = Math.sqrt(dx * dx + dz * dz);
         double startAngle = Math.atan2(dz, dx);
         double angle = startAngle + Math.toRadians(t * 120.0);
         double x = target.getX() + Math.cos(angle) * radius;
         double z = target.getZ() + Math.sin(angle) * radius;
         double y = this.orbitStartPos.y;
         cameraPos = new Vec3(x, y, z);
      }

      this.moveTo(cameraPos.x, cameraPos.y, cameraPos.z);
      this.lookAt(Anchor.EYES, targetEye);
   }

   private void tickOrbit3(Entity target) {
      int elapsed = 300 - this.lifeTicks;
      Vec3 targetEye = target.getBoundingBox().getCenter();
      Vec3 cameraPos;
      if (elapsed < 20) {
         float t = elapsed / 20.0F;
         Vec3 nearPos = targetEye.add(-2.5, 0.5, 0.0);
         cameraPos = this.startPos.lerp(nearPos, easeOutCubic(t));
      } else if (elapsed < 40) {
         float t = (elapsed - 20) / 20.0F;
         Vec3 from = targetEye.add(-2.5, 0.5, 0.0);
         Vec3 to = targetEye.add(-6.0, 2.0, 0.0);
         cameraPos = from.lerp(to, easeInOut(t));
         this.orbitStartPos = cameraPos;
      } else {
         if (this.orbitStartPos == null) {
            this.orbitStartPos = targetEye.add(-6.0, 2.0, 0.0);
         }

         float t = (elapsed - 40) / 60.0F;
         double dx = this.orbitStartPos.x - target.getX();
         double dz = this.orbitStartPos.z - target.getZ();
         double radius = Math.sqrt(dx * dx + dz * dz);
         double startAngle = Math.atan2(dz, dx);
         double angle = startAngle + Math.toRadians(t * 120.0);
         double x = target.getX() + Math.cos(angle) * radius;
         double z = target.getZ() + Math.sin(angle) * radius;
         double y = this.orbitStartPos.y;
         cameraPos = new Vec3(x, y, z);
      }

      this.moveTo(cameraPos.x, cameraPos.y, cameraPos.z);
      this.lookAt(Anchor.EYES, targetEye);
   }

   private void tickOrbit2(Entity target) {
      int elapsed = 300 - this.lifeTicks;
      if (this.lookTarget == null) {
         this.lookTarget = target.position().add(0.0, target.getBbHeight() * 0.6, 0.0);
      }

      Vec3 cameraPos;
      if (elapsed < 20) {
         float t = elapsed / 20.0F;
         Vec3 nearPos = this.lookTarget.add(-2.5, 0.5, 0.0);
         cameraPos = this.startPos.lerp(nearPos, easeOutCubic(t));
      } else if (elapsed < 40) {
         float t = (elapsed - 20) / 20.0F;
         Vec3 from = this.lookTarget.add(-2.5, 0.5, 0.0);
         Vec3 to = this.lookTarget.add(-6.0, 2.0, 0.0);
         cameraPos = from.lerp(to, easeInOut(t));
         this.orbitStartPos = cameraPos;
         this.orbitCenter = this.lookTarget;
         this.orbitRadius = Math.sqrt(
            Math.pow(this.orbitStartPos.x - this.orbitCenter.x, 2.0) + Math.pow(this.orbitStartPos.z - this.orbitCenter.z, 2.0)
         );
         this.orbitStartAngle = Math.atan2(this.orbitStartPos.z - this.orbitCenter.z, this.orbitStartPos.x - this.orbitCenter.x);
      } else {
         if (this.orbitStartPos == null) {
            this.orbitStartPos = this.lookTarget.add(-6.0, 2.0, 0.0);
            this.orbitCenter = this.lookTarget;
            this.orbitRadius = Math.sqrt(
               Math.pow(this.orbitStartPos.x - this.orbitCenter.x, 2.0) + Math.pow(this.orbitStartPos.z - this.orbitCenter.z, 2.0)
            );
            this.orbitStartAngle = Math.atan2(this.orbitStartPos.z - this.orbitCenter.z, this.orbitStartPos.x - this.orbitCenter.x);
         }

         float t = (elapsed - 40) / 60.0F;
         double angle = this.orbitStartAngle + Math.toRadians(t * 120.0);
         double x = this.orbitCenter.x + Math.cos(angle) * this.orbitRadius;
         double z = this.orbitCenter.z + Math.sin(angle) * this.orbitRadius;
         double y = this.orbitStartPos.y;
         cameraPos = new Vec3(x, y, z);
      }

      this.moveTo(cameraPos.x, cameraPos.y, cameraPos.z);
      this.lookAt(Anchor.EYES, this.lookTarget);
   }

   private static float easeOutCubic(float t) {
      return 1.0F - (float)Math.pow(1.0F - t, 3.0);
   }

   private static float easeInOut(float t) {
      return t < 0.5F ? 2.0F * t * t : 1.0F - (float)Math.pow(-2.0F * t + 2.0F, 2.0) / 2.0F;
   }

   public boolean shouldBeSaved() {
      return false;
   }

   public boolean save(CompoundTag tag) {
      return false;
   }
}
