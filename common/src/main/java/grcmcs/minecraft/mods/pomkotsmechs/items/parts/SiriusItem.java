package grcmcs.minecraft.mods.pomkotsmechs.items.parts;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.SiriusItemRenderer;
import net.minecraft.world.item.Item.Properties;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;

public class SiriusItem {
   public static String SERIES_NAME = "sirius";
   private static final String DEFAULT_COLOR = "white";

   public static class Arm extends BasePartsItem.Arm {
      public Arm(Properties properties) {
         super(properties);
      }

      public SiriusItemRenderer.Arm newRenderer() {
         return new SiriusItemRenderer.Arm();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return SiriusItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "white";
      }
   }

   public static class Body extends BasePartsItem.Body {
      public Body(Properties properties) {
         super(properties);
      }

      public SiriusItemRenderer.Body newRenderer() {
         return new SiriusItemRenderer.Body();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return SiriusItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "white";
      }
   }

   public static class Head extends BasePartsItem.Head {
      public Head(Properties properties) {
         super(properties);
      }

      public SiriusItemRenderer.Head newRenderer() {
         return new SiriusItemRenderer.Head();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return SiriusItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "white";
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

      public SiriusItemRenderer.Legs newRenderer() {
         return new SiriusItemRenderer.Legs();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return SiriusItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "white";
      }
   }
}
