package grcmcs.minecraft.mods.pomkotsmechs.misc.scan;

public enum ScanVisibility {
   OWNER,
   TEAM,
   ALL;

   public static ScanVisibility fromId(byte id) {
      ScanVisibility[] values = values();
      return id >= 0 && id < values.length ? values[id] : OWNER;
   }

   public byte id() {
      return (byte)this.ordinal();
   }
}
