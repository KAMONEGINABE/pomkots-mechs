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
      mech.m_20240_(entityData);
      sanitizeCapturedEntityData(entityData);
      ResourceLocation entityTypeId = EntityType.m_20613_(mech.m_6095_());
      CompoundTag storedMech = new CompoundTag();
      storedMech.m_128405_("DataVersion", 1);
      storedMech.m_128359_("EntityType", entityTypeId.toString());
      storedMech.m_128365_("EntityData", entityData);
      return storedMech;
   }

   public static Optional<Entity> spawn(ServerLevel level, CompoundTag storedMech, Vec3 spawnPosition, float spawnYaw) {
      if (storedMech.m_128425_("EntityType", 8) && storedMech.m_128425_("EntityData", 10)) {
         ResourceLocation entityTypeId = ResourceLocation.m_135820_(storedMech.m_128461_("EntityType"));
         if (entityTypeId == null) {
            return Optional.empty();
         }

         Optional<EntityType<?>> entityTypeOptional = EntityType.m_20632_(entityTypeId.toString());
         if (entityTypeOptional.isEmpty()) {
            return Optional.empty();
         }

         EntityType<?> entityType = entityTypeOptional.get();
         Entity entity = entityType.m_20615_(level);
         if (entity == null) {
            return Optional.empty();
         }

         CompoundTag entityData = storedMech.m_128469_("EntityData").m_6426_();
         sanitizeBeforeSpawn(entityData);

         try {
            entity.m_20258_(entityData);
         } catch (RuntimeException exception) {
            entity.m_146870_();
            return Optional.empty();
         }

         entity.m_7678_(spawnPosition.f_82479_, spawnPosition.f_82480_, spawnPosition.f_82481_, spawnYaw, 0.0F);
         entity.m_20256_(Vec3.f_82478_);
         entity.f_19789_ = 0.0F;
         if (!level.m_7967_(entity)) {
            entity.m_146870_();
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
      tag.m_128473_("UUID");
   }

   private static void removeWorldStateData(CompoundTag tag) {
      tag.m_128473_("Pos");
      tag.m_128473_("Motion");
      tag.m_128473_("Rotation");
      tag.m_128473_("FallDistance");
      tag.m_128473_("OnGround");
      tag.m_128473_("PortalCooldown");
      tag.m_128473_("Fire");
      tag.m_128473_("Air");
   }

   private static void removeRelationshipData(CompoundTag tag) {
      tag.m_128473_("Passengers");
      tag.m_128473_("Leash");
   }
}
