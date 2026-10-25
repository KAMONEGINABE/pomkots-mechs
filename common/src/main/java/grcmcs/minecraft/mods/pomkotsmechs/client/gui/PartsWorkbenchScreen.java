package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Axis;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.registries.RegistrySupplier;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPack;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.PomkotsDataPackManager;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

public class PartsWorkbenchScreen extends AbstractContainerScreen<PartsWorkbenchMenu> {
   private static final ResourceLocation TEXTURE_PROD = new ResourceLocation("pomkotsmechs", "textures/gui/partsworkbench_bg.png");
   private static final ResourceLocation TEXTURE_ENH = new ResourceLocation("pomkotsmechs", "textures/gui/partsworkbench_bg_upgrade.png");
   private PartsWorkbenchMenu.Tab currentTab;
   private final List<PartsWorkbenchScreen.PartEntry> craftablePartsList = new ArrayList<>();
   private int scrollOffset = 0;
   private int selectedIndex = -1;
   private final List<PartsWorkbenchScreen.Material> requiredMaterials = new ArrayList<>();
   private ItemStack previewItem = ItemStack.EMPTY;
   private int previewX = 9;
   private int previewY = 45;
   private int previewSize = 64;
   private final int previewSectionWidth;
   private final int materialSectionWidth;
   private final int partsSelectSectionWidth;
   private final int inventorySectionWidth;
   private Button craftButton;
   private Button upgradeButton;
   private ItemStack prevUpgradeTarget = ItemStack.EMPTY;
   private ItemStack upgradeTarget = ItemStack.EMPTY;
   private List<ItemStack> upgradeTargetLevels = new ArrayList<>();
   private static final int ENTRY_SIZE = 24;
   private static final int ENTRIES_PER_ROW = 4;
   private static int LEVEL_ROW_WIDTH = 110;
   private static int LEVEL_ROW_HEIGHT = 20;

   public PartsWorkbenchScreen(PartsWorkbenchMenu menu, Inventory inv, Component title) {
      super(menu, inv, title);
      this.imageWidth = 400;
      this.imageHeight = 180;
      this.previewSectionWidth = 80;
      this.materialSectionWidth = 80;
      this.partsSelectSectionWidth = 120;
      this.inventorySectionWidth = this.imageWidth - (this.previewSectionWidth + this.materialSectionWidth + this.partsSelectSectionWidth);
      this.titleLabelX = 90;
      this.inventoryLabelY = 60;
   }

   protected void init() {
      super.init();
      this.currentTab = ((PartsWorkbenchMenu)this.menu).getCurrentTab();
      this.craftablePartsList.clear();
      this.registerAllParts();
      this.craftButton = (Button)this.addRenderableWidget(
         Button.builder(Utils.string2Component("{text.pomkotsmechs.gui.partsworkbench.btn.craft}"), this::onCraftButtonPressed)
            .bounds(this.leftPos + this.previewX, this.topPos + 120, 70, 14)
            .build()
      );
      this.upgradeButton = (Button)this.addRenderableWidget(
         Button.builder(Utils.string2Component("{text.pomkotsmechs.gui.partsworkbench.btn.upgrade}"), this::onUpgradeButtonPressed)
            .bounds(this.leftPos + this.previewX, this.topPos + 120, 70, 14)
            .build()
      );
      this.setupButtonVisibility(this.currentTab);
   }

