package grcmcs.minecraft.mods.pomkotsmechs.misc.scan;

import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ScanPulseEntity extends Entity {
   private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.defineId(ScanPulseEntity.class, EntityDataSerializers.OPTIONAL_UUID);
   private static final EntityDataAccessor<String> OWNER_TEAM = SynchedEntityData.defineId(ScanPulseEntity.class, EntityDataSerializers.STRING);
   private static final EntityDataAccessor<Byte> VISIBILITY = SynchedEntityData.defineId(ScanPulseEntity.class, EntityDataSerializers.BYTE);
   private static final EntityDataAccessor<Float> MAX_RADIUS = SynchedEntityData.defineId(ScanPulseEntity.class, EntityDataSerializers.FLOAT);
   private static final EntityDataAccessor<Integer> EXPANSION_TICKS = SynchedEntityData.defineId(ScanPulseEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> HIGHLIGHT_TICKS = SynchedEntityData.defineId(ScanPulseEntity.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Long> START_GAME_TIME = SynchedEntityData.defineId(ScanPulseEntity.class, EntityDataSerializers.LONG);

   public ScanPulseEntity(EntityType<? extends ScanPulseEntity> type, Level level) {
      super(type, level);
      this.noCulling = true;
      this.noPhysics = true;
      this.setNoGravity(true);
   }

   protected void defineSynchedData() {
      this.entityData.define(OWNER_UUID, Optional.empty());
      this.entityData.define(OWNER_TEAM, "");
      this.entityData.define(VISIBILITY, ScanVisibility.OWNER.id());
      this.entityData.define(MAX_RADIUS, 64.0F);
      this.entityData.define(EXPANSION_TICKS, 60);
      this.entityData.define(HIGHLIGHT_TICKS, 100);
      this.entityData.define(START_GAME_TIME, 0L);
   }

   public void initialize(Player owner, ScanVisibility visibility, float maxRadius, int expansionTicks, int highlightTicks) {
      this.setOwnerUUID(owner.getUUID());
      this.setVisibility(visibility);
      this.setMaxRadius(maxRadius);
      this.setExpansionTicks(expansionTicks);
      this.setHighlightTicks(highlightTicks);
      this.setOwnerTeam(owner.getTeam() != null ? owner.getTeam().getName() : "");
      this.setStartGameTime(this.level().getGameTime());
   }

   public void tick() {
      if (this.firstTick && this.level().isClientSide) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_SCAN2.get(), 0.7F);
      }

      super.tick();
      if (!this.level().isClientSide) {
         long elapsed = this.level().getGameTime() - this.getStartGameTime();
         if (elapsed > this.getExpansionTicks() + 2L) {
            this.discard();
         }
      }
   }

   protected void playSoundEffect(SoundEvent event) {
      this.playSoundEffect(event, 1.0F);
   }

   protected void playSoundEffect(SoundEvent event, float volume) {
      float soundEffectVolume = this.computeVolume(100.0);
      if (soundEffectVolume > 0.0F) {
         this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), event, SoundSource.PLAYERS, volume * soundEffectVolume, 1.0F, false);
      }
   }

   private float computeVolume(double maxDistance) {
      Minecraft mc = Minecraft.getInstance();
      LocalPlayer player = mc.player;
      if (player == null) {
         return 0.0F;
      }

      double distance = player.distanceTo(this);
      if (distance > maxDistance) {
         return 0.0F;
      }

      float volume = 1.0F - (float)(distance / maxDistance);
      return Mth.clamp(volume, 0.0F, 1.0F);
   }

   public float getRadius(float partialTick) {
      double elapsed = (float)(this.level().getGameTime() - this.getStartGameTime()) + partialTick;
      float progress = (float)Math.max(0.0, Math.min(1.0, elapsed / this.getExpansionTicks()));
      float eased = 1.0F - (1.0F - progress) * (1.0F - progress);
      return this.getMaxRadius() * eased;
   }

   public float getPreviousRadius() {
      double elapsed = this.level().getGameTime() - this.getStartGameTime() - 1.0;
      float progress = (float)Math.max(0.0, Math.min(1.0, elapsed / this.getExpansionTicks()));
      float eased = 1.0F - (1.0F - progress) * (1.0F - progress);
      return this.getMaxRadius() * eased;
   }

   public boolean isVisibleTo(Player player) {
      return switch (this.getVisibility()) {
         case ALL -> true;
         case OWNER -> this.getOwnerUUID() != null && this.getOwnerUUID().equals(player.getUUID());
         case TEAM -> this.isOwnerOrSameTeam(player);
      };
   }

   private boolean isOwnerOrSameTeam(Player player) {
      UUID ownerUUID = this.getOwnerUUID();
      if (ownerUUID != null && ownerUUID.equals(player.getUUID())) {
         return true;
      }

      String ownerTeam = this.getOwnerTeam();
      return !ownerTeam.isEmpty() && player.getTeam() != null && ownerTeam.equals(player.getTeam().getName());
   }

   @Nullable
   public UUID getOwnerUUID() {
      return (UUID)((Optional)this.entityData.get(OWNER_UUID)).orElse(null);
   }

   public void setOwnerUUID(@Nullable UUID uuid) {
      this.entityData.set(OWNER_UUID, Optional.ofNullable(uuid));
   }

   public String getOwnerTeam() {
      return (String)this.entityData.get(OWNER_TEAM);
   }

   public void setOwnerTeam(String teamName) {
      this.entityData.set(OWNER_TEAM, teamName != null ? teamName : "");
   }

   public ScanVisibility getVisibility() {
      return ScanVisibility.fromId((Byte)this.entityData.get(VISIBILITY));
   }

   public void setVisibility(ScanVisibility visibility) {
      this.entityData.set(VISIBILITY, visibility.id());
   }

   public float getMaxRadius() {
      return (Float)this.entityData.get(MAX_RADIUS);
   }

   public void setMaxRadius(float radius) {
      this.entityData.set(MAX_RADIUS, Math.max(0.0F, radius));
   }

   public int getExpansionTicks() {
      return (Integer)this.entityData.get(EXPANSION_TICKS);
   }

   public void setExpansionTicks(int ticks) {
      this.entityData.set(EXPANSION_TICKS, Math.max(1, ticks));
   }

   public int getHighlightTicks() {
      return (Integer)this.entityData.get(HIGHLIGHT_TICKS);
   }

   public void setHighlightTicks(int ticks) {
      this.entityData.set(HIGHLIGHT_TICKS, Math.max(1, ticks));
   }

   public long getStartGameTime() {
      return (Long)this.entityData.get(START_GAME_TIME);
   }

   public void setStartGameTime(long gameTime) {
      this.entityData.set(START_GAME_TIME, gameTime);
   }

   protected void addAdditionalSaveData(CompoundTag tag) {
      UUID ownerUUID = this.getOwnerUUID();
      if (ownerUUID != null) {
         tag.putUUID("OwnerUUID", ownerUUID);
      }

      tag.putString("OwnerTeam", this.getOwnerTeam());
      tag.putByte("Visibility", this.getVisibility().id());
      tag.putFloat("MaxRadius", this.getMaxRadius());
      tag.putInt("ExpansionTicks", this.getExpansionTicks());
      tag.putInt("HighlightTicks", this.getHighlightTicks());
      tag.putLong("StartGameTime", this.getStartGameTime());
   }

   protected void readAdditionalSaveData(CompoundTag tag) {
      if (tag.hasUUID("OwnerUUID")) {
         this.setOwnerUUID(tag.getUUID("OwnerUUID"));
      }

      this.setOwnerTeam(tag.getString("OwnerTeam"));
      this.setVisibility(ScanVisibility.fromId(tag.getByte("Visibility")));
      this.setMaxRadius(tag.getFloat("MaxRadius"));
      this.setExpansionTicks(tag.getInt("ExpansionTicks"));
      this.setHighlightTicks(tag.getInt("HighlightTicks"));
      this.setStartGameTime(tag.getLong("StartGameTime"));
   }

   public Packet<ClientGamePacketListener> getAddEntityPacket() {
      return NetworkManager.createAddEntityPacket(this);
   }

   public boolean isPickable() {
      return false;
   }

   public boolean isAttackable() {
      return false;
   }

   public boolean displayFireAnimation() {
      return false;
   }
}
