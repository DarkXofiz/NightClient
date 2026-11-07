package dev.nightclient.module.modules.client;

import dev.nightclient.gui.HudEditorScreen;
import dev.nightclient.gui.NightGui;
import dev.nightclient.module.Category;
import dev.nightclient.module.Module;
import dev.nightclient.module.setting.BooleanSetting;
import dev.nightclient.module.setting.Setting;

public final class Fps15 extends Module {
   private final BooleanSetting blur = new BooleanSetting("Blur", true).describe("Blurred game behind the GUI");
   private final BooleanSetting descriptions = new BooleanSetting("Descriptions", true).describe("Tooltip with the module / setting description");
   private boolean closing;

   public Fps15() {
      super("ClickGUI", "Opens the nightclient interface", Category.CLIENT);
      this.addSettings(new Setting[]{this.blur, this.descriptions});
      this.key(-1);
   }

   public boolean blur() {
      return this.blur.get();
   }

   public boolean descriptions() {
      return this.descriptions.get();
   }

   @Override
   protected boolean playsToggleSound() {
      return false;
   }

   @Override
   protected boolean persistsEnabled() {
      return false;
   }

   @Override
   public void onEnable() {
      if (!(this.mc.currentScreen instanceof NightGui) && !(this.mc.currentScreen instanceof HudEditorScreen)) {
         this.mc.setScreen(new NightGui());
      }
   }

   @Override
   public void onDisable() {
      if (this.mc.currentScreen instanceof NightGui) {
         this.closing = true;
         this.mc.setScreen(null);
         this.closing = false;
      }
   }

   public void onScreenClosed() {
      if (!this.closing && this.isEnabled()) {
         this.setEnabled(false);
      }
   }
}
