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
   private static final EntityDataAccessor<Optional<UUID>> OWNER_UUID = SynchedEntityData.m_135353_(ScanPulseEntity.class, EntityDataSerializers.f_135041_);
   private static final EntityDataAccessor<String> OWNER_TEAM = SynchedEntityData.m_135353_(ScanPulseEntity.class, EntityDataSerializers.f_135030_);
   private static final EntityDataAccessor<Byte> VISIBILITY = SynchedEntityData.m_135353_(ScanPulseEntity.class, EntityDataSerializers.f_135027_);
   private static final EntityDataAccessor<Float> MAX_RADIUS = SynchedEntityData.m_135353_(ScanPulseEntity.class, EntityDataSerializers.f_135029_);
   private static final EntityDataAccessor<Integer> EXPANSION_TICKS = SynchedEntityData.m_135353_(ScanPulseEntity.class, EntityDataSerializers.f_135028_);
   private static final EntityDataAccessor<Integer> HIGHLIGHT_TICKS = SynchedEntityData.m_135353_(ScanPulseEntity.class, EntityDataSerializers.f_135028_);
   private static final EntityDataAccessor<Long> START_GAME_TIME = SynchedEntityData.m_135353_(ScanPulseEntity.class, EntityDataSerializers.f_244073_);

   public ScanPulseEntity(EntityType<? extends ScanPulseEntity> type, Level level) {
      super(type, level);
      this.f_19811_ = true;
      this.f_19794_ = true;
      this.m_20242_(true);
   }

   protected void m_8097_() {
      this.f_19804_.m_135372_(OWNER_UUID, Optional.empty());
      this.f_19804_.m_135372_(OWNER_TEAM, "");
      this.f_19804_.m_135372_(VISIBILITY, ScanVisibility.OWNER.id());
      this.f_19804_.m_135372_(MAX_RADIUS, 64.0F);
      this.f_19804_.m_135372_(EXPANSION_TICKS, 60);
      this.f_19804_.m_135372_(HIGHLIGHT_TICKS, 100);
      this.f_19804_.m_135372_(START_GAME_TIME, 0L);
   }

   public void initialize(Player owner, ScanVisibility visibility, float maxRadius, int expansionTicks, int highlightTicks) {
      this.setOwnerUUID(owner.m_20148_());
      this.setVisibility(visibility);
      this.setMaxRadius(maxRadius);
      this.setExpansionTicks(expansionTicks);
      this.setHighlightTicks(highlightTicks);
      this.setOwnerTeam(owner.m_5647_() != null ? owner.m_5647_().m_5758_() : "");
      this.setStartGameTime(this.m_9236_().m_46467_());
   }

   public void m_8119_() {
      if (this.f_19803_ && this.m_9236_().f_46443_) {
         this.playSoundEffect((SoundEvent)PomkotsMechs.SE_SCAN2.get(), 0.7F);
      }

      super.m_8119_();
      if (!this.m_9236_().f_46443_) {
         long elapsed = this.m_9236_().m_46467_() - this.getStartGameTime();
         if (elapsed > this.getExpansionTicks() + 2L) {
            this.m_146870_();
         }
      }
   }

   protected void playSoundEffect(SoundEvent event) {
      this.playSoundEffect(event, 1.0F);
   }

   protected void playSoundEffect(SoundEvent event, float volume) {
      float soundEffectVolume = this.computeVolume(100.0);
      if (soundEffectVolume > 0.0F) {
         this.m_9236_().m_7785_(this.m_20185_(), this.m_20186_(), this.m_20189_(), event, SoundSource.PLAYERS, volume * soundEffectVolume, 1.0F, false);
      }
   }

   private float computeVolume(double maxDistance) {
      Minecraft mc = Minecraft.m_91087_();
      LocalPlayer player = mc.f_91074_;
      if (player == null) {
         return 0.0F;
      }

      double distance = player.m_20270_(this);
      if (distance > maxDistance) {
         return 0.0F;
      }

      float volume = 1.0F - (float)(distance / maxDistance);
      return Mth.m_14036_(volume, 0.0F, 1.0F);
   }

   public float getRadius(float partialTick) {
      double elapsed = (float)(this.m_9236_().m_46467_() - this.getStartGameTime()) + partialTick;
      float progress = (float)Math.max(0.0, Math.min(1.0, elapsed / this.getExpansionTicks()));
      float eased = 1.0F - (1.0F - progress) * (1.0F - progress);
      return this.getMaxRadius() * eased;
   }

   public float getPreviousRadius() {
      double elapsed = this.m_9236_().m_46467_() - this.getStartGameTime() - 1.0;
      float progress = (float)Math.max(0.0, Math.min(1.0, elapsed / this.getExpansionTicks()));
      float eased = 1.0F - (1.0F - progress) * (1.0F - progress);
      return this.getMaxRadius() * eased;
   }

   public boolean isVisibleTo(Player player) {
      return switch (this.getVisibility()) {
         case ALL -> true;
         case OWNER -> this.getOwnerUUID() != null && this.getOwnerUUID().equals(player.m_20148_());
         case TEAM -> this.isOwnerOrSameTeam(player);
      };
   }

   private boolean isOwnerOrSameTeam(Player player) {
      UUID ownerUUID = this.getOwnerUUID();
      if (ownerUUID != null && ownerUUID.equals(player.m_20148_())) {
         return true;
      }

      String ownerTeam = this.getOwnerTeam();
      return !ownerTeam.isEmpty() && player.m_5647_() != null && ownerTeam.equals(player.m_5647_().m_5758_());
   }

   @Nullable
   public UUID getOwnerUUID() {
      return (UUID)((Optional)this.f_19804_.m_135370_(OWNER_UUID)).orElse(null);
   }

   public void setOwnerUUID(@Nullable UUID uuid) {
      this.f_19804_.m_135381_(OWNER_UUID, Optional.ofNullable(uuid));
   }

   public String getOwnerTeam() {
      return (String)this.f_19804_.m_135370_(OWNER_TEAM);
   }

   public void setOwnerTeam(String teamName) {
      this.f_19804_.m_135381_(OWNER_TEAM, teamName != null ? teamName : "");
   }

   public ScanVisibility getVisibility() {
      return ScanVisibility.fromId((Byte)this.f_19804_.m_135370_(VISIBILITY));
   }

   public void setVisibility(ScanVisibility visibility) {
      this.f_19804_.m_135381_(VISIBILITY, visibility.id());
   }

   public float getMaxRadius() {
      return (Float)this.f_19804_.m_135370_(MAX_RADIUS);
   }

   public void setMaxRadius(float radius) {
      this.f_19804_.m_135381_(MAX_RADIUS, Math.max(0.0F, radius));
   }

   public int getExpansionTicks() {
      return (Integer)this.f_19804_.m_135370_(EXPANSION_TICKS);
   }

   public void setExpansionTicks(int ticks) {
      this.f_19804_.m_135381_(EXPANSION_TICKS, Math.max(1, ticks));
   }

   public int getHighlightTicks() {
      return (Integer)this.f_19804_.m_135370_(HIGHLIGHT_TICKS);
   }

   public void setHighlightTicks(int ticks) {
      this.f_19804_.m_135381_(HIGHLIGHT_TICKS, Math.max(1, ticks));
   }

   public long getStartGameTime() {
      return (Long)this.f_19804_.m_135370_(START_GAME_TIME);
   }

   public void setStartGameTime(long gameTime) {
      this.f_19804_.m_135381_(START_GAME_TIME, gameTime);
   }

   protected void m_7380_(CompoundTag tag) {
      UUID ownerUUID = this.getOwnerUUID();
      if (ownerUUID != null) {
         tag.m_128362_("OwnerUUID", ownerUUID);
      }

      tag.m_128359_("OwnerTeam", this.getOwnerTeam());
      tag.m_128344_("Visibility", this.getVisibility().id());
      tag.m_128350_("MaxRadius", this.getMaxRadius());
      tag.m_128405_("ExpansionTicks", this.getExpansionTicks());
      tag.m_128405_("HighlightTicks", this.getHighlightTicks());
      tag.m_128356_("StartGameTime", this.getStartGameTime());
   }

   protected void m_7378_(CompoundTag tag) {
      if (tag.m_128403_("OwnerUUID")) {
         this.setOwnerUUID(tag.m_128342_("OwnerUUID"));
      }

      this.setOwnerTeam(tag.m_128461_("OwnerTeam"));
      this.setVisibility(ScanVisibility.fromId(tag.m_128445_("Visibility")));
      this.setMaxRadius(tag.m_128457_("MaxRadius"));
      this.setExpansionTicks(tag.m_128451_("ExpansionTicks"));
      this.setHighlightTicks(tag.m_128451_("HighlightTicks"));
      this.setStartGameTime(tag.m_128454_("StartGameTime"));
   }

   public Packet<ClientGamePacketListener> m_5654_() {
      return NetworkManager.createAddEntityPacket(this);
   }

   public boolean m_6087_() {
      return false;
   }

   public boolean m_6097_() {
      return false;
   }

   public boolean m_6051_() {
      return false;
   }
}
