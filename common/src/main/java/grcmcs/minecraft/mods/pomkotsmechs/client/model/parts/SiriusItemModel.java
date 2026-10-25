package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts;

import grcmcs.minecraft.mods.pomkotsmechs.items.parts.SiriusItem;
import net.minecraft.resources.ResourceLocation;

public class SiriusItemModel {
   public static String name = SiriusItem.SERIES_NAME;

   public static class Arm extends BasePartsItemModel<SiriusItem.Arm> {
      public ResourceLocation getModelResource(SiriusItem.Arm animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + SiriusItemModel.name + "arm.geo.json");
      }
   }

   public static class Body extends BasePartsItemModel<SiriusItem.Body> {
      public ResourceLocation getModelResource(SiriusItem.Body animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + SiriusItemModel.name + "body.geo.json");
      }
   }

   public static class Head extends BasePartsItemModel<SiriusItem.Head> {
      public ResourceLocation getModelResource(SiriusItem.Head animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + SiriusItemModel.name + "head.geo.json");
      }
   }

   public static class Legs extends BasePartsItemModel<SiriusItem.Legs> {
      public ResourceLocation getModelResource(SiriusItem.Legs animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + SiriusItemModel.name + "legs.geo.json");
      }
   }
}
