package grcmcs.minecraft.mods.pomkotsmechs.client.model;

import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb06Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class Pmb06EntityModel extends GeoModel<Pmb06Entity> {
   public ResourceLocation getAnimationResource(Pmb06Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "animations/pmb06.animation.json");
   }

   public ResourceLocation getModelResource(Pmb06Entity animatable) {
      return new ResourceLocation("pomkotsmechs", "geo/pmb06.geo.json");
   }

   public ResourceLocation getTextureResource(Pmb06Entity animatable) {
      return animatable.getAiMode() == -2
         ? new ResourceLocation("pomkotsmechs", "textures/entity/pmb01inactive.png")
         : new ResourceLocation("pomkotsmechs", "textures/entity/pmb05.png");
   }

   public void setCustomAnimations(Pmb06Entity animatable, long instanceId, AnimationState animationState) {
      EntityModelData entityData = (EntityModelData)animationState.getData(DataTickets.ENTITY_MODEL_DATA);
      CoreGeoBone body = this.getAnimationProcessor().getBone("root");
      CoreGeoBone beam = this.getAnimationProcessor().getBone("beam_scale");
      if (beam != null && body != null && animatable.getLaserLength() > 1) {
         beam.setRotX(entityData.headPitch() * (float) (Math.PI / 180.0));
      }
   }

   public static void pointBonePitchTowards(CoreGeoBone bone, Vec3 fromPos, Vec3 toPos) {
      Vec3 delta = toPos.m_82546_(fromPos);
      double horizontalDistance = Math.sqrt(delta.f_82479_ * delta.f_82479_ + delta.f_82481_ * delta.f_82481_);
      float pitchRadians = (float)(-Math.atan2(delta.f_82480_, horizontalDistance));
      bone.setRotX(pitchRadians);
   }
}
