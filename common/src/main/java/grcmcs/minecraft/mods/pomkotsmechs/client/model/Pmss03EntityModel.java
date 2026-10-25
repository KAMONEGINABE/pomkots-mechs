package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.tiny.Pmss03Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class Pmss03EntityModel extends GeoModel<Pmss03Entity> {
   public ResourceLocation getAnimationResource(Pmss03Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pmss03.animation.json");
   }

   public ResourceLocation getModelResource(Pmss03Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pmss03.geo.json");
   }

   public ResourceLocation getTextureResource(Pmss03Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/pms01.png");
   }

   public void setCustomAnimations(Pmss03Entity animatable, long instanceId, AnimationState animationState) {
      EntityModelData entityData = (EntityModelData)animationState.getData(DataTickets.ENTITY_MODEL_DATA);
      CoreGeoBone head = this.getAnimationProcessor().getBone("head");
      if (head != null) {
         head.setRotY(entityData.netHeadYaw() * (float) (Math.PI / 180.0));
         head.setRotX(entityData.headPitch() * (float) (Math.PI / 180.0));
      }
   }
}
