package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.trader.MechTraderEntity;
import grcmcs.minecraft.mods.pomkotsmechs.entity.npc.trader.TraderInventory;
import grcmcs.minecraft.mods.pomkotsmechs.items.coin.AbstractPomCoinItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MechTraderScreen extends AbstractContainerScreen<MechTraderMenu> {
   private static final ResourceLocation TEXTURE_BG_1 = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/background_hangar.png");
   private static final ResourceLocation TEXTURE_BG_2 = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/background_gradient.png");
   private static final ResourceLocation TEXTURE_BUTTON = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/button_normal.png");
   private static final ResourceLocation TEXTURE_PANEL_INV_PLAYER = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/inventory_player.png");
   private static final ResourceLocation TEXTURE_OFFERS_PANEL = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/mech_trader_offers.png");
   private static final ResourceLocation TEXTURE_CARD = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/mech_trader_card.png");
   private static final ResourceLocation TEXTURE_CARD_SELECTED = new ResourceLocation(
      "pomkotsmechs", "textures/gui/mechworkbench/mech_trader_card_selected.png"
   );
   private static final int GUI_WIDTH = 480;
   private static final int GUI_HEIGHT = 270;
   private static final int BUTTON_WIDTH = 70;
   private static final int BUTTON_HEIGHT = 14;
   private static final int CARD_WIDTH = 58;
   private static final int CARD_HEIGHT = 18;
   private static final int OFFER_WIDTH = 264;
   private static final int OFFER_HEIGHT = 198;
   private int offersOffsetX = 0;
   private int offersOffsetY = 0;
   private int panelWidth = 108;
   private int panelHeight = 160;
   private int playerInvX = 0;
   private int playerInvY = 0;
   private int playerInvWidth = 164;
   private int playerInvHeight = 94;
   private int selectedOfferSlot = -1;
   private Button buyButton;
   private MechTraderEntity trader = null;

   public MechTraderScreen(MechTraderMenu menu, Inventory inventory, Component component) {
      super(menu, inventory, component);
      this.imageWidth = 480;
      this.imageHeight = 270;
   }

   protected void init() {
      super.init();
      this.offersOffsetX = this.leftPos + 20;
      this.offersOffsetY = this.topPos + 35;
      this.playerInvX = this.leftPos + 310;
      this.playerInvY = this.topPos + 35;
      this.buyButton = (Button)this.addRenderableWidget(
         new ImageButton(
            this.playerInvX,
            this.playerInvY + this.playerInvHeight + 10,
            70,
            14,
            TEXTURE_BUTTON,
            0,
            0,
            70,
            56,
            Component.literal("Buy"),
            btn -> this.buySelected()
         )
      );
   }

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      if (this.minecraft != null && this.minecraft.level != null && this.trader == null) {
         for (Entity entity : this.minecraft.level.entitiesForRendering()) {
            if (entity instanceof MechTraderEntity m && (short)entity.getUUID().hashCode() == ((MechTraderMenu)this.menu).getEntityId()) {
               this.trader = m;
            }
         }
      }

      this.renderBackground(guiGraphics);
      super.render(guiGraphics, mouseX, mouseY, partialTick);
      this.renderTooltip(guiGraphics, mouseX, mouseY);
   }

   protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      this.renderOfferPanel(guiGraphics, partialTick, mouseX, mouseY);
      this.renderPlayerInventoryPanel(guiGraphics);
      guiGraphics.drawString(this.font, "Time Left: " + this.getRemainingTimeString(), this.offersOffsetX, this.offersOffsetY - 15, -1, false);
   }

   private void renderPlayerInventoryPanel(GuiGraphics guiGraphics) {
      this.renderBgTexture(TEXTURE_PANEL_INV_PLAYER, this.playerInvX, this.playerInvY, this.playerInvWidth, this.playerInvHeight, guiGraphics);
      guiGraphics.drawString(this.font, "Player Inventory", this.playerInvX + 18, this.playerInvY + 4, -1);
   }

   private void renderOfferPanel(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      this.renderBgTexture(TEXTURE_OFFERS_PANEL, this.offersOffsetX, this.offersOffsetY, 264, 198, guiGraphics);

      for (MechTraderEntity.TraderCategory category : MechTraderEntity.TraderCategory.values()) {
         int col = category.ordinal();
         drawCenteredStringNoShadow(
            guiGraphics, this.font, this.getCategoryString(category), this.offersOffsetX + 5 + col * 65 + 31, this.offersOffsetY + 4, -1
         );

         for (int row = 0; row < 8; row++) {
            int offerSlot = TraderInventory.getOfferSlot(category, row);
            int priceSlot = TraderInventory.getPriceSlot(category, row);
            this.renderOfferCard(guiGraphics, offerSlot, priceSlot, col, row, mouseX, mouseY);
         }
      }
   }

   private String getCategoryString(MechTraderEntity.TraderCategory category) {
      return switch (category) {
         case BLUEPRINT -> "Blue Print";
         case MATERIAL -> "Material";
         case CONSUMABLE -> "Consumable";
         case FOOD -> "Food";
      };
   }

   private void renderOfferCard(GuiGraphics gg, int offerSlot, int priceSlot, int col, int row, int mouseX, int mouseY) {
      Slot offer = ((MechTraderMenu)this.menu).getSlot(offerSlot);
      Slot price = ((MechTraderMenu)this.menu).getSlot(priceSlot);
      ItemStack offerStack = offer.getItem();
      ItemStack priceStack = price.getItem();
      if (!offerStack.isEmpty()) {
         int x = this.offersOffsetX + 5 + col * 65;
         int y = this.offersOffsetY + 17 + row * 22;
         boolean selected = this.selectedOfferSlot == offerSlot;
         if (selected) {
            this.renderBgTexture(TEXTURE_CARD_SELECTED, x, y, 58, 18, gg);
         } else {
            this.renderBgTexture(TEXTURE_CARD, x, y, 58, 18, gg);
         }

         drawCenteredStringNoShadow(gg, this.font, "=", x + 30, y + 6, -1);
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      for (MechTraderEntity.TraderCategory category : MechTraderEntity.TraderCategory.values()) {
         int col = category.ordinal();

         for (int row = 0; row < 8; row++) {
            int offerSlot = TraderInventory.getOfferSlot(category, row);
            int x = this.offersOffsetX + 5 + col * 65;
            int y = this.offersOffsetY + 17 + row * 22;
            if (this.isPointInside(x, y, 58, 18, mouseX, mouseY)) {
               if (!((MechTraderMenu)this.menu).getSlot(offerSlot).getItem().isEmpty()) {
                  this.selectedOfferSlot = offerSlot;
               }

               return true;
            }
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   private boolean isPointInside(int x, int y, int width, int height, double mouseX, double mouseY) {
      return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
   }

   private void buySelected() {
      if (this.selectedOfferSlot >= 0) {
         if (!((MechTraderMenu)this.menu).getSlot(this.selectedOfferSlot).getItem().is((Item)PomkotsMechs.SOLD_ITEM.get())) {
            this.minecraft.gameMode.handleInventoryButtonClick(((MechTraderMenu)this.menu).containerId, this.selectedOfferSlot);
         }
      }
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

   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
   }

   protected void renderTooltip(GuiGraphics guiGraphics, int i, int j) {
      if (((MechTraderMenu)this.menu).getCarried().isEmpty()
         && this.hoveredSlot != null
         && this.hoveredSlot.hasItem()
         && !(this.hoveredSlot.getItem().getItem() instanceof AbstractPomCoinItem)) {
         super.renderTooltip(guiGraphics, i, j);
      }
   }

   public static void drawCenteredStringNoShadow(GuiGraphics guiGraphics, Font font, String text, int centerX, int y, int color) {
      int width = font.width(text);
      guiGraphics.drawString(font, text, centerX - width / 2, y, color, false);
   }

   public String getRemainingTimeString() {
      if (this.trader != null && this.trader.getDespawnTime() >= 0L) {
         long despawnDayTime = this.trader.getDespawnTime();
         long currentDayTime = this.trader.level().getDayTime();
         long remainingTicks = Math.max(0L, despawnDayTime - currentDayTime);
         int totalSeconds = (int)(remainingTicks / 20L);
         int minutes = totalSeconds / 60;
         int seconds = totalSeconds % 60;
         return String.format("%02d:%02d", minutes, seconds);
      } else {
         return "--:--";
      }
   }
}
