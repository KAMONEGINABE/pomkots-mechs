package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.Pms08Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class Pms08EntityModel extends GeoModel<Pms08Entity> {
   public ResourceLocation getAnimationResource(Pms08Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pms08.animation.json");
   }

   public ResourceLocation getModelResource(Pms08Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pms08.geo.json");
   }

   public ResourceLocation getTextureResource(Pms08Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/pms01.png");
   }

   public void setCustomAnimations(Pms08Entity animatable, long instanceId, AnimationState animationState) {
      EntityModelData entityData = (EntityModelData)animationState.getData(DataTickets.ENTITY_MODEL_DATA);
      CoreGeoBone head = this.getAnimationProcessor().getBone("head");
      if (head != null) {
         head.setRotY(entityData.netHeadYaw() * (float) (Math.PI / 180.0));
         head.setRotX(entityData.headPitch() * (float) (Math.PI / 180.0));
      }
   }
}
