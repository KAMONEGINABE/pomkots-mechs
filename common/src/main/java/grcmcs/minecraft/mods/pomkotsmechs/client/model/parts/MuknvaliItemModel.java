package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts;

import grcmcs.minecraft.mods.pomkotsmechs.items.parts.MuknvaliItem;
import net.minecraft.resources.ResourceLocation;

public class MuknvaliItemModel {
   public static String name = MuknvaliItem.SERIES_NAME;

   public static class Arm extends BasePartsItemModel<MuknvaliItem.Arm> {
      public ResourceLocation getModelResource(MuknvaliItem.Arm animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + MuknvaliItemModel.name + "arm.geo.json");
      }
   }

   public static class Body extends BasePartsItemModel<MuknvaliItem.Body> {
      public ResourceLocation getModelResource(MuknvaliItem.Body animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + MuknvaliItemModel.name + "body.geo.json");
      }
   }

   public static class Head extends BasePartsItemModel<MuknvaliItem.Head> {
      public ResourceLocation getModelResource(MuknvaliItem.Head animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + MuknvaliItemModel.name + "head.geo.json");
      }
   }

   public static class Legs extends BasePartsItemModel<MuknvaliItem.Legs> {
      public ResourceLocation getModelResource(MuknvaliItem.Legs animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + MuknvaliItemModel.name + "legs.geo.json");
      }
   }
}
