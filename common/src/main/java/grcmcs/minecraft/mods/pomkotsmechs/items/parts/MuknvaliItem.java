package grcmcs.minecraft.mods.pomkotsmechs.items.parts;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.MuknvaliItemRenderer;
import net.minecraft.world.item.Item.Properties;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;

public class MuknvaliItem {
   public static String SERIES_NAME = "muknvali";
   private static final String DEFAULT_COLOR = "darkgray";

   public static class Arm extends BasePartsItem.Arm {
      public Arm(Properties properties) {
         super(properties);
      }

      public MuknvaliItemRenderer.Arm newRenderer() {
         return new MuknvaliItemRenderer.Arm();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return MuknvaliItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "darkgray";
      }
   }

   public static class Body extends BasePartsItem.Body {
      public Body(Properties properties) {
         super(properties);
      }

      public MuknvaliItemRenderer.Body newRenderer() {
         return new MuknvaliItemRenderer.Body();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return MuknvaliItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "darkgray";
      }

      @Override
      public float getNeckPos() {
         return 0.3F;
      }
   }

   public static class Head extends BasePartsItem.Head {
      public Head(Properties properties) {
         super(properties);
      }

      public MuknvaliItemRenderer.Head newRenderer() {
         return new MuknvaliItemRenderer.Head();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return MuknvaliItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "darkgray";
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

      public MuknvaliItemRenderer.Legs newRenderer() {
         return new MuknvaliItemRenderer.Legs();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return MuknvaliItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "darkgray";
      }
   }
}
