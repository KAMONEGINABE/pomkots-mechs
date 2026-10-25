package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts;

import grcmcs.minecraft.mods.pomkotsmechs.items.parts.RustyItem;
import net.minecraft.resources.ResourceLocation;

public class RustyItemModel {
   public static String name = RustyItem.SERIES_NAME;

   public static class Arm extends BasePartsItemModel<RustyItem.Arm> {
      public ResourceLocation getModelResource(RustyItem.Arm animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + RustyItemModel.name + "arm.geo.json");
      }
   }

   public static class Body extends BasePartsItemModel<RustyItem.Body> {
      public ResourceLocation getModelResource(RustyItem.Body animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + RustyItemModel.name + "body.geo.json");
      }
   }

   public static class Head extends BasePartsItemModel<RustyItem.Head> {
      public ResourceLocation getModelResource(RustyItem.Head animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + RustyItemModel.name + "head.geo.json");
      }
   }

   public static class Legs extends BasePartsItemModel<RustyItem.Legs> {
      public ResourceLocation getModelResource(RustyItem.Legs animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + RustyItemModel.name + "legs.geo.json");
      }
   }
}
