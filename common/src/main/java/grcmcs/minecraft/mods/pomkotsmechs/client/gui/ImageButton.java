package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class ImageButton extends Button {
   private final ResourceLocation texture;
   private final int texU;
   private final int texV;
   private final int texWidth;
   private final int texHeight;
   private final Component label;
   private int badgeCount = 0;

   public ImageButton(
      int x, int y, int width, int height, ResourceLocation texture, int texU, int texV, int texWidth, int texHeight, Component label, OnPress onPress
   ) {
      super(x, y, width, height, label, onPress, f_252438_);
      this.texture = texture;
      this.texU = texU;
      this.texV = texV;
      this.texWidth = texWidth;
      this.texHeight = texHeight;
      this.label = label;
   }

   protected void m_87963_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      RenderSystem.enableBlend();
      boolean pressed = this.f_93622_ && GLFW.glfwGetMouseButton(Minecraft.m_91087_().m_91268_().m_85439_(), 0) == 1;
      int v = this.texV;
      if (!this.f_93623_) {
         v += this.f_93619_ * 3;
      } else if (pressed) {
         v += this.f_93619_ * 2;
      } else if (this.f_93622_) {
         v += this.f_93619_;
      }

      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.9F);
      guiGraphics.m_280163_(this.texture, this.m_252754_(), this.m_252907_(), this.texU, v, this.f_93618_, this.f_93619_, this.texWidth, this.texHeight);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      Font font = Minecraft.m_91087_().f_91062_;
      int textWidth = font.m_92852_(this.label);
      int textX = this.m_252754_() + this.f_93618_ / 2 - textWidth / 2;
      int textY = this.m_252907_() + (this.f_93619_ - 8) / 2;
      int color = -1;
      if (!this.f_93623_) {
         color = -14540254;
      } else if (pressed) {
         textX++;
         textY++;
      }

      guiGraphics.m_280614_(font, this.label, textX, textY, color, false);
      this.renderBadge(guiGraphics);
   }

   private void renderBadge(GuiGraphics graphics) {
      if (this.badgeCount > 0) {
         int badgeX = this.m_252754_() + this.f_93618_ - 10;
         int badgeY = this.m_252907_() - 2;
         graphics.m_280509_(badgeX, badgeY, badgeX + 10, badgeY + 10, -65536);
         String text = this.badgeCount > 99 ? "!" : String.valueOf(this.badgeCount);
         graphics.m_280137_(Minecraft.m_91087_().f_91062_, text, badgeX + 5, badgeY + 1, -1);
      }
   }

   public void setBadgeCount(int badgeCount) {
      this.badgeCount = badgeCount;
   }

   public int getBadgeCount() {
      return this.badgeCount;
   }
}