   private void registerAllParts() {
      this.registerParts(PomkotsMechs.ALTAIR_HEAD);
      this.registerParts(PomkotsMechs.ALTAIR_BODY);
      this.registerParts(PomkotsMechs.ALTAIR_ARM);
      this.registerParts(PomkotsMechs.ALTAIR_LEGS);
      this.registerParts(PomkotsMechs.DENEB_HEAD);
      this.registerParts(PomkotsMechs.DENEB_BODY);
      this.registerParts(PomkotsMechs.DENEB_ARM);
      this.registerParts(PomkotsMechs.DENEB_LEGS);
      this.registerParts(PomkotsMechs.VEGA_HEAD);
      this.registerParts(PomkotsMechs.VEGA_BODY);
      this.registerParts(PomkotsMechs.VEGA_ARM);
      this.registerParts(PomkotsMechs.VEGA_LEGS);
      this.registerParts(PomkotsMechs.SIRIUS_HEAD);
      this.registerParts(PomkotsMechs.SIRIUS_BODY);
      this.registerParts(PomkotsMechs.SIRIUS_ARM);
      this.registerParts(PomkotsMechs.SIRIUS_LEGS);
      this.registerParts(PomkotsMechs.ALDEBARAN_HEAD);
      this.registerParts(PomkotsMechs.ALDEBARAN_BODY);
      this.registerParts(PomkotsMechs.ALDEBARAN_ARM);
      this.registerParts(PomkotsMechs.ALDEBARAN_LEGS);
      this.registerParts(PomkotsMechs.MUKNVALI_HEAD);
      this.registerParts(PomkotsMechs.MUKNVALI_BODY);
      this.registerParts(PomkotsMechs.MUKNVALI_ARM);
      this.registerParts(PomkotsMechs.MUKNVALI_LEGS);
      this.registerParts(PomkotsMechs.SHAKUJI_WEAPON);
      this.registerParts(PomkotsMechs.SHINOBAZU_WEAPON);
      this.registerParts(PomkotsMechs.SENZOKU_WEAPON);
      this.registerParts(PomkotsMechs.KASUMI_WEAPON);
      this.registerParts(PomkotsMechs.KAGAMI_WEAPON);
      this.registerParts(PomkotsMechs.UGUISU_WEAPON);
      this.registerParts(PomkotsMechs.MASHU_WEAPON);
      this.registerParts(PomkotsMechs.TENPOU_WEAPON);
      this.registerParts(PomkotsMechs.TSURUGI_WEAPON);
      this.registerParts(PomkotsMechs.MITAKE_WEAPON);
      this.registerParts(PomkotsMechs.KAGENOBU_WEAPON);
      this.registerParts(PomkotsMechs.TAKAO_WEAPON);
      this.registerParts(PomkotsMechs.JINBA_WEAPON);
      this.registerParts(PomkotsMechs.AMAGI_WEAPON);
      this.registerParts(PomkotsMechs.DAIGOMARU_WEAPON);
      this.registerParts(PomkotsMechs.SHOUTOU_WEAPON);
      this.registerParts(PomkotsMechs.WADA_WEAPON);
      this.registerParts(PomkotsMechs.BIWA_WEAPON);
      this.registerParts(PomkotsMechs.SUWA_WEAPON);
      this.registerParts(PomkotsMechs.KAWASEMI_WEAPON);
      this.registerParts(PomkotsMechs.NOSURI_WEAPON);
      this.registerParts(PomkotsMechs.TSUBAME_WEAPON);
      this.registerParts(PomkotsMechs.MUKUDORI_WEAPON);
      this.registerParts(PomkotsMechs.DODO_WEAPON);
      this.registerParts(PomkotsMechs.SAGA_GENERATOR);
      this.registerParts(PomkotsMechs.SHIGA_GENERATOR);
      this.registerParts(PomkotsMechs.CHIBA_GENERATOR);
      this.registerParts(PomkotsMechs.KANSAI_BOOSTER);
      this.registerParts(PomkotsMechs.NARITA_BOOSTER);
      this.registerParts(PomkotsMechs.HANEDA_BOOSTER);
      this.registerParts(PomkotsMechs.SB_PROTO);
      this.registerParts(PomkotsMechs.BUILDER_UNIT);
      this.registerParts(PomkotsMechs.SOFT_LOCK_CIRCUIT);
      this.registerParts(PomkotsMechs.HARD_LOCK_CIRCUIT);
      this.registerParts(PomkotsMechs.HOVER_UNIT);
      this.registerParts(PomkotsMechs.RAIL_SLIDER);
      this.registerParts(PomkotsMechs.GLIDER_UNIT);
   }

   private void registerParts(RegistrySupplier<Item> partsSupplier) {
      PomkotsDataPack dataPack = PomkotsDataPackManager.getInstance().getDataPack();
      PomkotsDataPack.PartsData partsData = dataPack.getPartsData(partsSupplier.getId().getPath());
      if (partsData != null) {
         Minecraft mc = Minecraft.getInstance();
         boolean isCreative = mc.player.isCreative();
         boolean hasBluePrint = this.hasBluePrint(mc.player.getInventory(), partsSupplier.getId());
         if (isCreative || hasBluePrint) {
            this.craftablePartsList.add(new PartsWorkbenchScreen.PartEntry(partsSupplier.getId(), partsData, new ItemStack((ItemLike)partsSupplier.get())));
         }
      }
   }

