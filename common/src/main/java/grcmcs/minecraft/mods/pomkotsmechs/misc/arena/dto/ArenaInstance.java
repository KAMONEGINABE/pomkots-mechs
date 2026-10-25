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
      tag.putString("ArenaId", this.arenaId);
      tag.putString("DisplayName", this.displayName);
      if (this.controllerPos != null) {
         CompoundTag posTag = new CompoundTag();
         posTag.putString("Dimension", this.controllerPos.dimension().location().toString());
         posTag.putInt("X", this.controllerPos.pos().getX());
         posTag.putInt("Y", this.controllerPos.pos().getY());
         posTag.putInt("Z", this.controllerPos.pos().getZ());
         tag.put("ControllerPos", posTag);
      }

      ListTag fighterList = new ListTag();

      for (ArenaFighterData fighter : this.fighters.values()) {
         fighterList.add(fighter.save());
      }

      tag.put("Fighters", fighterList);
      ListTag historyList = new ListTag();

      for (ArenaBattleRecord record : this.battleHistory) {
         historyList.add(record.save());
      }

      tag.put("History", historyList);
      if (this.activeMatch != null) {
         tag.put("ActiveMatch", this.activeMatch.save());
      }

      return tag;
   }

   public static ArenaInstance load(CompoundTag tag) {
      ArenaInstance arena = new ArenaInstance();
      arena.arenaId = tag.getString("ArenaId");
      arena.displayName = tag.getString("DisplayName");
      if (tag.contains("ControllerPos", 10)) {
         CompoundTag posTag = tag.getCompound("ControllerPos");
         ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(posTag.getString("Dimension")));
         BlockPos pos = new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z"));
         arena.controllerPos = GlobalPos.of(dimension, pos);
      }

      for (Tag element : tag.getList("Fighters", 10)) {
         ArenaFighterData fighter = ArenaFighterData.load((CompoundTag)element);
         arena.fighters.put(fighter.getFighterId(), fighter);
      }

      for (Tag element : tag.getList("History", 10)) {
         arena.battleHistory.add(ArenaBattleRecord.load((CompoundTag)element));
      }

      if (tag.contains("ActiveMatch")) {
         arena.activeMatch = ArenaMatchData.load(tag.getCompound("ActiveMatch"));
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
