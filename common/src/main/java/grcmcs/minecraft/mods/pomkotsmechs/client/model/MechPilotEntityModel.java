package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.pilot.MechPilotEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class MechPilotEntityModel extends GeoModel<MechPilotEntity> {
   public ResourceLocation getAnimationResource(MechPilotEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/mech_pilot.animation.json");
   }

   public ResourceLocation getModelResource(MechPilotEntity animatable) {
      return new ResourceLocation(animatable.getModelLocation());
   }

   public ResourceLocation getTextureResource(MechPilotEntity animatable) {
      return new ResourceLocation(animatable.getTextureLocation());
   }

   public void setCustomAnimations(MechPilotEntity animatable, long instanceId, AnimationState animationState) {
      EntityModelData entityData = (EntityModelData)animationState.getData(DataTickets.ENTITY_MODEL_DATA);
      CoreGeoBone head = this.getAnimationProcessor().getBone("Head");
      if (head != null) {
         head.setRotY(entityData.netHeadYaw() * (float) (Math.PI / 180.0));
         head.setRotX(entityData.headPitch() * (float) (Math.PI / 180.0));
      }
   }
}
