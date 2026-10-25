package grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad;

import com.mojang.blaze3d.systems.RenderSystem;
import grcmcs.minecraft.mods.pomkotsmechs.items.KeycardItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DataPadKeyCardScreen extends AbstractContainerScreen<DataPadKeyCardMenu> {
   private static final ResourceLocation TEXTURE_PANEL_INV_PLAYER = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/inventory_player.png");
   private static final ResourceLocation TEXTURE_ITEM_CONTAINER_KEYCARD = new ResourceLocation(
      "pomkotsmechs", "textures/gui/mechworkbench/inventory_mech_keycard.png"
   );
   private static final int GUI_WIDTH = 480;
   private static final int GUI_HEIGHT = 270;
   private int leftCardOffsetX = 0;
   private int leftCardOffsetY = 0;
   private int rightCardOffsetX = 0;
   private int rightCardOffsetY = 0;
   private static final int CARD_WIDTH = 164;
   private static final int CARD_HEIGHT = 130;

   public DataPadKeyCardScreen(DataPadKeyCardMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.f_97726_ = 480;
      this.f_97727_ = 270;
   }

   protected void m_7856_() {
      super.m_7856_();
      int offsetX = 20;
      int offsetY = 40;
      this.leftCardOffsetX = offsetX + 28;
      this.leftCardOffsetY = offsetY;
      this.rightCardOffsetX = offsetX + 28 + 164 + 28 + 28;
      this.rightCardOffsetY = offsetY;
   }

   public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.m_280273_(graphics);
      super.m_88315_(graphics, mouseX, mouseY, partialTick);
      this.m_280072_(graphics, mouseX, mouseY);
   }

   protected void m_7286_(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      this.renderBgTexture(TEXTURE_ITEM_CONTAINER_KEYCARD, this.leftCardOffsetX, this.leftCardOffsetY, 164, 170, guiGraphics);
      guiGraphics.m_280488_(this.f_96547_, "Put mech Key Cards here", this.leftCardOffsetX + 18, this.leftCardOffsetY + 4, -1);

      for (int i = 0; i < 8 && i < ((DataPadKeyCardMenu)this.f_97732_).f_38839_.size(); i++) {
         if (((Slot)((DataPadKeyCardMenu)this.f_97732_).f_38839_.get(i)).m_7993_().m_41720_() instanceof KeycardItem) {
            ItemStack stack = ((Slot)((DataPadKeyCardMenu)this.f_97732_).f_38839_.get(i)).m_7993_();
            String name = KeycardItem.getMechName(stack);
            if (!name.isEmpty()) {
               guiGraphics.m_280488_(this.f_96547_, name, this.leftCardOffsetX + 36, this.leftCardOffsetY + 30 + i * 18 - 4, -1);
            }
         }
      }

      this.renderBgTexture(TEXTURE_PANEL_INV_PLAYER, this.rightCardOffsetX, this.leftCardOffsetY, 164, 94, guiGraphics);
      guiGraphics.m_280488_(this.f_96547_, "Player Inventory", this.rightCardOffsetX + 18, this.leftCardOffsetY + 4, -1);
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
}
