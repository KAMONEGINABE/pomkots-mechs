package grcmcs.minecraft.mods.pomkotsmechs.items;

import dev.architectury.platform.Platform;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.CartonItemRenderer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class CartonItem extends Item implements GeoItem {
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private Supplier<Object> renderProvider;
   public static BiConsumer<Consumer<Object>, CartonItem> regForForge;

   public CartonItem(Properties properties) {
      super(properties);
      if (Platform.isFabric()) {
         this.renderProvider = GeoItem.makeRenderer(this);
      }
   }

   public Supplier<Object> getRenderProvider() {
      return this.renderProvider;
   }

   public void initializeClient(Consumer<Object> consumer) {
      regForForge.accept(consumer, this);
   }

   public void createRenderer(Consumer<Object> consumer) {
      consumer.accept(new RenderProvider() {
         private BlockEntityWithoutLevelRenderer renderer;

         public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            if (this.renderer == null) {
               this.renderer = (BlockEntityWithoutLevelRenderer)CartonItem.this.newRenderer();
            }

            return this.renderer;
         }
      });
   }

   public Object newRenderer() {
      return new CartonItemRenderer();
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, "Activation", 0, state -> PlayState.STOP)
               .triggerableAnim("use", RawAnimation.begin().thenPlayAndHold("animation.carton.use"))
               .triggerableAnim("stop", RawAnimation.begin().thenPlay("animation.carton.stopUsing"))
         }
      );
      controllers.add(
         new AnimationController[]{
            new AnimationController(this, 0, state -> state.setAndContinue(RawAnimation.begin().thenPlayAndHold("animation.carton.use")))
         }
      );
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }
}
