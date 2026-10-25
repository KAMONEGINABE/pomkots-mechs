package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.Pms04Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class Pms04EntityModel extends GeoModel<Pms04Entity> {
   public ResourceLocation getAnimationResource(Pms04Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pms04.animation.json");
   }

   public ResourceLocation getModelResource(Pms04Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pms04.geo.json");
   }

   public ResourceLocation getTextureResource(Pms04Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/pms04.png");
   }

   public void setCustomAnimations(Pms04Entity animatable, long instanceId, AnimationState animationState) {
      EntityModelData entityData = (EntityModelData)animationState.getData(DataTickets.ENTITY_MODEL_DATA);
      CoreGeoBone head = this.getAnimationProcessor().getBone("head");
      if (head != null) {
         head.setRotY(entityData.netHeadYaw() * (float) (Math.PI / 180.0));
         head.setRotX(entityData.headPitch() * (float) (Math.PI / 180.0));
      }
   }
}
