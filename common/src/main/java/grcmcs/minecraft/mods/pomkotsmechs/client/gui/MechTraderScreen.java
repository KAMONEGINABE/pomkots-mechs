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
      this.f_97726_ = 480;
      this.f_97727_ = 270;
   }

   protected void m_7856_() {
      super.m_7856_();
      this.offersOffsetX = this.f_97735_ + 20;
      this.offersOffsetY = this.f_97736_ + 35;
      this.playerInvX = this.f_97735_ + 310;
      this.playerInvY = this.f_97736_ + 35;
      this.buyButton = (Button)this.m_142416_(
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
            Component.m_237113_("Buy"),
            btn -> this.buySelected()
         )
      );
   }

   public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      if (this.f_96541_ != null && this.f_96541_.f_91073_ != null && this.trader == null) {
         for (Entity entity : this.f_96541_.f_91073_.m_104735_()) {
            if (entity instanceof MechTraderEntity m && (short)entity.m_20148_().hashCode() == ((MechTraderMenu)this.f_97732_).getEntityId()) {
               this.trader = m;
            }
         }
      }

      this.m_280273_(guiGraphics);
      super.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
      this.m_280072_(guiGraphics, mouseX, mouseY);
   }

   protected void m_7286_(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      this.renderOfferPanel(guiGraphics, partialTick, mouseX, mouseY);
      this.renderPlayerInventoryPanel(guiGraphics);
      guiGraphics.m_280056_(this.f_96547_, "Time Left: " + this.getRemainingTimeString(), this.offersOffsetX, this.offersOffsetY - 15, -1, false);
   }

   private void renderPlayerInventoryPanel(GuiGraphics guiGraphics) {
      this.renderBgTexture(TEXTURE_PANEL_INV_PLAYER, this.playerInvX, this.playerInvY, this.playerInvWidth, this.playerInvHeight, guiGraphics);
      guiGraphics.m_280488_(this.f_96547_, "Player Inventory", this.playerInvX + 18, this.playerInvY + 4, -1);
   }

   private void renderOfferPanel(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      this.renderBgTexture(TEXTURE_OFFERS_PANEL, this.offersOffsetX, this.offersOffsetY, 264, 198, guiGraphics);

      for (MechTraderEntity.TraderCategory category : MechTraderEntity.TraderCategory.values()) {
         int col = category.ordinal();
         drawCenteredStringNoShadow(
            guiGraphics, this.f_96547_, this.getCategoryString(category), this.offersOffsetX + 5 + col * 65 + 31, this.offersOffsetY + 4, -1
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
      Slot offer = ((MechTraderMenu)this.f_97732_).m_38853_(offerSlot);
      Slot price = ((MechTraderMenu)this.f_97732_).m_38853_(priceSlot);
      ItemStack offerStack = offer.m_7993_();
      ItemStack priceStack = price.m_7993_();
      if (!offerStack.m_41619_()) {
         int x = this.offersOffsetX + 5 + col * 65;
         int y = this.offersOffsetY + 17 + row * 22;
         boolean selected = this.selectedOfferSlot == offerSlot;
         if (selected) {
            this.renderBgTexture(TEXTURE_CARD_SELECTED, x, y, 58, 18, gg);
         } else {
            this.renderBgTexture(TEXTURE_CARD, x, y, 58, 18, gg);
         }

         drawCenteredStringNoShadow(gg, this.f_96547_, "=", x + 30, y + 6, -1);
      }
   }

   public boolean m_6375_(double mouseX, double mouseY, int button) {
      for (MechTraderEntity.TraderCategory category : MechTraderEntity.TraderCategory.values()) {
         int col = category.ordinal();

         for (int row = 0; row < 8; row++) {
            int offerSlot = TraderInventory.getOfferSlot(category, row);
            int x = this.offersOffsetX + 5 + col * 65;
            int y = this.offersOffsetY + 17 + row * 22;
            if (this.isPointInside(x, y, 58, 18, mouseX, mouseY)) {
               if (!((MechTraderMenu)this.f_97732_).m_38853_(offerSlot).m_7993_().m_41619_()) {
                  this.selectedOfferSlot = offerSlot;
               }

               return true;
            }
         }
      }

      return super.m_6375_(mouseX, mouseY, button);
   }

   private boolean isPointInside(int x, int y, int width, int height, double mouseX, double mouseY) {
      return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
   }

   private void buySelected() {
      if (this.selectedOfferSlot >= 0) {
         if (!((MechTraderMenu)this.f_97732_).m_38853_(this.selectedOfferSlot).m_7993_().m_150930_((Item)PomkotsMechs.SOLD_ITEM.get())) {
            this.f_96541_.f_91072_.m_105208_(((MechTraderMenu)this.f_97732_).f_38840_, this.selectedOfferSlot);
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
      guiGraphics.m_280163_(texture, x, y, 0.0F, 0.0F, width, height, width, height);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }

   protected void m_280003_(GuiGraphics guiGraphics, int mouseX, int mouseY) {
   }

   protected void m_280072_(GuiGraphics guiGraphics, int i, int j) {
      if (((MechTraderMenu)this.f_97732_).m_142621_().m_41619_()
         && this.f_97734_ != null
         && this.f_97734_.m_6657_()
         && !(this.f_97734_.m_7993_().m_41720_() instanceof AbstractPomCoinItem)) {
         super.m_280072_(guiGraphics, i, j);
      }
   }

   public static void drawCenteredStringNoShadow(GuiGraphics guiGraphics, Font font, String text, int centerX, int y, int color) {
      int width = font.m_92895_(text);
      guiGraphics.m_280056_(font, text, centerX - width / 2, y, color, false);
   }

   public String getRemainingTimeString() {
      if (this.trader != null && this.trader.getDespawnTime() >= 0L) {
         long despawnDayTime = this.trader.getDespawnTime();
         long currentDayTime = this.trader.m_9236_().m_46468_();
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
