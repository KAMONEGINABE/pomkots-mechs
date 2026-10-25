package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.Pms02Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class Pms02EntityModel extends GeoModel<Pms02Entity> {
   public ResourceLocation getAnimationResource(Pms02Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pms02.animation.json");
   }

   public ResourceLocation getModelResource(Pms02Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pms02.geo.json");
   }

   public ResourceLocation getTextureResource(Pms02Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/pms02.png");
   }

   public void setCustomAnimations(Pms02Entity animatable, long instanceId, AnimationState animationState) {
      EntityModelData entityData = (EntityModelData)animationState.getData(DataTickets.ENTITY_MODEL_DATA);
      CoreGeoBone head = this.getAnimationProcessor().getBone("gun");
      if (head != null) {
         head.setRotY(entityData.netHeadYaw() * (float) (Math.PI / 180.0));
         head.setRotX(entityData.headPitch() * (float) (Math.PI / 180.0));
      }
   }
}
