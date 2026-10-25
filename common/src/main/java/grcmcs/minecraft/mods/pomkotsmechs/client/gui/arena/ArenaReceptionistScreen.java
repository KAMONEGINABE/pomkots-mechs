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
      this.f_97726_ = 480;
      this.f_97727_ = 270;
   }

   protected void m_7856_() {
      super.m_7856_();
      this.tabSelectorOffsetX = this.f_97735_ + 20;
      this.tabSelectorOffsetY = this.f_97736_ + 35;
      this.rankingOffsetX = this.tabSelectorOffsetX;
      this.rankingOffsetY = this.tabSelectorOffsetY + 14;
      this.playerInvX = this.f_97735_ + 310;
      this.playerInvY = this.f_97736_ + 35;
      this.m_142416_(
         this.buildButton("Ranking", this.tabSelectorOffsetX, this.tabSelectorOffsetY, btn -> this.currentTab = ArenaReceptionistScreen.ArenaTab.RANKED)
      );
      if (((ArenaReceptionistMenu)this.f_97732_).getMode() == 0) {
         this.m_142416_(
            this.buildButton(
               "Profile", this.tabSelectorOffsetX + 70, this.tabSelectorOffsetY, btn -> this.currentTab = ArenaReceptionistScreen.ArenaTab.PROFILE
            )
         );
      }

      this.rankMatchButton = (Button)this.m_142416_(
         this.buildButton("Rank Match", this.tabSelectorOffsetX + 280, this.rankingOffsetY + 190 + 5, btn -> this.startMatch(true))
      );
      this.freeMatchButton = (Button)this.m_142416_(
         this.buildButton("Free Match", this.tabSelectorOffsetX + 350 + 15, this.rankingOffsetY + 190 + 5, btn -> this.startMatch(false))
      );
      if (((ArenaReceptionistMenu)this.f_97732_).getMode() == 1) {
         this.acceptButton = (ImageButton)this.m_142416_(
            this.buildButton("Teleport", this.tabSelectorOffsetX + 210 - 15, this.rankingOffsetY - 18, btn -> this.btnClicked(0), this.isAcceptEnable())
         );
         this.declineButton = (ImageButton)this.m_142416_(
            this.buildButton("Decline", this.tabSelectorOffsetX + 280, this.rankingOffsetY - 18, btn -> this.btnClicked(1), this.isDeclineEnable())
         );
         this.returnButton = (ImageButton)this.m_142416_(
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

      this.nameField = new EditBox(this.f_96547_, this.rankingOffsetX + 20, this.rankingOffsetY + 10 + 15, 180, 18, Component.m_237113_("Display Name"));
      this.nameField.m_94199_(32);
      this.m_142416_(this.nameField);
      this.commentField = new EditBox(this.f_96547_, this.rankingOffsetX + 20, this.rankingOffsetY + 10 + 45 + 15, 180, 18, Component.m_237113_("Comment"));
      this.commentField.m_94199_(64);
      this.m_142416_(this.commentField);
      this.saveProfileButton = (Button)this.m_142416_(
         this.buildButton("Save", this.rankingOffsetX, this.rankingOffsetY + 145 + 20, btn -> this.savePlayerProfile())
      );
      this.removeProfileButton = (Button)this.m_142416_(
         this.buildButton("Remove", this.rankingOffsetX + 70 + 20, this.rankingOffsetY + 145 + 20, btn -> this.removePlayerPlofile())
      );
      this.updateScrollLimits();
      this.updateComponentsVisibility();
      this.refleshData();
   }

   private void btnClicked(int btnId) {
      this.f_96541_.f_91072_.m_105208_(((ArenaReceptionistMenu)this.f_97732_).f_38840_, btnId);
      this.m_7379_();
   }

   private void startMatch(boolean isFree) {
      if (this.selectedEntry != null) {
         if (!isFree) {
         }

         FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
         buf.writeBoolean(isFree);
         buf.m_130070_(((ArenaReceptionistMenu)this.f_97732_).getArenaId());
         buf.m_130077_(this.selectedEntry.getFighterId());
         NetworkManager.sendToServer(PomkotsMechs.id("asm"), buf);
         this.m_7379_();
      }
   }

   private void savePlayerProfile() {
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
      buf.m_130070_(this.nameField.m_94155_());
      buf.m_130070_(this.commentField.m_94155_());
      buf.m_130070_(((ArenaReceptionistMenu)this.f_97732_).getArenaId());
      NetworkManager.sendToServer(PomkotsMechs.id("aspp"), buf);
   }

   private void removePlayerPlofile() {
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
      buf.m_130070_(((ArenaReceptionistMenu)this.f_97732_).getArenaId());
      NetworkManager.sendToServer(PomkotsMechs.id("arpp"), buf);
   }

   public void refleshData() {
      this.selfProfile = null;
      LocalPlayer player = Minecraft.m_91087_().f_91074_;
      if (((ArenaReceptionistMenu)this.f_97732_).getRankings() != null && player != null) {
         for (ArenaRankingEntry fighter : ((ArenaReceptionistMenu)this.f_97732_).getRankings()) {
            if (fighter.getFighterId().equals(player.m_20148_())) {
               if (this.nameField != null) {
                  this.nameField.m_94144_(fighter.getDisplayName());
               }

               if (this.commentField != null) {
                  this.commentField.m_94144_(fighter.getComment());
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
         && ((ArenaReceptionistMenu)this.f_97732_).getMode() == 0
         && this.selfProfile != null;
      this.rankMatchButton.f_93623_ = enable;
      this.freeMatchButton.f_93623_ = enable;
   }

   private void updateComponentsVisibility() {
      boolean visible = this.currentTab == ArenaReceptionistScreen.ArenaTab.PROFILE;
      if (this.nameField != null) {
         this.nameField.f_93624_ = visible;
         this.nameField.m_94186_(visible);
      }

      if (this.commentField != null) {
         this.commentField.f_93624_ = visible;
         this.commentField.m_94186_(visible);
      }

      if (this.saveProfileButton != null) {
         this.saveProfileButton.f_93624_ = visible;
         this.saveProfileButton.f_93623_ = visible;
      }

      if (this.removeProfileButton != null) {
         this.removeProfileButton.f_93624_ = visible;
         this.removeProfileButton.f_93623_ = visible;
      }

      for (int i = 0; i < ((ArenaReceptionistMenu)this.f_97732_).f_38839_.size(); i++) {
         if (((ArenaReceptionistMenu)this.f_97732_).m_38853_(i) instanceof ArenaReceptionistMenu.ToggleableSlot ts) {
            ts.setActive(visible);
         }
      }
   }

   private void updateScrollLimits() {
      this.contentHeight = ((ArenaReceptionistMenu)this.f_97732_).getRankings().size() * 64;
      this.maxScroll = Math.max(0, this.contentHeight - 190);
      this.scrollOffset = Mth.m_14045_(this.scrollOffset, 0, this.maxScroll);
   }

   public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.updateMatchButtons();
      this.updateComponentsVisibility();
      this.updateScrollLimits();
      this.m_280273_(graphics);
      super.m_88315_(graphics, mouseX, mouseY, partialTick);
      if (this.currentTab == ArenaReceptionistScreen.ArenaTab.RANKED) {
         this.renderRanking(graphics);
      } else if (this.currentTab == ArenaReceptionistScreen.ArenaTab.PROFILE) {
         this.renderProfileTab(graphics);
      }

      this.m_280072_(graphics, mouseX, mouseY);
      if (this.acceptButton != null && this.acceptButton.m_274382_()) {
         graphics.m_280677_(
            this.f_96547_,
            List.of(
               Component.m_237113_("Accept the challenge and teleport to the arena"),
               Component.m_237113_("After the match, you can return to your original location using the \"Return\" button"),
               Component.m_237113_("* You must be piloting a Mech").m_130940_(ChatFormatting.RED)
            ),
            Optional.empty(),
            mouseX,
            mouseY
         );
      }

      if (this.declineButton != null && this.declineButton.m_274382_()) {
         graphics.m_280677_(
            this.f_96547_,
            List.of(
               Component.m_237113_("Decline the challenge"),
               Component.m_237113_("If the request times out, it will be automatically declined"),
               Component.m_237113_("No ranking changes will occur").m_130940_(ChatFormatting.RED)
            ),
            Optional.empty(),
            mouseX,
            mouseY
         );
      }

      if (this.returnButton != null && this.returnButton.m_274382_()) {
         graphics.m_280677_(
            this.f_96547_,
            List.of(
               Component.m_237113_("Teleport to your pre-teleport location"),
               Component.m_237113_("* You must be piloting a Mech").m_130940_(ChatFormatting.RED)
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
      graphics.m_280488_(this.f_96547_, "Display Name", x, y, 16777215);
      graphics.m_280488_(this.f_96547_, "Comment", x, y + 45, 16777215);
      graphics.m_280488_(this.f_96547_, "Registered Mech", x, y + 90, 16777215);
      this.renderBgTexture(TEXTURE_ITEM_CONTAINER, x + 10, y + 105, 16, 16, graphics);
      graphics.m_280488_(this.f_96547_, "Put your Data Pad here", x + 34, y + 109, 11184810);
      graphics.m_280488_(this.f_96547_, "Registered Mech:", x, y + 135, 16777215);
      graphics.m_280488_(this.f_96547_, this.mechName, x + 100, y + 135, 5635925);
      this.renderPlayerInventoryPanel(graphics);
   }

   private void renderPlayerInventoryPanel(GuiGraphics guiGraphics) {
      this.renderBgTexture(TEXTURE_PANEL_INV_PLAYER, this.playerInvX, this.playerInvY, this.playerInvWidth, this.playerInvHeight, guiGraphics);
      guiGraphics.m_280488_(this.f_96547_, "Player Inventory", this.playerInvX + 18, this.playerInvY + 4, -1);
   }

   private void renderRanking(GuiGraphics graphics) {
      List<ArenaRankingEntry> rankings = ((ArenaReceptionistMenu)this.f_97732_).getRankings();
      int areaX = this.rankingOffsetX;
      int areaY = this.rankingOffsetY;
      graphics.m_280509_(areaX, areaY, areaX + 440, areaY + 190, 1711276032);
      graphics.m_280588_(areaX, areaY, areaX + 440, areaY + 190);
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

      graphics.m_280618_();
      this.renderScrollbar(graphics);
   }

   private void renderRankingCard(GuiGraphics graphics, ArenaRankingEntry entry, int rank, int x, int y) {
      boolean selected = entry.getFighterId().equals(this.selectedFighterId);
      if (selected) {
         this.renderBgTexture(TEXTURE_CARD_SELECTED, x - 2, y - 2, 434, 64, graphics);
      } else {
         this.renderBgTexture(TEXTURE_CARD, x, y, 430, 60, graphics);
      }

      graphics.m_280509_(x + 6, y + 6, x + 6 + 48, y + 6 + 48, -12566464);
      if (((ArenaReceptionistMenu)this.f_97732_).getMode() == 1 && this.challengerUuid != null && this.challengerUuid.equals(entry.getFighterId())) {
         this.renderBadge("C", x + 12, y + 12, -65536, graphics);
      }

      graphics.m_280137_(this.f_96547_, "#" + (rank + 1), x + 30, y + 24, 16777215);
      String text = entry.isOnline() ? "ONLINE" : "OFFLINE";
      int color = entry.isOnline() ? -11141291 : -11184811;
      graphics.m_280137_(this.f_96547_, text, x + 30, y + 40, color);
      graphics.m_280056_(this.f_96547_, entry.getDisplayName() + (entry.getType() == 0 ? "" : " (NPC)"), x + 62, y + 6, 16777215, false);
      graphics.m_280056_(this.f_96547_, "Rating : " + entry.getRating(), x + 62, y + 18, 12632256, false);
      graphics.m_280056_(this.f_96547_, "Win " + entry.getWins() + " / Lose " + entry.getLosses(), x + 62, y + 30, 12632256, false);
      ArenaRank curRank = ArenaRank.getRank(entry.getRating());
      graphics.m_280056_(this.f_96547_, "Rank : " + curRank.getName(), x + 62, y + 45, curRank.getColor(), false);
      graphics.m_280056_(this.f_96547_, "Mech : " + entry.getMechName(), x + 240, y + 6, 8454143, false);
      String comment = this.f_96547_.m_92834_(entry.getComment(), 170);
      graphics.m_280056_(this.f_96547_, comment, x + 240, y + 20, 12632256, false);
   }

   private void renderBadge(String text, int x, int y, int bgcolor, GuiGraphics graphics) {
      int badgeX = x - 2;
      int badgeY = y - 2;
      graphics.m_280509_(badgeX, badgeY, badgeX + this.f_96547_.m_92895_(text), badgeY + 10, bgcolor);
      graphics.m_280137_(Minecraft.m_91087_().f_91062_, text, badgeX + this.f_96547_.m_92895_(text) / 2, badgeY + 1, -1);
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

   public boolean m_6050_(double mouseX, double mouseY, double delta) {
      this.scrollOffset -= (int)(delta * 12.0);
      this.scrollOffset = Mth.m_14045_(this.scrollOffset, 0, this.maxScroll);
      return true;
   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      boolean result = super.m_6375_(mouseX, mouseY, button);
      if (!this.nameField.m_5953_(mouseX, mouseY)) {
         this.nameField.m_93692_(false);
      }

      if (!this.commentField.m_5953_(mouseX, mouseY)) {
         this.commentField.m_93692_(false);
      }

      if (this.currentTab == ArenaReceptionistScreen.ArenaTab.RANKED) {
         if (mouseX < this.rankingOffsetX || mouseX > this.rankingOffsetX + 440 || mouseY < this.rankingOffsetY || mouseY > this.rankingOffsetY + 190) {
            return result;
         }

         int cardY = this.rankingOffsetY + 4 - this.scrollOffset;

         for (ArenaRankingEntry entry : ((ArenaReceptionistMenu)this.f_97732_).getRankings()) {
            int cardX = this.rankingOffsetX + 4;
            if (mouseX >= cardX && mouseX <= cardX + 430 && mouseY >= cardY && mouseY <= cardY + 60) {
               if (entry.isOnline()
                  && (
                     this.f_96541_ == null
                        || this.f_96541_.f_91074_ == null
                        || entry.getFighterId() == null
                        || !this.f_96541_.f_91074_.m_20148_().equals(entry.getFighterId())
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

   public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
      if (this.nameField.m_93696_()) {
         if (keyCode == 256) {
            this.nameField.m_93692_(false);
            return true;
         }

         if (this.nameField.m_7933_(keyCode, scanCode, modifiers)) {
            return true;
         }

         if (this.f_96541_.f_91066_.f_92092_.m_90832_(keyCode, scanCode)) {
            return true;
         }
      }

      if (this.commentField.m_93696_()) {
         if (keyCode == 256) {
            this.commentField.m_93692_(false);
            return true;
         }

         if (this.commentField.m_7933_(keyCode, scanCode, modifiers)) {
            return true;
         }

         if (this.f_96541_.f_91066_.f_92092_.m_90832_(keyCode, scanCode)) {
            return true;
         }
      }

      return super.m_7933_(keyCode, scanCode, modifiers);
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
      ItemStack p = ((ArenaReceptionistMenu)this.f_97732_).getDataPadStack();
      return p.m_41720_() instanceof PomkotsDatapadItem ? p : null;
   }

   private ImageButton buildButton(String text, int x, int y, OnPress onPress) {
      return this.buildButton(text, x, y, onPress, true);
   }

   private ImageButton buildButton(String text, int x, int y, OnPress onPress, boolean active) {
      ImageButton b = new ImageButton(x, y, 70, 14, TEXTURE_BUTTON, 0, 0, 70, 56, Component.m_237113_(text), onPress);
      b.f_93623_ = active;
      return b;
   }

   protected void m_7286_(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
   }

   protected void m_280003_(GuiGraphics guiGraphics, int mouseX, int mouseY) {
   }

   private void renderBgTexture(ResourceLocation texture, int x, int y, int width, int height, GuiGraphics guiGraphics) {
      this.renderBgTexture(texture, x, y, width, height, 0.5F, guiGraphics);
   }

   private void renderBgTexture(ResourceLocation texture, int x, int y, int width, int height, float alpha, GuiGraphics guiGraphics) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      RenderSystem.enableBlend();
      RenderSystem.setShaderTexture(0, texture);
      guiGraphics.m_280163_(texture, x, y, 0.0F, 0.0F, width, height, width, height);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public enum ArenaTab {
      RANKED,
      TOURNAMENT,
      PROFILE;
   }
}
