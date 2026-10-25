package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ArenaInstance {
   private String arenaId;
   private String displayName;
   private ArenaMatchData activeMatch;
   private final Map<UUID, ArenaFighterData> fighters = new HashMap<>();
   private final List<ArenaBattleRecord> battleHistory = new ArrayList<>();
   private GlobalPos controllerPos;

   ArenaInstance() {
   }

   public CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.m_128359_("ArenaId", this.arenaId);
      tag.m_128359_("DisplayName", this.displayName);
      if (this.controllerPos != null) {
         CompoundTag posTag = new CompoundTag();
         posTag.m_128359_("Dimension", this.controllerPos.m_122640_().m_135782_().toString());
         posTag.m_128405_("X", this.controllerPos.m_122646_().m_123341_());
         posTag.m_128405_("Y", this.controllerPos.m_122646_().m_123342_());
         posTag.m_128405_("Z", this.controllerPos.m_122646_().m_123343_());
         tag.m_128365_("ControllerPos", posTag);
      }

      ListTag fighterList = new ListTag();

      for (ArenaFighterData fighter : this.fighters.values()) {
         fighterList.add(fighter.save());
      }

      tag.m_128365_("Fighters", fighterList);
      ListTag historyList = new ListTag();

      for (ArenaBattleRecord record : this.battleHistory) {
         historyList.add(record.save());
      }

      tag.m_128365_("History", historyList);
      if (this.activeMatch != null) {
         tag.m_128365_("ActiveMatch", this.activeMatch.save());
      }

      return tag;
   }

   public static ArenaInstance load(CompoundTag tag) {
      ArenaInstance arena = new ArenaInstance();
      arena.arenaId = tag.m_128461_("ArenaId");
      arena.displayName = tag.m_128461_("DisplayName");
      if (tag.m_128425_("ControllerPos", 10)) {
         CompoundTag posTag = tag.m_128469_("ControllerPos");
         ResourceKey<Level> dimension = ResourceKey.m_135785_(Registries.f_256858_, new ResourceLocation(posTag.m_128461_("Dimension")));
         BlockPos pos = new BlockPos(posTag.m_128451_("X"), posTag.m_128451_("Y"), posTag.m_128451_("Z"));
         arena.controllerPos = GlobalPos.m_122643_(dimension, pos);
      }

      for (Tag element : tag.m_128437_("Fighters", 10)) {
         ArenaFighterData fighter = ArenaFighterData.load((CompoundTag)element);
         arena.fighters.put(fighter.getFighterId(), fighter);
      }

      for (Tag element : tag.m_128437_("History", 10)) {
         arena.battleHistory.add(ArenaBattleRecord.load((CompoundTag)element));
      }

      if (tag.m_128441_("ActiveMatch")) {
         arena.activeMatch = ArenaMatchData.load(tag.m_128469_("ActiveMatch"));
      }

      return arena;
   }

   public ArenaMatchData getActiveMatch() {
      return this.activeMatch;
   }

   public void setActiveMatch(ArenaMatchData activeMatch) {
      this.activeMatch = activeMatch;
   }

   public String getArenaId() {
      return this.arenaId;
   }

   public void setArenaId(String arenaId) {
      this.arenaId = arenaId;
   }

   public List<ArenaBattleRecord> getBattleHistory() {
      return this.battleHistory;
   }

   public String getDisplayName() {
      return this.displayName;
   }

   public void setDisplayName(String displayName) {
      this.displayName = displayName;
   }

   public Map<UUID, ArenaFighterData> getFighters() {
      return this.fighters;
   }

   public void addFighter(ArenaFighterData fighter) {
      this.fighters.put(fighter.getFighterId(), fighter);
   }

   public ArenaFighterData getFighter(UUID fighter) {
      return this.fighters.get(fighter);
   }

   public void removeFighter(UUID fighter) {
      this.fighters.remove(fighter);
   }

   public GlobalPos getControllerPos() {
      return this.controllerPos;
   }

   public void setControllerPos(GlobalPos controllerPos) {
      this.controllerPos = controllerPos;
   }
}
