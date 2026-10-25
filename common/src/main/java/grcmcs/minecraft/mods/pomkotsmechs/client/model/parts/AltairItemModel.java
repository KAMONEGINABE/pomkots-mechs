package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts;

import grcmcs.minecraft.mods.pomkotsmechs.items.parts.AltairItem;
import net.minecraft.resources.ResourceLocation;

public class AltairItemModel {
   public static String name = AltairItem.SERIES_NAME;

   public static class Arm extends BasePartsItemModel<AltairItem.Arm> {
      public ResourceLocation getModelResource(AltairItem.Arm animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + AltairItemModel.name + "arm.geo.json");
      }
   }

   public static class Body extends BasePartsItemModel<AltairItem.Body> {
      public ResourceLocation getModelResource(AltairItem.Body animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + AltairItemModel.name + "body.geo.json");
      }
   }

   public static class Head extends BasePartsItemModel<AltairItem.Head> {
      public ResourceLocation getModelResource(AltairItem.Head animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + AltairItemModel.name + "head.geo.json");
      }
   }

   public static class Legs extends BasePartsItemModel<AltairItem.Legs> {
      public ResourceLocation getModelResource(AltairItem.Legs animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + AltairItemModel.name + "legs.geo.json");
      }
   }
}
