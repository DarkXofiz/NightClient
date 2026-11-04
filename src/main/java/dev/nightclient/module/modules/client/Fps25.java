package dev.nightclient.module.modules.client;

import dev.nightclient.NightClient;
import dev.nightclient.gui.FriendsScreen;
import dev.nightclient.module.Category;
import dev.nightclient.module.Module;
import net.minecraft.client.MinecraftClient;

public final class Fps25 extends Module {
   private boolean closing;

   public Fps25() {
      super("Friends", "Arkadas listesini ac \u2014 secim bu isimlere vurmaz", Category.CLIENT);
   }

   @Override
   public void onEnable() {
      MinecraftClient client = this.getMC();
      if (client == null) {
         this.setEnabledSilently(false);
         return;
      }

      // ClickGUI açıksa onu sessizce kapalı say: ekran değişirken ikinci bir setScreen(null) tetiklenmesin.
      Fps15 gui = NightClient.get(Fps15.class);
      if (gui != null) {
         gui.setEnabledSilently(false);
      }

      // Ekranı tıklama olayının içinde değil, bir sonraki tick'te aç (çökme sebebi buydu).
      client.send(() -> {
         try {
            if (this.isEnabled()) {
               client.setScreen(new FriendsScreen());
            }
         } catch (Throwable t) {
            this.setEnabledSilently(false);
            client.setScreen(null);
         }
      });
   }

   @Override
   public void onDisable() {
      MinecraftClient client = this.getMC();
      if (client != null && client.currentScreen instanceof FriendsScreen) {
         this.closing = true;
         try {
            client.setScreen(null);
         } finally {
            this.closing = false;
         }
      }
   }

   public void onScreenClosed() {
      if (!this.closing && this.isEnabled()) {
         this.setEnabled(false);
      }
   }
}
