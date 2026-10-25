package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto;

import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPack;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class ArenaSavedData extends SavedData {
   public static final String DATA_NAME = "pomkotsmechs_mech_arena";
   private final Map<String, ArenaInstance> arenas = new HashMap<>();

   public static ArenaSavedData get(MinecraftServer server) {
      ServerLevel level = server.m_129783_();
      return (ArenaSavedData)level.m_8895_().m_164861_(ArenaSavedData::load, ArenaSavedData::create, "pomkotsmechs_mech_arena");
   }

   private static ArenaSavedData create() {
      ArenaSavedData data = new ArenaSavedData();
      data.m_77762_();
      return data;
   }

   public Map<String, ArenaInstance> getArenas() {
      return this.arenas;
   }

   public ArenaInstance getArena(String arenaId) {
      return this.arenas.get(arenaId);
   }

   public ArenaInstance getOrCreateArena(String arenaId) {
      return this.arenas.computeIfAbsent(arenaId, id -> {
         ArenaInstance arena = createArena(arenaId, null);
         this.m_77762_();
         return arena;
      });
   }

   private static ArenaInstance createArena(String arenaId, GlobalPos controllerPos) {
      ArenaInstance arena = new ArenaInstance();
      arena.setArenaId(arenaId);
      arena.setDisplayName(arenaId);

      for (PomkotsDataPack.FighterJson npc : PomkotsDataPackManager.getInstance().getDataPack().getFighterPoolItems()) {
         ArenaFighterData fighter = new ArenaFighterData();
         UUID fighterId = UUID.nameUUIDFromBytes(("arena:" + npc.name).getBytes(StandardCharsets.UTF_8));
         fighter.setFighterId(fighterId);
         fighter.setAiLevel(npc.ai_level);
         fighter.setDisplayName(npc.name);
         fighter.setComment(npc.comment);
         fighter.setRating(npc.initial_rate);
         fighter.setWins(npc.win);
         fighter.setLosses(npc.lose);
         fighter.setTexture(new ResourceLocation(npc.texture));
         fighter.setModel(new ResourceLocation(npc.model));
         fighter.setType(ArenaFighterData.FighterType.NPC);
         ArenaMechData mech = new ArenaMechData();
         mech.setMechName(npc.mech.name);
         mech.setTextureColor(npc.mech.texture_color);

         for (PomkotsDataPack.PartJson part : npc.mech.parts) {
            Item item = (Item)BuiltInRegistries.f_257033_.m_7745_(new ResourceLocation(part.item));
            if (item instanceof BasePartsItem bitem) {
               ItemStack stack = new ItemStack(item);
               if (part.level != 0) {
                  if (part.level <= bitem.getMaxLevel()) {
                     bitem.setLevel(stack, part.level);
                  } else {
                     bitem.setLevel(stack, bitem.getMaxLevel());
                  }
               } else {
                  bitem.setLevel(stack, 1);
               }

               mech.getParts().set(part.slot, stack);
            }
         }

         fighter.setMechData(mech);
         arena.getFighters().put(fighterId, fighter);
      }

      return arena;
   }

   @NotNull
   public CompoundTag m_7176_(CompoundTag tag) {
      ListTag arenaList = new ListTag();

      for (ArenaInstance arena : this.arenas.values()) {
         arenaList.add(arena.save());
      }

      tag.m_128365_("Arenas", arenaList);
      return tag;
   }

   public static ArenaSavedData load(CompoundTag tag) {
      ArenaSavedData data = new ArenaSavedData();

      for (Tag element : tag.m_128437_("Arenas", 10)) {
         ArenaInstance arena = ArenaInstance.load((CompoundTag)element);
         data.arenas.put(arena.getArenaId(), arena);
      }

      return data;
   }
}
