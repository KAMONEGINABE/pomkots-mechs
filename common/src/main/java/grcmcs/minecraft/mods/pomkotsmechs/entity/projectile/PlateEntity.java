package grcmcs.minecraft.mods.pomkotsmechs.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PlateEntity extends Mob implements GeoEntity, GeoAnimatable {
   private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
   private static final int MAX_LIFE_TICKS = 300;
   private int breakTicks = 0;

   public static Builder createMobAttributes() {
      return LivingEntity.createLivingAttributes()
         .add(Attributes.ATTACK_KNOCKBACK)
         .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
         .add(Attributes.FOLLOW_RANGE, 200.0)
         .add(Attributes.MAX_HEALTH, 300.0);
   }

   public PlateEntity(EntityType<? extends PlateEntity> entityType, Level world) {
      super(entityType, world);
      this.setNoGravity(true);
      this.noCulling = true;
   }

   public void tick() {
      if (!this.level().isClientSide) {
      }

      super.tick();
      if (!this.level().isClientSide) {
         if (this.breakTicks > 0) {
            if (this.breakTicks++ >= 300) {
               this.discard();
            }

            if (!this.isNoGravity()) {
               this.push(0.0, -0.17640000343322754, 0.0);
            }
         } else {
            if (this.level() instanceof ServerLevel serverLevel) {
               moveEntityToNearestAir(serverLevel, this, 2);
            }

            snapYawTo90(this);
         }
      }
   }

   public static void snapYawTo90(Entity entity) {
      float yaw = entity.getYRot();
      yaw = Mth.wrapDegrees(yaw);
      float snappedYaw = Math.round(yaw / 90.0F) * 90.0F;
      snappedYaw = Mth.wrapDegrees(snappedYaw);
      entity.setYRot(snappedYaw);
      entity.setYBodyRot(snappedYaw);
      entity.setYHeadRot(snappedYaw);
      entity.yRotO = snappedYaw;
   }

   public static boolean moveEntityToNearestAir(ServerLevel level, Entity entity, int maxRadius) {
      if (level.noCollision(entity)) {
         return false;
      }

      BlockPos origin = entity.blockPosition();
      AABB box = entity.getBoundingBox();

      for (int r = 1; r <= maxRadius; r++) {
         for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
               for (int dz = -r; dz <= r; dz++) {
                  if (dx * dx + dy * dy + dz * dz <= r * r) {
                     BlockPos pos = origin.offset(dx, dy, dz);
                     double x = pos.getX() + 0.5;
                     double y = pos.getY();
                     double z = pos.getZ() + 0.5;
                     AABB movedBox = box.move(x - entity.getX(), y - entity.getY(), z - entity.getZ());
                     if (level.noCollision(entity, movedBox)) {
                        entity.teleportTo(x, y, z);
                        entity.setDeltaMovement(Vec3.ZERO);
                        return true;
                     }
                  }
               }
            }
         }
      }

      return false;
   }

   public boolean hurt(DamageSource source, float amount) {
      if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel && this.breakTicks == 0) {
         this.triggerAnim("action_controller", "break");
         this.breakTicks = 1;
         moveEntityToNearestAir(serverLevel, this, 2);
         this.setOnGround(false);
         this.setNoGravity(false);
         Vec3 v = this.getDeltaMovement();
         if (v.y >= 0.0) {
            this.setDeltaMovement(v.x, -0.08, v.z);
         }
      }

      return super.hurt(source, amount);
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "action_controller", state -> PlayState.STOP)
               .triggerableAnim("break", RawAnimation.begin().thenPlayAndHold("animation.bossbox.break"))
         }
      );
   }

   public boolean isPersistenceRequired() {
      return true;
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.geoCache;
   }

   public boolean ignoreExplosion() {
      return false;
   }

   public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
      return true;
   }

   public boolean shouldRenderAtSqrDistance(double d) {
      return true;
   }

   public Iterable<ItemStack> getArmorSlots() {
      return NonNullList.withSize(4, ItemStack.EMPTY);
   }

   public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
      return ItemStack.EMPTY;
   }

   public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
   }

   public boolean causeFallDamage(float f1, float f2, DamageSource damageSource) {
      return false;
   }

   public HumanoidArm getMainArm() {
      return null;
   }

   public boolean isPushable() {
      return false;
   }

   protected void checkInsideBlocks() {
   }
}
