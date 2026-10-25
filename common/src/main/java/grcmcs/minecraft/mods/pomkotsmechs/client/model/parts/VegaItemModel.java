package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts;

import grcmcs.minecraft.mods.pomkotsmechs.items.parts.VegaItem;
import net.minecraft.resources.ResourceLocation;

public class VegaItemModel {
   public static String name = VegaItem.SERIES_NAME;

   public static class Arm extends BasePartsItemModel<VegaItem.Arm> {
      public ResourceLocation getModelResource(VegaItem.Arm animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + VegaItemModel.name + "arm.geo.json");
      }
   }

   public static class Body extends BasePartsItemModel<VegaItem.Body> {
      public ResourceLocation getModelResource(VegaItem.Body animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + VegaItemModel.name + "body.geo.json");
      }
   }

   public static class Head extends BasePartsItemModel<VegaItem.Head> {
      public ResourceLocation getModelResource(VegaItem.Head animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + VegaItemModel.name + "head.geo.json");
      }
   }

   public static class Legs extends BasePartsItemModel<VegaItem.Legs> {
      public ResourceLocation getModelResource(VegaItem.Legs animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + VegaItemModel.name + "legs.geo.json");
      }
   }
}
