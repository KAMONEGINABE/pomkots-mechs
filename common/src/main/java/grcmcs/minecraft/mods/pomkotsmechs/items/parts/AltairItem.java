package grcmcs.minecraft.mods.pomkotsmechs.items.parts;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.AltairItemRenderer;
import net.minecraft.world.item.Item.Properties;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;

public class AltairItem {
   public static String SERIES_NAME = "altair";
   private static final String DEFAULT_COLOR = "red";

   public static class Arm extends BasePartsItem.Arm {
      public Arm(Properties properties) {
         super(properties);
      }

      public AltairItemRenderer.Arm newRenderer() {
         return new AltairItemRenderer.Arm();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return AltairItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "red";
      }
   }

   public static class Body extends BasePartsItem.Body {
      public Body(Properties properties) {
         super(properties);
      }

      public AltairItemRenderer.Body newRenderer() {
         return new AltairItemRenderer.Body();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return AltairItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "red";
      }
   }

   public static class Head extends BasePartsItem.Head {
      public Head(Properties properties) {
         super(properties);
      }

      public AltairItemRenderer.Head newRenderer() {
         return new AltairItemRenderer.Head();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return AltairItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "red";
      }
   }

   public static class Legs extends BasePartsItem.Legs {
      public Legs(Properties properties) {
         super(properties);
      }

      public AltairItemRenderer.Legs newRenderer() {
         return new AltairItemRenderer.Legs();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return AltairItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "red";
      }
   }
}
