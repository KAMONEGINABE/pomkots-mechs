package grcmcs.minecraft.mods.pomkotsmechs.client.hud;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.architectury.event.events.client.ClientGuiEvent.RenderHud;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.ScreenFadeManager;
import grcmcs.minecraft.mods.pomkotsmechs.client.input.TargetLocker;
import grcmcs.minecraft.mods.pomkotsmechs.client.misc.ClientHudShake;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.PomkotsVehicleBase;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.Action;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.equipment.action.ActionController;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.turret.Pmvt01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult.Type;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PomkotsHud implements RenderHud {
   private static final int FG_COLOR = -1728018743;
   private static final int BG_COLOR = 1431655765;
   private static boolean isActive = true;
   private static final ResourceLocation CROSSHAIR_TEXTURE = PomkotsMechs.id("textures/crosshair/crosshair0.png");
   private static final ResourceLocation TARGET_LOCK_TEXTURE = PomkotsMechs.id("textures/crosshair/crosshair2.png");
   private static final ResourceLocation TARGET_LOCK_HARD_TEXTURE = PomkotsMechs.id("textures/crosshair/custom/target_hard.png");
   private static final ResourceLocation TARGET_LOCK_SOFT_TEXTURE = PomkotsMechs.id("textures/crosshair/custom/target_soft.png");
   private static final ResourceLocation TARGET_LOCK_SOFT_TEXTURE2 = PomkotsMechs.id("textures/crosshair/custom/target_icon.png");
   private static final ResourceLocation TARGET_LOCK_MULTI_TEXTURE = PomkotsMechs.id("textures/crosshair/custom/target_multi.png");
   private static final ResourceLocation REPAIR_KIT_ICON = PomkotsMechs.id("textures/crosshair/custom/repair_icon.png");
   protected Minecraft mc = Minecraft.m_91087_();
   private static final Logger LOGGER = LoggerFactory.getLogger("pomkotsmechs");
   private float curHealth = 0.0F;
   private float prevHealth = 0.0F;
   private int curEN = 0;
   private int prevEN = 0;
   private int maxEN = 0;
   private int curCTRightArm = 0;
   private int prevCTRightArm = 0;
   private int maxCTRightArm = 0;
   private int curCTLeftArm = 0;
   private int prevCTLeftArm = 0;
   private int maxCTLeftArm = 0;
   private int curCTRightShoulder = 0;
   private int prevCTRightShoulder = 0;
   private int maxCTRightShoulder = 0;
   private int curCTLeftShoulder = 0;
   private int prevCTLeftShoulder = 0;
   private int maxCTLeftShoulder = 0;
   private int prevTick;
   private static final int FG_COLOR2 = -3681058;
   private static final int BG_COLOR2 = 1431655765;
   private static final int BG_ERR_COLOR2 = 1437204480;
   private static final int FG_ERR_COLOR2 = -1431699456;
   private static final ResourceLocation CROSSHAIR_TEXTURE2 = PomkotsMechs.id("textures/crosshair/custom/crosshair.png");
   private static final ResourceLocation DONUT_CD_FORE = PomkotsMechs.id("textures/crosshair/custom/front_cooldown.png");
   private static final ResourceLocation DONUT_AM_FORE = PomkotsMechs.id("textures/crosshair/custom/front_ammo.png");
   private static final int[] DIST_COLOR_GREEN = new int[]{-16711800, -16764126};
   private static final int[] DIST_COLOR_YELLOW = new int[]{15129088, -10593792};
   private static final int[] DIST_COLOR_RED = new int[]{-48060, -13434880};

   public static void setActive(boolean f) {
      isActive = f;
   }

   public void renderHud(GuiGraphics guiGraphics, float tickDelta) {
      LocalPlayer pl = this.mc.f_91074_;
      if (pl != null) {
         if (ScreenFadeManager.isActive()) {
            screenFade(guiGraphics);
            return;
         }

         if (!isActive) {
            return;
         }

         if (pl.m_20202_() instanceof PomkotsVehicleBase mech) {
            if (this.prevTick != pl.f_19797_) {
               this.updateValues(mech);
            }

            this.renderLockOnMarks(mech, guiGraphics, tickDelta);
            if (mech instanceof Pmvc01Entity pmvc01) {
               this.renderCustomMechHud(pmvc01, guiGraphics, tickDelta);
            } else if (mech instanceof Pmvt01Entity tr) {
               this.renderTurretHud(tr, guiGraphics, tickDelta);
            } else if (mech.isMainMode()) {
               this.renderHudBattle(mech, guiGraphics, tickDelta);
            } else {
               this.renderHudNormal(mech, guiGraphics, tickDelta);
            }

            this.prevTick = pl.f_19797_;
         }
      }
   }

   public static void screenFade(GuiGraphics gui) {
      float alpha = ScreenFadeManager.getAlpha();
      if (!(alpha <= 0.0F)) {
         int width = Minecraft.m_91087_().m_91268_().m_85445_();
         int height = Minecraft.m_91087_().m_91268_().m_85446_();
         int a = (int)(alpha * 255.0F);
         int color = a << 24;
         gui.m_280509_(0, 0, width, height, color);
      }
   }

   public void renderLockOnMarks(PomkotsVehicleBase mech, GuiGraphics guiGraphics, float tickDelta) {
      TargetLocker locker = TargetLocker.getInstance();
      if (locker.getHardLockTarget() != null) {
         this.renderLockOnMark(mech, locker.getHardLockTarget(), TARGET_LOCK_HARD_TEXTURE, guiGraphics, tickDelta, true);
      } else if (locker.getSoftLockTarget() != null) {
         this.renderLockOnMark(mech, locker.getSoftLockTarget(), TARGET_LOCK_SOFT_TEXTURE, guiGraphics, tickDelta, true);
      }

      this.renderMultiLockOnMarks(mech, locker.targetMulti, guiGraphics, tickDelta);
      this.renderMultiLockOnMarks(mech, locker.targetMultiRA, guiGraphics, tickDelta);
      this.renderMultiLockOnMarks(mech, locker.targetMultiLA, guiGraphics, tickDelta);
      this.renderMultiLockOnMarks(mech, locker.targetMultiRS, guiGraphics, tickDelta);
      this.renderMultiLockOnMarks(mech, locker.targetMultiLS, guiGraphics, tickDelta);
   }

   private static boolean shouldRenderDistanceOnTarget() {
      TargetLocker locker = TargetLocker.getInstance();
      return locker.getSoftLockTarget() != null || locker.getHardLockTarget() != null;
   }

   private void renderMultiLockOnMarks(PomkotsVehicleBase mech, Map<Integer, Entity> multiLock, GuiGraphics guiGraphics, float tickDelta) {
      if (!multiLock.isEmpty()) {
         for (Entry<Integer, Entity> entry : multiLock.entrySet()) {
            this.renderLockOnMark(mech, entry.getValue(), TARGET_LOCK_MULTI_TEXTURE, guiGraphics, tickDelta, false);
         }
      }
   }

   private void renderLockOnMark(
      PomkotsVehicleBase mech, Entity targetEntity, ResourceLocation texture, GuiGraphics guiGraphics, float tickDelta, boolean renderDistance
   ) {
      Vector3f screenPos = this.projectEntityToScreen(targetEntity, tickDelta);
      if (screenPos != null) {
         int screenX = (int)screenPos.x();
         int screenY = (int)screenPos.y();
         if (renderDistance && mech instanceof Pmvc01Entity pmvc01) {
            renderDistance(pmvc01, screenX, screenY, new EntityHitResult(targetEntity), guiGraphics, tickDelta, Minecraft.m_91087_());
         }

         int size = 16;
         int sizeSub = 10;
         guiGraphics.m_280163_(texture, screenX - size / 2, screenY - size / 2, 0.0F, 0.0F, size, size, size, size);
      }
   }

   private Vector3f projectEntityToScreen(Entity entity, float tickDelta) {
      Camera camera = this.mc.f_91063_.m_109153_();
      Vec3 entityPos = entity.m_20182_().m_82520_(0.0, entity.m_20206_() / 2.0F, 0.0);
      Vec3 camPos = camera.m_90583_();
      Quaternionf cameraRotation = camera.m_253121_();
      double x = Mth.m_14139_(tickDelta, entity.f_19790_, entity.m_20185_()) - camPos.f_82479_;
      double y = Mth.m_14139_(tickDelta, entity.f_19791_ + entity.m_20206_() / 2.0F, entity.m_20186_() + entity.m_20206_() / 2.0F) - camPos.f_82480_;
      double z = Mth.m_14139_(tickDelta, entity.f_19792_, entity.m_20189_()) - camPos.f_82481_;
      Vec3 relativePos = new Vec3(x, y, z);
      Quaternionf q = new Quaternionf();
      Vector3f transformed = new Vector3f((float)relativePos.f_82479_, (float)relativePos.f_82480_, (float)relativePos.f_82481_);
      transformed.rotate(cameraRotation.conjugate(q));
      if (transformed.z() < 0.1F) {
         return null;
      }

      Window window = this.mc.m_91268_();
      float aRatio = (float)window.m_85445_() / window.m_85446_();
      float fov = (float)Math.toRadians(((Integer)this.mc.f_91066_.m_231837_().m_231551_()).intValue());
      float halfFov = (float)Math.tan(fov / 2.0F);
      float screenX = window.m_85445_() / 2.0F * (1.0F - transformed.x() / (transformed.z() * halfFov * aRatio));
      float screenY = window.m_85446_() / 2.0F * (1.0F - transformed.y() / (transformed.z() * halfFov));
      return new Vector3f(screenX, screenY, transformed.z());
   }

   private void updateValues(PomkotsVehicleBase vehicle) {
      this.prevHealth = this.curHealth;
      this.curHealth = vehicle.m_21223_();
      this.prevEN = this.curEN;
      this.curEN = vehicle.getEnergy();
      this.maxEN = vehicle.getMaxEnergy();
      Action rArm;
      Action lArm;
      Action rShoulder;
      Action lShoulder;
      if (vehicle.isMainMode()) {
         rArm = vehicle.actionController.getActionFromType(ActionController.ActionType.R_ARM_MAIN);
         lArm = vehicle.actionController.getActionFromType(ActionController.ActionType.L_ARM_MAIN);
         rShoulder = vehicle.actionController.getActionFromType(ActionController.ActionType.R_SHL_MAIN);
         lShoulder = vehicle.actionController.getActionFromType(ActionController.ActionType.L_SHL_MAIN);
      } else {
         rArm = vehicle.actionController.getActionFromType(ActionController.ActionType.R_ARM_SUB);
         lArm = vehicle.actionController.getActionFromType(ActionController.ActionType.L_ARM_SUB);
         rShoulder = vehicle.actionController.getActionFromType(ActionController.ActionType.R_SHL_SUB);
         lShoulder = vehicle.actionController.getActionFromType(ActionController.ActionType.L_SHL_SUB);
      }

      if (rArm != null) {
         this.prevCTRightArm = this.curCTRightArm;
         this.curCTRightArm = rArm.currentCoolTime;
         this.maxCTRightArm = rArm.maxCoolTime;
      } else {
         this.prevCTRightArm = this.curCTRightArm = this.maxCTRightArm = 1;
      }

      if (lArm != null) {
         this.prevCTLeftArm = this.curCTLeftArm;
         this.curCTLeftArm = lArm.currentCoolTime;
         this.maxCTLeftArm = lArm.maxCoolTime;
      } else {
         this.prevCTLeftArm = this.curCTLeftArm = this.maxCTLeftArm = 1;
      }

      if (rShoulder != null) {
         this.prevCTRightShoulder = this.curCTRightShoulder;
         this.curCTRightShoulder = rShoulder.currentCoolTime;
         this.maxCTRightShoulder = rShoulder.maxCoolTime;
      } else {
         this.prevCTRightShoulder = this.curCTRightShoulder = this.maxCTRightShoulder = 1;
      }

      if (lShoulder != null) {
         this.prevCTLeftShoulder = this.curCTLeftShoulder;
         this.curCTLeftShoulder = lShoulder.currentCoolTime;
         this.maxCTLeftShoulder = lShoulder.maxCoolTime;
      } else {
         this.prevCTLeftShoulder = this.curCTLeftShoulder = this.maxCTLeftShoulder = 1;
      }
   }

   private void renderHudNormal(PomkotsVehicleBase protobot, GuiGraphics guiGraphics, float tickDelta) {
      this.renderCrossHair(guiGraphics, tickDelta);
      this.renderHealthBar(protobot, guiGraphics, tickDelta);
      this.renderEnergyBar(protobot, guiGraphics, tickDelta);
      this.renderCooldowns(protobot, guiGraphics, tickDelta);
   }

   private void renderHudBattle(PomkotsVehicleBase protobot, GuiGraphics guiGraphics, float tickDelta) {
      this.renderCrossHair(guiGraphics, tickDelta);
      this.renderHealthBar(protobot, guiGraphics, tickDelta);
      this.renderEnergyBar(protobot, guiGraphics, tickDelta);
      this.renderCooldowns(protobot, guiGraphics, tickDelta);
   }

   private void renderCrossHair(GuiGraphics guiGraphics, float tickDelta) {
      int textureWidth = 32;
      int textureHeight = 32;
      int screenWidth = this.mc.m_91268_().m_85445_();
      int screenHeight = this.mc.m_91268_().m_85446_();
      int startX = screenWidth / 2 - textureWidth / 2;
      int startY = screenHeight / 2 - textureHeight / 2;
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.6F);
      RenderSystem.setShader(GameRenderer::m_172817_);
      RenderSystem.setShaderTexture(0, CROSSHAIR_TEXTURE);
      guiGraphics.m_280398_(CROSSHAIR_TEXTURE, startX, startY, 0, 0.0F, 0.0F, textureWidth, textureHeight, textureWidth, textureHeight);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void renderHealthBar(PomkotsVehicleBase protobot, GuiGraphics guiGraphics, float tickDelta) {
      int screenWidth = this.mc.m_91268_().m_85445_();
      int screenHeight = this.mc.m_91268_().m_85446_();
      int width = 120;
      int height = 3;
      int x = screenWidth / 2 - width / 2;
      int y = screenHeight - 10;
      if (protobot.shouldRenderDefaultHud("renderHotbar")) {
         y -= 20;
      }

      int maxHealth = (int)protobot.m_21233_();
      guiGraphics.m_280509_(x, y, x + width, y + height, 1431655765);
      int healthWidthCur = (int)(width * Mth.m_14179_(tickDelta, this.curHealth / maxHealth, this.curHealth / maxHealth));
      int healthWidthPrev = (int)(width * Mth.m_14179_(tickDelta, this.prevHealth / maxHealth, this.prevHealth / maxHealth));
      guiGraphics.m_280509_(x, y, x + Mth.m_269140_(tickDelta, healthWidthPrev, healthWidthCur), y + height, -1728018743);
   }

   private void renderEnergyBar(PomkotsVehicleBase protobot, GuiGraphics guiGraphics, float tickDelta) {
      int screenWidth = this.mc.m_91268_().m_85445_();
      int screenHeight = this.mc.m_91268_().m_85446_();
      int maxFuel = this.maxEN;
      int width = 25;
      int height = 2;
      int x = screenWidth / 2 - width / 2;
      int y = screenHeight / 2 + 19;
      guiGraphics.m_280509_(x, y, x + width, y + height, 1431655765);
      float enLerp = Mth.m_14179_(tickDelta, this.prevEN, this.curEN);
      float fuelWidth = width * (enLerp / maxFuel);
      guiGraphics.m_280509_((int)(x + (width - fuelWidth) / 2.0F), y, (int)(x + (width - fuelWidth) / 2.0F + fuelWidth) + 1, y + height, -1728018743);
   }

   private void renderCooldowns(PomkotsVehicleBase protobot, GuiGraphics guiGraphics, float tickDelta) {
      int offx = this.mc.m_91268_().m_85445_() / 2;
      int offy = this.mc.m_91268_().m_85446_() / 2;
      this.renderCooldown(this.prevCTRightArm, this.curCTRightArm, this.maxCTRightArm, offx + 19, offy - 9, false, guiGraphics, tickDelta);
      this.renderCooldown(this.prevCTLeftArm, this.curCTLeftArm, this.maxCTLeftArm, offx - 21, offy - 9, true, guiGraphics, tickDelta);
      this.renderCooldown(this.prevCTRightShoulder, this.curCTRightShoulder, this.maxCTRightShoulder, offx + 19, offy + 1, false, guiGraphics, tickDelta);
      this.renderCooldown(this.prevCTLeftShoulder, this.curCTLeftShoulder, this.maxCTLeftShoulder, offx - 21, offy + 1, true, guiGraphics, tickDelta);
   }

   private void renderCooldown(int prevCT, int curCT, int maxCT, int x, int y, boolean reverse, GuiGraphics guiGraphics, float tickDelta) {
      int blockWidth = 2;
      int blockHeight = 8;
      float ctLerp = Mth.m_14179_(tickDelta, prevCT, curCT);
      int cooldownHeight = (int)(blockHeight * ((maxCT - ctLerp) / maxCT));
      guiGraphics.m_280509_(x, y, x + blockWidth, y + blockHeight, 1431655765);
      guiGraphics.m_280509_(x, y + blockHeight - cooldownHeight, x + blockWidth, y + blockHeight, -1728018743);
   }

   protected void renderCustomMechHud(Pmvc01Entity mech, GuiGraphics guiGraphics, float tickDelta) {
      PoseStack pose = guiGraphics.m_280168_();
      pose.m_85836_();
      float shake = ClientHudShake.shake;
      if (shake > 0.0F) {
         float offsetX = (Minecraft.m_91087_().f_91073_.f_46441_.m_188501_() - 0.5F) * shake;
         float offsetY = (Minecraft.m_91087_().f_91073_.f_46441_.m_188501_() - 0.5F) * shake;
         pose.m_252880_(offsetX, offsetY, 0.0F);
      }

      this.renderCrossHair2(guiGraphics, tickDelta);
      renderPickedDistance(mech, guiGraphics, tickDelta);
      this.renderWeaponInformation(mech, guiGraphics, tickDelta);
      this.renderHealthBar2(mech, guiGraphics, tickDelta);
      this.renderFuelBar(mech, guiGraphics, tickDelta);
      this.renderEnergyBar2(mech, guiGraphics, tickDelta);
      this.renderRepairKitNum(mech, guiGraphics, tickDelta);
      this.renderMechHPWarnings(mech, guiGraphics, tickDelta);
      pose.m_85849_();
   }

   public static void renderPickedDistance(Pmvc01Entity mech, GuiGraphics guiGraphics, float partialTick) {
      if (!shouldRenderDistanceOnTarget()) {
         Minecraft mc = Minecraft.m_91087_();
         if (mc.f_91074_ != null && mc.f_91073_ != null) {
            if (!mc.f_91066_.f_92062_) {
               double maxDist = 150.0;
               Vec3 eyePos = mc.f_91074_.m_20299_(partialTick);
               Vec3 lookVec = mc.f_91074_.m_20252_(partialTick);
               Vec3 endPos = eyePos.m_82549_(lookVec.m_82490_(maxDist));
               BlockHitResult blockHit = mc.f_91073_.m_45547_(new ClipContext(eyePos, endPos, Block.OUTLINE, Fluid.NONE, mc.f_91074_));
               int centerX = guiGraphics.m_280182_() / 2;
               int centerY = guiGraphics.m_280206_() / 2;
               renderDistance(mech, centerX, centerY, blockHit, guiGraphics, partialTick, mc);
            }
         }
      }
   }

   private static void renderDistance(
      Pmvc01Entity mech, int centerX, int centerY, HitResult hitResult, GuiGraphics guiGraphics, float partialTick, Minecraft mc
   ) {
      String distText;
      int[] color;
      if (hitResult.m_6662_() == Type.MISS) {
         distText = "-- m";
         color = DIST_COLOR_GREEN;
      } else {
         Vec3 playerEye = mc.f_91074_.m_20299_(partialTick);
         double dist = playerEye.m_82554_(hitResult.m_82450_());
         distText = String.format("%.1fm", dist);
         color = chooseColor(mech, hitResult.m_6662_() == Type.ENTITY, dist);
      }

      int lineStartX = centerX + 12;
      int lineEndX = centerX + 40;
      int lineY = centerY;
      guiGraphics.m_280509_(lineStartX, lineY, lineEndX, lineY + 1, color[0]);
      guiGraphics.m_280509_(lineEndX, lineY - 4, lineEndX + 1, lineY + 1, color[0]);
      Font font = mc.f_91062_;
      int textX = lineEndX + 3;
      int textY = lineY - 9 / 2;
      guiGraphics.m_280056_(font, distText, textX + 1, textY + 1, color[1], false);
      guiGraphics.m_280056_(font, distText, textX, textY, color[0], false);
   }

   private static int[] chooseColor(Pmvc01Entity mech, boolean isEntity, double dist) {
      if (isEntity) {
         if (mech.getDriverInput().isWeaponRightHandPressed()) {
            return chooseColorFromWeapon(mech.getRightArmWeapon().m_41720_(), dist);
         } else {
            return mech.getDriverInput().isWeaponLeftHandPressed()
               ? chooseColorFromWeapon(mech.getLeftArmWeapon().m_41720_(), dist)
               : chooseColorFromWeapon(mech.getRightArmWeapon().m_41720_(), dist);
         }
      } else {
         return DIST_COLOR_GREEN;
      }
   }

   private static int[] chooseColorFromWeapon(Item item, double distance) {
      if (item instanceof BasePartsItem.Weapon weapon) {
         return switch (weapon.getCurrentRange((int)distance)) {
            case OPTIMAL -> DIST_COLOR_GREEN;
            case EFFECTIVE -> DIST_COLOR_YELLOW;
            case MAXIMUM, OUT -> DIST_COLOR_RED;
            default -> DIST_COLOR_GREEN;
         };
      } else {
         return DIST_COLOR_GREEN;
      }
   }

   private static HitResult pickClosest(Vec3 eyePos, BlockHitResult blockHit, EntityHitResult entityHit) {
      double blockDist = blockHit != null && blockHit.m_6662_() != Type.MISS ? eyePos.m_82557_(blockHit.m_82450_()) : Double.MAX_VALUE;
      double entityDist = entityHit != null && entityHit.m_6662_() != Type.MISS ? eyePos.m_82557_(entityHit.m_82450_()) : Double.MAX_VALUE;
      return (HitResult)(blockDist <= entityDist ? blockHit : entityHit);
   }

   protected void renderTurretHud(Pmvt01Entity mech, GuiGraphics guiGraphics, float tickDelta) {
      this.renderCrossHair2(guiGraphics, tickDelta);
      this.renderHealthBar2(mech, guiGraphics, tickDelta);
   }

   private void renderWeaponInformation(Pmvc01Entity mech, GuiGraphics guiGraphics, float tickDelta) {
      int offx = this.mc.m_91268_().m_85445_() / 2;
      int offy = this.mc.m_91268_().m_85446_() / 2;
      this.renderCooldown2(this.prevCTRightArm, this.curCTRightArm, this.maxCTRightArm, offx + 36 - 2, offy - 20, false, guiGraphics, tickDelta);
      this.renderBulletNum(mech.getAmmoManager(6), offx + 36 + 3 - 2, offy - 20, guiGraphics);
      this.renderAmmoRight(mech.getAmmoManager(6), offx + 36 + 12 - 2, offy - 18, guiGraphics);
      this.renderCooldown2(this.prevCTRightShoulder, this.curCTRightShoulder, this.maxCTRightShoulder, offx + 36 - 2, offy + 12, false, guiGraphics, tickDelta);
      this.renderBulletNum(mech.getAmmoManager(8), offx + 36 + 3 - 2, offy + 12, guiGraphics);
      this.renderAmmoRight(mech.getAmmoManager(8), offx + 36 + 12 - 2, offy + 14, guiGraphics);
      this.renderCooldown2(this.prevCTLeftArm, this.curCTLeftArm, this.maxCTLeftArm, offx - 36, offy - 20, true, guiGraphics, tickDelta);
      this.renderBulletNum(mech.getAmmoManager(7), offx - 36 - 3, offy - 20, guiGraphics);
      this.renderAmmoLeft(mech.getAmmoManager(7), offx - 36, offy - 18, guiGraphics);
      this.renderCooldown2(this.prevCTLeftShoulder, this.curCTLeftShoulder, this.maxCTLeftShoulder, offx - 36, offy + 12, true, guiGraphics, tickDelta);
      this.renderBulletNum(mech.getAmmoManager(9), offx - 36 - 3, offy + 12, guiGraphics);
      this.renderAmmoLeft(mech.getAmmoManager(9), offx - 36, offy + 14, guiGraphics);
   }

   private void renderCooldown2(int prevCT, int curCT, int maxCT, int x, int y, boolean reverse, GuiGraphics guiGraphics, float tickDelta) {
      int blockWidth = 2;
      int blockHeight = 12;
      float ctLerp = Mth.m_14179_(tickDelta, prevCT, curCT);
      int cooldownHeight = (int)(blockHeight * ((maxCT - ctLerp) / maxCT));
      guiGraphics.m_280509_(x, y, x + blockWidth, y + blockHeight, 1431655765);
      guiGraphics.m_280509_(x, y + blockHeight - cooldownHeight, x + blockWidth, y + blockHeight, -3681058);
   }

   private void renderBulletNum(Pmvc01Entity.AmmoManager amm, int x, int y, GuiGraphics guiGraphics) {
      int bulletNum = amm.getBulletNum();
      int maxBulletNum = amm.getBulletNumPerMagazine();
      if (maxBulletNum > 0) {
         int blockWidth = 2;
         int blockHeight = 12;
         if (!amm.isReloading()) {
            int bulletNumHeight = (int)(blockHeight * ((float)bulletNum / maxBulletNum));
            if (bulletNum == 0) {
               guiGraphics.m_280509_(x, y, x + blockWidth, y + blockHeight, 1437204480);
            } else {
               guiGraphics.m_280509_(x, y, x + blockWidth, y + blockHeight, 1431655765);
            }

            guiGraphics.m_280509_(x, y + blockHeight - bulletNumHeight, x + blockWidth, y + blockHeight, -3681058);
         } else {
            int bulletNumHeight = (int)(blockHeight * ((float)(amm.RELOAD_TICKS - amm.getReloadTicks()) / amm.RELOAD_TICKS));
            guiGraphics.m_280509_(x, y, x + blockWidth, y + blockHeight, 1437204480);
            guiGraphics.m_280509_(x, y + blockHeight - bulletNumHeight, x + blockWidth, y + blockHeight, -1431699456);
         }
      }
   }

   private void renderAmmoLeft(Pmvc01Entity.AmmoManager amm, int x, int y, GuiGraphics gui) {
      if (amm.getBulletNumPerMagazine() != 0) {
         int bColor = 12386273;
         int mColor = -3681058;
         if (amm.getBulletNum() == 0) {
            bColor = 16711680;
         }

         if (amm.getMagazineNum() == 0) {
            mColor = 16711680;
         }

         gui.m_280056_(Minecraft.m_91087_().f_91062_, String.format("%2s", amm.getMagazineNum()), x - 20, y, mColor, false);
      }
   }

   private void renderAmmoRight(Pmvc01Entity.AmmoManager amm, int x, int y, GuiGraphics gui) {
      if (amm.getBulletNumPerMagazine() != 0) {
         int bColor = 12386273;
         int mColor = -3681058;
         if (amm.getBulletNum() == 0) {
            bColor = 16711680;
         }

         if (amm.getMagazineNum() == 0) {
            mColor = 16711680;
         }

         gui.m_280056_(Minecraft.m_91087_().f_91062_, String.format("%-2s", amm.getMagazineNum()), x, y, mColor, false);
      }
   }

   private void renderCrossHair2(GuiGraphics guiGraphics, float tickDelta) {
      int textureWidth = 64;
      int textureHeight = 64;
      int screenWidth = this.mc.m_91268_().m_85445_();
      int screenHeight = this.mc.m_91268_().m_85446_();
      int startX = screenWidth / 2 - textureWidth / 2;
      int startY = screenHeight / 2 - textureHeight / 2;
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.6F);
      RenderSystem.setShader(GameRenderer::m_172817_);
      RenderSystem.setShaderTexture(0, CROSSHAIR_TEXTURE2);
      guiGraphics.m_280398_(CROSSHAIR_TEXTURE2, startX, startY, 0, 0.0F, 0.0F, textureWidth, textureHeight, textureWidth, textureHeight);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void renderHealthBar2(PomkotsVehicleBase protobot, GuiGraphics guiGraphics, float tickDelta) {
      int screenWidth = this.mc.m_91268_().m_85445_();
      int screenHeight = this.mc.m_91268_().m_85446_();
      int width = 120;
      int height = 3;
      int x = screenWidth / 2 - width / 2;
      int y = screenHeight - 13;
      if (protobot.shouldRenderDefaultHud("renderHotbar")) {
         y -= 20;
      }

      int maxHealth = (int)protobot.m_21233_();
      guiGraphics.m_280509_(x, y, x + width, y + height, 1431655765);
      int healthWidthCur = (int)(width * Mth.m_14179_(tickDelta, this.curHealth / maxHealth, this.curHealth / maxHealth));
      int healthWidthPrev = (int)(width * Mth.m_14179_(tickDelta, this.prevHealth / maxHealth, this.prevHealth / maxHealth));
      int healthWidth = Mth.m_269140_(tickDelta, healthWidthPrev, healthWidthCur);
      guiGraphics.m_280509_(x, y, x + healthWidth, y + height, healthWidth < 40 ? -1431699456 : -3681058);
   }

   private void renderFuelBar(Pmvc01Entity protobot, GuiGraphics guiGraphics, float tickDelta) {
      int screenWidth = this.mc.m_91268_().m_85445_();
      int screenHeight = this.mc.m_91268_().m_85446_();
      int width = 120;
      int height = 3;
      int x = screenWidth / 2 - width / 2;
      int y = screenHeight - 8;
      if (protobot.shouldRenderDefaultHud("renderHotbar")) {
         y -= 20;
      }

      int maxFuel = protobot.getMaxFuel();
      float fuelWidth = (maxFuel == 0 ? 0.0F : (float)protobot.getFuelNow() / maxFuel) * width;
      guiGraphics.m_280509_(x, y, x + width, y + height, fuelWidth == 0.0F ? 1437204480 : 1431655765);
      guiGraphics.m_280509_(x, y, x + (int)fuelWidth, y + height, fuelWidth < 40.0F ? -1431699456 : -3681058);
   }

   private void renderEnergyBar2(PomkotsVehicleBase protobot, GuiGraphics guiGraphics, float tickDelta) {
      int screenWidth = this.mc.m_91268_().m_85445_();
      int screenHeight = this.mc.m_91268_().m_85446_();
      int maxFuel = this.maxEN;
      int width = 35;
      int height = 2;
      int x = screenWidth / 2 - width / 2;
      int y = screenHeight / 2 - 40;
      float enLerp = Mth.m_14179_(tickDelta, this.prevEN, this.curEN);
      float fuelWidth = width * (enLerp / maxFuel);
      if (protobot instanceof Pmvc01Entity mech) {
         guiGraphics.m_280509_(x, y, x + width, y + height, mech.isOverHeat() ? 1437204480 : 1431655765);
         guiGraphics.m_280509_(
            (int)(x + (width - fuelWidth) / 2.0F),
            y,
            (int)(x + (width - fuelWidth) / 2.0F + fuelWidth) + 1,
            y + height,
            mech.isOverHeat() ? -1431699456 : -3681058
         );
      } else {
         guiGraphics.m_280509_(x, y, x + width, y + height, fuelWidth < width * 0.2 ? 1437204480 : 1431655765);
         guiGraphics.m_280509_((int)(x + (width - fuelWidth) / 2.0F), y, (int)(x + (width - fuelWidth) / 2.0F + fuelWidth) + 1, y + height, -3681058);
      }
   }

   private void renderRepairKitNum(PomkotsVehicleBase protobot, GuiGraphics guiGraphics, float tickDelta) {
      if (protobot instanceof Pmvc01Entity pmvc01) {
         int screenWidth = this.mc.m_91268_().m_85445_();
         int screenHeight = this.mc.m_91268_().m_85446_();
         byte width = 120;
         int x = screenWidth / 2 + width / 2 + 8;
         int y = screenHeight - 13;
         if (protobot.shouldRenderDefaultHud("renderHotbar")) {
            y -= 20;
         }

         int size = 8;
         guiGraphics.m_280163_(REPAIR_KIT_ICON, x, y, 0.0F, 0.0F, size, size, size, size);
         guiGraphics.m_280056_(Minecraft.m_91087_().f_91062_, String.format("%2s", pmvc01.getRepairKitNum()), x + 10, y, -3681058, false);
      }
   }

   private void renderMechHPWarnings(PomkotsVehicleBase mech, GuiGraphics guiGraphics, float tickDelta) {
      Minecraft mc = Minecraft.m_91087_();
      float hpRatio = mech.m_21223_() / mech.m_21233_();
      if (hpRatio <= 0.4F) {
         String text;
         int rgb;
         float speed;
         if (hpRatio <= 0.3F) {
            text = "DANGER: ARMOR LOW";
            rgb = 16711680;
            speed = 0.4F;
         } else {
            text = "WARNING: ARMOR LOW";
            rgb = 16776960;
            speed = 0.15F;
         }

         float time = (mc.f_91074_.f_19797_ + tickDelta) * speed;
         float alpha = (Mth.m_14031_(time) + 1.0F) * 0.5F;
         alpha = 0.2F + alpha * 0.8F;
         int a = (int)(alpha * 255.0F);
         int color = a << 24 | rgb;
         int screenWidth = mc.m_91268_().m_85445_();
         int screenHeight = mc.m_91268_().m_85446_();
         int x = screenWidth / 2;
         int y = screenHeight / 2 + 40;
         Font font = mc.f_91062_;
         int textWidth = font.m_92895_(text);
         int xx = x - textWidth / 2;
         guiGraphics.m_280056_(font, text, xx, y, color, false);
      }
   }

   private void renderHealthBar(LivingEntity entity, GuiGraphics guiGraphics, float tickDelta) {
      Minecraft client = Minecraft.m_91087_();
      Vec3 cameraPos = client.f_91063_.m_109153_().m_90583_();
      double distance = entity.m_20238_(cameraPos);
      if (!(distance > 4096.0)) {
         double x = entity.f_19790_ + (entity.m_20185_() - entity.f_19790_) * tickDelta;
         double y = entity.f_19791_ + (entity.m_20186_() - entity.f_19791_) * tickDelta + entity.m_20206_() + 0.5;
         double z = entity.f_19792_ + (entity.m_20189_() - entity.f_19792_) * tickDelta;
         Vec3 projectedPos = new Vec3(x, y, z).m_82546_(cameraPos);
         Vector4f pos = new Vector4f((float)projectedPos.f_82479_, (float)projectedPos.f_82480_, (float)projectedPos.f_82481_, 1.0F);
         pos = client.f_91063_.m_253088_(tickDelta).transform(pos);
         int screenX = (int)(client.m_91268_().m_85445_() / 2 + pos.x() / pos.w() * client.m_91268_().m_85445_() / 2.0F);
         int screenY = (int)(client.m_91268_().m_85446_() / 2 - pos.y() / pos.w() * client.m_91268_().m_85446_() / 2.0F);
         int barWidth = 40;
         int barHeight = 6;
         int maxHealth = (int)entity.m_21233_();
         int currentHealth = (int)entity.m_21223_();
         int healthBarWidth = (int)((float)currentHealth / maxHealth * barWidth);
         guiGraphics.m_280509_(screenX - barWidth / 2, screenY, screenX + barWidth / 2, screenY + barHeight, 1711276032);
         guiGraphics.m_280509_(screenX - barWidth / 2, screenY, screenX - barWidth / 2 + healthBarWidth, screenY + barHeight, -16711936);
         LOGGER.info(screenX + ":" + screenY);
      }
   }

   private void renderHealthBar(Monster monster, Camera camera, GuiGraphics guiGraphics, float tickDelta) {
      Vec3 entityPos = this.getEntityRenderPosition(monster, tickDelta);
      Vec3 cameraPos = camera.m_90583_();
      double x = entityPos.f_82479_ - cameraPos.f_82479_;
      double y = entityPos.f_82480_ - cameraPos.f_82480_ + monster.m_20206_() + 0.5;
      double z = entityPos.f_82481_ - cameraPos.f_82481_;
      int screenX = (int)(this.mc.m_91268_().m_85445_() / 2.0 + x * 100.0);
      int screenY = (int)(this.mc.m_91268_().m_85446_() / 2.0 - y * 100.0 - z * 50.0);
      float health = monster.m_21223_();
      float maxHealth = monster.m_21233_();
      float healthPercentage = health / maxHealth;
      int barWidth = 50;
      int barHeight = 5;
      this.fill(guiGraphics, screenX - barWidth / 2, screenY, screenX + barWidth / 2, screenY + barHeight, -65536);
      this.fill(guiGraphics, screenX - barWidth / 2, screenY, screenX - barWidth / 2 + (int)(barWidth * healthPercentage), screenY + barHeight, -16711936);
   }

   private Vec3 getEntityRenderPosition(LivingEntity entity, float tickDelta) {
      double x = entity.f_19790_ + (entity.m_20185_() - entity.f_19790_) * tickDelta;
      double y = entity.f_19791_ + (entity.m_20186_() - entity.f_19791_) * tickDelta;
      double z = entity.f_19792_ + (entity.m_20189_() - entity.f_19792_) * tickDelta;
      return new Vec3(x, y, z);
   }

   private void fill(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
      RenderSystem.setShader(GameRenderer::m_172811_);
      guiGraphics.m_280509_(x1, y1, x2, y2, color);
      LOGGER.info(x1 + ":" + y1 + ":" + x2 + ":" + y2);
   }
}
