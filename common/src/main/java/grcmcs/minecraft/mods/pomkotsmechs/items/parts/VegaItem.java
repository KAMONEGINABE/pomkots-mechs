package grcmcs.minecraft.mods.pomkotsmechs.items.parts;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.VegaItemRenderer;
import net.minecraft.world.item.Item.Properties;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;

public class VegaItem {
   public static String SERIES_NAME = "vega";
   private static final String DEFAULT_COLOR = "orange";

   public static class Arm extends BasePartsItem.Arm {
      public Arm(Properties properties) {
         super(properties);
      }

      public VegaItemRenderer.Arm newRenderer() {
         return new VegaItemRenderer.Arm();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return VegaItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "orange";
      }
   }

   public static class Body extends BasePartsItem.Body {
      public Body(Properties properties) {
         super(properties);
      }

      public VegaItemRenderer.Body newRenderer() {
         return new VegaItemRenderer.Body();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return VegaItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "orange";
      }

      @Override
      public float getNeckPos() {
         return 0.4F;
      }
   }

   public static class Head extends BasePartsItem.Head {
      public Head(Properties properties) {
         super(properties);
      }

      public VegaItemRenderer.Head newRenderer() {
         return new VegaItemRenderer.Head();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return VegaItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "orange";
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

      public VegaItemRenderer.Legs newRenderer() {
         return new VegaItemRenderer.Legs();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return VegaItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "orange";
      }
   }
}
