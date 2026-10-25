package grcmcs.minecraft.mods.pomkotsmechs.client.model.turret;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.turret.Pmt03Entity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class Pmt03EntityModel extends GeoModel<Pmt03Entity> {
   public ResourceLocation getAnimationResource(Pmt03Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pmt03.animation.json");
   }

   public ResourceLocation getModelResource(Pmt03Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pmt03.geo.json");
   }

   public ResourceLocation getTextureResource(Pmt03Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/pms01.png");
   }

   public void setCustomAnimations(Pmt03Entity animatable, long instanceId, AnimationState animationState) {
      EntityModelData entityData = (EntityModelData)animationState.getData(DataTickets.ENTITY_MODEL_DATA);
      CoreGeoBone body = this.getAnimationProcessor().getBone("body");
      CoreGeoBone main = this.getAnimationProcessor().getBone("main");
      if (body != null && main != null) {
         body.setRotY(entityData.netHeadYaw() * (float) (Math.PI / 180.0));
         main.setRotX(entityData.headPitch() * (float) (Math.PI / 180.0));
      }
   }
}