   private boolean hasBluePrint(Inventory inventory, ResourceLocation partsId) {
      String partsName = partsId.getPath();
      if (partsName.endsWith("head") || partsName.endsWith("body") || partsName.endsWith("arm") || partsName.endsWith("legs")) {
         partsName = partsName.replaceAll("head", "").replaceAll("body", "").replaceAll("arm", "").replaceAll("legs", "");
      }

      ResourceLocation bluePrintId = this.getBluePrintId(partsId.getNamespace(), partsName);
      Item bluePrint = (Item)BuiltInRegistries.ITEM.get(bluePrintId);
      return bluePrint == Items.AIR ? false : inventory.countItem(bluePrint) > 0;
   }

   private ResourceLocation getBluePrintId(String nameSpace, String partsName) {
      return switch (partsName) {
         case "hoverunit" -> new ResourceLocation(nameSpace, "blue_print_hover_unit");
         case "railslider" -> new ResourceLocation(nameSpace, "blue_print_rail_slider_unit");
         case "protosbunit" -> new ResourceLocation(nameSpace, "blue_print_proto_super_boost_unit");
         case "builderunit" -> new ResourceLocation(nameSpace, "blue_print_builder_unit");
         case "gliderunit" -> new ResourceLocation(nameSpace, "blue_print_glider_unit");
         default -> new ResourceLocation(nameSpace, "blue_print_" + partsName);
      };
   }

