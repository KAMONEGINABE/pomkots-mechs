package grcmcs.minecraft.mods.pomkotsmechs.items;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

public final class MechCapsuleData {
   public static final String TAG_STORED_MECH = "StoredMech";
   private static final String TAG_DATA_VERSION = "DataVersion";
   private static final String TAG_ENTITY_TYPE = "EntityType";
   private static final String TAG_ENTITY_DATA = "EntityData";
   private static final int CURRENT_DATA_VERSION = 1;

   private MechCapsuleData() {
   }

   public static CompoundTag capture(Pmvc01Entity mech) {
      CompoundTag entityData = new CompoundTag();
      mech.saveWithoutId(entityData);
      sanitizeCapturedEntityData(entityData);
      ResourceLocation entityTypeId = EntityType.getKey(mech.getType());
      CompoundTag storedMech = new CompoundTag();
      storedMech.putInt("DataVersion", 1);
      storedMech.putString("EntityType", entityTypeId.toString());
      storedMech.put("EntityData", entityData);
      return storedMech;
   }

   public static Optional<Entity> spawn(ServerLevel level, CompoundTag storedMech, Vec3 spawnPosition, float spawnYaw) {
      if (storedMech.contains("EntityType", 8) && storedMech.contains("EntityData", 10)) {
         ResourceLocation entityTypeId = ResourceLocation.tryParse(storedMech.getString("EntityType"));
         if (entityTypeId == null) {
            return Optional.empty();
         }

         Optional<EntityType<?>> entityTypeOptional = EntityType.byString(entityTypeId.toString());
         if (entityTypeOptional.isEmpty()) {
            return Optional.empty();
         }

         EntityType<?> entityType = entityTypeOptional.get();
         Entity entity = entityType.create(level);
         if (entity == null) {
            return Optional.empty();
         }

         CompoundTag entityData = storedMech.getCompound("EntityData").copy();
         sanitizeBeforeSpawn(entityData);

         try {
            entity.load(entityData);
         } catch (RuntimeException exception) {
            entity.discard();
            return Optional.empty();
         }

         entity.moveTo(spawnPosition.x, spawnPosition.y, spawnPosition.z, spawnYaw, 0.0F);
         entity.setDeltaMovement(Vec3.ZERO);
         entity.fallDistance = 0.0F;
         if (!level.addFreshEntity(entity)) {
            entity.discard();
            return Optional.empty();
         } else {
            return Optional.of(entity);
         }
      } else {
         return Optional.empty();
      }
   }

   private static void sanitizeCapturedEntityData(CompoundTag tag) {
      removeIdentityData(tag);
      removeWorldStateData(tag);
      removeRelationshipData(tag);
   }

   private static void sanitizeBeforeSpawn(CompoundTag tag) {
      removeIdentityData(tag);
      removeWorldStateData(tag);
      removeRelationshipData(tag);
   }

   private static void removeIdentityData(CompoundTag tag) {
      tag.remove("UUID");
   }

   private static void removeWorldStateData(CompoundTag tag) {
      tag.remove("Pos");
      tag.remove("Motion");
      tag.remove("Rotation");
      tag.remove("FallDistance");
      tag.remove("OnGround");
      tag.remove("PortalCooldown");
      tag.remove("Fire");
      tag.remove("Air");
   }

   private static void removeRelationshipData(CompoundTag tag) {
      tag.remove("Passengers");
      tag.remove("Leash");
   }
}
