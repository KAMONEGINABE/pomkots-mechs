package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts;

import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public abstract class BasePartsItemModel<T extends BasePartsItem> extends GeoModel<T> {
   public static List<String> BASE_COLORS = List.of("gray", "darkgray", "white", "red", "green", "lightgreen", "blue", "orange", "rusty");
   public static Map<String, Integer> COLOR_INDEX_MAP = new HashMap<>();

   public static int getColorIndex(String colorName) {
      return COLOR_INDEX_MAP.get(colorName);
   }

   public ResourceLocation getAnimationResource(T animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/weapon.animation.json");
   }

   public ResourceLocation getTextureResource(T animatable) {
      if (animatable.getParentEntity() != null) {
         String color = BASE_COLORS.get(animatable.getParentEntity().getTextureColor());
         return color != null
            ? new ResourceLocation("pomkotsmechs", "textures/entity/parts/" + color + ".png")
            : new ResourceLocation("pomkotsmechs", "textures/entity/parts/gray.png");
      } else {
         return new ResourceLocation("pomkotsmechs", "textures/entity/parts/" + animatable.getDefaultColor() + ".png");
      }
   }

   static {
      for (int i = 0; i < BASE_COLORS.size(); i++) {
         COLOR_INDEX_MAP.put(BASE_COLORS.get(i), i);
      }
   }
}
