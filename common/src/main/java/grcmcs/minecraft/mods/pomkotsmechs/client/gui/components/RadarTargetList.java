package grcmcs.minecraft.mods.pomkotsmechs.client.gui.components;

import grcmcs.minecraft.mods.pomkotsmechs.client.gui.RadarTargetSelectScreen;
import grcmcs.minecraft.mods.pomkotsmechs.items.radar.RadarTarget;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class RadarTargetList extends AbstractPomkotsSelectionList<RadarTargetList.Entry> {
   public static final int LIST_X = 100;
   public static final int LIST_Y = 35;
   public static final int LIST_WIDTH = 260;
   public static final int LIST_HEIGHT = 180;
   public static final int ROW_HEIGHT = 22;
   private final RadarTargetSelectScreen parent;

   public RadarTargetList(RadarTargetSelectScreen parent, Minecraft minecraft) {
      super(parent, minecraft, 100, 35, 260, 180, 22);
      this.parent = parent;
   }

   public void addTargets(List<RadarTarget> targets) {
      this.clearEntries();

      for (int i = 0; i < targets.size(); i++) {
         this.addEntry(new RadarTargetList.Entry(this.parent, targets.get(i), i));
      }
   }

   public static class Entry extends AbstractPomkotsSelectionList.PomkotsEntry<RadarTargetList.Entry> {
      private static final int TEXT_X = 8;
      private static final int TEXT_Y = 7;
      private final RadarTarget target;
      private final int index;
      private final RadarTargetSelectScreen parent;

      public Entry(RadarTargetSelectScreen parent, RadarTarget target, int index) {
         this.parent = parent;
         this.target = target;
         this.index = index;
      }

      @Override
      public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
         super.render(graphics, index, top, left, width, height, mouseX, mouseY, hovered, partialTick);
         graphics.drawString(
            Minecraft.getInstance().font, this.target.label() + " : " + this.dimensionName(this.target.dimension()), left + 8, top + 7, 16777215, false
         );
      }

      public boolean mouseClicked(double mouseX, double mouseY, int button) {
         this.parent.onTargetSelected(this.index);
         return true;
      }

      public String dimensionName(ResourceLocation dim) {
         return switch (dim.getPath()) {
            case "overworld" -> "Over World";
            case "the_nether" -> "The Nether";
            case "the_end" -> "The End";
            default -> dim.getPath();
         };
      }
   }
}