   private void onCraftButtonPressed(Button btn) {
      if (!((PartsWorkbenchMenu)this.menu).getSlot(0).getItem().isEmpty()) {
         this.displayMessage("{text.pomkotsmechs.messages.partsworkbench.01}");
      } else if (this.previewItem.isEmpty()) {
         this.displayMessage("{text.pomkotsmechs.messages.partsworkbench.08}");
      } else if (this.previewItem.getItem() instanceof BasePartsItem parts) {
         PomkotsDataPack.PartsData partsData = PartsWorkbenchMenu.getPartsData(parts);
         if (partsData == null || partsData.recipes.isEmpty() || partsData.recipes.get(0).isEmpty()) {
            this.displayMessage("{text.pomkotsmechs.messages.partsworkbench.02}");
            return;
         }

         if (PartsWorkbenchMenu.isCraftable(Minecraft.getInstance().player.getInventory(), partsData.recipes.get(0))) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeId(BuiltInRegistries.ITEM, parts);
            NetworkManager.sendToServer(PomkotsMechs.id("pwc"), buf);
         } else {
            this.displayMessage("{text.pomkotsmechs.messages.partsworkbench.03}");
         }
      } else {
         this.displayMessage("{text.pomkotsmechs.messages.partsworkbench.09}");
      }
   }

   private void onUpgradeButtonPressed(Button btn) {
      ItemStack inputItemStack = ((PartsWorkbenchMenu)this.menu).getSlot(1).getItem();
      if (inputItemStack.isEmpty()) {
         this.displayMessage("{text.pomkotsmechs.messages.partsworkbench.10}");
      } else if (inputItemStack.getItem() instanceof BasePartsItem parts) {
         PomkotsDataPack.PartsData partsData = PartsWorkbenchMenu.getPartsData(parts);
         int curLevel = parts.getLevel(inputItemStack);
         if (partsData == null) {
            this.displayMessage("{text.pomkotsmechs.messages.partsworkbench.05}");
            return;
         }

         if (parts.getMaxLevel() <= curLevel) {
            this.displayMessage("{text.pomkotsmechs.messages.partsworkbench.06}");
            return;
         }

         if (partsData.recipes.isEmpty() || partsData.recipes.size() < curLevel || partsData.recipes.get(curLevel).isEmpty()) {
            this.displayMessage("{text.pomkotsmechs.messages.partsworkbench.02}");
            return;
         }

         if (PartsWorkbenchMenu.isCraftable(Minecraft.getInstance().player.getInventory(), partsData.recipes.get(curLevel))) {
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
            buf.writeId(BuiltInRegistries.ITEM, parts);
            NetworkManager.sendToServer(PomkotsMechs.id("pwu"), buf);
         } else {
            this.displayMessage("{text.pomkotsmechs.messages.partsworkbench.03}");
         }
      } else {
         this.displayMessage("{text.pomkotsmechs.messages.partsworkbench.07}");
      }
   }

   public void render(GuiGraphics guiGraphics, int i, int j, float f) {
      super.render(guiGraphics, i, j, f);
      this.renderTooltip(guiGraphics, i, j);
      if (PartsWorkbenchMenu.Tab.UPGRADE == this.currentTab) {
         this.prevUpgradeTarget = this.upgradeTarget;
         this.upgradeTarget = ((PartsWorkbenchMenu)this.menu).getSlot(1).getItem();
         if (this.prevUpgradeTarget != this.upgradeTarget) {
            this.setupUpgradeTarget(this.upgradeTarget);
         }
      }
   }

   protected void renderBg(GuiGraphics gui, float partialTick, int mouseX, int mouseY) {
      if (this.currentTab == PartsWorkbenchMenu.Tab.CRAFT) {
         this.renderBgTexture(TEXTURE_PROD, gui);
         int left = this.leftPos;
         int top = this.topPos;
         gui.fill(left + this.previewSectionWidth, top + 35, left + this.previewSectionWidth + 1, top + this.imageHeight - 22, -10461088);
         gui.fill(
            left + this.previewSectionWidth + this.materialSectionWidth,
            top + 35,
            left + this.previewSectionWidth + this.materialSectionWidth + 1,
            top + this.imageHeight - 22,
            -10461088
         );
         gui.fill(
            left + this.previewSectionWidth + this.materialSectionWidth + this.partsSelectSectionWidth,
            top + 35,
            left + this.previewSectionWidth + this.materialSectionWidth + this.partsSelectSectionWidth + 1,
            top + this.imageHeight - 22,
            -10461088
         );
         this.renderPreview(gui, this.previewItem, left, top, this.previewSectionWidth, this.previewSize, partialTick);
         this.renderMaterialSection(gui, left + this.previewSectionWidth + 4, top, this.materialSectionWidth - 8, mouseX, mouseY);
         this.renderPartList(gui, left + this.previewSectionWidth + this.materialSectionWidth + 4, top, this.partsSelectSectionWidth - 8, mouseX, mouseY);
         this.renderInventory(
            gui,
            left + this.previewSectionWidth + this.materialSectionWidth + this.partsSelectSectionWidth + 4,
            top,
            this.inventorySectionWidth - 8,
            mouseX,
            mouseY
         );
      } else {
         this.renderBgTexture(TEXTURE_ENH, gui);
         int left = this.leftPos;
         int top = this.topPos;
         gui.fill(left + this.previewSectionWidth, top + 35, left + this.previewSectionWidth + 1, top + this.imageHeight - 22, -10461088);
         gui.fill(
            left + this.previewSectionWidth + this.materialSectionWidth,
            top + 35,
            left + this.previewSectionWidth + this.materialSectionWidth + 1,
            top + this.imageHeight - 22,
            -10461088
         );
         gui.fill(
            left + this.previewSectionWidth + this.materialSectionWidth + this.partsSelectSectionWidth,
            top + 35,
            left + this.previewSectionWidth + this.materialSectionWidth + this.partsSelectSectionWidth + 1,
            top + this.imageHeight - 22,
            -10461088
         );
         this.renderPreview(gui, this.previewItem, left, top, this.previewSectionWidth, this.previewSize, partialTick);
         this.renderMaterialSection(gui, left + this.previewSectionWidth + 4, top, this.materialSectionWidth - 8, mouseX, mouseY);
         this.renderPartsLevels(gui, left + this.previewSectionWidth + this.materialSectionWidth + 4, top, this.partsSelectSectionWidth - 8, mouseX, mouseY);
         this.renderInventory(
            gui,
            left + this.previewSectionWidth + this.materialSectionWidth + this.partsSelectSectionWidth + 4,
            top,
            this.inventorySectionWidth - 8,
            mouseX,
            mouseY
         );
      }
   }

   private void renderBgTexture(ResourceLocation texture, GuiGraphics gfx) {
      RenderSystem.setShaderTexture(0, texture);
      int x = this.leftPos;
      int y = this.topPos;
      gfx.blit(texture, x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, this.imageWidth, 256);
   }

   protected void renderLabels(GuiGraphics guiGraphics, int i, int j) {
      guiGraphics.drawCenteredString(this.font, "Parts Workbench: Level 1", this.imageWidth / 2, this.titleLabelY, 16777215);
      if (this.currentTab == PartsWorkbenchMenu.Tab.CRAFT) {
         guiGraphics.drawCenteredString(this.font, this.getLocalizedString("{text.pomkotsmechs.gui.partsworkbench.tab.craft}"), 30, 18, 16777215);
         guiGraphics.drawCenteredString(this.font, this.getLocalizedString("{text.pomkotsmechs.gui.partsworkbench.tab.upgrade}"), 81, 18, 4210752);
         guiGraphics.drawString(this.font, this.getLocalizedString("{text.pomkotsmechs.gui.partsworkbench.label.output}"), 30, 145, 16777215, false);
      } else if (this.currentTab == PartsWorkbenchMenu.Tab.UPGRADE) {
         guiGraphics.drawCenteredString(this.font, this.getLocalizedString("{text.pomkotsmechs.gui.partsworkbench.tab.craft}"), 30, 18, 4210752);
         guiGraphics.drawCenteredString(this.font, this.getLocalizedString("{text.pomkotsmechs.gui.partsworkbench.tab.upgrade}"), 81, 18, 16777215);
         guiGraphics.drawString(this.font, this.getLocalizedString("{text.pomkotsmechs.gui.partsworkbench.label.target}"), 30, 145, 16777215, false);
      }
   }

   private void renderPreview(GuiGraphics gui, ItemStack stack, int left, int top, int width, int size, float partialTicks) {
      Lighting.setupForEntityInInventory();
      gui.drawCenteredString(this.font, this.getLocalizedString("{text.pomkotsmechs.gui.partsworkbench.label.preview}"), left + width / 2, top + 33, 16777215);
      left += this.previewX;
      top += this.previewY;
      gui.fill(left, top, left + size, top + size, -12566464);
      if (!this.previewItem.isEmpty()) {
         Minecraft mc = Minecraft.getInstance();
         PoseStack pose = gui.pose();
         pose.pushPose();
         pose.translate(left + size / 2.0, top + size / 2.0 + 8.0, 150.0);
         pose.scale(1.0F, -1.0F, 1.0F);
         float scale = size / 8.0F;
         pose.scale(scale, scale, scale);
         double time = System.currentTimeMillis() % 8000L / 8000.0;
         float rotation = (float)(time * 360.0);
         pose.mulPose(Axis.YP.rotationDegrees(rotation));
         pose.mulPose(Axis.XP.rotationDegrees(-25.0F));
         mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, 15728880, 0, pose, gui.bufferSource(), mc.level, 0);
         gui.bufferSource().endBatch();
         pose.popPose();
         Lighting.setupFor3DItems();
      }
   }

   protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
      super.renderTooltip(graphics, mouseX, mouseY);
      if (PartsWorkbenchMenu.Tab.CRAFT == this.currentTab) {
         int px = this.leftPos + this.previewX;
         int py = this.topPos + this.previewY;
         if (mouseX >= px && mouseX <= px + this.previewSize && mouseY >= py && mouseY <= py + this.previewSize && !this.previewItem.isEmpty()) {
            graphics.renderTooltip(this.font, this.previewItem, mouseX, mouseY);
         }
      }
   }

   private void renderMaterialSection(GuiGraphics gui, int left, int top, int width, int mouseX, int mouseY) {
      gui.drawCenteredString(this.font, this.getLocalizedString("{text.pomkotsmechs.gui.partsworkbench.label.requirements}"), left + width / 2, top + 33, 16777215);
      if (!this.previewItem.isEmpty()) {
         int x = left + 2;
         int y = top + 45;
         int slotSize = 18;
         int index = 0;

         for (PartsWorkbenchScreen.Material mat : this.requiredMaterials) {
            Item item = (Item)mat.getFirst();
            int required = (Integer)mat.getSecond();
            int has = Minecraft.getInstance().player.getInventory().countItem(item);
            gui.fill(x - 1, y - 1, x + slotSize + 1, y + slotSize + 1, -12566464);
            ItemStack stack = new ItemStack(item);
            gui.renderItem(stack, x + 1, y + 1);
            String countText = has + "/" + required;
            int color = has >= required ? 8454016 : 16744576;
            drawScaledTextCentered(gui, this.font, Component.literal(countText), x + slotSize / 2, y + slotSize + 2, slotSize, color);
            if (mouseX >= x && mouseX <= x + slotSize && mouseY >= y && mouseY <= y + slotSize) {
               gui.renderTooltip(this.font, stack, mouseX, mouseY);
            }

            index++;
            x += slotSize + 8;
            if (index % 3 == 0) {
               x = left + 2;
               y += slotSize + 16;
            }
         }
      }
   }

   private void renderPartList(GuiGraphics gui, int left, int top, int width, int mouseX, int mouseY) {
      gui.drawCenteredString(this.font, this.getLocalizedString("{text.pomkotsmechs.gui.partsworkbench.label.selectparts}"), left + width / 2, top + 33, 16777215);
      int listTop = top + 45;
      int listHeight = this.imageHeight - 55;
      int entriesPerRow = 4;
      int visibleRows = listHeight / 26;
      int totalRows = (int)Math.ceil((float)this.craftablePartsList.size() / entriesPerRow);
      int startRow = this.scrollOffset;
      int endRow = Math.min(totalRows, startRow + visibleRows);

      for (int row = startRow; row < endRow; row++) {
         for (int col = 0; col < entriesPerRow; col++) {
            int index = row * entriesPerRow + col;
            if (index >= this.craftablePartsList.size()) {
               break;
            }

            PartsWorkbenchScreen.PartEntry entry = this.craftablePartsList.get(index);
            int x = left + col * 26;
            int y = listTop + (row - startRow) * 26;
            boolean hovered = this.isInside(mouseX, mouseY, x, y, 24, 24);
            boolean selected = index == this.selectedIndex;
            int bg = hovered ? -11513776 : (selected ? -10461024 : -13619152);
            gui.fill(x, y, x + 24, y + 24, bg);
            gui.renderItem(entry.getItemStack(), x + 4, y + 4);
         }
      }

      int barHeight = Math.max(10, (int)((float)listHeight * visibleRows / Math.max(1, totalRows)));
      int barPos = listTop + (int)((float)this.scrollOffset / Math.max(1, totalRows - visibleRows) * (listHeight - barHeight));
      gui.fill(left + width - 6, barPos, left + width - 2, barPos + barHeight, -11513776);

      for (int row = startRow; row < endRow; row++) {
         for (int col = 0; col < entriesPerRow; col++) {
            int index = row * entriesPerRow + col;
            if (index >= this.craftablePartsList.size()) {
               break;
            }

            int x = left + col * 26;
            int y = listTop + (row - startRow) * 26;
            if (this.isInside(mouseX, mouseY, x, y, 24, 24)) {
               PartsWorkbenchScreen.PartEntry entry = this.craftablePartsList.get(index);
               gui.renderTooltip(this.font, entry.getItemStack(), mouseX, mouseY);
               return;
            }
         }
      }
   }

   private void renderPartsLevels(GuiGraphics gui, int left, int top, int width, int mouseX, int mouseY) {
      gui.drawCenteredString(this.font, this.getLocalizedString("{text.pomkotsmechs.gui.partsworkbench.label.partslevels}"), left + width / 2, top + 33, 16777215);
      if (!this.upgradeTargetLevels.isEmpty() && this.upgradeTarget.getItem() instanceof BasePartsItem parts) {
         int var16 = top + 45;
         int startRow = 0;

         for (int row = startRow; row < this.upgradeTargetLevels.size(); row++) {
            int x = left;
            int y = var16 + (row - startRow) * (LEVEL_ROW_HEIGHT + 2);
            boolean isCurrent = parts.getLevel(this.upgradeTarget) - 1 == row;
            int bg = isCurrent ? -10461024 : -13619152;
            gui.fill(x, y, x + LEVEL_ROW_WIDTH, y + LEVEL_ROW_HEIGHT, bg);
            gui.drawString(this.font, "Level " + (row + 1) + (isCurrent ? ": Current" : ""), left + 24, y + 6, 16777215, false);
            ItemStack itemStack = this.upgradeTargetLevels.get(row);
            gui.renderItem(itemStack, x + 4, y + 4);
         }

         for (int row = startRow; row < this.upgradeTargetLevels.size(); row++) {
            int x = left;
            int y = var16 + (row - startRow) * (LEVEL_ROW_HEIGHT + 2);
            boolean hovered = this.isInside(mouseX, mouseY, x, y, LEVEL_ROW_WIDTH, LEVEL_ROW_HEIGHT);
            ItemStack itemStack = this.upgradeTargetLevels.get(row);
            if (hovered) {
               gui.renderTooltip(this.font, itemStack, mouseX, mouseY);
            }
         }
      }
   }

   private void setupUpgradeTarget(ItemStack target) {
      this.setPreviewItem(target);
      this.upgradeTargetLevels.clear();
      this.requiredMaterials.clear();
      if (target.getItem() instanceof BasePartsItem partsItem) {
         int maxLevel = partsItem.getMaxLevel();

         for (int l = 0; l < maxLevel; l++) {
            ItemStack stack = new ItemStack(partsItem);
            partsItem.setLevel(stack, l + 1);
            this.upgradeTargetLevels.add(stack);
         }

         PomkotsDataPack.PartsData pd = PartsWorkbenchMenu.getPartsData(partsItem);
         if (pd != null) {
            this.setupRequiredMaterials(partsItem.getLevel(target), pd);
         }
      }
   }

   private void renderInventory(GuiGraphics gui, int left, int top, int width, int mouseX, int mouseY) {
      gui.drawCenteredString(this.font, this.getLocalizedString("{text.pomkotsmechs.gui.partsworkbench.label.inventory}"), left + width / 2, top + 33, 16777215);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      if (PartsWorkbenchMenu.Tab.CRAFT == this.currentTab) {
         int listHeight = this.imageHeight - 55;
         int visibleRows = listHeight / 26;
         int totalRows = (int)Math.ceil(this.craftablePartsList.size() / 4.0F);
         int maxScroll = Math.max(0, totalRows - visibleRows);
         this.scrollOffset = Mth.clamp(this.scrollOffset - (int)Math.signum(delta), 0, maxScroll);
      }

      return true;
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.isInside(mouseX, mouseY, this.leftPos + 5, this.topPos + 19, 48, 10)) {
         this.switchTab(PartsWorkbenchMenu.Tab.CRAFT);
         return true;
      }

      if (this.isInside(mouseX, mouseY, this.leftPos + 55, this.topPos + 19, 48, 10)) {
         this.switchTab(PartsWorkbenchMenu.Tab.UPGRADE);
         return true;
      }

      if (PartsWorkbenchMenu.Tab.CRAFT == this.currentTab && button == 0) {
         int listLeft = this.leftPos + this.previewSectionWidth + this.materialSectionWidth + 4;
         int listTop = this.topPos + 48;
         int listHeight = this.imageHeight - 55;
         int entriesPerRow = 4;
         int visibleRows = listHeight / 24;
         int startRow = this.scrollOffset;
         int endRow = Math.min((int)Math.ceil((float)this.craftablePartsList.size() / entriesPerRow), startRow + visibleRows);

         for (int row = startRow; row < endRow; row++) {
            for (int col = 0; col < entriesPerRow; col++) {
               int index = row * entriesPerRow + col;
               if (index >= this.craftablePartsList.size()) {
                  break;
               }

               int x = listLeft + col * 26;
               int y = listTop + (row - startRow) * 26;
               if (this.isInside(mouseX, mouseY, x, y, 24, 24)) {
                  this.partsForCraftSelected(index);
                  return true;
               }
            }
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   private void partsForCraftSelected(int i) {
      this.setupCraftTarget(i);
      Minecraft.getInstance().player.displayClientMessage(Component.literal("Selected: " + this.craftablePartsList.get(i).getName().getString()), true);
   }

   private void setupCraftTarget(int i) {
      this.selectedIndex = i;
      if (i < 0) {
         this.setPreviewItem(ItemStack.EMPTY);
         this.requiredMaterials.clear();
      } else {
         this.setPreviewItem(this.craftablePartsList.get(this.selectedIndex).getItemStack());
         this.setupRequiredMaterials(0, this.craftablePartsList.get(this.selectedIndex).itemParams);
      }
   }

   public void setPreviewItem(ItemStack stack) {
      this.previewItem = stack;
   }

   private boolean setupRequiredMaterials(int level, PomkotsDataPack.PartsData partsData) {
      this.requiredMaterials.clear();
      if (partsData.recipes.size() <= level) {
         return false;
      }

      if (partsData.levels.size() == level) {
         return false;
      }

      for (PomkotsDataPack.SerializablePair<String, Integer> material : partsData.recipes.get(level)) {
         String key = material.getFirst();
         Integer num = material.getSecond();
         Item item = (Item)BuiltInRegistries.ITEM.get(new ResourceLocation(key));
         if (Items.AIR.equals(item)) {
            return false;
         }

         this.requiredMaterials.add(new PartsWorkbenchScreen.Material(item, num));
      }

      return true;
   }

   private void switchTab(PartsWorkbenchMenu.Tab tab) {
      if (tab != this.currentTab) {
         this.currentTab = tab;
         ((PartsWorkbenchMenu)this.menu).setTab(tab);
         this.sendTabChange(tab);
         if (tab == PartsWorkbenchMenu.Tab.CRAFT) {
            this.setupCraftTarget(this.selectedIndex);
         } else if (tab == PartsWorkbenchMenu.Tab.UPGRADE) {
            this.setupUpgradeTarget(((PartsWorkbenchMenu)this.menu).getSlot(1).getItem());
         }
      }

      this.setupButtonVisibility(this.currentTab);
   }

   private void sendTabChange(PartsWorkbenchMenu.Tab tab) {
      FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
      buf.writeInt(tab.getInt());
      NetworkManager.sendToServer(PomkotsMechs.id("pwt"), buf);
   }

   private boolean isInside(double x, double y, int bx, int by, int bw, int bh) {
      return x >= bx && x <= bx + bw && y >= by && y <= by + bh;
   }

   private void setupButtonVisibility(PartsWorkbenchMenu.Tab tab) {
      this.craftButton.visible = PartsWorkbenchMenu.Tab.CRAFT == tab;
      this.upgradeButton.visible = PartsWorkbenchMenu.Tab.UPGRADE == tab;
   }

   private void displayMessage(String msg) {
      Minecraft.getInstance().player.displayClientMessage(Utils.string2Component(msg), false);
   }

   private String getLocalizedString(String id) {
      return Utils.string2Component(id).getString();
   }

   public static void drawScaledTextCentered(GuiGraphics graphics, Font font, Component text, float centerX, float y, float maxWidth, int color) {
      float textWidth = font.width(text);
      float scale = Math.min(1.0F, maxWidth / textWidth);
      float scaledWidth = textWidth * scale;
      float x = centerX - scaledWidth / 2.0F;
      PoseStack poseStack = graphics.pose();
      poseStack.pushPose();
      poseStack.translate(x, y, 0.0F);
      poseStack.scale(scale, scale, 1.0F);
      graphics.drawString(font, text, 0, 0, color, true);
      poseStack.popPose();
   }

   public static class Material extends Pair<Item, Integer> {
      public Material(Item first, Integer second) {
         super(first, second);
      }
   }

   public static class PartEntry {
      private final ResourceLocation id;
      private final PomkotsDataPack.PartsData itemParams;
      private final ItemStack itemStack;
      private final Component name;

      public PartEntry(ResourceLocation id, PomkotsDataPack.PartsData itemParams, ItemStack itemStack) {
         this.id = id;
         this.itemParams = itemParams;
         this.itemStack = itemStack;
         this.name = itemStack.getItem().getName(itemStack);
      }

      public ResourceLocation getId() {
         return this.id;
      }

      public PomkotsDataPack.PartsData getItemParams() {
         return this.itemParams;
      }

      public ItemStack getItemStack() {
         return this.itemStack;
      }

      public Component getName() {
         return this.name;
      }
   }
}
