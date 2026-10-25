package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.ArenaReceptionistEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class ArenaReceptionistEntityModel extends GeoModel<ArenaReceptionistEntity> {
   public ResourceLocation getAnimationResource(ArenaReceptionistEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/mech_pilot.animation.json");
   }

   public ResourceLocation getModelResource(ArenaReceptionistEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/mech_pilot_slim.geo.json");
   }

   public ResourceLocation getTextureResource(ArenaReceptionistEntity animatable) {
      return new ResourceLocation("minecraft", "textures/entity/player/slim/efe.png");
   }

   public void setCustomAnimations(ArenaReceptionistEntity animatable, long instanceId, AnimationState animationState) {
      EntityModelData entityData = (EntityModelData)animationState.getData(DataTickets.ENTITY_MODEL_DATA);
      CoreGeoBone head = this.getAnimationProcessor().getBone("Head");
      if (head != null) {
         head.setRotY(entityData.netHeadYaw() * (float) (Math.PI / 180.0));
         head.setRotX(entityData.headPitch() * (float) (Math.PI / 180.0));
      }
   }
}
