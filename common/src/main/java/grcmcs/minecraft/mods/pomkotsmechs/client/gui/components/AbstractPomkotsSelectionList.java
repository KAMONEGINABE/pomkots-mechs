package grcmcs.minecraft.mods.pomkotsmechs.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList.Entry;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class AbstractPomkotsSelectionList<T extends Entry<T>> extends ObjectSelectionList<T> {
   public static final ResourceLocation TEXTURE_LIST_BG_1 = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/arena_fighter_card.png");
   public static final ResourceLocation TEXTURE_LIST_SELECTION = new ResourceLocation(
      "pomkotsmechs", "textures/gui/mechworkbench/arena_fighter_card_selected.png"
   );
   public static final ResourceLocation TEXTURE_SCROLLBAR_BG = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/scroll_bar_background.png");
   public static final ResourceLocation TEXTURE_SCROLLBAR_THUMB = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/scroll_bar.png");
   private static final int SCROLLBAR_WIDTH = 2;
   private static final int THUMB_MIN_HEIGHT = 16;
   protected final AbstractContainerScreen<?> screen;
   protected final int listX;
   protected final int listY;
   protected final int listWidth;
   protected final int listHeight;
   protected T hovered;

   protected AbstractPomkotsSelectionList(
      AbstractContainerScreen<?> screen, Minecraft minecraft, int listX, int listY, int listWidth, int listHeight, int rowHeight
   ) {
      super(minecraft, listWidth, listHeight, listY, listY + listHeight, rowHeight);
      this.screen = screen;
      this.listX = listX;
      this.listY = listY;
      this.listWidth = listWidth;
      this.listHeight = listHeight;
      this.m_93507_(listX);
      this.m_93488_(false);
      this.m_93496_(false);
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.m_7733_(guiGraphics);
      this.hovered = (T)(this.m_5953_(mouseX, mouseY) ? this.m_93412_(mouseX, mouseY) : null);
      this.m_280310_(guiGraphics);
      this.m_239227_(guiGraphics, mouseX, mouseY, partialTick);
      guiGraphics.m_280618_();
      this.m_7154_(guiGraphics, mouseX, mouseY);
      RenderSystem.disableBlend();
      this.renderCustomScrollbar(guiGraphics);
   }

   protected T getHovered() {
      return this.hovered;
   }

   private void renderCustomScrollbar(GuiGraphics graphics) {
      if (this.hasScrollbar()) {
         int barX = this.m_5756_();
         int barY = this.listY;
         int barHeight = this.listHeight;
         renderBgTexture(TEXTURE_SCROLLBAR_BG, barX, barY, 2, barHeight, graphics);
         int maxScroll = this.m_93518_();
         if (maxScroll > 0) {
            int thumbHeight = Math.max(16, (int)((float)barHeight * barHeight / this.m_5775_()));
            int thumbY = (int)(this.m_93517_() * (barHeight - thumbHeight) / maxScroll);
            renderBgTexture(TEXTURE_SCROLLBAR_THUMB, barX - 1, barY + thumbY, 4, thumbHeight, graphics);
         }
      }
   }

   private boolean hasScrollbar() {
      return this.m_93518_() > 0;
   }

   protected void m_240140_(GuiGraphics graphics, int top, int width, int height, int outerColor, int innerColor) {
      renderBgTexture(TEXTURE_LIST_SELECTION, this.m_5747_(), top, this.m_5759_(), height, graphics);
   }

   public int m_5747_() {
      return this.listX;
   }

   public int m_5759_() {
      return this.listWidth;
   }

   protected int m_5756_() {
      return this.m_5747_() + this.listWidth - 2;
   }

   private static void renderBgTexture(ResourceLocation texture, int x, int y, int width, int height, GuiGraphics guiGraphics) {
      renderBgTexture(texture, x, y, width, height, 0.5F, guiGraphics);
   }

   private static void renderBgTexture(ResourceLocation texture, int x, int y, int width, int height, float alpha, GuiGraphics guiGraphics) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      RenderSystem.enableBlend();
      RenderSystem.setShaderTexture(0, texture);
      guiGraphics.m_280163_(texture, x, y, 0.0F, 0.0F, width, height, width, height);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public abstract static class PomkotsEntry<E extends AbstractPomkotsSelectionList.PomkotsEntry<E>> extends Entry<E> {
      public void m_6311_(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
      }

      public void m_274437_(
         GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick
      ) {
         AbstractPomkotsSelectionList.renderBgTexture(AbstractPomkotsSelectionList.TEXTURE_LIST_BG_1, left, top, width, height, graphics);
      }

      public Component m_142172_() {
         return Component.m_237113_("");
      }
   }
}
