package grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena;

import com.mojang.blaze3d.systems.RenderSystem;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaRank;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ArenaBattleResultScreen extends AbstractContainerScreen<ArenaBattleResultMenu> {
   private static final ResourceLocation TEXTURE_BG_1 = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/background_hangar.png");
   private static final ResourceLocation TEXTURE_BG_2 = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/background_gradient.png");
   private static final ResourceLocation TEXTURE_BUTTON = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/button_normal.png");
   private static final ResourceLocation TEXTURE_PANEL_INV_PLAYER = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/inventory_player.png");
   private static final ResourceLocation TEXTURE_PANEL_INV_REWARDS = new ResourceLocation(
      "pomkotsmechs", "textures/gui/mechworkbench/inventory_arena_rewards.png"
   );
   private static final ResourceLocation TEXTURE_OFFERS_PANEL = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/mech_trader_offers.png");
   private static final ResourceLocation TEXTURE_CARD = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/arena_fighter_card.png");
   private static final ResourceLocation TEXTURE_CARD_SELECTED = new ResourceLocation(
      "pomkotsmechs", "textures/gui/mechworkbench/mech_trader_card_selected.png"
   );
   private static final ResourceLocation TEXTURE_SCROLL_BAR_BG = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/scroll_bar_background.png");
   private static final ResourceLocation TEXTURE_SCROLL_BAR = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/scroll_bar.png");
   private static final ResourceLocation TEXTURE_ITEM_CONTAINER = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/item_container.png");
   private static final int GUI_WIDTH = 480;
   private static final int GUI_HEIGHT = 270;
   private static final int BUTTON_WIDTH = 70;
   private static final int BUTTON_HEIGHT = 14;
   private static final int CARD_WIDTH = 164;
   private static final int CARD_HEIGHT = 130;
   private int leftCardOffsetX = 0;
   private int leftCardOffsetY = 0;
   private int rightCardOffsetX = 0;
   private int rightCardOffsetY = 0;
   private final ArenaMatchResultData.FighterResult challenger;
   private final ArenaMatchResultData.FighterResult opponent;

   public ArenaBattleResultScreen(ArenaBattleResultMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 480;
      this.imageHeight = 270;
      this.challenger = menu.getChallengerResult();
      this.opponent = menu.getOpponentResult();
   }

   protected void init() {
      super.init();
      int offsetX = 20;
      int offsetY = 10;
      this.leftCardOffsetX = offsetX + 28;
      this.leftCardOffsetY = offsetY;
      this.rightCardOffsetX = offsetX + 28 + 164 + 28 + 28;
      this.rightCardOffsetY = offsetY;
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(guiGraphics);
      this.renderFighterCard(guiGraphics, this.challenger, this.leftCardOffsetX, this.leftCardOffsetY, true);
      this.renderFighterCard(guiGraphics, this.opponent, this.rightCardOffsetX, this.leftCardOffsetY, false);
      this.renderTooltip(guiGraphics, mouseX, mouseY);
      super.render(guiGraphics, mouseX, mouseY, partialTick);
   }

   protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
      this.renderBgTexture(TEXTURE_PANEL_INV_REWARDS, this.leftCardOffsetX, this.leftCardOffsetY + 10 + 130, 164, 94, guiGraphics);
      guiGraphics.drawString(this.font, "Rewards", this.leftCardOffsetX + 18, this.leftCardOffsetY + 10 + 130 + 4, -1);
      int offsetX = this.leftCardOffsetX + 25 + 26;
      int offsetY = this.leftCardOffsetY + 10 + 130 + 26;
      guiGraphics.drawString(this.font, "Match Reward", offsetX, offsetY, -1);
      guiGraphics.drawString(this.font, "Rank Reward", offsetX, offsetY + 18, -1);
      guiGraphics.drawString(this.font, "Special Reward", offsetX, offsetY + 36, -1);
      this.renderBgTexture(TEXTURE_PANEL_INV_PLAYER, this.rightCardOffsetX, this.leftCardOffsetY + 10 + 130, 164, 94, guiGraphics);
      guiGraphics.drawString(this.font, "Player Inventory", this.rightCardOffsetX + 18, this.leftCardOffsetY + 10 + 130 + 4, -1);
   }

   private void renderHeader(GuiGraphics gg) {
      String title = switch (this.challenger.getResultType()) {
         case 1 -> "VICTORY";
         case 2 -> "DEFEAT";
         default -> "DRAW";
      };

      int color = switch (this.challenger.getResultType()) {
         case 1 -> 5635925;
         case 2 -> 16733525;
         default -> 16777045;
      };
      gg.drawCenteredString(this.font, title, this.leftPos + this.imageWidth / 2, this.topPos + 10, color);
   }

   private void renderFighterCard(GuiGraphics gg, ArenaMatchResultData.FighterResult result, int x, int y, boolean challenger) {
      gg.fill(x, y, x + 164, y + 130, 1711276032);
      int line = y + 8;
      gg.drawCenteredString(this.font, result.getDisplayName(), x + 82, line, 16777215);
      line += 16;
      this.renderResultBadge(gg, result, x + 8, line);
      line += 18;
      this.drawChangeLine(gg, "Rating", result.getRatingBefore(), result.getRatingAfter(), x, line);
      line += 14;
      this.drawChangeLine(gg, "Wins", result.getWinsBefore(), result.getWinsAfter(), x, line);
      line += 14;
      this.drawChangeLine(gg, "Losses", result.getLossesBefore(), result.getLossesAfter(), x, line);
      line += 14;
      this.drawChangeLine(gg, "Draws", result.getDrawsBefore(), result.getDrawsAfter(), x, line);
      line += 18;
      gg.drawString(this.font, "Rank", x + 8, line, 11184810);
      gg.drawString(this.font, result.getRankBefore() + " -> " + result.getRankAfter(), x + 60, line, this.getRankColor(result.getRankAfter()));
   }

   private void renderResultBadge(GuiGraphics gg, ArenaMatchResultData.FighterResult result, int x, int y) {
      String text;
      int color;
      switch (result.getResultType()) {
         case 1:
            text = "WIN";
            color = 5635925;
            break;
         case 2:
            text = "LOSE";
            color = 16733525;
            break;
         default:
            text = "DRAW";
            color = 16777045;
      }

      gg.drawString(this.font, text, x, y, color);
   }

   private void drawChangeLine(GuiGraphics gg, String label, int before, int after, int x, int y) {
      gg.drawString(this.font, label, x + 8, y, 11184810);
      int color;
      if (after > before) {
         color = 5635925;
      } else if (after < before) {
         color = 16733525;
      } else {
         color = 16777215;
      }

      gg.drawString(this.font, before + " -> " + after, x + 60, y, color);
   }

   private int getRankColor(String rank) {
      ArenaRank r = ArenaRank.getRank(rank);
      return r.getColor();
   }

   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
   }

   private void renderBgTexture(ResourceLocation texture, int x, int y, int width, int height, GuiGraphics guiGraphics) {
      this.renderBgTexture(texture, x, y, width, height, 0.5F, guiGraphics);
   }

   private void renderBgTexture(ResourceLocation texture, int x, int y, int width, int height, float alpha, GuiGraphics guiGraphics) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      RenderSystem.enableBlend();
      RenderSystem.setShaderTexture(0, texture);
      guiGraphics.blit(texture, x, y, 0.0F, 0.0F, width, height, width, height);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }
}
