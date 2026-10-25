package grcmcs.minecraft.mods.pomkotsmechs.client.gui.arena;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.ImageButton;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto.ArenaRank;
import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ArenaReceptionistScreen extends AbstractContainerScreen<ArenaReceptionistMenu> {
   private static final ResourceLocation TEXTURE_BG_1 = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/background_hangar.png");
   private static final ResourceLocation TEXTURE_BG_2 = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/background_gradient.png");
   private static final ResourceLocation TEXTURE_BUTTON = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/button_normal.png");
   private static final ResourceLocation TEXTURE_PANEL_INV_PLAYER = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/inventory_player.png");
   private static final ResourceLocation TEXTURE_OFFERS_PANEL = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/mech_trader_offers.png");
   private static final ResourceLocation TEXTURE_CARD = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/arena_fighter_card.png");
   private static final ResourceLocation TEXTURE_CARD_SELECTED = new ResourceLocation(
      "pomkotsmechs", "textures/gui/mechworkbench/arena_fighter_card_selected.png"
   );
   private static final ResourceLocation TEXTURE_SCROLL_BAR_BG = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/scroll_bar_background.png");
   private static final ResourceLocation TEXTURE_SCROLL_BAR = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/scroll_bar.png");
   private static final ResourceLocation TEXTURE_ITEM_CONTAINER = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/item_container.png");
   private static final int GUI_WIDTH = 480;
   private static final int GUI_HEIGHT = 270;
   private static final int BUTTON_WIDTH = 70;
   private static final int BUTTON_HEIGHT = 14;
   private int tabSelectorOffsetX = 0;
   private int tabSelectorOffsetY = 0;
   private int rankingOffsetX = 0;
   private int rankingOffsetY = 0;
   private int playerInvX = 0;
   private int playerInvY = 0;
   private int playerInvWidth = 164;
   private int playerInvHeight = 94;
   private static final int RANKING_AREA_WIDTH = 440;
   private static final int RANKING_AREA_HEIGHT = 190;
   private static final int CARD_WIDTH = 430;
   private static final int CARD_HEIGHT = 60;
   private static final int CARD_MARGIN = 4;
   private static final int PORTRAIT_SIZE = 48;
   private static final int SCROLL_SPEED = 12;
   private ArenaReceptionistScreen.ArenaTab currentTab = ArenaReceptionistScreen.ArenaTab.RANKED;
   private int scrollOffset;
   private int maxScroll;
   private int contentHeight;
   private UUID challengerUuid = null;
   private EditBox nameField;
   private EditBox commentField;
   private String mechName = "-";
   private UUID selectedFighterId;
   private ArenaRankingEntry selectedEntry;
   private Button rankMatchButton;
   private Button freeMatchButton;
   private ImageButton acceptButton;
   private ImageButton declineButton;
   private ImageButton returnButton;
   private Button saveProfileButton;
   private Button removeProfileButton;
   private ArenaRankingEntry selfProfile = null;

   public ArenaReceptionistScreen(ArenaReceptionistMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 480;
      this.imageHeight = 270;
   }

   protected void init() {
      super.init();
      this.tabSelectorOffsetX = this.leftPos + 20;
      this.tabSelectorOffsetY = this.topPos + 35;
      this.rankingOffsetX = this.tabSelectorOffsetX;
      this.rankingOffsetY = this.tabSelectorOffsetY + 14;
      this.playerInvX = this.leftPos + 310;
      this.playerInvY = this.topPos + 35;
      this.addRenderableWidget(
         this.buildButton("Ranking", this.tabSelectorOffsetX, this.tabSelectorOffsetY, btn -> this.currentTab = ArenaReceptionistScreen.ArenaTab.RANKED)
      );
      if (((ArenaReceptionistMenu)this.menu).getMode() == 0) {
         this.addRenderableWidget(
            this.buildButton(
               "Profile", this.tabSelectorOffsetX + 70, this.tabSelectorOffsetY, btn -> this.currentTab = ArenaReceptionistScreen.ArenaTab.PROFILE
            )
         );
      }

      this.rankMatchButton = (Button)this.addRenderableWidget(
         this.buildButton("Rank Match", this.tabSelectorOffsetX + 280, this.rankingOffsetY + 190 + 5, btn -> this.startMatch(true))
      );
      this.freeMatchButton = (Button)this.addRenderableWidget(
         this.buildButton("Free Match", this.tabSelectorOffsetX + 350 + 15, this.rankingOffsetY + 190 + 5, btn -> this.startMatch(false))
      );
      if (((ArenaReceptionistMenu)this.menu).getMode() == 1) {
         this.acceptButton = (ImageButton)this.addRenderableWidget(
            this.buildButton("Teleport", this.tabSelectorOffsetX + 210 - 15, this.rankingOffsetY - 18, btn -> this.btnClicked(0), this.isAcceptEnable())
         );
         this.declineButton = (ImageButton)this.addRenderableWidget(
            this.buildButton("Decline", this.tabSelectorOffsetX + 280, this.rankingOffsetY - 18, btn -> this.btnClicked(1), this.isDeclineEnable())
         );
         this.returnButton = (ImageButton)this.addRenderableWidget(
            this.buildButton("Return", this.tabSelectorOffsetX + 350 + 15, this.rankingOffsetY - 18, btn -> this.btnClicked(2), this.isReturnEnable())
         );
         if (this.isAcceptEnable()) {
            this.acceptButton.setBadgeCount(100);
         }

         if (this.isDeclineEnable()) {
            this.declineButton.setBadgeCount(100);
         }

         if (this.isReturnEnable()) {
            this.returnButton.setBadgeCount(100);
         }

         ItemStack dataPad = this.getDataPad();
         if (dataPad != null) {
            List<PomkotsDatapadItem.ArenaRequestInfo> reqs = PomkotsDatapadItem.getArenaRequests(dataPad);
            if (!reqs.isEmpty()) {
               this.challengerUuid = reqs.get(0).challengerUuid();
            }
         }
      }

      this.nameField = new EditBox(this.font, this.rankingOffsetX + 20, this.rankingOffsetY + 10 + 15, 180, 18, Component.literal("Display Name"));
      this.nameField.setMaxLength(32);
      this.addRenderableWidget(this.nameField);
      this.commentField = new EditBox(this.font, this.rankingOffsetX + 20, this.rankingOffsetY + 10 + 45 + 15, 180, 18, Component.literal("Comment"));
      this.commentField.setMaxLength(64);
      this.addRenderableWidget(this.commentField);
      this.saveProfileButton = (Button)this.addRenderableWidget(
         this.buildButton("Save", this.rankingOffsetX, this.rankingOffsetY + 145 + 20, btn -> this.savePlayerProfile())
      );
      this.removeProfileButton = (Button)this.addRenderableWidget(
         this.buildButton("Remove", this.rankingOffsetX + 70 + 20, this.rankingOffsetY + 145 + 20, btn -> this.removePlayerPlofile())
      );
      this.updateScrollLimits();
      this.updateComponentsVisibility();
      this.refleshData();
   }

   private void btnClicked(int btnId) {
      this.minecraft.gameMode.handleInventoryButtonClick(((ArenaReceptionistMenu)this.menu).containerId, btnId);
      this.onClose();
   }

   private void startMatch(boolean isFree) {
      if (this.selectedEntry != null) {
         if (!isFree) {
         }

         FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
         buf.writeBoolean(isFree);
         buf.writeUtf(((ArenaReceptionistMenu)this.menu).getArenaId());
         buf.writeUUID(this.selectedEntry.getFighterId());
         NetworkManager.sendToServer(PomkotsMechs.id("asm"), buf);
         this.onClose();
      }
   }

   private void savePlayerProfile() {
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
      buf.writeUtf(this.nameField.getValue());
      buf.writeUtf(this.commentField.getValue());
      buf.writeUtf(((ArenaReceptionistMenu)this.menu).getArenaId());
      NetworkManager.sendToServer(PomkotsMechs.id("aspp"), buf);
   }

   private void removePlayerPlofile() {
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
      buf.writeUtf(((ArenaReceptionistMenu)this.menu).getArenaId());
      NetworkManager.sendToServer(PomkotsMechs.id("arpp"), buf);
   }

   public void refleshData() {
      this.selfProfile = null;
      LocalPlayer player = Minecraft.getInstance().player;
      if (((ArenaReceptionistMenu)this.menu).getRankings() != null && player != null) {
         for (ArenaRankingEntry fighter : ((ArenaReceptionistMenu)this.menu).getRankings()) {
            if (fighter.getFighterId().equals(player.getUUID())) {
               if (this.nameField != null) {
                  this.nameField.setValue(fighter.getDisplayName());
               }

               if (this.commentField != null) {
                  this.commentField.setValue(fighter.getComment());
               }

               this.mechName = fighter.getMechName();
               this.selfProfile = fighter;
            }
         }
      }
   }

   private void updateMatchButtons() {
      boolean enable = this.currentTab == ArenaReceptionistScreen.ArenaTab.RANKED
         && this.selectedEntry != null
         && ((ArenaReceptionistMenu)this.menu).getMode() == 0
         && this.selfProfile != null;
      this.rankMatchButton.active = enable;
      this.freeMatchButton.active = enable;
   }

   private void updateComponentsVisibility() {
      boolean visible = this.currentTab == ArenaReceptionistScreen.ArenaTab.PROFILE;
      if (this.nameField != null) {
         this.nameField.visible = visible;
         this.nameField.setEditable(visible);
      }

      if (this.commentField != null) {
         this.commentField.visible = visible;
         this.commentField.setEditable(visible);
      }

      if (this.saveProfileButton != null) {
         this.saveProfileButton.visible = visible;
         this.saveProfileButton.active = visible;
      }

      if (this.removeProfileButton != null) {
         this.removeProfileButton.visible = visible;
         this.removeProfileButton.active = visible;
      }

      for (int i = 0; i < ((ArenaReceptionistMenu)this.menu).slots.size(); i++) {
         if (((ArenaReceptionistMenu)this.menu).getSlot(i) instanceof ArenaReceptionistMenu.ToggleableSlot ts) {
            ts.setActive(visible);
         }
      }
   }

   private void updateScrollLimits() {
      this.contentHeight = ((ArenaReceptionistMenu)this.menu).getRankings().size() * 64;
      this.maxScroll = Math.max(0, this.contentHeight - 190);
      this.scrollOffset = Mth.clamp(this.scrollOffset, 0, this.maxScroll);
   }

   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.updateMatchButtons();
      this.updateComponentsVisibility();
      this.updateScrollLimits();
      this.renderBackground(graphics);
      super.render(graphics, mouseX, mouseY, partialTick);
      if (this.currentTab == ArenaReceptionistScreen.ArenaTab.RANKED) {
         this.renderRanking(graphics);
      } else if (this.currentTab == ArenaReceptionistScreen.ArenaTab.PROFILE) {
         this.renderProfileTab(graphics);
      }

      this.renderTooltip(graphics, mouseX, mouseY);
      if (this.acceptButton != null && this.acceptButton.isHovered()) {
         graphics.renderTooltip(
            this.font,
            List.of(
               Component.literal("Accept the challenge and teleport to the arena"),
               Component.literal("After the match, you can return to your original location using the \"Return\" button"),
               Component.literal("* You must be piloting a Mech").withStyle(ChatFormatting.RED)
            ),
            Optional.empty(),
            mouseX,
            mouseY
         );
      }

      if (this.declineButton != null && this.declineButton.isHovered()) {
         graphics.renderTooltip(
            this.font,
            List.of(
               Component.literal("Decline the challenge"),
               Component.literal("If the request times out, it will be automatically declined"),
               Component.literal("No ranking changes will occur").withStyle(ChatFormatting.RED)
            ),
            Optional.empty(),
            mouseX,
            mouseY
         );
      }

      if (this.returnButton != null && this.returnButton.isHovered()) {
         graphics.renderTooltip(
            this.font,
            List.of(
               Component.literal("Teleport to your pre-teleport location"),
               Component.literal("* You must be piloting a Mech").withStyle(ChatFormatting.RED)
            ),
            Optional.empty(),
            mouseX,
            mouseY
         );
      }
   }

   private void renderProfileTab(GuiGraphics graphics) {
      int x = this.rankingOffsetX + 10;
      int y = this.rankingOffsetY + 10;
      graphics.drawString(this.font, "Display Name", x, y, 16777215);
      graphics.drawString(this.font, "Comment", x, y + 45, 16777215);
      graphics.drawString(this.font, "Registered Mech", x, y + 90, 16777215);
      this.renderBgTexture(TEXTURE_ITEM_CONTAINER, x + 10, y + 105, 16, 16, graphics);
      graphics.drawString(this.font, "Put your Data Pad here", x + 34, y + 109, 11184810);
      graphics.drawString(this.font, "Registered Mech:", x, y + 135, 16777215);
      graphics.drawString(this.font, this.mechName, x + 100, y + 135, 5635925);
      this.renderPlayerInventoryPanel(graphics);
   }

   private void renderPlayerInventoryPanel(GuiGraphics guiGraphics) {
      this.renderBgTexture(TEXTURE_PANEL_INV_PLAYER, this.playerInvX, this.playerInvY, this.playerInvWidth, this.playerInvHeight, guiGraphics);
      guiGraphics.drawString(this.font, "Player Inventory", this.playerInvX + 18, this.playerInvY + 4, -1);
   }

   private void renderRanking(GuiGraphics graphics) {
      List<ArenaRankingEntry> rankings = ((ArenaReceptionistMenu)this.menu).getRankings();
      int areaX = this.rankingOffsetX;
      int areaY = this.rankingOffsetY;
      graphics.fill(areaX, areaY, areaX + 440, areaY + 190, 1711276032);
      graphics.enableScissor(areaX, areaY, areaX + 440, areaY + 190);
      int cardY = areaY + 4 - this.scrollOffset;

      for (int i = 0; i < rankings.size(); i++) {
         ArenaRankingEntry entry = rankings.get(i);
         if (cardY + 60 < areaY) {
            cardY += 64;
         } else {
            if (cardY > areaY + 190) {
               break;
            }

            this.renderRankingCard(graphics, entry, i, areaX + 4, cardY);
            cardY += 64;
         }
      }

      graphics.disableScissor();
      this.renderScrollbar(graphics);
   }

   private void renderRankingCard(GuiGraphics graphics, ArenaRankingEntry entry, int rank, int x, int y) {
      boolean selected = entry.getFighterId().equals(this.selectedFighterId);
      if (selected) {
         this.renderBgTexture(TEXTURE_CARD_SELECTED, x - 2, y - 2, 434, 64, graphics);
      } else {
         this.renderBgTexture(TEXTURE_CARD, x, y, 430, 60, graphics);
      }

      graphics.fill(x + 6, y + 6, x + 6 + 48, y + 6 + 48, -12566464);
      if (((ArenaReceptionistMenu)this.menu).getMode() == 1 && this.challengerUuid != null && this.challengerUuid.equals(entry.getFighterId())) {
         this.renderBadge("C", x + 12, y + 12, -65536, graphics);
      }

      graphics.drawCenteredString(this.font, "#" + (rank + 1), x + 30, y + 24, 16777215);
      String text = entry.isOnline() ? "ONLINE" : "OFFLINE";
      int color = entry.isOnline() ? -11141291 : -11184811;
      graphics.drawCenteredString(this.font, text, x + 30, y + 40, color);
      graphics.drawString(this.font, entry.getDisplayName() + (entry.getType() == 0 ? "" : " (NPC)"), x + 62, y + 6, 16777215, false);
      graphics.drawString(this.font, "Rating : " + entry.getRating(), x + 62, y + 18, 12632256, false);
      graphics.drawString(this.font, "Win " + entry.getWins() + " / Lose " + entry.getLosses(), x + 62, y + 30, 12632256, false);
      ArenaRank curRank = ArenaRank.getRank(entry.getRating());
      graphics.drawString(this.font, "Rank : " + curRank.getName(), x + 62, y + 45, curRank.getColor(), false);
      graphics.drawString(this.font, "Mech : " + entry.getMechName(), x + 240, y + 6, 8454143, false);
      String comment = this.font.plainSubstrByWidth(entry.getComment(), 170);
      graphics.drawString(this.font, comment, x + 240, y + 20, 12632256, false);
   }

   private void renderBadge(String text, int x, int y, int bgcolor, GuiGraphics graphics) {
      int badgeX = x - 2;
      int badgeY = y - 2;
      graphics.fill(badgeX, badgeY, badgeX + this.font.width(text), badgeY + 10, bgcolor);
      graphics.drawCenteredString(Minecraft.getInstance().font, text, badgeX + this.font.width(text) / 2, badgeY + 1, -1);
   }

   private void renderScrollbar(GuiGraphics graphics) {
      if (this.maxScroll > 0) {
         int barX = this.rankingOffsetX + 440 + 2;
         int barY = this.rankingOffsetY;
         int barHeight = 190;
         float visibleRatio = 190.0F / this.contentHeight;
         int thumbHeight = Math.max(16, (int)(barHeight * visibleRatio));
         float progress = (float)this.scrollOffset / this.maxScroll;
         int movable = barHeight - thumbHeight;
         int thumbY = barY + (int)(progress * movable);
         this.renderBgTexture(TEXTURE_SCROLL_BAR_BG, barX, barY, 2, barHeight, graphics);
         this.renderBgTexture(TEXTURE_SCROLL_BAR, barX, thumbY, 4, thumbHeight, graphics);
      }
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      this.scrollOffset -= (int)(delta * 12.0);
      this.scrollOffset = Mth.clamp(this.scrollOffset, 0, this.maxScroll);
      return true;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      boolean result = super.mouseClicked(mouseX, mouseY, button);
      if (!this.nameField.isMouseOver(mouseX, mouseY)) {
         this.nameField.setFocused(false);
      }

      if (!this.commentField.isMouseOver(mouseX, mouseY)) {
         this.commentField.setFocused(false);
      }

      if (this.currentTab == ArenaReceptionistScreen.ArenaTab.RANKED) {
         if (mouseX < this.rankingOffsetX || mouseX > this.rankingOffsetX + 440 || mouseY < this.rankingOffsetY || mouseY > this.rankingOffsetY + 190) {
            return result;
         }

         int cardY = this.rankingOffsetY + 4 - this.scrollOffset;

         for (ArenaRankingEntry entry : ((ArenaReceptionistMenu)this.menu).getRankings()) {
            int cardX = this.rankingOffsetX + 4;
            if (mouseX >= cardX && mouseX <= cardX + 430 && mouseY >= cardY && mouseY <= cardY + 60) {
               if (entry.isOnline()
                  && (
                     this.minecraft == null
                        || this.minecraft.player == null
                        || entry.getFighterId() == null
                        || !this.minecraft.player.getUUID().equals(entry.getFighterId())
                  )) {
                  this.selectedFighterId = entry.getFighterId();
                  this.selectedEntry = entry;
                  return true;
               }
            } else {
               cardY += 64;
            }
         }
      }

      return result;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (this.nameField.isFocused()) {
         if (keyCode == 256) {
            this.nameField.setFocused(false);
            return true;
         }

         if (this.nameField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
         }

         if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            return true;
         }
      }

      if (this.commentField.isFocused()) {
         if (keyCode == 256) {
            this.commentField.setFocused(false);
            return true;
         }

         if (this.commentField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
         }

         if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            return true;
         }
      }

      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   private boolean isAcceptEnable() {
      ItemStack dataPadStack = this.getDataPad();
      return dataPadStack == null ? false : PomkotsDatapadItem.hasArenaRequests(dataPadStack);
   }

   private boolean isDeclineEnable() {
      ItemStack dataPadStack = this.getDataPad();
      return dataPadStack == null ? false : PomkotsDatapadItem.hasArenaRequests(dataPadStack);
   }

   private boolean isReturnEnable() {
      ItemStack dataPadStack = this.getDataPad();
      return dataPadStack == null ? false : PomkotsDatapadItem.hasReturnLocation(dataPadStack);
   }

   private ItemStack getDataPad() {
      ItemStack p = ((ArenaReceptionistMenu)this.menu).getDataPadStack();
      return p.getItem() instanceof PomkotsDatapadItem ? p : null;
   }

   private ImageButton buildButton(String text, int x, int y, OnPress onPress) {
      return this.buildButton(text, x, y, onPress, true);
   }

   private ImageButton buildButton(String text, int x, int y, OnPress onPress, boolean active) {
      ImageButton b = new ImageButton(x, y, 70, 14, TEXTURE_BUTTON, 0, 0, 70, 56, Component.literal(text), onPress);
      b.active = active;
      return b;
   }

   protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
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

   public enum ArenaTab {
      RANKED,
      TOURNAMENT,
      PROFILE;
   }
}
