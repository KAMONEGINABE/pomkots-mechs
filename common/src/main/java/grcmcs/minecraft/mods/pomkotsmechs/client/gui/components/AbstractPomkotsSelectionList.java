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
      this.setLeftPos(listX);
      this.setRenderBackground(false);
      this.setRenderTopAndBottom(false);
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(guiGraphics);
      this.hovered = (T)(this.isMouseOver(mouseX, mouseY) ? this.getEntryAtPosition(mouseX, mouseY) : null);
      this.enableScissor(guiGraphics);
      this.renderList(guiGraphics, mouseX, mouseY, partialTick);
      guiGraphics.disableScissor();
      this.renderDecorations(guiGraphics, mouseX, mouseY);
      RenderSystem.disableBlend();
      this.renderCustomScrollbar(guiGraphics);
   }

   protected T getHovered() {
      return this.hovered;
   }

   private void renderCustomScrollbar(GuiGraphics graphics) {
      if (this.hasScrollbar()) {
         int barX = this.getScrollbarPosition();
         int barY = this.listY;
         int barHeight = this.listHeight;
         renderBgTexture(TEXTURE_SCROLLBAR_BG, barX, barY, 2, barHeight, graphics);
         int maxScroll = this.getMaxScroll();
         if (maxScroll > 0) {
            int thumbHeight = Math.max(16, (int)((float)barHeight * barHeight / this.getMaxPosition()));
            int thumbY = (int)(this.getScrollAmount() * (barHeight - thumbHeight) / maxScroll);
            renderBgTexture(TEXTURE_SCROLLBAR_THUMB, barX - 1, barY + thumbY, 4, thumbHeight, graphics);
         }
      }
   }

   private boolean hasScrollbar() {
      return this.getMaxScroll() > 0;
   }

   protected void renderSelection(GuiGraphics graphics, int top, int width, int height, int outerColor, int innerColor) {
      renderBgTexture(TEXTURE_LIST_SELECTION, this.getRowLeft(), top, this.getRowWidth(), height, graphics);
   }

   public int getRowLeft() {
      return this.listX;
   }

   public int getRowWidth() {
      return this.listWidth;
   }

   protected int getScrollbarPosition() {
      return this.getRowLeft() + this.listWidth - 2;
   }

   private static void renderBgTexture(ResourceLocation texture, int x, int y, int width, int height, GuiGraphics guiGraphics) {
      renderBgTexture(texture, x, y, width, height, 0.5F, guiGraphics);
   }

   private static void renderBgTexture(ResourceLocation texture, int x, int y, int width, int height, float alpha, GuiGraphics guiGraphics) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      RenderSystem.enableBlend();
      RenderSystem.setShaderTexture(0, texture);
      guiGraphics.blit(texture, x, y, 0.0F, 0.0F, width, height, width, height);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public abstract static class PomkotsEntry<E extends AbstractPomkotsSelectionList.PomkotsEntry<E>> extends Entry<E> {
      public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
      }

      public void renderBack(
         GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick
      ) {
         AbstractPomkotsSelectionList.renderBgTexture(AbstractPomkotsSelectionList.TEXTURE_LIST_BG_1, left, top, width, height, graphics);
      }

      public Component getNarration() {
         return Component.literal("");
      }
   }
}
