package grcmcs.minecraft.mods.pomkotsmechs.items;

import dev.architectury.platform.Platform;
import grcmcs.minecraft.mods.pomkotsmechs.client.renderer.PomkotsArmorItemRenderer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item.Properties;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.renderer.GeoArmorRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;

public class PomkotsArmorItem extends ArmorItem implements GeoItem {
   private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
   private Supplier<Object> renderProvider;
   public static BiConsumer<Consumer<Object>, PomkotsArmorItem> regForForge;

   public PomkotsArmorItem(ArmorMaterial armorMaterial, Type type, Properties properties) {
      super(armorMaterial, type, properties);
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
      consumer.accept(
         new RenderProvider() {
            private GeoArmorRenderer<?> renderer;

            public HumanoidModel<LivingEntity> getHumanoidArmorModel(
               LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<LivingEntity> original
            ) {
               if (this.renderer == null) {
                  this.renderer = new PomkotsArmorItemRenderer();
               }

               this.renderer.prepForRender(livingEntity, itemStack, equipmentSlot, original);
               return (HumanoidModel<LivingEntity>)this.renderer;
            }
         }
      );
   }

   public void registerControllers(ControllerRegistrar controllers) {
      controllers.add(new AnimationController[]{new AnimationController(this, 20, state -> PlayState.STOP)});
   }

   public AnimatableInstanceCache getAnimatableInstanceCache() {
      return this.cache;
   }
}
