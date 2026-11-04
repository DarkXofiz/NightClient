package dev.nightclient.gui;

import dev.nightclient.NightClient;
import dev.nightclient.module.Category;
import dev.nightclient.module.Module;
import dev.nightclient.render.Render2D;
import dev.nightclient.render.Theme;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;

/** Küçük, sürüklenebilir kategori penceresi. */
public final class CategoryWindow {
   public static final int WIDTH = 140;
   public static final int HEADER = 20;
   private final Category category;
   private final List<ModuleButton> buttons = new ArrayList<>();
   private float x;
   private float y;
   private float scroll;
   private float maxHeight = 260.0F;
   private boolean dragging;
   private float dragDX;
   private float dragDY;

   public CategoryWindow(Category category, float x, float y) {
      this.category = category;
      this.x = x;
      this.y = y;

      for (Module module : NightClient.byCategory(category)) {
         this.buttons.add(new ModuleButton(module));
      }
   }

   public void setPosition(float x, float y) {
      this.x = x;
      this.y = y;
   }

   public void setMaxHeight(float maxHeight) {
      this.maxHeight = maxHeight;
   }

   private float bodyHeight() {
      float h = 3.0F;

      for (ModuleButton b : this.buttons) {
         h += b.height();
      }

      return h + 3.0F;
   }

   private float visibleBody() {
      return Math.min(this.bodyHeight(), this.maxHeight - (float)HEADER);
   }

   public float totalHeight() {
      return (float)HEADER + this.visibleBody();
   }

   private void place() {
      float maxScroll = Math.max(0.0F, this.bodyHeight() - this.visibleBody());
      this.scroll = Math.max(0.0F, Math.min(maxScroll, this.scroll));
      float yy = this.y + (float)HEADER + 3.0F - this.scroll;

      for (ModuleButton b : this.buttons) {
         b.setBounds(this.x, yy, (float)WIDTH);
         yy += b.height();
      }
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {
      float h = this.totalHeight();
      Render2D.roundedRect(context, this.x - 1.0F, this.y - 1.0F, (float)WIDTH + 2.0F, h + 2.0F, 8.0F, Theme.scaleAlpha(0xFF000000, alpha * 0.35F));
      Render2D.roundedRect(context, this.x, this.y, (float)WIDTH, h, 7.0F, Theme.scaleAlpha(0xFF0F0F14, alpha));
      Render2D.roundedOutline(context, this.x, this.y, (float)WIDTH, h, 7.0F, 1.0F, Theme.scaleAlpha(0xFF23232C, alpha));

      // başlık: yumuşak degrade çizgi + başlık yazısı
      for (int i = 0; i < WIDTH - 12; i += 2) {
         float t = (float)i / (float)(WIDTH - 12);
         int c = Theme.withAlpha(Theme.ACCENT, (int)(220.0F * (1.0F - t)));
         context.fill((int)(this.x + 6.0F) + i, (int)(this.y + (float)HEADER - 2.0F), (int)(this.x + 6.0F) + i + 2, (int)(this.y + (float)HEADER - 1.0F), Theme.scaleAlpha(c, alpha));
      }

      Render2D.circle(context, this.x + 11.0F, this.y + 9.5F, 2.0F, Theme.scaleAlpha(Theme.ACCENT, alpha));
      Render2D.text(context, this.category.label, this.x + 18.0F, this.y + 6.0F, Theme.scaleAlpha(0xFFE8E8F0, alpha), true);
      String count = Integer.toString(this.buttons.size());
      Render2D.text(context, count, this.x + (float)WIDTH - 9.0F - (float)Render2D.textWidth(count), this.y + 6.0F, Theme.scaleAlpha(0xFF55555F, alpha));

      this.place();
      context.enableScissor((int)this.x, (int)(this.y + (float)HEADER), (int)(this.x + (float)WIDTH), (int)(this.y + h - 1.0F));
      int my = mouseY >= (int)(this.y + (float)HEADER) && (float)mouseY <= this.y + h ? mouseY : -9999;
      for (ModuleButton b : this.buttons) {
         b.render(context, mouseX, my, delta, alpha);
      }

      context.disableScissor();
   }

   private boolean inside(double mx, double my) {
      return mx >= (double)this.x && mx <= (double)(this.x + (float)WIDTH) && my >= (double)this.y && my <= (double)(this.y + this.totalHeight());
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.inside(mouseX, mouseY)) {
         return false;
      }

      if (mouseY < (double)(this.y + (float)HEADER)) {
         if (button == 0) {
            this.dragging = true;
            this.dragDX = (float)mouseX - this.x;
            this.dragDY = (float)mouseY - this.y;
         }

         return true;
      }

      this.place();
      for (ModuleButton b : this.buttons) {
         if (b.mouseClicked(mouseX, mouseY, button)) {
            return true;
         }
      }

      return true;
   }

   public void mouseDragged(double mouseX, double mouseY, int button) {
      if (this.dragging && button == 0) {
         this.x = (float)mouseX - this.dragDX;
         this.y = (float)mouseY - this.dragDY;
      } else {
         for (ModuleButton b : this.buttons) {
            b.mouseDragged(mouseX, mouseY, button);
         }
      }
   }

   public void mouseReleased() {
      this.dragging = false;

      for (ModuleButton b : this.buttons) {
         b.mouseReleased();
      }
   }

   public void mouseScrolled(double mouseX, double mouseY, double amount) {
      if (this.inside(mouseX, mouseY)) {
         this.scroll -= (float)(amount * 16.0);
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      for (ModuleButton b : this.buttons) {
         if (b.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
         }
      }

      return false;
   }

   public boolean charTyped(char chr) {
      for (ModuleButton b : this.buttons) {
         if (b.charTyped(chr)) {
            return true;
         }
      }

      return false;
   }

   public void resetAnimations() {
      for (ModuleButton b : this.buttons) {
         b.resetAnimations();
      }
   }
}
