package grcmcs.minecraft.mods.pomkotsmechs.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.Pmvc01EntityModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb99Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.function.Predicate;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class Pmvc01EntityRenderer extends GeoEntityRenderer<Pmvc01Entity> {
   private static final Logger LOGGER = LoggerFactory.getLogger("pomkotsmechs");
   ItemRenderer itemRenderer;

   public Pmvc01EntityRenderer(Context renderManager) {
      this(renderManager, new Pmvc01EntityModel());
   }

   public Pmvc01EntityRenderer(Context renderManager, Pmvc01EntityModel model) {
      super(renderManager, model);
      this.addRenderLayer(new Pmvc01EntityPartsLayer(this, renderManager.m_174025_()));
      this.itemRenderer = renderManager.m_174025_();
   }

   public void actuallyRender(
      PoseStack poseStack,
      Pmvc01Entity animatable,
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
      if (animatable.m_20202_() instanceof Pmb99Entity pmb99) {
         poseStack.m_85836_();
         poseStack.m_85837_(pmb99.seatPos.f_82479_, pmb99.seatPos.f_82480_, pmb99.seatPos.f_82481_);
      }

      super.actuallyRender(
         poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha
      );
      this.renderParts(
         poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha
      );
      if (animatable.getDrivingPassenger() != null) {
         animatable.setClientSeatPos(this.getSeatPosition(model));
         animatable.setMainCameraPosition(this.getMainCameraPosition(model, animatable, partialTick, 1.0F));
      }

      RenderUtils.renderAdditionalHud2(animatable, poseStack, bufferSource, this.f_114476_, 1.0F);
      if (animatable.m_20202_() instanceof Pmb99Entity pmb99) {
         poseStack.m_85849_();
      }
   }

   public void renderParts(
      PoseStack poseStack,
      Pmvc01Entity animatable,
      BakedGeoModel bakedModel,
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
      poseStack.m_85836_();
      float yaw = this.lerpYaw(partialTick, animatable.f_20884_, animatable.f_20883_) + 180.0F;
      poseStack.m_252781_(Axis.f_252436_.m_252977_(-yaw));
      this.renderPart(
         animatable.getHeadParts(),
         item -> item instanceof BasePartsItem.Head,
         poseStack,
         animatable,
         bakedModel,
         renderType,
         bufferSource,
         packedLight,
         packedOverlay
      );
      this.renderPart(
         animatable.getBodyParts(),
         item -> item instanceof BasePartsItem.Body,
         poseStack,
         animatable,
         bakedModel,
         renderType,
         bufferSource,
         packedLight,
         packedOverlay
      );
      this.renderPart(
         animatable.getArmParts(),
         item -> item instanceof BasePartsItem.Arm,
         poseStack,
         animatable,
         bakedModel,
         renderType,
         bufferSource,
         packedLight,
         packedOverlay
      );
      this.renderPart(
         animatable.getLegsParts(),
         item -> item instanceof BasePartsItem.Legs,
         poseStack,
         animatable,
         bakedModel,
         renderType,
         bufferSource,
         packedLight,
         packedOverlay
      );
      poseStack.m_85849_();
   }

   private float lerpYaw(float partialTick, float prevYaw, float currentYaw) {
      float delta = Mth.m_14177_(currentYaw - prevYaw);
      return prevYaw + partialTick * delta;
   }

   public void renderPart(
      ItemStack itemStack,
      Predicate<Item> isTarget,
      PoseStack poseStack,
      Pmvc01Entity animatable,
      BakedGeoModel bakedModel,
      RenderType renderType,
      MultiBufferSource bufferSource,
      int packedLight,
      int packedOverlay
   ) {
      this.renderPart(itemStack, isTarget, null, poseStack, animatable, bakedModel, renderType, bufferSource, packedLight, packedOverlay);
   }

   public void renderPart(
      ItemStack itemStack,
      Predicate<Item> isTarget,
      BasePartsItem.AttachSide side,
      PoseStack poseStack,
      Pmvc01Entity animatable,
      BakedGeoModel bakedModel,
      RenderType renderType,
      MultiBufferSource bufferSource,
      int packedLight,
      int packedOverlay
   ) {
      if (!itemStack.m_41619_()) {
         Item i = itemStack.m_41720_();
         if (isTarget.test(i)) {
            BasePartsItem ti = (BasePartsItem)i;
            if (ti instanceof BasePartsItem.Weapon w) {
               w.setSide(side);
            }

            ti.setParentEntity(animatable);
            ti.setCachedBoneFinder(animatable.getBoneFinder());
            this.itemRenderer
               .m_269491_(animatable, itemStack, ItemDisplayContext.NONE, false, poseStack, bufferSource, null, packedLight, packedOverlay, packedLight);
            ti.setCachedBoneFinder(null);
            ti.setParentEntity(null);
            if (ti instanceof BasePartsItem.Weapon w) {
               w.setSide(null);
            }
         }
      }
   }

   private Vec3 getSeatPosition(BakedGeoModel model) {
      if (model != null) {
         GeoBone vcSeat = (GeoBone)model.getBone("head").get();
         GeoBone vcRoot = (GeoBone)model.getBone("root").get();
         Vector3d seatPos = vcSeat.getLocalPosition();
         Vector3d rootPos = vcRoot.getLocalPosition();
         return new Vec3(seatPos.x * 1.0 - rootPos.x * 1.0, seatPos.y * 1.0 - rootPos.y * 1.0 - 4.2F, seatPos.z * 1.0 - rootPos.z * 1.0);
      } else {
         return Vec3.f_82478_;
      }
   }

   private Vec3 getMainCameraPosition(BakedGeoModel model, Pmvc01Entity entity, float partialTick, float scale) {
      if (model != null) {
         GeoBone vcSeat = null;
         if (Utils.shouldRenderCockpit(entity)) {
            vcSeat = (GeoBone)model.getBone("eyecam").get();
         } else {
            vcSeat = (GeoBone)model.getBone("maincam").get();
         }

         Vector3d seatPos = vcSeat.getLocalPosition();
         return new Vec3(seatPos.x, seatPos.y * scale, seatPos.z);
      } else {
         return Vec3.f_82478_;
      }
   }

   public void preApplyRenderLayers(
      PoseStack poseStack,
      Pmvc01Entity animatable,
      BakedGeoModel model,
      RenderType renderType,
      MultiBufferSource bufferSource,
      VertexConsumer buffer,
      float partialTick,
      int packedLight,
      int packedOverlay
   ) {
      this.scaleHeight = 1.0F;
      this.scaleWidth = 1.0F;
      animatable.setupBoneFinder(model);
      this.setWeaponsVisibility(model, animatable, animatable.isMainMode());
      super.preApplyRenderLayers(poseStack, animatable, model, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
   }

   private void rotateBooster(Pmvc01Entity entity, long uniqueId, float partialTick) {
      CoreGeoBone flame = (CoreGeoBone)this.getGeoModel().getBone("fire_bpc_super_boost").orElse(null);
      if (flame != null) {
         if (!entity.isSuperBoost()) {
            flame.setRotY(0.0F);
            flame.setRotX(0.0F);
         } else {
            float bodyYaw = entity.m_146908_();
            float bodyPitch = entity.m_146909_();
            Vec3 vel = entity.m_20184_();
            if (!(vel.m_82556_() < 1.0E-6)) {
               float velocityYaw = (float)(Mth.m_14136_(vel.f_82481_, vel.f_82479_) * (180.0 / Math.PI)) - 90.0F;
               float velocityPitch = (float)(
                  -Mth.m_14136_(vel.f_82480_, Math.sqrt(vel.f_82479_ * vel.f_82479_ + vel.f_82481_ * vel.f_82481_)) * (180.0 / Math.PI)
               );
               float localYaw = velocityYaw - bodyYaw;
               float localPitch = velocityPitch - bodyPitch;
               localYaw += 180.0F;
               localPitch *= -1.0F;
               float currentYaw = flame.getRotY() * (180.0F / (float)Math.PI);
               float currentPitch = flame.getRotX() * (180.0F / (float)Math.PI);
               float smoothYaw = Mth.m_14179_(0.2F, currentYaw, localYaw);
               float smoothPitch = Mth.m_14179_(0.2F, currentPitch, localPitch);
               flame.setRotY(smoothYaw * (float) (Math.PI / 180.0));
               flame.setRotX(smoothPitch * (float) (Math.PI / 180.0));
            }
         }
      }
   }

   private void rotateBooster3(Pmvc01Entity entity, long uniqueId, float partialTick) {
      CoreGeoBone flame = (CoreGeoBone)this.getGeoModel().getBone("fire_bpcr_quick").orElse(null);
      if (flame != null) {
         if (!entity.getActionEvasion().isInAction()) {
            flame.setRotY(0.0F);
            flame.setRotX(0.0F);
         } else {
            Vec3 motion = entity.m_20184_();
            float targetYaw = (float)Math.toDegrees(Math.atan2(-motion.f_82479_, motion.f_82481_));
            float targetPitch = (float)Math.toDegrees(
               Math.atan2(motion.f_82480_, Math.sqrt(motion.f_82479_ * motion.f_82479_ + motion.f_82481_ * motion.f_82481_))
            );
            float currentYaw = (float)Math.toDegrees(flame.getRotY());
            float currentPitch = (float)Math.toDegrees(flame.getRotX());
            float deltaYaw = Mth.m_14177_(targetYaw - currentYaw);
            float deltaPitch = Mth.m_14177_(targetPitch - currentPitch);
            float maxTurnPerTick = 5.0F;
            deltaYaw = Mth.m_14036_(deltaYaw, -maxTurnPerTick, maxTurnPerTick);
            deltaPitch = Mth.m_14036_(deltaPitch, -maxTurnPerTick, maxTurnPerTick);
            float newYaw = Mth.m_14177_(currentYaw + deltaYaw * 0.5F);
            float newPitch = Mth.m_14177_(currentPitch + deltaPitch * 0.5F);
            flame.setRotY((float)Math.toRadians(newYaw));
            flame.setRotX((float)Math.toRadians(newPitch));
         }
      }
   }

   private void rotateBooster2(Pmvc01Entity entity, long uniqueId, float partialTick) {
      if (entity.getActionEvasion().isInAction()) {
         CoreGeoBone flame1 = (CoreGeoBone)this.getGeoModel().getBone("fire_bpcr_quick").orElse(null);
         if (flame1 != null) {
            CoreGeoBone flame2 = (CoreGeoBone)this.getGeoModel().getBone("fire_bpcl_quick").orElse(null);
            if (flame2 != null) {
               double dx = entity.m_20184_().f_82479_;
               double dy = entity.m_20184_().f_82480_;
               double dz = entity.m_20184_().f_82481_;
               float yaw = (float)(Mth.m_14136_(-dx, dz) * (180.0 / Math.PI));
               float pitch = (float)(Mth.m_14136_(dy, Math.sqrt(dx * dx + dz * dz)) * (180.0 / Math.PI));
               yaw += 180.0F;
               pitch = -pitch;
               yaw = Mth.m_14177_(yaw);
               pitch = Mth.m_14177_(pitch);
               flame1.setRotX(pitch * (float) (Math.PI / 180.0));
               flame1.setRotY(yaw * (float) (Math.PI / 180.0));
               flame2.setRotX(pitch * (float) (Math.PI / 180.0));
               flame2.setRotY(yaw * (float) (Math.PI / 180.0));
            }
         }
      }
   }

   private void setBoosterVisibilitty(BakedGeoModel model, Pmvc01Entity ent) {
      Vec3 vel = ent.m_20184_();
      vel = vel.m_82524_((float)Math.toRadians(ent.m_146908_()));
      boolean hasHorizontalBoost = !ent.noHorizontalBoost();
      if (vel.f_82479_ > 1.0 && hasHorizontalBoost) {
         ((GeoBone)model.getBone("fire_rsb").get()).setHidden(false);
         ((GeoBone)model.getBone("fire_lsb").get()).setHidden(true);
      } else if (vel.f_82479_ < -1.0 && hasHorizontalBoost) {
         ((GeoBone)model.getBone("fire_rsb").get()).setHidden(true);
         ((GeoBone)model.getBone("fire_lsb").get()).setHidden(false);
      } else {
         ((GeoBone)model.getBone("fire_rsb").get()).setHidden(true);
         ((GeoBone)model.getBone("fire_lsb").get()).setHidden(true);
      }

      if (vel.f_82481_ > 1.0 && hasHorizontalBoost) {
         ((GeoBone)model.getBone("fire_bpcr").get()).setHidden(false);
         ((GeoBone)model.getBone("fire_bpcl").get()).setHidden(false);
         ((GeoBone)model.getBone("fire_rl").get()).setHidden(false);
         ((GeoBone)model.getBone("fire_ll").get()).setHidden(false);
      } else {
         ((GeoBone)model.getBone("fire_bpcr").get()).setHidden(true);
         ((GeoBone)model.getBone("fire_bpcl").get()).setHidden(true);
         ((GeoBone)model.getBone("fire_rl").get()).setHidden(true);
         ((GeoBone)model.getBone("fire_ll").get()).setHidden(true);
      }

      if (ent.getDriverInput() != null && ent.getDriverInput().isJumpPressed() && ent.m_20068_() && !ent.isOverHeat()) {
         ((GeoBone)model.getBone("fire_bpcr").get()).setHidden(false);
         ((GeoBone)model.getBone("fire_bpcl").get()).setHidden(false);
      } else {
         ((GeoBone)model.getBone("fire_bpcr").get()).setHidden(true);
         ((GeoBone)model.getBone("fire_bpcl").get()).setHidden(true);
      }
   }

   private void setWeaponsVisibility(BakedGeoModel model, Pmvc01Entity ent, boolean b) {
      ((GeoBone)model.getBone("left_shoulder_hunger").get()).setHidden(((Pmvc01Entity)this.animatable).getLeftShoulderWeapon().m_41619_());
      ((GeoBone)model.getBone("right_shoulder_hunger").get()).setHidden(((Pmvc01Entity)this.animatable).getRightShoulderWeapon().m_41619_());
   }

   public boolean shouldShowName(Pmvc01Entity entity) {
      return false;
   }
}
