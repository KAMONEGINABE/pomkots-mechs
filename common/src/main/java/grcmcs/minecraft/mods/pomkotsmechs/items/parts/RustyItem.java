package grcmcs.minecraft.mods.pomkotsmechs.items.parts;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.RustyItemRenderer;
import net.minecraft.world.item.Item.Properties;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;

public class RustyItem {
   public static String SERIES_NAME = "rusty";
   private static final String DEFAULT_COLOR = "rusty";

   public static class Arm extends BasePartsItem.Arm {
      public Arm(Properties properties) {
         super(properties);
      }

      public RustyItemRenderer.Arm newRenderer() {
         return new RustyItemRenderer.Arm();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return RustyItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "rusty";
      }
   }

   public static class Body extends BasePartsItem.Body {
      public Body(Properties properties) {
         super(properties);
      }

      public RustyItemRenderer.Body newRenderer() {
         return new RustyItemRenderer.Body();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return RustyItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "rusty";
      }
   }

   public static class Head extends BasePartsItem.Head {
      public Head(Properties properties) {
         super(properties);
      }

      public RustyItemRenderer.Head newRenderer() {
         return new RustyItemRenderer.Head();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return RustyItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "rusty";
      }
   }

   public static class Legs extends BasePartsItem.Legs {
      public Legs(Properties properties) {
         super(properties);
      }

      public RustyItemRenderer.Legs newRenderer() {
         return new RustyItemRenderer.Legs();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return RustyItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "rusty";
      }
   }
}
