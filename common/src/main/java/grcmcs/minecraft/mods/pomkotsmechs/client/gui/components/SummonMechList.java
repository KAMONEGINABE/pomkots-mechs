package grcmcs.minecraft.mods.pomkotsmechs.client.gui.components;

import com.mojang.datafixers.util.Pair;
import grcmcs.minecraft.mods.pomkotsmechs.client.gui.MechSalvagerScreen;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class SummonMechList extends AbstractPomkotsSelectionList<SummonMechList.Entry> {
   public static final int LIST_X = 58;
   public static final int LIST_Y = 60;
   public static final int LIST_WIDTH = 145;
   public static final int LIST_HEIGHT = 150;
   public static final int ROW_HEIGHT = 22;
   private final MechSalvagerScreen parent;

   public SummonMechList(MechSalvagerScreen parent, Minecraft minecraft) {
      super(parent, minecraft, 58, 60, 145, 150, 22);
      this.parent = parent;
   }

   public void addTargets(List<Pair<UUID, String>> targets) {
      this.m_93516_();

      for (int i = 0; i < targets.size(); i++) {
         Pair<UUID, String> target = targets.get(i);
         this.m_7085_(new SummonMechList.Entry(this.parent, (UUID)target.getFirst(), (String)target.getSecond(), i));
      }
   }

   public int getSize() {
      return this.m_5773_();
   }

   public static class Entry extends AbstractPomkotsSelectionList.PomkotsEntry<SummonMechList.Entry> {
      private static final int TEXT_X = 8;
      private static final int TEXT_Y = 7;
      private final UUID mechId;
      private final String mechName;
      private final int index;
      private final MechSalvagerScreen parent;

      public Entry(MechSalvagerScreen parent, UUID id, String name, int index) {
         this.parent = parent;
         this.mechId = id;
         this.mechName = name;
         this.index = index;
      }

      @Override
      public void m_6311_(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {
         super.m_6311_(graphics, index, top, left, width, height, mouseX, mouseY, hovered, partialTick);
         graphics.m_280056_(Minecraft.m_91087_().f_91062_, "Name : " + this.mechName + " / ID : " + this.mechId, left + 8, top + 7, 16777215, false);
      }

      public boolean m_6375_(double mouseX, double mouseY, int button) {
         System.out.println(this.mechName);
         this.parent.setSelected(this.mechId);
         return true;
      }
   }
}
