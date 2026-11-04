package dev.nightclient.gui;

import dev.nightclient.NightClient;
import dev.nightclient.module.Category;
import dev.nightclient.module.modules.client.Fps15;
import dev.nightclient.render.Animation;
import dev.nightclient.render.Toasts;
import dev.nightclient.util.ConfigManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Combat ve HUD için iki küçük kategori penceresi. Sol tık: aç/kapa, sağ tık: ayarlar. */
public final class ClickGuiScreen extends Screen {
   private static final int GAP = 12;
   private final List<CategoryWindow> windows = new ArrayList<>();
   private final Animation open = new Animation(0.0F);

   public ClickGuiScreen() {
      super(Text.literal("NightClient"));
   }

   protected void init() {
      this.windows.clear();
      Category[] cats = Category.values();
      float total = (float)(cats.length * CategoryWindow.WIDTH + (cats.length - 1) * GAP);
      float x = ((float)this.width - total) / 2.0F;
      float y = Math.max(14.0F, ((float)this.height - 220.0F) / 2.0F);

      for (Category category : cats) {
         CategoryWindow window = new CategoryWindow(category, x, y);
         window.setMaxHeight(Math.max(120.0F, (float)this.height - y - 14.0F));
         window.resetAnimations();
         this.windows.add(window);
         x += (float)(CategoryWindow.WIDTH + GAP);
      }

      this.open.set(0.0F);
   }

   @Override
   public void renderBackground(DrawContext context) {
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      float alpha = this.open.animate(1.0F, 12.0F);
      context.fill(0, 0, this.width, this.height, ((int)(alpha * 110.0F) & 0xFF) << 24);

      for (CategoryWindow window : this.windows) {
         window.render(context, mouseX, mouseY, delta, alpha);
      }

      super.render(context, mouseX, mouseY, delta);
      Toasts.render(context);
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      for (int i = this.windows.size() - 1; i >= 0; i--) {
         CategoryWindow window = this.windows.get(i);
         if (window.mouseClicked(mouseX, mouseY, button)) {
            if (button == 0 && i != this.windows.size() - 1) {
               this.windows.remove(i);
               this.windows.add(window);
            }

            return true;
         }
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      for (CategoryWindow window : this.windows) {
         window.mouseDragged(mouseX, mouseY, button);
      }

      return true;
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      for (CategoryWindow window : this.windows) {
         window.mouseReleased();
      }

      ConfigManager.save(NightClient.modules());
      return super.mouseReleased(mouseX, mouseY, button);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      for (CategoryWindow window : this.windows) {
         window.mouseScrolled(mouseX, mouseY, amount);
      }

      return true;
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      for (CategoryWindow window : this.windows) {
         if (window.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
         }
      }

      return super.keyPressed(keyCode, scanCode, modifiers);
   }

   public boolean charTyped(char chr, int modifiers) {
      for (CategoryWindow window : this.windows) {
         if (window.charTyped(chr)) {
            return true;
         }
      }

      return super.charTyped(chr, modifiers);
   }

   public void close() {
      Fps15 module = NightClient.get(Fps15.class);
      if (module != null && module.isEnabled()) {
         module.setEnabled(false);
      } else {
         super.close();
      }
   }

   public void removed() {
      Fps15 module = NightClient.get(Fps15.class);
      if (module != null) {
         module.onScreenClosed();
      }

      ConfigManager.save(NightClient.modules());
   }

   public boolean shouldPause() {
      return false;
   }
}
