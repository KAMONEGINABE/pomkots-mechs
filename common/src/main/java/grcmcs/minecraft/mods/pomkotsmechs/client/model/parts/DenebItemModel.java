package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts;

import grcmcs.minecraft.mods.pomkotsmechs.items.parts.DenebItem;
import net.minecraft.resources.ResourceLocation;

public class DenebItemModel {
   public static String name = DenebItem.SERIES_NAME;

   public static class Arm extends BasePartsItemModel<DenebItem.Arm> {
      public ResourceLocation getModelResource(DenebItem.Arm animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + DenebItemModel.name + "arm.geo.json");
      }
   }

   public static class Body extends BasePartsItemModel<DenebItem.Body> {
      public ResourceLocation getModelResource(DenebItem.Body animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + DenebItemModel.name + "body.geo.json");
      }
   }

   public static class Head extends BasePartsItemModel<DenebItem.Head> {
      public ResourceLocation getModelResource(DenebItem.Head animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + DenebItemModel.name + "head.geo.json");
      }
   }

   public static class Legs extends BasePartsItemModel<DenebItem.Legs> {
      public ResourceLocation getModelResource(DenebItem.Legs animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + DenebItemModel.name + "legs.geo.json");
      }
   }
}
