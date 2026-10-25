package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.Pmb01Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class Pmb01EntityModel extends GeoModel<Pmb01Entity> {
   public ResourceLocation getAnimationResource(Pmb01Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pmb01.animation.json");
   }

   public ResourceLocation getModelResource(Pmb01Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pmb01.geo.json");
   }

   public ResourceLocation getTextureResource(Pmb01Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/pmb01.png");
   }

   public void setCustomAnimations(Pmb01Entity animatable, long instanceId, AnimationState animationState) {
      EntityModelData entityData = (EntityModelData)animationState.getData(DataTickets.ENTITY_MODEL_DATA);
      CoreGeoBone head = this.getAnimationProcessor().getBone("head");
      if (head != null) {
         head.setRotY(entityData.netHeadYaw() * (float) (Math.PI / 180.0));
         head.setRotX(entityData.headPitch() * (float) (Math.PI / 180.0));
      }
   }
}
