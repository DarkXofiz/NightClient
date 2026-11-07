package dev.nightclient.render;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;

/** 1.20.1 için Tessellator yardımcı: tampon al ve başlat. */
public final class Gl {
   private Gl() {
   }

   public static BufferBuilder begin(VertexFormat.DrawMode mode, VertexFormat format) {
      BufferBuilder buffer = Tessellator.getInstance().getBuffer();
      buffer.begin(mode, format);
      return buffer;
   }
}
