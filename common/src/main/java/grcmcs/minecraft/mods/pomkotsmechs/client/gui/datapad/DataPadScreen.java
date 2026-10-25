package grcmcs.minecraft.mods.pomkotsmechs.client.gui.datapad;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.architectury.platform.Platform;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.ImageButton;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class DataPadScreen extends AbstractContainerScreen<DataPadMenu> {
   private static final ResourceLocation TEXTURE_BUTTON = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/button_large.png");
   private static final int GUI_WIDTH = 480;
   private static final int GUI_HEIGHT = 270;
   private static final int BUTTON_WIDTH = 140;
   private static final int BUTTON_HEIGHT = 28;
   private int menuButtonsOffsetX = 0;
   private int menuButtonsOffsetY = 0;
   private ImageButton btnMech;
   private ImageButton btnMechKeycards;
   private ImageButton btnArena;
   private ImageButton btnRadar;
   private ImageButton btnQuests;
   private ImageButton btnTeams;
   private boolean isSlotInited = false;

   public DataPadScreen(DataPadMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.f_97726_ = 480;
      this.f_97727_ = 270;
   }

   protected void m_7856_() {
      super.m_7856_();
      this.menuButtonsOffsetX = this.f_97735_ + 100;
      this.menuButtonsOffsetY = this.f_97736_ + 75;
      this.btnMech = (ImageButton)this.m_142416_(
         this.buildMenuButton(
            "Mech",
            this.menuButtonsOffsetX,
            this.menuButtonsOffsetY,
            btn -> this.btnClicked(0),
            ((DataPadMenu)this.f_97732_).player != null && ((DataPadMenu)this.f_97732_).player.m_20202_() instanceof Pmvc01Entity
         )
      );
      this.btnMechKeycards = (ImageButton)this.m_142416_(
         this.buildMenuButton("Mech Key Cards", this.menuButtonsOffsetX + 140 + 20, this.menuButtonsOffsetY, btn -> this.btnClicked(1), true)
      );
      this.btnArena = (ImageButton)this.m_142416_(
         this.buildMenuButton("Arena", this.menuButtonsOffsetX, this.menuButtonsOffsetY + 38, btn -> this.btnClicked(2), true)
      );
      this.btnRadar = (ImageButton)this.m_142416_(
         this.buildMenuButton("Radar", this.menuButtonsOffsetX + 140 + 20, this.menuButtonsOffsetY + 38, btn -> this.btnClicked(3), true)
      );
      this.btnQuests = (ImageButton)this.m_142416_(
         this.buildMenuButton("Quests", this.menuButtonsOffsetX, this.menuButtonsOffsetY + 76 + 10, btn -> this.btnClicked(4), this.isFtbQuestLoaded())
      );
      this.btnTeams = (ImageButton)this.m_142416_(
         this.buildMenuButton(
            "Teams", this.menuButtonsOffsetX + 140 + 20, this.menuButtonsOffsetY + 76 + 10, btn -> this.btnClicked(5), this.isFtbTeamsLoaded()
         )
      );
   }

   private boolean isFtbQuestLoaded() {
      return Platform.isModLoaded("ftbquests");
   }

   private boolean isFtbTeamsLoaded() {
      return false;
   }

   private ImageButton buildMenuButton(String text, int x, int y, OnPress onPress, boolean active) {
      ImageButton b = new ImageButton(x, y, 140, 28, TEXTURE_BUTTON, 0, 0, 140, 112, Component.m_237113_(text), onPress);
      b.f_93623_ = active;
      return b;
   }

   private void btnClicked(int btnId) {
      this.f_96541_.f_91072_.m_105208_(((DataPadMenu)this.f_97732_).f_38840_, btnId);
   }

   public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.m_280273_(graphics);
      if (!this.isSlotInited
         && ((DataPadMenu)this.f_97732_).getDataPad() != null
         && ((DataPadMenu)this.f_97732_).getDataPad().m_41720_() instanceof PomkotsDatapadItem) {
         this.isSlotInited = true;
         if (PomkotsDatapadItem.getRegisteredArenaIds(((DataPadMenu)this.f_97732_).getDataPad()).isEmpty()) {
            this.btnArena.f_93623_ = false;
         } else {
            this.btnArena.setBadgeCount(PomkotsDatapadItem.getArenaRequests(((DataPadMenu)this.f_97732_).getDataPad()).size());
         }
      }

      super.m_88315_(graphics, mouseX, mouseY, partialTick);
      this.m_280072_(graphics, mouseX, mouseY);
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
}
