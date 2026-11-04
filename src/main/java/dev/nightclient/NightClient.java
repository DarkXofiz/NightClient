package dev.nightclient;

import dev.nightclient.module.Category;
import dev.nightclient.module.Module;
import dev.nightclient.module.ModuleManager;
import dev.nightclient.module.modules.client.Fps15;
import dev.nightclient.module.modules.client.Fps25;
import dev.nightclient.util.ConfigManager;
import java.util.List;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class NightClient implements ClientModInitializer {
   public static final String MOD_ID = "nightclient";
   public static final String VERSION = "1.1";
   public static final String NAME = "NightClient";
   public static final ModuleManager MANAGER = new ModuleManager();

   public static ModuleManager get() {
      return MANAGER;
   }

   public static List<Module> modules() {
      return ModuleManager.modules();
   }

   public static <T extends Module> T get(Class<T> type) {
      return ModuleManager.get(type);
   }

   public static List<Module> byCategory(Category cat) {
      return ModuleManager.byCategory(cat);
   }

   public void onInitializeClient() {
      ModuleManager.init();
      ConfigManager.load(modules());
      Fps15 gui = get(Fps15.class);
      if (gui != null) {
         gui.setEnabledSilently(false);
      }

      Fps25 friends = get(Fps25.class);
      if (friends != null) {
         friends.setEnabledSilently(false);
      }

      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> ModuleManager.tick());
      HudRenderCallback.EVENT.register((HudRenderCallback)(ctx, tickDelta) -> ModuleManager.renderHUD(ctx, tickDelta));
   }
}
