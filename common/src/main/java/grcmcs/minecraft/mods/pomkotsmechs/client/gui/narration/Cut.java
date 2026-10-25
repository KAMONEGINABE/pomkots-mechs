package grcmcs.minecraft.mods.pomkotsmechs.client.gui.narration;

import com.mojang.blaze3d.systems.RenderSystem;
import grcmcs.minecraft.mods.pomkotsmechs.util.Utils;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public abstract class Cut {
   public abstract void tick(IntroNarrationScreen var1);

   public abstract void render(GuiGraphics var1, IntroNarrationScreen var2, Font var3, float var4);

   public abstract boolean isFinished();

   public static class DarkCut extends Cut {
      private final int duration;
      private int tick = 0;

      public DarkCut(int duration) {
         this.duration = duration;
      }

      @Override
      public void tick(IntroNarrationScreen screen) {
         this.tick++;
      }

      @Override
      public void render(GuiGraphics gfx, IntroNarrationScreen screen, Font font, float partialTicks) {
         gfx.m_280509_(0, 0, screen.f_96543_, screen.f_96544_, -16777216);
      }

      @Override
      public boolean isFinished() {
         return this.tick >= this.duration;
      }
   }

   public static class ImageCut extends Cut {
      private final ResourceLocation background;
      private final List<String> lines;
      private final int fadeInTicks;
      private final int stayTicks;
      private final int fadeOutTicks;
      private int tick = 0;
      private int lineIndex = 0;
      private float alpha = 0.0F;
      private boolean fadingIn = true;
      private boolean fadingOut = false;
      private static final int IMG_WIDTH = 571;
      private static final int IMG_HEIGHT = 300;

      public ImageCut(String backgroundPath, List<String> lines, int fadeIn, int stay, int fadeOut) {
         if (backgroundPath != null) {
            this.background = new ResourceLocation(backgroundPath);
         } else {
            this.background = null;
         }

         this.lines = lines;
         this.fadeInTicks = fadeIn;
         this.stayTicks = stay;
         this.fadeOutTicks = fadeOut;
      }

      @Override
      public void tick(IntroNarrationScreen screen) {
         this.tick++;
         if (this.fadingIn) {
            this.alpha = this.alpha + 1.0F / this.fadeInTicks;
            if (this.alpha >= 1.0F) {
               this.alpha = 1.0F;
               this.fadingIn = false;
               this.tick = 0;
            }
         } else if (this.tick >= this.stayTicks) {
            this.alpha = this.alpha - 1.0F / this.fadeOutTicks;
            this.fadingOut = true;
            if (this.alpha <= 0.0F) {
               this.alpha = 0.0F;
               this.nextLine();
            }
         }
      }

      private void nextLine() {
         this.tick = 0;
         this.fadingIn = true;
         this.fadingOut = false;
         this.lineIndex++;
      }

      private boolean shouldFadeImage() {
         return this.lineIndex == 0 && this.fadingIn || this.lineIndex == this.lines.size() - 1 && this.fadingOut;
      }

      @Override
      public void render(GuiGraphics gfx, IntroNarrationScreen screen, Font font, float partialTicks) {
         Minecraft mc = Minecraft.m_91087_();
         int screenWidth = mc.m_91268_().m_85445_();
         int screenHeight = mc.m_91268_().m_85446_();
         if (this.background != null) {
            if (this.shouldFadeImage()) {
               RenderSystem.enableBlend();
               RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            }

            gfx.m_280163_(this.background, 0, 0, 0.0F, 0.0F, screenWidth, screenHeight, screenWidth, screenHeight);
            if (this.shouldFadeImage()) {
               RenderSystem.disableBlend();
            }

            if (this.lineIndex < this.lines.size() && this.alpha != 0.0F) {
               int color = (int)(this.alpha * 255.0F) << 24 | 16777215;
               gfx.m_280137_(font, this.getLocalizedString(this.lines.get(this.lineIndex)), screen.f_96543_ / 2, screen.f_96544_ / 2, color);
            }
         } else if (this.lineIndex < this.lines.size() && this.alpha != 0.0F) {
            int color = (int)(this.alpha * 255.0F) << 24 | 16777215;
            gfx.m_280137_(font, this.getLocalizedString(this.lines.get(this.lineIndex)), screen.f_96543_ / 2, screen.f_96544_ / 2, color);
         }
      }

      private String getLocalizedString(String id) {
         return id != null && !id.isEmpty() ? Utils.string2Component(id).getString() : "";
      }

      @Override
      public boolean isFinished() {
         return this.lineIndex >= this.lines.size();
      }
   }
}
