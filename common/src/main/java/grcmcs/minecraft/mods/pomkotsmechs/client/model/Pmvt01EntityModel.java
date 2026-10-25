package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.turret.Pmvt01Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class Pmvt01EntityModel extends GeoModel<Pmvt01Entity> {
   public ResourceLocation getAnimationResource(Pmvt01Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pmvt01.animation.json");
   }

   public ResourceLocation getModelResource(Pmvt01Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pmvt01.geo.json");
   }

   public ResourceLocation getTextureResource(Pmvt01Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/pms01.png");
   }

   public void setCustomAnimations(Pmvt01Entity animatable, long instanceId, AnimationState animationState) {
      EntityModelData entityData = (EntityModelData)animationState.getData(DataTickets.ENTITY_MODEL_DATA);
      CoreGeoBone body = this.getAnimationProcessor().getBone("body");
      CoreGeoBone main = this.getAnimationProcessor().getBone("main");
      if (body != null && main != null) {
         body.setRotY(entityData.netHeadYaw() * (float) (Math.PI / 180.0));
         main.setRotX(entityData.headPitch() * (float) (Math.PI / 180.0));
      }
   }
}
