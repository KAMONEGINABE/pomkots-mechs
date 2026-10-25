package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.components.RadarTargetList;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.radar.PomkotsRadarItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.radar.RadarTarget;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class RadarTargetSelectScreen extends AbstractContainerScreen<RadarTargetSelectMenu> {
   private static final ResourceLocation TEXTURE_BG_1 = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/radar_menu.png");
   private static final ResourceLocation TEXTURE_BUTTON = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/button_large.png");
   private static final int GUI_WIDTH = 480;
   private static final int GUI_HEIGHT = 270;
   private static final int BUTTON_WIDTH = 140;
   private static final int BUTTON_HEIGHT = 28;
   private int buttonOffsetX = 0;
   private int buttonOffsetY = 0;
   private RadarTargetList targetList;

   public RadarTargetSelectScreen(RadarTargetSelectMenu menu, Inventory inv, Component title) {
      super(menu, inv, title);
      this.imageWidth = 480;
      this.imageHeight = 270;
   }

   protected void init() {
      super.init();
      this.buttonOffsetX = 170;
      this.buttonOffsetY = 50;
      this.targetList = new RadarTargetList(this, this.minecraft);
      this.addRenderableWidget(this.targetList);
      this.targetList.addTargets((List<RadarTarget>)this.loadTargets().getSecond());
   }

   private Pair<Integer, List<RadarTarget>> loadTargets() {
      List<RadarTarget> targets = null;
      int selectedIdx = 0;
      ItemStack radarStack = ((RadarTargetSelectMenu)this.menu).getRadarStack();
      if (radarStack.getItem() instanceof PomkotsRadarItem) {
         targets = PomkotsRadarItem.getTargets(radarStack);
         selectedIdx = PomkotsRadarItem.getSelectedIndex(radarStack);
      } else if (radarStack.getItem() instanceof PomkotsDatapadItem) {
         targets = PomkotsDatapadItem.getTargets(radarStack);
         selectedIdx = PomkotsDatapadItem.getSelectedIndex(radarStack);
      } else {
         targets = new ArrayList<>();
      }

      return new Pair(selectedIdx, targets);
   }

   public void onTargetSelected(int idx) {
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
      buf.writeInt(idx);
      NetworkManager.sendToServer(PomkotsMechs.id("radar_select_target"), buf);
   }

   public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      this.renderBackground(graphics);
      this.renderBgTexture(TEXTURE_BG_1, this.leftPos, this.topPos, this.leftPos + 480, this.topPos + 270, graphics);
      graphics.drawCenteredString(this.font, "Select Radar Target", this.leftPos + this.imageWidth / 2, this.buttonOffsetY - 10, 16777215);
      super.render(graphics, mouseX, mouseY, partialTick);
      this.renderTooltip(graphics, mouseX, mouseY);
   }

   protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
   }

   protected void renderLabels(GuiGraphics gui, int mouseX, int mouseY) {
   }

   private ImageButton buildButton(String text, int x, int y, OnPress onPress) {
      return this.buildButton(text, x, y, onPress, true);
   }

   private ImageButton buildButton(String text, int x, int y, OnPress onPress, boolean active) {
      ImageButton b = new ImageButton(x, y, 140, 28, TEXTURE_BUTTON, 0, 0, 140, 112, Component.literal(text), onPress);
      b.active = active;
      return b;
   }

   private void renderBgTexture(ResourceLocation texture, int x, int y, int width, int height, GuiGraphics guiGraphics) {
      this.renderBgTexture(texture, x, y, width, height, 0.3F, guiGraphics);
   }

   private void renderBgTexture(ResourceLocation texture, int x, int y, int width, int height, float alpha, GuiGraphics guiGraphics) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      RenderSystem.enableBlend();
      RenderSystem.setShaderTexture(0, texture);
      guiGraphics.blit(texture, x, y, 0.0F, 0.0F, width, height, width, height);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }
}
