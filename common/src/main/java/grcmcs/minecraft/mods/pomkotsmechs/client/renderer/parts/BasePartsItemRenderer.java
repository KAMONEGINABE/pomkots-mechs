package grcmcs.minecraft.mods.pomkotsmechs.client.renderer.parts;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.CachedBoneFinder;
import java.util.Optional;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class BasePartsItemRenderer<T extends BasePartsItem> extends GeoItemRenderer<T> {
   public BasePartsItemRenderer(BasePartsItemModel model) {
      super(model);
   }

   protected void copy(CachedBoneFinder mech, BakedGeoModel parts, String boneName) {
      GeoBone m = mech.getBone(boneName);
      GeoBone p = (GeoBone)parts.getBone(boneName).get();
      p.setPosX(m.getPosX());
      p.setPosY(m.getPosY());
      p.setPosZ(m.getPosZ());
      p.setRotX(m.getRotX());
      p.setRotY(m.getRotY());
      p.setRotZ(m.getRotZ());
   }

   public static class Arm<T extends BasePartsItem> extends BasePartsItemRenderer<T> {
      public Arm(BasePartsItemModel model) {
         super(model);
      }

      public void preApplyRenderLayers(
         PoseStack poseStack,
         T animatable,
         BakedGeoModel model,
         RenderType renderType,
         MultiBufferSource bufferSource,
         VertexConsumer buffer,
         float partialTick,
         int packedLight,
         int packedOverlay
      ) {
         CachedBoneFinder parent = animatable.getCachedBoneFinder();
         if (parent != null) {
            this.copy(parent, model, "hips");
            this.copy(parent, model, "spine");
            this.copy(parent, model, "body");
            this.copy(parent, model, "body_rot");
            this.copy(parent, model, "right_shoulder");
            this.copy(parent, model, "right_upper_arm");
            this.copy(parent, model, "right_lower_arm");
            this.copy(parent, model, "left_shoulder");
            this.copy(parent, model, "left_upper_arm");
            this.copy(parent, model, "left_lower_arm");
         }

         super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
      }
   }

   public static class Body<T extends BasePartsItem> extends BasePartsItemRenderer<T> {
      public Body(BasePartsItemModel model) {
         super(model);
      }

      public void preApplyRenderLayers(
         PoseStack poseStack,
         T animatable,
         BakedGeoModel model,
         RenderType renderType,
         MultiBufferSource bufferSource,
         VertexConsumer buffer,
         float partialTick,
         int packedLight,
         int packedOverlay
      ) {
         CachedBoneFinder parent = animatable.getCachedBoneFinder();
         if (parent != null) {
            this.copy(parent, model, "root");
            this.copy(parent, model, "hips");
            this.copy(parent, model, "spine");
            this.copy(parent, model, "body");
            this.copy(parent, model, "body_rot");
         }

         Pmvc01Entity pe = animatable.getParentEntity();
         boolean showLid = pe != null && pe.getHeadParts().m_41720_() instanceof BasePartsItem.Head head && head.isFullCovered();
         Optional<GeoBone> lid = model.getBone("body_lid");
         if (lid.isPresent()) {
            lid.get().setHidden(!showLid);
         }

         super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
      }
   }

   public static class Booster<T extends BasePartsItem.Booster> extends BasePartsItemRenderer<T> {
      public Booster(BasePartsItemModel model) {
         super(model);
      }
   }

   public static class Extension<T extends BasePartsItem.Extension> extends BasePartsItemRenderer<T> {
      public Extension(BasePartsItemModel model) {
         super(model);
      }
   }

   public static class Fuel<T extends BasePartsItem.Fuel> extends BasePartsItemRenderer<T> {
      public Fuel(BasePartsItemModel model) {
         super(model);
      }
   }

   public static class Generator<T extends BasePartsItem.Generator> extends BasePartsItemRenderer<T> {
      public Generator(BasePartsItemModel model) {
         super(model);
      }
   }

   public static class Head<T extends BasePartsItem> extends BasePartsItemRenderer<T> {
      public Head(BasePartsItemModel model) {
         super(model);
      }

      public void preApplyRenderLayers(
         PoseStack poseStack,
         T animatable,
         BakedGeoModel model,
         RenderType renderType,
         MultiBufferSource bufferSource,
         VertexConsumer buffer,
         float partialTick,
         int packedLight,
         int packedOverlay
      ) {
         CachedBoneFinder parent = animatable.getCachedBoneFinder();
         if (parent != null) {
            this.copy(parent, model, "hips");
            this.copy(parent, model, "spine");
            this.copy(parent, model, "body");
            this.copy(parent, model, "body_rot");
         }

         super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
      }

      public void actuallyRender(
         PoseStack poseStack,
         T animatable,
         BakedGeoModel model,
         RenderType renderType,
         MultiBufferSource bufferSource,
         VertexConsumer buffer,
         boolean isReRender,
         float partialTick,
         int packedLight,
         int packedOverlay,
         float red,
         float green,
         float blue,
         float alpha
      ) {
         Pmvc01Entity parentEntity = animatable.getParentEntity();
         if (parentEntity != null && parentEntity.getBodyParts().m_41720_() instanceof BasePartsItem.Body body && body.getNeckPos() != 0.0F) {
            poseStack.m_85836_();
            poseStack.m_252880_(0.0F, body.getNeckPos(), 0.0F);
            super.actuallyRender(
               poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha
            );
            poseStack.m_85849_();
         } else {
            super.actuallyRender(
               poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha
            );
         }
      }
   }

   public static class Legs<T extends BasePartsItem> extends BasePartsItemRenderer<T> {
      public Legs(BasePartsItemModel model) {
         super(model);
      }

      public void preApplyRenderLayers(
         PoseStack poseStack,
         T animatable,
         BakedGeoModel model,
         RenderType renderType,
         MultiBufferSource bufferSource,
         VertexConsumer buffer,
         float partialTick,
         int packedLight,
         int packedOverlay
      ) {
         CachedBoneFinder parent = animatable.getCachedBoneFinder();
         if (parent != null) {
            this.copy(parent, model, "root");
            this.copy(parent, model, "hips");
            this.copy(parent, model, "right_upper_leg");
            this.copy(parent, model, "right_lower_leg");
            this.copy(parent, model, "right_foot");
            this.copy(parent, model, "left_upper_leg");
            this.copy(parent, model, "left_lower_leg");
            this.copy(parent, model, "left_foot");
         }

         super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
      }
   }

   public static class Magazine<T extends BasePartsItem.Magazine> extends BasePartsItemRenderer<T> {
      public Magazine(BasePartsItemModel model) {
         super(model);
      }
   }

   public static class Weapon<T extends BasePartsItem.Weapon> extends BasePartsItemRenderer<T> {
      public Weapon(BasePartsItemModel model) {
         super(model);
      }

      public void actuallyRender(
         PoseStack poseStack,
         T animatable,
         BakedGeoModel model,
         RenderType renderType,
         MultiBufferSource bufferSource,
         VertexConsumer buffer,
         boolean isReRender,
         float partialTick,
         int packedLight,
         int packedOverlay,
         float red,
         float green,
         float blue,
         float alpha
      ) {
         super.actuallyRender(
            poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha
         );
      }
   }
}
