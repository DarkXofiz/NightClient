package dev.nightclient.gui;

import dev.nightclient.NightClient;
import dev.nightclient.gui.rows.SettingRow;
import dev.nightclient.module.Module;
import dev.nightclient.module.setting.Setting;
import dev.nightclient.render.Animation;
import dev.nightclient.render.Render2D;
import dev.nightclient.render.Theme;
import dev.nightclient.util.ConfigManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.DrawContext;

/** Kompakt modül satırı: sol tık = aç/kapa, sağ tık = ayarlar. */
public final class ModuleButton {
   public static final int HEADER = 18;
   private final Module module;
   private final List<SettingRow> rows = new ArrayList<>();
   private final Animation expand = new Animation(0.0F);
   private final Animation hover = new Animation(0.0F);
   private final Animation enable;
   private float x;
   private float y;
   private float width;
   private boolean expanded;

   public ModuleButton(Module module) {
      this.module = module;
      this.enable = new Animation(module.isEnabled() ? 1.0F : 0.0F);

      for (Setting<?> setting : module.settings()) {
         this.rows.add(SettingRow.create(setting, this));
      }
   }

   public Module module() {
      return this.module;
   }

   public void setBounds(float x, float y, float width) {
      this.x = x;
      this.y = y;
      this.width = width;
   }

   public float y() {
      return this.y;
   }

   public float height() {
      return (float)HEADER + this.expand.get();
   }

   private float settingsHeight() {
      float height = 0.0F;

      for (SettingRow row : this.rows) {
         if (row.setting().isVisible()) {
            height += 22.0F;
         }
      }

      return height > 0.0F ? height + 3.0F : 0.0F;
   }

   public boolean isHeaderHovered(double mx, double my) {
      return mx >= (double)this.x && mx <= (double)(this.x + this.width) && my >= (double)this.y && my < (double)(this.y + (float)HEADER);
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta, float alpha) {
      float h = this.hover.animate(this.isHeaderHovered((double)mouseX, (double)mouseY) ? 1.0F : 0.0F, 14.0F);
      float on = this.enable.animate(this.module.isEnabled() ? 1.0F : 0.0F, 12.0F);
      this.expand.animate(this.expanded ? this.settingsHeight() : 0.0F, 14.0F);
      float total = this.height();

      if (h > 0.02F || on > 0.02F) {
         int bg = Theme.mix(Theme.withAlpha(0xFFFFFF, (int)(14.0F * h)), Theme.withAlpha(Theme.ACCENT, 34), on);
         Render2D.roundedRect(context, this.x + 3.0F, this.y + 1.0F, this.width - 6.0F, total - 2.0F, 5.0F, Theme.scaleAlpha(bg, alpha));
      }

      int nameColor = Theme.mix(Theme.mix(0xFF858592, 0xFFE8E8F0, h), 0xFFFFFFFF, on);
      Render2D.text(context, this.module.getName(), this.x + 10.0F, this.y + 5.0F, Theme.scaleAlpha(nameColor, alpha));

      // sağda küçük açma/kapama hapı
      float pillW = 16.0F;
      float pillX = this.x + this.width - pillW - 8.0F;
      float pillY = this.y + 5.0F;
      Render2D.roundedRect(context, pillX, pillY, pillW, 8.0F, 4.0F, Theme.scaleAlpha(Theme.mix(0xFF23232C, Theme.ACCENT, on), alpha));
      Render2D.circle(context, pillX + 4.0F + 8.0F * on, pillY + 4.0F, 2.6F, Theme.scaleAlpha(Theme.mix(0xFF6A6A77, 0xFFFFFFFF, on), alpha));

      if (!this.rows.isEmpty()) {
         float cx = pillX - 8.0F;
         int dot = Theme.scaleAlpha(this.expanded ? Theme.ACCENT : Theme.mix(0xFF4A4A55, 0xFFB0B0BC, h), alpha);
         for (int i = 0; i < 3; i++) {
            Render2D.circle(context, cx, this.y + 5.5F + (float)i * 3.5F, 1.0F, dot);
         }
      }

      if (this.expand.get() > 0.5F) {
         context.enableScissor((int)this.x, (int)(this.y + (float)HEADER), (int)(this.x + this.width), (int)(this.y + total));
         float rowY = this.y + (float)HEADER;

         for (SettingRow row : this.rows) {
            if (row.setting().isVisible()) {
               row.setBounds(this.x + 10.0F, rowY, this.width - 20.0F);
               row.render(context, mouseX, mouseY, delta);
               rowY += 22.0F;
            }
         }

         context.disableScissor();
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (this.expanded && mouseY > (double)(this.y + (float)HEADER) && mouseY < (double)(this.y + this.height())) {
         for (SettingRow row : this.rows) {
            if (row.setting().isVisible() && row.mouseClicked(mouseX, mouseY, button)) {
               return true;
            }
         }

         return true;
      } else if (!this.isHeaderHovered(mouseX, mouseY)) {
         return false;
      } else {
         if (button == 0) {
            this.module.toggle();
            ConfigManager.save(NightClient.modules());
         } else if (button == 1 && !this.rows.isEmpty()) {
            this.expanded = !this.expanded;
         }

         return true;
      }
   }

   public void mouseDragged(double mouseX, double mouseY, int button) {
      if (this.expanded) {
         for (SettingRow row : this.rows) {
            if (row.setting().isVisible()) {
               row.mouseDragged(mouseX, mouseY, button);
            }
         }
      }
   }

   public void mouseReleased() {
      for (SettingRow row : this.rows) {
         row.mouseReleased();
      }
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      for (SettingRow row : this.rows) {
         if (row.setting().isVisible() && row.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
         }
      }

      return false;
   }

   public boolean charTyped(char chr) {
      for (SettingRow row : this.rows) {
         if (row.setting().isVisible() && row.charTyped(chr)) {
            return true;
         }
      }

      return false;
   }

   public void resetAnimations() {
      this.expand.set(this.expanded ? this.settingsHeight() : 0.0F);
      this.hover.set(0.0F);
      this.enable.set(this.module.isEnabled() ? 1.0F : 0.0F);
   }
}
