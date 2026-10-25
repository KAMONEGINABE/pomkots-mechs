package grcmcs.minecraft.mods.pomkotsmechs.items.parts;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.AldebaranItemRenderer;
import net.minecraft.world.item.Item.Properties;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;

public class AldebaranItem {
   public static String SERIES_NAME = "aldebaran";
   private static final String DEFAULT_COLOR = "green";

   public static class Arm extends BasePartsItem.Arm {
      public Arm(Properties properties) {
         super(properties);
      }

      public AldebaranItemRenderer.Arm newRenderer() {
         return new AldebaranItemRenderer.Arm();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return AldebaranItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "green";
      }
   }

   public static class Body extends BasePartsItem.Body {
      public Body(Properties properties) {
         super(properties);
      }

      public AldebaranItemRenderer.Body newRenderer() {
         return new AldebaranItemRenderer.Body();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return AldebaranItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "green";
      }
   }

   public static class Head extends BasePartsItem.Head {
      public Head(Properties properties) {
         super(properties);
      }

      public AldebaranItemRenderer.Head newRenderer() {
         return new AldebaranItemRenderer.Head();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return AldebaranItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "green";
      }

      @Override
      public boolean isFullCovered() {
         return true;
      }
   }

   public static class Legs extends BasePartsItem.Legs {
      public Legs(Properties properties) {
         super(properties);
      }

      public AldebaranItemRenderer.Legs newRenderer() {
         return new AldebaranItemRenderer.Legs();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return AldebaranItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "green";
      }
   }
}
