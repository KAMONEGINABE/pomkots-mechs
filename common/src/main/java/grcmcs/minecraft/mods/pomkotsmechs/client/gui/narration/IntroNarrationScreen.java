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
      super(Component.m_237119_());
      this.tickNum = 0;
   }

   public void m_86600_() {
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
         Minecraft mc = Minecraft.m_91087_();
         mc.m_6937_(() -> mc.m_91152_(null));
      } else {
         this.f_96541_.m_91152_(null);
      }

      PomkotsBGMManager.setState(BGMState.RUIN_CITY);
   }

   public void m_88315_(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
      gfx.m_280509_(0, 0, this.f_96543_, this.f_96544_, -16777216);
      if (this.cutIndex < this.cutsDefinition.size()) {
         this.cutsDefinition.get(this.cutIndex).render(gfx, this, this.f_96547_, partialTicks);
      }

      super.m_88315_(gfx, mouseX, mouseY, partialTicks);
   }

   public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.quitIntro();
         return true;
      } else {
         return super.m_7933_(keyCode, scanCode, modifiers);
      }
   }
}
