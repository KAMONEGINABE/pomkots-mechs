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
      super(Component.empty());
   }

   public void tick() {
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
         this.minecraft.setScreen(null);
      }
   }

   public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks) {
      gfx.fill(0, 0, this.width, this.height, -16777216);
      if (this.lineIndex / 4 < BACKGROUNDS.length) {
         gfx.blit(BACKGROUNDS[this.lineIndex / 4], 0, 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);
      }

      if (this.lineIndex < NARRATION.size() && this.alpha != 0) {
         int color = this.alpha << 24 | 16777215;
         gfx.drawCenteredString(this.font, NARRATION.get(this.lineIndex), this.width / 2, this.height / 2, color);
      }

      super.render(gfx, mouseX, mouseY, partialTicks);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (keyCode == 256) {
         this.minecraft.setScreen(null);
         return true;
      } else {
         return super.keyPressed(keyCode, scanCode, modifiers);
      }
   }
}
