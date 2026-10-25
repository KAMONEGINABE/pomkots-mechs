package grcmcs.minecraft.mods.pomkotsmechs.client.gui;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class IntroNarrationScreen extends Screen {
   private static final List<String> NARRATION = List.of(
      "──遠い遠い未来。けれど、どこか身近な星で──",
      "",
      "人類はかつて、宇宙の果てへと手を伸ばし、",
      "飽くなき欲望と無限の技術で、星々の時代を築いていった",
      "",
      "しかし、どれほど文明が進化しようと、争いが無くなる事はなかった",
      "理解し合えるはずの人々は、より深く傷つけ合い、",
      "そして、終わりの日が訪れた",
      "国家は滅び、企業は崩壊し、大地は焼かれた",
      "",
      "それから百年が経ち、緑が廃墟を覆いつくした頃",
      "遺された無人兵器は今なお稼働し、人類を“敵”として行動している",
      "人々はその影に怯えながら、それでも強かに生き延びていた",
      "",
      "そして今日、ひとりの若者がシェルターで成人を迎える"
   );
   private int tickCounter = 0;
   private int lineIndex = 0;
   private int alpha = 0;
   private static final ResourceLocation[] BACKGROUNDS = new ResourceLocation[]{
      new ResourceLocation("mymod:textures/gui/intro/scene1.png"), new ResourceLocation("mymod:textures/gui/intro/scene2.png")
   };

   public IntroNarrationScreen() {
      super(Component.m_237119_());
   }

   public void m_86600_() {
      this.tickCounter++;
      if (this.tickCounter < 20) {
         if (this.alpha < 255) {
            this.alpha += 10;
         }
      } else if (this.tickCounter >= 100 && this.tickCounter < 120) {
         if (this.alpha >= 10) {
            this.alpha -= 10;
         }
      } else if (this.tickCounter >= 120) {
         this.nextLine();
      }
   }

   private void nextLine() {
      this.alpha = 0;
      this.tickCounter = 0;
      this.lineIndex++;
      if (this.lineIndex >= NARRATION.size()) {
         this.f_96541_.m_91152_(null);
      }
   }

   public void m_88315_(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
      gfx.m_280509_(0, 0, this.f_96543_, this.f_96544_, -16777216);
      if (this.lineIndex / 4 < BACKGROUNDS.length) {
         gfx.m_280163_(BACKGROUNDS[this.lineIndex / 4], 0, 0, 0.0F, 0.0F, this.f_96543_, this.f_96544_, this.f_96543_, this.f_96544_);
      }

      if (this.lineIndex < NARRATION.size() && this.alpha != 0) {
         int color = this.alpha << 24 | 16777215;
         gfx.m_280137_(this.f_96547_, NARRATION.get(this.lineIndex), this.f_96543_ / 2, this.f_96544_ / 2, color);
      }

      super.m_88315_(gfx, mouseX, mouseY, partialTicks);
   }

   public boolean m_7933_(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.f_96541_.m_91152_(null);
         return true;
      } else {
         return super.m_7933_(keyCode, scanCode, modifiers);
      }
   }
}
