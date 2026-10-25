package grcmcs.minecraft.mods.pomkotsmechs.items.pilot;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

public class PilotLicenseItem extends Item {
   public PilotLicenseItem(Properties properties) {
      super(properties);
   }

   public static class PilotLicenseAdvanced extends PilotLicenseItem {
      public PilotLicenseAdvanced(Properties properties) {
         super(properties);
      }
   }

   public static class PilotLicenseIntermediate extends PilotLicenseItem {
      public PilotLicenseIntermediate(Properties properties) {
         super(properties);
      }
   }

   public static class PilotLicenseLegend extends PilotLicenseItem {
      public PilotLicenseLegend(Properties properties) {
         super(properties);
      }
   }

   public static class PilotLicenseNovice extends PilotLicenseItem {
      public PilotLicenseNovice(Properties properties) {
         super(properties);
      }
   }
}
