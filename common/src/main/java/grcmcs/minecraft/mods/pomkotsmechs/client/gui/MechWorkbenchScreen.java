package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.architectury.networking.NetworkManager;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.client.model.parts.BasePartsItemModel;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.CircuitItem;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.CircuitItemStackHelper;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitPrefix;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import io.netty.buffer.Unpooled;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class MechWorkbenchScreen extends AbstractContainerScreen<MechWorkbenchMenu> {
   private static final ResourceLocation TEXTURE_BG_1 = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/background_hangar.png");
   private static final ResourceLocation TEXTURE_BG_2 = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/background_gradient.png");
   private static final ResourceLocation TEXTURE_PANEL_PARTS_SELECTOR = new ResourceLocation(
      "pomkotsmechs", "textures/gui/mechworkbench/panel_parts_selector_background.png"
   );
   private static final ResourceLocation TEXTURE_PANEL_INV_MECH = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/inventory_cargo.png");
   private static final ResourceLocation TEXTURE_PANEL_INV_PLAYER = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/inventory_player.png");
   private static final ResourceLocation TEXTURE_PANEL_SPEC = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/stats_panel.png");
   private static final ResourceLocation TEXTURE_PANEL_CIRCUIT = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/inventory_circuits.png");
   private static final ResourceLocation TEXTURE_ICON_AMMO = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/icon_ammo.png");
   private static final ResourceLocation TEXTURE_ICON_FUEL = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/icon_fuel.png");
   private static final ResourceLocation TEXTURE_ICON_REPAIR = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/icon_repair.png");
   private static final ResourceLocation TEXTURE_BUTTON = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/button_normal.png");
   private static final ResourceLocation TEXTURE_TAB_UNSELECTED_TOP = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/tab_unselected_top.png");
   private static final ResourceLocation TEXTURE_TAB_UNSELECTED_BOTTOM = new ResourceLocation(
      "pomkotsmechs", "textures/gui/mechworkbench/tab_unselected_bottom.png"
   );
   private static final ResourceLocation TEXTURE_TAB_SELECTED_TOP = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/tab_selected_top.png");
   private static final ResourceLocation TEXTURE_TAB_SELECTED_BOTTOM = new ResourceLocation(
      "pomkotsmechs", "textures/gui/mechworkbench/tab_selected_bottom.png"
   );
   private static final ResourceLocation TEXTURE_CARD_PARTS_BG = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/part_box_unselected.png");
   private static final ResourceLocation TEXTURE_CARD_PARTS_SELECTED_BG = new ResourceLocation(
      "pomkotsmechs", "textures/gui/mechworkbench/part_box_selected.png"
   );
   private static final ResourceLocation TEXTURE_WINDOW_TITLE = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/window_name.png");
   private static final ResourceLocation TEXTURE_SCROLL_BAR_BG = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/scroll_bar_background.png");
   private static final ResourceLocation TEXTURE_SCROLL_BAR = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/scroll_bar.png");
   private static final ResourceLocation TEXTURE_ITEM_CONTAINER = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/item_container.png");
   private static final ResourceLocation TEXTURE_ROAD_CROSSHAIR = new ResourceLocation("pomkotsmechs", "textures/gui/mechworkbench/road/crosshair.png");
   private static final ResourceLocation TEXTURE_ROAD_PART_INDICATOR_01 = new ResourceLocation(
      "pomkotsmechs", "textures/gui/mechworkbench/road/part_indicator_01.png"
   );
   private static final ResourceLocation TEXTURE_ROAD_PART_INDICATOR_02 = new ResourceLocation(
      "pomkotsmechs", "textures/gui/mechworkbench/road/part_indicator_02.png"
   );
   private static final ResourceLocation TEXTURE_ROAD_STAT_PANEL_SMALL = new ResourceLocation(
      "pomkotsmechs", "textures/gui/mechworkbench/road/stats_panel_small.png"
   );
   private static final int GUI_WIDTH = 480;
   private static final int GUI_HEIGHT = 270;
   private int panelX = 0;
   private int panelY = 0;
   private int panelWidth = 108;
   private int panelHeight = 160;
   private int tabTopOffsetY = -16;
   private int tabButtomOffsetY = 0;
   private final int cardWidth = 48;
   private final int cardHeight = 48;
   private final int cardModelHeight = 36;
   private final int cardSpacingX = 3;
   private final int cardSpacingY = 3;
   private final int columns = 2;
   private int cargoX = 0;
   private int cargoY = 0;
   private int cargoWidth = 164;
   private int cargoHeight = 90;
   private int playerInvX = 0;
   private int playerInvY = 0;
   private int playerInvWidth = 164;
   private int playerInvHeight = 94;
   private int specsX = 0;
   private int specsY = 0;
   private int specsWidth = 180;
   private int specsHeight = 56;
   private int slotSize = 18;
   private int slotSpacing = 20;
   private static final int BUTTON_WIDTH = 70;
   private static final int BUTTON_HEIGHT = 14;
   private static final int DROPDOWN_WIDTH = 70;
   private static final int DROPDOWN_HEIGHT = 14;
   private static final int DROPDOWN_OFFSET_X = 400;
   private static final int DROPDOWN_OFFSET_Y = 15;
   private float modelYaw = -20.0F;
   private float modelPitch = -10.0F;
   private boolean draggingModel = false;
   private double lastMouseX;
   private double lastMouseY;
   private int modelAreaX = 112;
   private int modelAreaY = 25;
   private int modelAreaWidth = 200;
   private int modelAreaHeight = 150;
   private int modelScale = 20;
   private int scrollOffset = 0;
   private int maxScroll = 0;
   private static final MechWorkbenchScreen.PartsTab[] TOP_TABS = new MechWorkbenchScreen.PartsTab[]{
      MechWorkbenchScreen.PartsTab.HEAD,
      MechWorkbenchScreen.PartsTab.BODY,
      MechWorkbenchScreen.PartsTab.ARM,
      MechWorkbenchScreen.PartsTab.LEGS,
      MechWorkbenchScreen.PartsTab.GENERATOR,
      MechWorkbenchScreen.PartsTab.BOOSTER
   };
   private static final MechWorkbenchScreen.PartsTab[] BOTTOM_TABS = new MechWorkbenchScreen.PartsTab[]{
      MechWorkbenchScreen.PartsTab.RIGHT_ARM,
      MechWorkbenchScreen.PartsTab.LEFT_ARM,
      MechWorkbenchScreen.PartsTab.RIGHT_SHOULDER,
      MechWorkbenchScreen.PartsTab.LEFT_SHOULDER,
      MechWorkbenchScreen.PartsTab.EXT1,
      MechWorkbenchScreen.PartsTab.EXT2
   };
   private MechWorkbenchScreen.PartsTab currentTab = MechWorkbenchScreen.PartsTab.HEAD;
   private final List<Integer> visiblePartSlots = new ArrayList<>();
   private Pmvc01Entity mech = null;
   private EditBox nameField;
   private boolean nameFieldInited = false;
   private Button renameButton;
   private Button generateCardButton;
   private Button repairMechButton;
   private Button autoSupply;
   private Button textureColorButton;
   private Button circuitButton;
   private boolean isDropdownOpen = false;
   private List<String> options = BasePartsItemModel.BASE_COLORS;
   private String selectedOption = "Color";
   private boolean circuitInvActive = false;
   private int contentHeight;
   private final int rowHeight = 22;
   private int lastInventoryRevision = -1;

   public MechWorkbenchScreen(MechWorkbenchMenu menu, Inventory inventory, Component title) {
      super(menu, inventory, title);
      this.imageWidth = 480;
      this.imageHeight = 270;
      this.titleLabelX = 44;
      this.inventoryLabelY = this.imageHeight - 94;
   }

   protected void init() {
      super.init();
      this.panelX = this.leftPos + 8;
      this.panelY = this.topPos + 50;
      this.cargoX = this.leftPos + 310;
      this.cargoY = this.topPos + 35;
      this.playerInvX = this.leftPos + 310;
      this.playerInvY = this.topPos + 135;
      this.specsX = this.panelX + this.panelWidth + 7;
      this.specsY = this.panelY + this.panelHeight + this.slotSize * 2 + 5 - this.specsHeight;
      this.nameField = new EditBox(this.font, this.panelX + 120, this.topPos + 15, 80, 14, Component.literal("Name"));
      this.nameField.setMaxLength(32);
      this.addRenderableWidget(this.nameField);
      this.circuitButton = (Button)this.addRenderableWidget(
         new ImageButton(
            this.specsX + this.specsWidth - 70 - 5,
            this.specsY + 2,
            70,
            14,
            TEXTURE_BUTTON,
            0,
            0,
            70,
            56,
            Utils.string2Component("{text.pomkotsmechs.gui.mechworkbench.circuit}"),
            btn -> {
               this.circuitInvActive = !this.circuitInvActive;
               ((MechWorkbenchMenu)this.menu).setActivationForCircuitSlots(this.circuitInvActive);
            }
         )
      );
      this.renameButton = (Button)this.addRenderableWidget(
         new ImageButton(
            this.panelX + 120 + 80 + 5,
            this.topPos + 15,
            70,
            14,
            TEXTURE_BUTTON,
            0,
            0,
            70,
            56,
            Utils.string2Component("{text.pomkotsmechs.gui.mechworkbench.rename}"),
            btn -> this.sendMechName(this.nameField.getValue())
         )
      );
      this.generateCardButton = (Button)this.addRenderableWidget(
         new ImageButton(
            this.playerInvX,
            this.topPos + 15,
            70,
            14,
            TEXTURE_BUTTON,
            0,
            0,
            70,
            56,
            Utils.string2Component("{text.pomkotsmechs.gui.mechworkbench.getcard}"),
            btn -> this.sendMechOperationAction("sgc")
         )
      );
      this.textureColorButton = (Button)this.addRenderableWidget(
         new ImageButton(
            this.leftPos + 400,
            this.topPos + 15,
            70,
            14,
            TEXTURE_BUTTON,
            0,
            0,
            70,
            56,
            Component.literal(this.selectedOption),
            btn -> this.isDropdownOpen = !this.isDropdownOpen
         )
      );
      int buttonBottom = this.panelY + this.panelHeight + this.slotSize + 16 + 4;
      this.repairMechButton = (Button)this.addRenderableWidget(
         new ImageButton(
            this.playerInvX,
            buttonBottom - 14,
            70,
            14,
            TEXTURE_BUTTON,
            0,
            0,
            70,
            56,
            Utils.string2Component("{text.pomkotsmechs.gui.mechworkbench.repair}"),
            btn -> this.sendMechOperationAction("rpm")
         )
      );
      this.autoSupply = (Button)this.addRenderableWidget(
         new ImageButton(
            this.playerInvX + 90,
            buttonBottom - 14,
            70,
            14,
            TEXTURE_BUTTON,
            0,
            0,
            70,
            56,
            Utils.string2Component("{text.pomkotsmechs.gui.mechworkbench.autosupply}"),
            btn -> this.sendMechOperationAction("asp")
         )
      );
      this.mech = null;
      ((MechWorkbenchMenu)this.menu).setActivationForCircuitSlots(false);
      this.rebuildVisibleParts();
   }

   protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
      if (!this.nameFieldInited && this.mech != null && this.mech.hasCustomName()) {
         this.nameField.setValue(this.mech.getCustomName().getString());
         this.nameFieldInited = true;
      }

      if (((MechWorkbenchMenu)this.menu).getMode() == MechWorkbenchMenu.MODE_VIEW) {
         this.renameButton.visible = false;
         this.generateCardButton.visible = false;
         this.repairMechButton.visible = false;
         this.textureColorButton.visible = false;
         this.nameField.active = false;
      }

      guiGraphics.pose().pushPose();
      guiGraphics.pose().translate(0.0F, 0.0F, 0.0F);
      if (((MechWorkbenchMenu)this.menu).getMode() == MechWorkbenchMenu.MODE_ASSEMBLE) {
         RenderSystem.setShaderTexture(0, TEXTURE_BG_1);
         guiGraphics.blit(TEXTURE_BG_1, 0, 0, this.width, this.height, 0.0F, 0.0F, 960, 540, 960, 540);
      }

      String title;
      if (((MechWorkbenchMenu)this.menu).getMode() == MechWorkbenchMenu.MODE_VIEW) {
         title = "Mech Resupply";
      } else {
         title = "Mech Assemble";
      }

      guiGraphics.drawString(this.font, title, this.panelX, this.topPos + 15, -1, false);
      this.renderCircuitSlotHighlights(guiGraphics);
      guiGraphics.pose().popPose();
   }

   private void renderCircuitSlotHighlights(GuiGraphics guiGraphics) {
      ItemStack carried = ((MechWorkbenchMenu)this.menu).getCarried();
      if (carried.getItem() instanceof CircuitItem) {
         CircuitPrefix carriedPrefix = CircuitItemStackHelper.getPrefixOrDefault(carried);

         for (Slot slot : ((MechWorkbenchMenu)this.menu).slots) {
            if (slot instanceof MechWorkbenchMenu.MechCircuitSlot circuitSlot
               && !circuitSlot.isDisablePlace()
               && circuitSlot.acceptsCircuitPrefix(carriedPrefix)
               && !slot.hasItem()) {
               int x = this.leftPos + slot.x;
               int y = this.topPos + slot.y;
               renderSlotHighlight(guiGraphics, x, y);
            }
         }
      }
   }

   private static void renderSlotHighlight(GuiGraphics guiGraphics, int x, int y) {
      int fillColor = 1426063615;
      int borderColor = -866822657;
      guiGraphics.fill(x, y, x + 16, y + 16, fillColor);
      guiGraphics.fill(x - 1, y - 1, x + 17, y, borderColor);
      guiGraphics.fill(x - 1, y + 16, x + 17, y + 17, borderColor);
      guiGraphics.fill(x - 1, y, x, y + 16, borderColor);
      guiGraphics.fill(x + 16, y, x + 17, y + 16, borderColor);
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

   public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
      if (this.minecraft != null && this.minecraft.level != null && this.mech == null) {
         for (Entity entity : this.minecraft.level.entitiesForRendering()) {
            if (entity instanceof Pmvc01Entity m && (short)entity.getUUID().hashCode() == ((MechWorkbenchMenu)this.menu).getEntityId()) {
               this.mech = m;
               this.rebuildVisibleParts();
            }
         }
      }

      if (this.mech != null) {
         this.renderBackground(guiGraphics);
         super.render(guiGraphics, mouseX, mouseY, partialTick);
         if (!this.circuitInvActive) {
            this.renderMech3DModel(guiGraphics);
         }

         guiGraphics.pose().pushPose();
         guiGraphics.pose().translate(0.0F, 0.0F, 100.0F);
         this.renderPartsSelectPanel(guiGraphics, mouseX, mouseY);
         this.renderConsumablePanel(guiGraphics, this.panelX, this.panelY + this.panelHeight + this.slotSize + 4);
         this.renderCargoPanel(guiGraphics);
         this.renderPlayerInventoryPanel(guiGraphics);
         this.renderCircuitInventoryPanel(guiGraphics);
         guiGraphics.pose().popPose();
         guiGraphics.pose().pushPose();
         guiGraphics.pose().translate(0.0F, 0.0F, 300.0F);
         this.renderSpecsPanel(guiGraphics);
         guiGraphics.pose().popPose();
         if (((MechWorkbenchMenu)this.menu).getMode() == MechWorkbenchMenu.MODE_ASSEMBLE) {
            this.nameField.render(guiGraphics, mouseX, mouseY, partialTick);
         }

         if (!this.isDropdownOpen) {
            this.renderTooltip(guiGraphics, mouseX, mouseY);
         }

         if (this.isDropdownOpen) {
            this.renderDropdown(guiGraphics, mouseX, mouseY);
         }
      }
   }

   private void renderPartsSelectPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      this.renderBgTexture(TEXTURE_PANEL_PARTS_SELECTOR, this.panelX, this.panelY, this.panelWidth, this.panelHeight, 0.3F, guiGraphics);
      this.renderTabs(guiGraphics, TOP_TABS, this.panelX, this.panelY + this.tabTopOffsetY, true);
      this.renderTabs(guiGraphics, BOTTOM_TABS, this.panelX, this.panelY + this.panelHeight + this.tabButtomOffsetY, false);
      this.renderPartList(guiGraphics, mouseX, mouseY);
      this.renderScrollbar(guiGraphics);
   }

   private void rebuildVisibleParts() {
      this.visiblePartSlots.clear();

      for (int i = 0; i < ((MechWorkbenchMenu)this.menu).slots.size(); i++) {
         Slot slot = ((MechWorkbenchMenu)this.menu).getSlot(i);
         ItemStack stack = slot.getItem();
         if (!stack.isEmpty() && this.matchesCurrentTab(stack)) {
            this.visiblePartSlots.add(i);
         }
      }

      this.visiblePartSlots.add(Integer.MIN_VALUE);
      this.updateScrollLimits();
   }

   private boolean matchesCurrentTab(ItemStack stack) {
      Item item = stack.getItem();

      return switch (this.currentTab) {
         case HEAD -> item instanceof BasePartsItem.Head;
         case BODY -> item instanceof BasePartsItem.Body;
         case ARM -> item instanceof BasePartsItem.Arm;
         case LEGS -> item instanceof BasePartsItem.Legs;
         case GENERATOR -> item instanceof BasePartsItem.Generator;
         case BOOSTER -> item instanceof BasePartsItem.Booster;
         case RIGHT_ARM, LEFT_ARM -> item instanceof BasePartsItem.WeaponArm;
         case RIGHT_SHOULDER, LEFT_SHOULDER -> item instanceof BasePartsItem.WeaponShoulder;
         case EXT1, EXT2 -> item instanceof BasePartsItem.Extension;
      };
   }

   private void updateScrollLimits() {
      int rows = (int)Math.ceil(this.visiblePartSlots.size() / 2.0);
      if (rows <= 0) {
         this.maxScroll = 0;
         this.contentHeight = 0;
      } else {
         this.contentHeight = rows * 48 + (rows - 1) * 3;
         this.maxScroll = Math.max(0, this.contentHeight - this.panelHeight);
      }
   }

   private void renderTabs(GuiGraphics guiGraphics, MechWorkbenchScreen.PartsTab[] tabs, int startX, int y, boolean top) {
      ResourceLocation sel = top ? TEXTURE_TAB_SELECTED_TOP : TEXTURE_TAB_SELECTED_BOTTOM;
      ResourceLocation unsel = top ? TEXTURE_TAB_UNSELECTED_TOP : TEXTURE_TAB_UNSELECTED_BOTTOM;

      for (int i = 0; i < tabs.length; i++) {
         MechWorkbenchScreen.PartsTab tab = tabs[i];
         int x = startX + i * this.slotSize;
         boolean selected = this.currentTab == tab;
         ResourceLocation bg;
         if (selected) {
            bg = sel;
         } else {
            bg = unsel;
         }

         int topAdj = top ? -3 : 0;
         this.renderBgTexture(bg, x, y + topAdj, this.slotSize, 19, 0.3F, guiGraphics);
         this.renderBgTexture(tab.icon, x + 1, y + 1, 16, 16, 1.0F, guiGraphics);
      }
   }

   private void renderPartList(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      int startX = this.panelX + 4;
      int startY = this.panelY + 4;
      guiGraphics.enableScissor(this.panelX, this.panelY, this.panelX + this.panelWidth, this.panelY + this.panelHeight);
      ItemStack hoverItem = null;

      for (int index = 0; index < this.visiblePartSlots.size(); index++) {
         int col = index % 2;
         int row = index / 2;
         int x = startX + col * 51;
         int y = startY + row * 51 - this.scrollOffset;
         if (y + 48 >= this.panelY && y <= this.panelY + this.panelHeight) {
            boolean hover = mouseX >= x && mouseX < x + 48 && mouseY >= y && mouseY < y + 48;
            this.renderBgTexture(TEXTURE_CARD_PARTS_BG, x, y, 48, 48, 0.7F, guiGraphics);
            if (this.currentTab.mechSlot == this.visiblePartSlots.get(index)) {
               this.renderBgTexture(TEXTURE_CARD_PARTS_SELECTED_BG, x, y, 48, 48, 0.7F, guiGraphics);
            }

            Integer slotIndex = this.visiblePartSlots.get(index);
            ItemStack stack = ItemStack.EMPTY;
            if (slotIndex >= 0) {
               stack = ((MechWorkbenchMenu)this.menu).getSlot(slotIndex).getItem();
            }

            if (stack.isEmpty()) {
               guiGraphics.drawString(this.font, "Empty", x + 10, y + 18, -1, false);
            } else {
               this.renderPartPreview(guiGraphics, stack, x + 24, y + 18, 2);
            }

            this.renderEquippedSlotTag(guiGraphics, slotIndex, x, y);
            if (stack.isEmpty()) {
               guiGraphics.drawString(this.font, "-", x + 4, y + 36 + 2, -1, false);
            } else {
               String name = stack.getHoverName().getString();
               if (name.contains("(")) {
                  name = name.substring(0, name.indexOf(40));
               }

               FormattedText seq = this.font.substrByWidth(Component.literal(name), 44);
               guiGraphics.drawString(this.font, seq.getString(), x + 4, y + 36 + 2, -1, false);
            }

            if (hover) {
               hoverItem = stack;
            }
         }
      }

      guiGraphics.disableScissor();
      if (hoverItem != null
         && !hoverItem.isEmpty()
         && this.mouseInRange(mouseX, mouseY, this.panelX, this.panelY, this.panelX + this.panelWidth, this.panelY + this.panelHeight)) {
         guiGraphics.renderTooltip(this.font, hoverItem, mouseX, mouseY);
      }
   }

   private void renderEquippedSlotTag(GuiGraphics guiGraphics, int slotIndex, int cardX, int cardY) {
      String label = switch (slotIndex) {
         case 6, 8 -> "R";
         case 7, 9 -> "L";
         case 10 -> "1";
         case 11 -> "2";
         default -> null;
      };
      if (label != null) {
         int textColor = switch (slotIndex) {
            case 6, 8, 10 -> -8328961;
            default -> -25384;
         };
         int textWidth = this.font.width(label);
         int tagWidth = textWidth + 4;
         int tagX = cardX + 48 - tagWidth - 2;
         int tagY = cardY + 2;
         guiGraphics.fill(tagX, tagY, tagX + tagWidth, tagY + 11, -871360480);
         guiGraphics.drawString(this.font, label, tagX + 2, tagY + 1, textColor, false);
      }
   }

   private boolean mouseInRange(int mx, int my, int x1, int y1, int x2, int y2) {
      return mx >= x1 && mx <= x2 && my >= y1 && my <= y2;
   }

   private void renderPartPreview(GuiGraphics guiGraphics, ItemStack stack, int x, int y, int scale) {
      guiGraphics.pose().pushPose();
      guiGraphics.pose().translate(x, y, 50.0F);
      this.renderParts(guiGraphics, stack);
      guiGraphics.pose().popPose();
   }

   private void renderParts(GuiGraphics guiGraphics, ItemStack stack) {
      Minecraft mc = Minecraft.getInstance();
      ItemRenderer itemRenderer = mc.getItemRenderer();
      PoseStack pose = guiGraphics.pose();
      pose.pushPose();
      if (!(stack.getItem() instanceof BasePartsItem.Weapon) && !(stack.getItem() instanceof BasePartsItem.Extension)) {
         pose.mulPose(Axis.XP.rotationDegrees(-25.0F));
         pose.mulPose(Axis.YP.rotationDegrees(45.0F));
      }

      pose.scale(36.0F, -36.0F, 36.0F);
      BakedModel model = itemRenderer.getModel(stack, null, null, 0);
      itemRenderer.render(stack, ItemDisplayContext.GUI, false, pose, guiGraphics.bufferSource(), 15728880, OverlayTexture.NO_OVERLAY, model);
      pose.popPose();
   }

   private void renderPartList2(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      int startX = this.panelX + 6;
      int startY = this.panelY + 6;

      for (int i = 0; i < this.visiblePartSlots.size(); i++) {
         int y = startY + i * 22 - this.scrollOffset;
         if (y >= this.panelY && y <= this.panelY + this.panelHeight - 22) {
            Slot slot = ((MechWorkbenchMenu)this.menu).getSlot(this.visiblePartSlots.get(i));
            ItemStack stack = slot.getItem();
            boolean hover = mouseX >= startX && mouseX < startX + this.panelWidth - 12 && mouseY >= y && mouseY < y + 22;
            if (hover) {
               guiGraphics.renderTooltip(this.font, stack, mouseX, mouseY);
            }

            boolean equippedNow = this.currentTab.mechSlot == this.visiblePartSlots.get(i);
            int bg;
            if (equippedNow) {
               bg = -1438318780;
            } else if (hover) {
               bg = -1996488705;
            } else {
               bg = -1440603614;
            }

            guiGraphics.fill(startX, y, startX + this.panelWidth - 12, y + 22, bg);
            guiGraphics.renderItem(stack, startX + 3, y + 3);
            String name = stack.getHoverName().getString();
            if (name.contains("(")) {
               name = name.substring(0, name.indexOf(40));
            }

            guiGraphics.drawString(this.font, Component.literal(name), startX + 24, y + 7, -1, false);
         }
      }
   }

   private void renderScrollbar(GuiGraphics guiGraphics) {
      if (this.maxScroll > 0) {
         int barX = this.panelX + this.panelWidth + 1;
         int barY = this.panelY + 4;
         int barHeight = this.panelHeight - 8;
         float visibleRatio = (float)this.panelHeight / this.contentHeight;
         int thumbHeight = Math.max(16, (int)(barHeight * visibleRatio));
         float progress = (float)this.scrollOffset / this.maxScroll;
         int movable = barHeight - thumbHeight;
         int thumbY = barY + (int)(progress * movable);
         this.renderBgTexture(TEXTURE_SCROLL_BAR_BG, this.panelX + this.panelWidth + 2, this.panelY, 2, this.panelHeight, guiGraphics);
         this.renderBgTexture(TEXTURE_SCROLL_BAR, barX, thumbY, 4, thumbHeight, guiGraphics);
      }
   }

   private void renderConsumablePanel(GuiGraphics guiGraphics, int startX, int y) {
      for (int i = 0; i < 6; i++) {
         int x = startX + i * this.slotSize;
         this.renderBgTexture(TEXTURE_ITEM_CONTAINER, x + 1, y + 1, 16, 16, guiGraphics);
         if (!((MechWorkbenchMenu)this.menu).getSlot(12 + i).hasItem()) {
            ResourceLocation icon;
            if (i == 4) {
               icon = TEXTURE_ICON_FUEL;
            } else if (i == 5) {
               icon = TEXTURE_ICON_REPAIR;
            } else {
               icon = TEXTURE_ICON_AMMO;
            }

            this.renderBgTexture(icon, x + 1, y + 1, 16, 16, 1.0F, guiGraphics);
         }
      }
   }

   private void renderCargoPanel(GuiGraphics guiGraphics) {
      this.renderBgTexture(TEXTURE_PANEL_INV_MECH, this.cargoX, this.cargoY, this.cargoWidth, this.cargoHeight, guiGraphics);
      guiGraphics.drawString(this.font, "Mech Cargo", this.cargoX + 18, this.cargoY + 4, -1);
   }

   private void renderPlayerInventoryPanel(GuiGraphics guiGraphics) {
      this.renderBgTexture(TEXTURE_PANEL_INV_PLAYER, this.playerInvX, this.playerInvY, this.playerInvWidth, this.playerInvHeight, guiGraphics);
      guiGraphics.drawString(this.font, "Player Inventory", this.playerInvX + 18, this.playerInvY + 4, -1);
   }

   private void renderCircuitInventoryPanel(GuiGraphics guiGraphics) {
      if (this.circuitInvActive) {
         this.renderBgTexture(TEXTURE_PANEL_CIRCUIT, this.specsX, this.cargoY, 180, 132, guiGraphics);
         guiGraphics.drawString(this.font, "Additional Circuits", this.specsX + 18, this.cargoY + 4, -1);
         int offX = this.specsX + 22;
         int offY = this.cargoY + 3;
         int rowH = 19;
         guiGraphics.drawString(this.font, "- Balance", offX, offY + rowH, -1);
         guiGraphics.drawString(this.font, "- Offence", offX, offY + rowH * 2, -1);
         guiGraphics.drawString(this.font, "- Defence", offX, offY + rowH * 3, -1);
         guiGraphics.drawString(this.font, "- Mobility", offX, offY + rowH * 4, -1);
         guiGraphics.drawString(this.font, "- Energy", offX, offY + rowH * 5, -1);
         guiGraphics.drawString(this.font, "- Utility", offX, offY + rowH * 6, -1);
      }
   }

   private void renderSpecsPanel(GuiGraphics guiGraphics) {
      this.renderBgTexture(TEXTURE_PANEL_SPEC, this.specsX, this.specsY, this.specsWidth, this.specsHeight, guiGraphics);
      guiGraphics.drawString(this.font, "Specs", this.specsX + 18, this.specsY + 4, -1);
      int offsetX = this.specsX + 4;
      int offsetY = this.specsY + 20;
      this.drawParams(
         0, offsetX, offsetY, "{text.pomkotsmechs.gui.mechworkbench.hp}", (int)this.mech.getHealth() + "/" + this.mech.getDurability(), false, guiGraphics
      );
      this.drawParams(
         1,
         offsetX,
         offsetY,
         "{text.pomkotsmechs.gui.mechworkbench.energy}",
         String.format("%d", this.mech.getMaxEnergy()) + "/" + String.format("%.1f", this.mech.getEnergyChargePerTick()),
         false,
         guiGraphics
      );
      this.drawParams(
         2,
         offsetX,
         offsetY,
         "{text.pomkotsmechs.gui.mechworkbench.weight}",
         this.mech.getWeight() + "/" + this.mech.getMaxWeight(),
         this.mech.getWeight() > this.mech.getMaxWeight(),
         guiGraphics
      );
      this.drawParams(
         3, offsetX, offsetY, "{text.pomkotsmechs.gui.mechworkbench.speed}", String.format("%.1f", this.mech.getSpeedModifier()), false, guiGraphics
      );
      this.drawParams(4, offsetX, offsetY, "{text.pomkotsmechs.gui.mechworkbench.jump}", String.format("%.1f", this.mech.getJumpModifier()), false, guiGraphics);
      this.drawParams(
         5, offsetX, offsetY, "{text.pomkotsmechs.gui.mechworkbench.boost}", String.format("%.1f", this.mech.getSpeedModifierEvasion()), false, guiGraphics
      );
   }

   protected void drawParams(int row, int offX, int offY, String paramName, String paramValue, boolean alert, GuiGraphics guiGraphics) {
      if (row > 2) {
         offX += this.specsWidth / 2 + 2;
      }

      int xoff1 = offX;
      int xoff2 = offX + this.specsWidth / 4;
      if (row <= 2) {
         xoff2 -= 16;
      }

      int yoff = offY;
      int color = alert ? 16711680 : 16777215;
      Component c = Utils.string2Component(paramName);
      guiGraphics.drawString(this.font, c.getString(), xoff1 + 1, yoff + 12 * (row % 3), color, false);
      guiGraphics.drawString(this.font, paramValue, xoff2 + 4, yoff + 12 * (row % 3), color, false);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.nameField.isMouseOver(mouseX, mouseY)) {
         this.nameField.setFocused(false);
      }

      if (this.isDropdownOpen) {
         int x = this.leftPos + 400;
         int startY = this.topPos + 15 + 14;

         for (int i = 0; i < this.options.size(); i++) {
            int y = startY + i * 14;
            if (mouseX >= x && mouseX <= x + 70 && mouseY >= y && mouseY <= y + 14) {
               this.selectedOption = this.options.get(i);
               this.textureColorButton.setMessage(Component.literal(this.selectedOption));
               this.sendTextureColor2Server(i);
               this.isDropdownOpen = false;
               return true;
            }
         }

         this.isDropdownOpen = false;
      }

      int tabSize = 18;
      int tabSpacing = 18;
      int topTabX = this.panelX;
      int topTabY = this.panelY + this.tabTopOffsetY;

      for (int i = 0; i < TOP_TABS.length; i++) {
         MechWorkbenchScreen.PartsTab tab = TOP_TABS[i];
         int x = topTabX + i * tabSpacing;
         int y = topTabY;
         if (mouseX >= x && mouseX < x + tabSize && mouseY >= y && mouseY < y + tabSize) {
            this.currentTab = tab;
            this.scrollOffset = 0;
            this.rebuildVisibleParts();
            return true;
         }
      }

      int bottomTabX = this.panelX;
      int bottomTabY = this.panelY + this.panelHeight + this.tabButtomOffsetY;

      for (int i = 0; i < BOTTOM_TABS.length; i++) {
         MechWorkbenchScreen.PartsTab tab = BOTTOM_TABS[i];
         int x = bottomTabX + i * tabSpacing;
         int y = bottomTabY;
         if (mouseX >= x && mouseX < x + tabSize && mouseY >= y && mouseY < y + tabSize) {
            this.currentTab = tab;
            this.scrollOffset = 0;
            this.rebuildVisibleParts();
            return true;
         }
      }

      if (((MechWorkbenchMenu)this.menu).getMode() == MechWorkbenchMenu.MODE_ASSEMBLE) {
         int startX = this.panelX + 6;
         int startY = this.panelY + 6;

         for (int index = 0; index < this.visiblePartSlots.size(); index++) {
            int col = index % 2;
            int row = index / 2;
            int x = startX + col * 51;
            int y = startY + row * 51 - this.scrollOffset;
            if (mouseX >= x
               && mouseX < x + 48
               && mouseY >= y
               && mouseY < y + 48
               && this.mouseInRange((int)mouseX, (int)mouseY, this.panelX, this.panelY, this.panelX + this.panelWidth, this.panelY + this.panelHeight)) {
               int idx = this.visiblePartSlots.get(index);
               if (idx < 0) {
                  if (!this.hasInventorySpace()) {
                     return true;
                  }

                  this.sendServerUnequip();
                  return true;
               }

               this.sendServerEquip(idx);
               return true;
            }
         }
      }

      if (button == 0
         && !this.circuitInvActive
         && mouseX >= this.modelAreaLeft()
         && mouseX <= this.modelAreaRight()
         && mouseY >= this.modelAreaTop()
         && mouseY <= this.modelAreaBottom()) {
         this.draggingModel = true;
         this.lastMouseX = mouseX;
         this.lastMouseY = mouseY;
         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   private boolean hasInventorySpace() {
      Inventory inv = this.minecraft.player.getInventory();

      for (int i = 0; i < inv.getContainerSize(); i++) {
         if (inv.getItem(i).isEmpty()) {
            return true;
         }
      }

      return false;
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
      if (this.draggingModel) {
         float dx = (float)(mouseX - this.lastMouseX);
         float dy = (float)(mouseY - this.lastMouseY);
         this.modelYaw -= dx * 1.2F;
         this.modelPitch -= dy * 0.7F;
         this.modelPitch = Mth.clamp(this.modelPitch, -40.0F, 40.0F);
         this.lastMouseX = mouseX;
         this.lastMouseY = mouseY;
         return true;
      } else {
         return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
      }
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.draggingModel = false;
      return super.mouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      this.scrollOffset -= (int)(delta * 12.0);
      this.scrollOffset = Mth.clamp(this.scrollOffset, 0, this.maxScroll);
      return true;
   }

   private void renderMech3DModel(GuiGraphics guiGraphics) {
      if (this.mech != null) {
         int centerX = this.modelAreaLeft() + this.modelAreaWidth / 2;
         int centerY = this.modelAreaTop() + this.modelAreaHeight;
         this.mech.setShowCustomHealthBar(false);
         renderEntityInInventoryRotation(guiGraphics, centerX, centerY, this.modelScale, this.modelYaw, this.modelPitch, this.mech);
         this.mech.setShowCustomHealthBar(true);
      }
   }

   public static void renderEntityInInventoryRotation(GuiGraphics guiGraphics, int x, int y, int scale, float yaw, float pitch, LivingEntity entity) {
      Quaternionf baseRot = new Quaternionf().rotateZ((float) Math.PI);
      Quaternionf pitchRot = new Quaternionf().rotateX(pitch * (float) (Math.PI / 180.0));
      baseRot.mul(pitchRot);
      float bodyRot = entity.yBodyRot;
      float yRot = entity.getYRot();
      float xRot = entity.getXRot();
      float headRotO = entity.yHeadRotO;
      float headRot = entity.yHeadRot;
      entity.yBodyRot = 180.0F + yaw;
      entity.setYRot(180.0F + yaw);
      entity.setXRot(-pitch);
      entity.yHeadRot = entity.getYRot();
      entity.yHeadRotO = entity.getYRot();
      renderEntityInInventory(guiGraphics, x, y, scale, baseRot, null, entity);
      entity.yBodyRot = bodyRot;
      entity.setYRot(yRot);
      entity.setXRot(xRot);
      entity.yHeadRotO = headRotO;
      entity.yHeadRot = headRot;
   }

   public static void renderEntityInInventory(
      GuiGraphics guiGraphics, int i, int j, int k, Quaternionf quaternionf, @Nullable Quaternionf quaternionf2, LivingEntity livingEntity
   ) {
      guiGraphics.pose().pushPose();
      guiGraphics.pose().translate(i, j, 50.0);
      guiGraphics.pose().mulPoseMatrix(new Matrix4f().scaling(k, k, -k));
      guiGraphics.pose().mulPose(quaternionf);
      Lighting.setupForEntityInInventory();
      EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      if (quaternionf2 != null) {
         quaternionf2.conjugate();
         entityRenderDispatcher.overrideCameraOrientation(quaternionf2);
      }

      entityRenderDispatcher.setRenderShadow(false);
      RenderSystem.runAsFancy(
         () -> entityRenderDispatcher.render(livingEntity, 0.0, 0.0, 0.0, 0.0F, 1.0F, guiGraphics.pose(), guiGraphics.bufferSource(), 15728880)
      );
      guiGraphics.flush();
      entityRenderDispatcher.setRenderShadow(true);
      guiGraphics.pose().popPose();
      Lighting.setupFor3DItems();
   }

   private int modelAreaLeft() {
      return this.leftPos + this.modelAreaX;
   }

   private int modelAreaTop() {
      return this.topPos + this.modelAreaY;
   }

   private int modelAreaRight() {
      return this.modelAreaLeft() + this.modelAreaWidth;
   }

   private int modelAreaBottom() {
      return this.modelAreaTop() + this.modelAreaHeight;
   }

   private void renderDropdown(GuiGraphics g, int mouseX, int mouseY) {
      g.pose().pushPose();
      g.pose().translate(0.0F, 0.0F, 400.0F);
      int x = this.leftPos + 400;
      int startY = this.topPos + 15 + 14;

      for (int i = 0; i < this.options.size(); i++) {
         int y = startY + i * 14;
         boolean hovered = mouseX >= x && mouseX <= x + 70 && mouseY >= y && mouseY <= y + 14;
         int bgColor = hovered ? -10066330 : -16777216;
         g.fill(x, y, x + 70, y + 14, bgColor);
         g.drawString(this.font, this.options.get(i), x + 3, y + 4, 16777215, false);
      }

      g.pose().popPose();
   }

   protected void renderLabels(GuiGraphics guiGraphics, int i, int j) {
   }

   private void sendServerEquip(int sourceSlot) {
      if (this.mech != null) {
         FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
         buf.writeInt(((MechWorkbenchMenu)this.menu).containerId);
         buf.writeInt(this.currentTab.mechSlot);
         buf.writeInt(sourceSlot);
         buf.writeInt(1);
         NetworkManager.sendToServer(PomkotsMechs.id("mwbcp"), buf);
      }
   }

   private void sendServerUnequip() {
      if (this.mech != null) {
         FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
         buf.writeInt(((MechWorkbenchMenu)this.menu).containerId);
         buf.writeInt(this.currentTab.mechSlot);
         buf.writeInt(0);
         buf.writeInt(0);
         NetworkManager.sendToServer(PomkotsMechs.id("mwbcp"), buf);
      }
   }

   private void sendTextureColor2Server(int color) {
      if (this.mech != null) {
         FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
         buf.writeUUID(this.mech.getUUID());
         buf.writeInt(color);
         NetworkManager.sendToServer(PomkotsMechs.id("ctx"), buf);
      }
   }

   private void sendMechOperationAction(String message) {
      if (this.mech != null) {
         FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
         buf.writeUUID(this.mech.getUUID());
         NetworkManager.sendToServer(PomkotsMechs.id(message), buf);
      }
   }

   private void sendMechName(String mechName) {
      if (this.mech != null) {
         FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
         buf.writeUUID(this.mech.getUUID());
         buf.writeUtf(mechName);
         NetworkManager.sendToServer(PomkotsMechs.id("ccn"), buf);
      }
   }

   protected void containerTick() {
      super.containerTick();
      int rev = ((MechWorkbenchMenu)this.menu).getInventoryRevision();
      if (rev != this.lastInventoryRevision) {
         this.lastInventoryRevision = rev;
         this.rebuildVisibleParts();
      }
   }

   private void renderCustomTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      if (((MechWorkbenchMenu)this.menu).getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
         guiGraphics.pose().pushPose();
         guiGraphics.pose().translate(0.0F, 0.0F, 400.0F);
         ItemStack itemStack = this.hoveredSlot.getItem();
         List<Component> lines = Screen.getTooltipFromItem(this.minecraft, itemStack);
         int padding = 4;
         int width = 0;

         for (Component line : lines) {
            width = Math.max(width, this.font.width(line));
         }

         int lineHeight = 12;
         int height = lines.size() * lineHeight + padding * 2;
         int x = mouseX + 12;
         int y = mouseY - 12;
         int screenW = this.width;
         int screenH = this.height;
         if (x + width + padding * 2 > screenW) {
            x = screenW - width - padding * 2 - 4;
         }

         if (y + height > screenH) {
            y = screenH - height - 4;
         }

         if (x < 4) {
            x = 4;
         }

         if (y < 4) {
            y = 4;
         }

         guiGraphics.fillGradient(x - padding, y - padding, x + width + padding, y + height, -300935136, -299881928);
         guiGraphics.renderOutline(x - padding, y - padding, width + padding * 2, height + padding, -10040065);

         for (int i = 0; i < lines.size(); i++) {
            guiGraphics.drawString(this.font, lines.get(i), x, y + i * lineHeight, -1, false);
         }

         guiGraphics.pose().popPose();
      }
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

      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   public boolean charTyped(char codePoint, int modifiers) {
      return this.nameField.isFocused() && this.nameField.charTyped(codePoint, modifiers) ? true : super.charTyped(codePoint, modifiers);
   }

   public enum PartsTab {
      HEAD(0, "textures/gui/mechworkbench/icon_head.png"),
      BODY(1, "textures/gui/mechworkbench/icon_body.png"),
      ARM(2, "textures/gui/mechworkbench/icon_arms.png"),
      LEGS(3, "textures/gui/mechworkbench/icon_legs.png"),
      GENERATOR(4, "textures/gui/mechworkbench/icon_generator.png"),
      BOOSTER(5, "textures/gui/mechworkbench/icon_booster.png"),
      RIGHT_ARM(6, "textures/gui/mechworkbench/icon_w_a_r.png"),
      LEFT_ARM(7, "textures/gui/mechworkbench/icon_w_a_l.png"),
      RIGHT_SHOULDER(8, "textures/gui/mechworkbench/icon_w_s_r.png"),
      LEFT_SHOULDER(9, "textures/gui/mechworkbench/icon_w_s_l.png"),
      EXT1(10, "textures/gui/mechworkbench/icon_ext_1.png"),
      EXT2(11, "textures/gui/mechworkbench/icon_ext_2.png");

      public final int mechSlot;
      public final ResourceLocation icon;

      PartsTab(int mechSlot, String texture) {
         this.mechSlot = mechSlot;
         this.icon = new ResourceLocation("pomkotsmechs", texture);
      }
   }
}
