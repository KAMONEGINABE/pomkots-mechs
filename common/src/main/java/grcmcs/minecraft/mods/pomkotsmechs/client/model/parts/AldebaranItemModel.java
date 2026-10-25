package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts;

import grcmcs.minecraft.mods.pomkotsmechs.items.parts.AldebaranItem;
import net.minecraft.resources.ResourceLocation;

public class AldebaranItemModel {
   public static String name = AldebaranItem.SERIES_NAME;

   public static class Arm extends BasePartsItemModel<AldebaranItem.Arm> {
      public ResourceLocation getModelResource(AldebaranItem.Arm animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + AldebaranItemModel.name + "arm.geo.json");
      }
   }

   public static class Body extends BasePartsItemModel<AldebaranItem.Body> {
      public ResourceLocation getModelResource(AldebaranItem.Body animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + AldebaranItemModel.name + "body.geo.json");
      }
   }

   public static class Head extends BasePartsItemModel<AldebaranItem.Head> {
      public ResourceLocation getModelResource(AldebaranItem.Head animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + AldebaranItemModel.name + "head.geo.json");
      }
   }

   public static class Legs extends BasePartsItemModel<AldebaranItem.Legs> {
      public ResourceLocation getModelResource(AldebaranItem.Legs animatable) {
         return new ResourceLocation("pomkotsmechs", "geo/" + AldebaranItemModel.name + "legs.geo.json");
      }
   }
}
