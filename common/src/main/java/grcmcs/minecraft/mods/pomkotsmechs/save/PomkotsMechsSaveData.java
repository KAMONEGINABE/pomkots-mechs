package grcmcs.minecraft.mods.pomkotsmechs.save;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

public class PomkotsMechsSaveData extends SavedData {
   private static final String DATA_NAME = PomkotsMechs.nbtName("SaveData");
   private final Map<UUID, PomkotsMechsSaveData.MechLocation> mechPositions = new HashMap<>();

   public static PomkotsMechsSaveData get(ServerLevel level) {
      return (PomkotsMechsSaveData)level.m_7654_().m_129783_().m_8895_().m_164861_(PomkotsMechsSaveData::load, PomkotsMechsSaveData::new, DATA_NAME);
   }

   public static PomkotsMechsSaveData load(CompoundTag tag) {
      PomkotsMechsSaveData data = new PomkotsMechsSaveData();

      for (Tag t : tag.m_128437_("mechs", 10)) {
         CompoundTag entry = (CompoundTag)t;
         UUID uuid = entry.m_128342_("uuid");
         ResourceLocation dimId = new ResourceLocation(entry.m_128461_("dimension"));
         ResourceKey<Level> dim = ResourceKey.m_135785_(Registries.f_256858_, dimId);
         int x = entry.m_128451_("x");
         int z = entry.m_128451_("z");
         data.mechPositions.put(uuid, new PomkotsMechsSaveData.MechLocation(dim, new ChunkPos(x, z)));
      }

      return data;
   }

   public CompoundTag m_7176_(CompoundTag tag) {
      ListTag list = new ListTag();

      for (Entry<UUID, PomkotsMechsSaveData.MechLocation> e : this.mechPositions.entrySet()) {
         CompoundTag entry = new CompoundTag();
         entry.m_128362_("uuid", e.getKey());
         entry.m_128359_("dimension", e.getValue().dimension.m_135782_().toString());
         entry.m_128405_("x", e.getValue().chunkPos.f_45578_);
         entry.m_128405_("z", e.getValue().chunkPos.f_45579_);
         list.add(entry);
      }

      tag.m_128365_("mechs", list);
      return tag;
   }

   public void updateMech(UUID uuid, ResourceKey<Level> dim, ChunkPos pos) {
      this.mechPositions.put(uuid, new PomkotsMechsSaveData.MechLocation(dim, pos));
      this.m_77762_();
   }

   public void removeMech(UUID uuid) {
      this.mechPositions.remove(uuid);
      this.m_77762_();
   }

   public PomkotsMechsSaveData.MechLocation get(UUID uuid) {
      return this.mechPositions.get(uuid);
   }

   public record MechLocation(ResourceKey<Level> dimension, ChunkPos chunkPos) {
   }
}
