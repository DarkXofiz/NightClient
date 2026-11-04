package dev.nightclient.module.modules.client;

import dev.nightclient.gui.ClickGuiScreen;
import dev.nightclient.module.Category;
import dev.nightclient.module.Module;

public final class Fps15 extends Module {
   private boolean closing;

   public Fps15() {
      super("ClickGUI", "Opens the NightClient interface", Category.CLIENT);
      this.key(-1);
   }

   @Override
   public void onEnable() {
      if (!(this.getMC().currentScreen instanceof ClickGuiScreen)) {
         this.getMC().setScreen(new ClickGuiScreen());
      }
   }

   @Override
   public void onDisable() {
      if (this.getMC().currentScreen instanceof ClickGuiScreen) {
         this.closing = true;
         this.getMC().setScreen(null);
         this.closing = false;
      }
   }

   public void onScreenClosed() {
      if (!this.closing && this.isEnabled()) {
         this.setEnabled(false);
      }
   }
}
