package grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid;

import java.io.Serializable;
import java.util.List;

public class EventDefinition implements Serializable {
   public String id;
   public String type;
   public String trigger_type;
   public int trigger_tick;
   public String message;
   public List<SpawnTarget> spawn_targets;
   public boolean force_end_wave_when_killed_all;
   public int amount;
   public int range_x;
   public int range_y;
   public int range_z;
   public boolean spawn_mobs_in_closed_area;

   @Override
   public String toString() {
      return "EventDefinition{id='"
         + this.id
         + "', type='"
         + this.type
         + "', trigger_type='"
         + this.trigger_type
         + "', trigger_tick="
         + this.trigger_tick
         + ", message='"
         + this.message
         + "', spawn_targets="
         + this.spawn_targets
         + "}";
   }
}
