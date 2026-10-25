package grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid;

import java.io.Serializable;
import java.util.Map;

public class SpawnTarget implements Serializable {
   public String mob_type;
   public String position;
   public String snbt;
   public Map<String, String> tags;

   @Override
   public String toString() {
      return "SpawnTarget{mob_type='" + this.mob_type + "', position='" + this.position + "', snbt=" + this.snbt + ", tags=" + this.tags + "}";
   }
}
