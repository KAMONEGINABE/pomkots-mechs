package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.mob.Pms05Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class Pms05EntityModel extends GeoModel<Pms05Entity> {
   public ResourceLocation getAnimationResource(Pms05Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pms05.animation.json");
   }

   public ResourceLocation getModelResource(Pms05Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pms05.geo.json");
   }

   public ResourceLocation getTextureResource(Pms05Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/pms05.png");
   }

   public void setCustomAnimations(Pms05Entity animatable, long instanceId, AnimationState animationState) {
      EntityModelData entityData = (EntityModelData)animationState.getData(DataTickets.ENTITY_MODEL_DATA);
      CoreGeoBone head = this.getAnimationProcessor().getBone("head");
      if (head != null) {
         head.setRotY(entityData.netHeadYaw() * (float) (Math.PI / 180.0));
      }

      CoreGeoBone weapon = this.getAnimationProcessor().getBone("weapon");
      if (weapon != null) {
         weapon.setRotX(entityData.headPitch() * (float) (Math.PI / 180.0));
      }
   }
}
