package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.trader.MechTraderEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class MechTraderEntityModel extends GeoModel<MechTraderEntity> {
   public ResourceLocation getAnimationResource(MechTraderEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/mech_trader.animation.json");
   }

   public ResourceLocation getModelResource(MechTraderEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/mech_trader.geo.json");
   }

   public ResourceLocation getTextureResource(MechTraderEntity animatable) {
      return new ResourceLocation("pomkotsmechs", "textures/entity/mech_trader.png");
   }

   public void setCustomAnimations(MechTraderEntity animatable, long instanceId, AnimationState animationState) {
      EntityModelData entityData = (EntityModelData)animationState.getData(DataTickets.ENTITY_MODEL_DATA);
      CoreGeoBone head = this.getAnimationProcessor().getBone("Head");
      if (head != null) {
         head.setRotY((entityData.netHeadYaw() - 90.0F) * (float) (Math.PI / 180.0));
         head.setRotX(entityData.headPitch() * (float) (Math.PI / 180.0));
      }
   }
}
