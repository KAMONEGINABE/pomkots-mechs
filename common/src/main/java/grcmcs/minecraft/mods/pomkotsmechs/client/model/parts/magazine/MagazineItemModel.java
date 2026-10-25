package grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.magazine;

import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineGatlingItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineMachineGunItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineMissileItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineMissileLargeItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineRifleItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.magazine.MagazineShotGunItem;
import net.minecraft.resources.ResourceLocation;

public class MagazineItemModel extends BasePartsItemModel<BasePartsItem.Magazine> {
   public ResourceLocation getModelResource(BasePartsItem.Magazine animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/magazine.geo.json");
   }

   public ResourceLocation getTextureResource(BasePartsItem.Magazine animatable) {
      if (animatable instanceof MagazineRifleItem) {
         return new ResourceLocation("pomkotsmechs", "textures/entity/magazine/magazine_1.png");
      } else if (animatable instanceof MagazineShotGunItem) {
         return new ResourceLocation("pomkotsmechs", "textures/entity/magazine/magazine_2.png");
      } else if (animatable instanceof MagazineMachineGunItem) {
         return new ResourceLocation("pomkotsmechs", "textures/entity/magazine/magazine_3.png");
      } else if (animatable instanceof MagazineGatlingItem) {
         return new ResourceLocation("pomkotsmechs", "textures/entity/magazine/magazine_4.png");
      } else if (animatable instanceof MagazineMissileItem) {
         return new ResourceLocation("pomkotsmechs", "textures/entity/magazine/magazine_5.png");
      } else {
         return animatable instanceof MagazineMissileLargeItem
            ? new ResourceLocation("pomkotsmechs", "textures/entity/magazine/magazine_6.png")
            : new ResourceLocation("pomkotsmechs", "textures/entity/magazine/magazine_7.png");
      }
   }
}
