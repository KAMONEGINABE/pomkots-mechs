package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.AldebaranItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.AldebaranItem;

public class AldebaranItemRenderer {
   public static class Arm extends BasePartsItemRenderer.Arm<AldebaranItem.Arm> {
      public Arm() {
         super(new AldebaranItemModel.Arm());
      }
   }

   public static class Body extends BasePartsItemRenderer.Body<AldebaranItem.Body> {
      public Body() {
         super(new AldebaranItemModel.Body());
      }
   }

   public static class Head extends BasePartsItemRenderer.Head<AldebaranItem.Head> {
      public Head() {
         super(new AldebaranItemModel.Head());
      }
   }

   public static class Legs extends BasePartsItemRenderer.Legs<AldebaranItem.Legs> {
      public Legs() {
         super(new AldebaranItemModel.Legs());
      }
   }
}
