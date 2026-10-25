package grcmcs.minecraft.mods.pomkotsmechs.client.gui.pilot;

import com.mojang.blaze3d.systems.RenderSystem;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.ImageButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PilotScreen extends AbstractContainerScreen<PilotMenu> {
   private static final ResourceLocation TEXTURE_BUTTON = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/button_normal.png");
   private static final ResourceLocation TEXTURE_PANEL_INV_PLAYER = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/inventory_player.png");
   private static final ResourceLocation TEXTURE_PANEL_INV_MOB = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/inventory_mob.png");
   private static final ResourceLocation TEXTURE_ITEM_CONTAINER = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/item_container.png");
   private static final int GUI_WIDTH = 480;
   private static final int GUI_HEIGHT = 270;
   private static final int BUTTON_WIDTH = 70;
   private static final int BUTTON_HEIGHT = 14;
   private static final int OFFER_WIDTH = 164;
   private static final int OFFER_HEIGHT = 120;
   private int offersOffsetX = 0;
   private int offersOffsetY = 0;
   private int playerInvX = 0;
   private int playerInvY = 0;
   private int playerInvWidth = 164;
   private int playerInvHeight = 94;

   public PilotScreen(PilotMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.f_97726_ = 480;
      this.f_97727_ = 270;
   }

   protected void m_7856_() {
      super.m_7856_();
      this.offersOffsetX = this.f_97735_ + 60;
      this.offersOffsetY = this.f_97736_ + 35;
      this.playerInvX = this.f_97735_ + 260;
      this.playerInvY = this.f_97736_ + 35;
      this.m_142416_(
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
            Component.m_237113_("Ride Mech"),
            btn -> this.rideMech()
         )
      );
   }

   private void rideMech() {
      this.f_96541_.f_91072_.m_105208_(((PilotMenu)this.f_97732_).f_38840_, 0);
   }

   public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.m_280273_(graphics);
      super.m_88315_(graphics, mouseX, mouseY, partialTick);
      this.m_280072_(graphics, mouseX, mouseY);
   }

   protected void m_7286_(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      this.renderOfferPanel(guiGraphics, partialTick, mouseX, mouseY);
      this.renderPlayerInventoryPanel(guiGraphics);
   }

   private void renderOfferPanel(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      this.renderBgTexture(TEXTURE_PANEL_INV_MOB, this.offersOffsetX, this.offersOffsetY, 164, 120, guiGraphics);
      guiGraphics.m_280488_(this.f_96547_, "Mob Equipments", this.offersOffsetX + 18, this.offersOffsetY + 4, -1);
      int offsetX = this.offersOffsetX + 10;
      int offsetY = this.offersOffsetY + 15;
      this.renderMobEquipment("Pilot License", offsetX, offsetY, guiGraphics);
      offsetY += 18;
      this.renderMobEquipment("Pilot Role", offsetX, offsetY, guiGraphics);
      offsetY += 18;
      this.renderMobEquipment("Head", offsetX, offsetY, guiGraphics);
      offsetY += 18;
      this.renderMobEquipment("Chest", offsetX, offsetY, guiGraphics);
      offsetY += 18;
      this.renderMobEquipment("Legs", offsetX, offsetY, guiGraphics);
      offsetY += 18;
      this.renderMobEquipment("Feet", offsetX, offsetY, guiGraphics);
   }

   private void renderMobEquipment(String text, int x, int y, GuiGraphics guiGraphics) {
      guiGraphics.m_280488_(this.f_96547_, text, x + 24, y + 5, -1);
      this.renderBgTexture(TEXTURE_ITEM_CONTAINER, x, y, 16, 16, guiGraphics);
   }

   private void renderPlayerInventoryPanel(GuiGraphics guiGraphics) {
      this.renderBgTexture(TEXTURE_PANEL_INV_PLAYER, this.playerInvX, this.playerInvY, this.playerInvWidth, this.playerInvHeight, guiGraphics);
      guiGraphics.m_280488_(this.f_96547_, "Player Inventory", this.playerInvX + 18, this.playerInvY + 4, -1);
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
}
