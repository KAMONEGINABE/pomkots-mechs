package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.components.SummonMechList;
import grcmcs.minecraft.mods.pomkotsmechs.items.KeycardItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.datapad.PomkotsDatapadItem;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class MechSalvagerScreen extends AbstractContainerScreen<MechSalvagerMenu> {
   private static final ResourceLocation TEXTURE_PANEL_INV_PLAYER = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/inventory_player.png");
   private static final ResourceLocation TEXTURE_MECH_SELECTOR = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/mech_selector.png");
   private static final ResourceLocation TEXTURE_BUTTON = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/button_normal.png");
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
   private boolean isSlotInited = false;
   private SummonMechList targetList;
   private UUID mechUUID = null;
   private Button summonButton;

   public MechSalvagerScreen(MechSalvagerMenu menu, Inventory inv, Component title) {
      super(menu, inv, title);
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
      this.targetList = new SummonMechList(this, this.f_96541_);
      this.m_142416_(this.targetList);
      this.summonButton = (Button)this.m_142416_(
         this.buildButton("Summon", this.rightCardOffsetX, this.rightCardOffsetY + 130, btn -> this.sendSummonPacket2Server())
      );
   }

   public void setSelected(UUID slot) {
      this.mechUUID = slot;
   }

   private void sendSummonPacket2Server() {
      if (this.mechUUID != null) {
         FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
         buf.m_130077_(this.mechUUID);
         NetworkManager.sendToServer(PomkotsMechs.id("smm"), buf);
      }
   }

   public void m_88315_(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      if (!this.isSlotInited
         && ((MechSalvagerMenu)this.f_97732_).getDataPad() != null
         && ((MechSalvagerMenu)this.f_97732_).getDataPad().m_41720_() instanceof PomkotsDatapadItem) {
         this.isSlotInited = true;
         ItemStack dataPad = ((MechSalvagerMenu)this.f_97732_).getDataPad();
         if (dataPad.m_41720_() instanceof PomkotsDatapadItem) {
            List<Pair<UUID, String>> targets = new ArrayList<>();

            for (ItemStack keyCardSlot : PomkotsDatapadItem.createKeyCardContainer(dataPad).f_19147_) {
               if (keyCardSlot.m_41720_() instanceof KeycardItem && KeycardItem.getMechUuid(keyCardSlot) != null) {
                  targets.add(new Pair(KeycardItem.getMechUuid(keyCardSlot), KeycardItem.getMechName(keyCardSlot)));
               }
            }

            this.targetList.addTargets(targets);
         }
      }

      this.summonButton.f_93623_ = this.mechUUID != null;
      this.m_280273_(graphics);
      super.m_88315_(graphics, mouseX, mouseY, partialTick);
      this.m_280072_(graphics, mouseX, mouseY);
   }

   protected void m_7286_(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      this.renderBgTexture(TEXTURE_MECH_SELECTOR, this.leftCardOffsetX, this.leftCardOffsetY, 164, 170, guiGraphics);
      guiGraphics.m_280488_(this.f_96547_, "Select a mech to summon", this.leftCardOffsetX + 18, this.leftCardOffsetY + 4, -1);
      if (((MechSalvagerMenu)this.f_97732_).getDataPad().m_41619_()) {
         guiGraphics.m_280488_(this.f_96547_, "No data pad in your inventory", this.leftCardOffsetX + 10, this.leftCardOffsetY + 70, -56798);
      } else if (this.targetList.getSize() == 0) {
         guiGraphics.m_280488_(this.f_96547_, "No key card in your data pad", this.leftCardOffsetX + 10, this.leftCardOffsetY + 70, -56798);
      }

      this.renderBgTexture(TEXTURE_PANEL_INV_PLAYER, this.rightCardOffsetX, this.leftCardOffsetY, 164, 94, guiGraphics);
      guiGraphics.m_280488_(this.f_96547_, "Player Inventory", this.rightCardOffsetX + 18, this.leftCardOffsetY + 4, -1);
   }

   protected void m_280003_(GuiGraphics gui, int mouseX, int mouseY) {
   }

   private ImageButton buildButton(String text, int x, int y, OnPress onPress) {
      return this.buildButton(text, x, y, onPress, true);
   }

   private ImageButton buildButton(String text, int x, int y, OnPress onPress, boolean active) {
      ImageButton b = new ImageButton(x, y, 70, 14, TEXTURE_BUTTON, 0, 0, 70, 56, Component.m_237113_(text), onPress);
      b.f_93623_ = active;
      return b;
   }

   private void renderBgTexture(ResourceLocation texture, int x, int y, int width, int height, GuiGraphics guiGraphics) {
      this.renderBgTexture(texture, x, y, width, height, 0.3F, guiGraphics);
   }

   private void renderBgTexture(ResourceLocation texture, int x, int y, int width, int height, float alpha, GuiGraphics guiGraphics) {
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
      RenderSystem.enableBlend();
      RenderSystem.setShaderTexture(0, texture);
      guiGraphics.m_280163_(texture, x, y, 0.0F, 0.0F, width, height, width, height);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
   }
}
