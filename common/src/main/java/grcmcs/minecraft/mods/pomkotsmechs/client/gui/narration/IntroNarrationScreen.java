package grcmcs.minecraft.mods.pomkotsmechs.client.gui.narration;

import dev.architectury.platform.Platform;
import grcmcs.minecraft.mods.pomkotsmechs.client.sound.PomkotsBGMManager;
import grcmcs.minecraft.mods.pomkotsmechs.sounds.bgm.BGMState;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class IntroNarrationScreen extends Screen {
   private final List<Cut> cutsDefinition = List.of(
      new Cut.ImageCut(null, List.of("{text.pomkotsmechs.gui.opening.00}"), 40, 40, 20),
      new Cut.DarkCut(20),
      new Cut.ImageCut(
         "pomkotsmechs:textures/scene/intro_01.png", List.of("{text.pomkotsmechs.gui.opening.01.1}", "{text.pomkotsmechs.gui.opening.01.2}"), 20, 60, 20
      ),
      new Cut.DarkCut(20),
      new Cut.ImageCut("pomkotsmechs:textures/scene/intro_02_01.png", List.of("{text.pomkotsmechs.gui.opening.02}"), 20, 80, 20),
      new Cut.ImageCut("pomkotsmechs:textures/scene/intro_02_02.png", List.of("{text.pomkotsmechs.gui.opening.03}"), 20, 80, 20),
      new Cut.ImageCut("pomkotsmechs:textures/scene/intro_03.png", List.of("{text.pomkotsmechs.gui.opening.04}"), 20, 80, 20),
      new Cut.DarkCut(20),
      new Cut.ImageCut(null, List.of("{text.pomkotsmechs.gui.opening.05}"), 20, 80, 20),
      new Cut.DarkCut(20),
      new Cut.ImageCut("pomkotsmechs:textures/scene/intro_04_01.png", List.of("{text.pomkotsmechs.gui.opening.06}"), 20, 80, 20),
      new Cut.ImageCut("pomkotsmechs:textures/scene/intro_04_02.png", List.of("{text.pomkotsmechs.gui.opening.07}"), 20, 80, 20),
      new Cut.ImageCut("pomkotsmechs:textures/scene/intro_04_03.png", List.of("{text.pomkotsmechs.gui.opening.08}"), 20, 80, 20),
      new Cut.DarkCut(20),
      new Cut.ImageCut(null, List.of("{text.pomkotsmechs.gui.opening.09}"), 20, 60, 20)
   );
   private int cutIndex = 0;
   private int tickNum = 0;

   public IntroNarrationScreen() {
      super(Component.empty());
      this.tickNum = 0;
   }

   public void tick() {
      if (this.tickNum == 10) {
         PomkotsBGMManager.setState(BGMState.OPENING);
      }

      if (this.cutIndex >= this.cutsDefinition.size()) {
         this.quitIntro();
      } else {
         Cut current = this.cutsDefinition.get(this.cutIndex);
         current.tick(this);
         if (current.isFinished()) {
            this.cutIndex++;
         }

         this.tickNum++;
      }
   }

   private void quitIntro() {
      if (Platform.isForge()) {
         Minecraft mc = Minecraft.getInstance();
         mc.tell(() -> mc.setScreen(null));
      } else {
         this.minecraft.setScreen(null);
      }

      PomkotsBGMManager.setState(BGMState.RUIN_CITY);
   }

   public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
      gfx.fill(0, 0, this.width, this.height, -16777216);
      if (this.cutIndex < this.cutsDefinition.size()) {
         this.cutsDefinition.get(this.cutIndex).render(gfx, this, this.font, partialTicks);
      }

      super.render(gfx, mouseX, mouseY, partialTicks);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.quitIntro();
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }
}
