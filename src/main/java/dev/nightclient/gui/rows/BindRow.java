package dev.nightclient.gui.rows;

import dev.nightclient.NightClient;
import dev.nightclient.gui.ModuleButton;
import dev.nightclient.module.Module;
import dev.nightclient.module.setting.KeySetting;
import dev.nightclient.render.Render2D;
import dev.nightclient.render.Theme;
import dev.nightclient.util.ConfigManager;
import net.minecraft.client.gui.DrawContext;

/** Modülün kısayol tuşunu atar. Tıkla, bir tuşa bas. Esc/Backspace/Delete tuşu siler. */
public final class BindRow extends SettingRow {
   private final Module module;
   private final KeySetting display;

   public BindRow(Module module, ModuleButton parent) {
      super(new KeySetting("Bind", module.getKey()), parent);
      this.module = module;
      this.display = (KeySetting)this.setting;
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      Render2D.text(context, "Bind", this.x, this.y + 7.0F, -8750457);
      this.display.setKey(this.display.isListening() ? this.display.getKey() : this.module.getKey());
      String value = this.display.isListening() ? "press a key" : this.display.display();
      int color = this.display.isListening() ? Theme.ACCENT : -1447440;
      Render2D.text(context, value, this.x + this.width - (float)Render2D.textWidth(value), this.y + 7.0F, color);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.isHovered(mouseX, mouseY)) {
         return false;
      } else if (this.display.isListening()) {
         this.display.setListening(false);
         return true;
      } else if (button == 0) {
         this.display.setListening(true);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean capturingInput() {
      return this.display.isListening();
   }

   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      if (!this.display.isListening()) {
         return false;
      } else {
         int key = keyCode == 256 || keyCode == 259 || keyCode == 261 ? -1 : keyCode;
         this.module.key(key);
         this.display.setKey(key);
         ConfigManager.save(NightClient.modules());
         return true;
      }
   }
}
