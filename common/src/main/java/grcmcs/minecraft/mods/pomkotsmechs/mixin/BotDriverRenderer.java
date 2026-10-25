package grcmcs.minecraft.mods.pomkotsmechs.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.Pmb99Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.monster.boss.legacy.Pmb01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicle;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class BotDriverRenderer<T extends LivingEntity, M extends EntityModel<T>> {
   private static final Logger LOGGER = LoggerFactory.getLogger("pomkotsmechs");

   @Inject(method = "render", at = @At("HEAD"), cancellable = true)
   public void onRender(T entity, float yaw, float tickDelta, PoseStack poseStack, MultiBufferSource vertexConsumers, int light, CallbackInfo ci) {
      if (entity.m_20202_() instanceof PomkotsVehicle robot) {
         Vec3 seatPos = robot.getClientSeatPos(entity);
         if (robot instanceof Pmvc01Entity pmvc && pmvc.m_20202_() instanceof Pmb99Entity pmb99) {
            poseStack.m_85837_(
               seatPos.m_7096_() + pmb99.seatPos.f_82479_, seatPos.m_7098_() + pmb99.seatPos.f_82480_, seatPos.m_7094_() + pmb99.seatPos.m_7094_()
            );
         }

         if (robot instanceof Pmvc01Entity pmvc && pmvc.getHeadParts().m_41720_() instanceof BasePartsItem.Head h && h.isFullCovered()) {
            poseStack.m_85837_(seatPos.m_7096_(), seatPos.m_7098_() - 1.0, seatPos.m_7094_());
         } else {
            poseStack.m_85837_(seatPos.m_7096_(), seatPos.m_7098_(), seatPos.m_7094_());
         }
      } else if (entity.m_20202_() instanceof Pmb01Entity robot) {
         Vec3 seatPos = robot.getClientSeatPos();
         poseStack.m_85837_(seatPos.m_7096_(), seatPos.m_7098_(), seatPos.m_7094_());
      }
   }
}
