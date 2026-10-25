package grcmcs.minecraft.mods.pomkotsmechs.items.parts;

import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts.DenebItemRenderer;
import net.minecraft.world.item.Item.Properties;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;

public class DenebItem {
   public static String SERIES_NAME = "deneb";
   private static final String DEFAULT_COLOR = "blue";

   public static class Arm extends BasePartsItem.Arm {
      public Arm(Properties properties) {
         super(properties);
      }

      public DenebItemRenderer.Arm newRenderer() {
         return new DenebItemRenderer.Arm();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return DenebItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "blue";
      }
   }

   public static class Body extends BasePartsItem.Body {
      public Body(Properties properties) {
         super(properties);
      }

      public DenebItemRenderer.Body newRenderer() {
         return new DenebItemRenderer.Body();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return DenebItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "blue";
      }
   }

   public static class Head extends BasePartsItem.Head {
      public Head(Properties properties) {
         super(properties);
      }

      public DenebItemRenderer.Head newRenderer() {
         return new DenebItemRenderer.Head();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return DenebItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "blue";
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

      public DenebItemRenderer.Legs newRenderer() {
         return new DenebItemRenderer.Legs();
      }

      public void registerControllers(ControllerRegistrar controllers) {
      }

      @Override
      public String getPartsSeriesName() {
         return DenebItem.SERIES_NAME;
      }

      @Override
      public String getDefaultColor() {
         return "blue";
      }
   }
}
