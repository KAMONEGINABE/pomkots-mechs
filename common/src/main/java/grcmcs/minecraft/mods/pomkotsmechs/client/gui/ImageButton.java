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
      super(x, y, width, height, label, onPress, DEFAULT_NARRATION);
      this.texture = texture;
      this.texU = texU;
      this.texV = texV;
      this.texWidth = texWidth;
      this.texHeight = texHeight;
      this.label = label;
   }

   protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      RenderSystem.enableBlend();
      boolean pressed = this.isHovered && GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), 0) == 1;
      int v = this.texV;
      if (!this.active) {
         v += this.height * 3;
      } else if (pressed) {
         v += this.height * 2;
      } else if (this.isHovered) {
         v += this.height;
      }

      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.9F);
      guiGraphics.blit(this.texture, this.getX(), this.getY(), this.texU, v, this.width, this.height, this.texWidth, this.texHeight);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      Font font = Minecraft.getInstance().font;
      int textWidth = font.width(this.label);
      int textX = this.getX() + this.width / 2 - textWidth / 2;
      int textY = this.getY() + (this.height - 8) / 2;
      int color = -1;
      if (!this.active) {
         color = -14540254;
      } else if (pressed) {
         textX++;
         textY++;
      }

      guiGraphics.drawString(font, this.label, textX, textY, color, false);
      this.renderBadge(guiGraphics);
   }

   private void renderBadge(GuiGraphics graphics) {
      if (this.badgeCount > 0) {
         int badgeX = this.getX() + this.width - 10;
         int badgeY = this.getY() - 2;
         graphics.fill(badgeX, badgeY, badgeX + 10, badgeY + 10, -65536);
         String text = this.badgeCount > 99 ? "!" : String.valueOf(this.badgeCount);
         graphics.drawCenteredString(Minecraft.getInstance().font, text, badgeX + 5, badgeY + 1, -1);
      }
   }

   public void setBadgeCount(int badgeCount) {
      this.badgeCount = badgeCount;
   }

   public int getBadgeCount() {
      return this.badgeCount;
   }
}
