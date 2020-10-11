package io.elves.http.server.handler.htt2;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static io.elves.common.util.Http2Util.toByteBuf;
import static io.netty.buffer.Unpooled.unreleasableBuffer;

public class ImageCache {
    public static ImageCache INSTANCE = new ImageCache();

    private final Map<String, ByteBuf> imageBank = new HashMap<String, ByteBuf>(200);

    private ImageCache() {
        init();
    }

    public static String name(int x, int y) {
        return "tile-" + y + "-" + x + ".jpeg";
    }

    public ByteBuf image(int x, int y) {
        return imageBank.get(name(x, y));
    }

    private void init() {
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 20; x++) {
                try {
                    String name = name(x, y);
                    ByteBuf fileBytes = unreleasableBuffer(toByteBuf(getClass()
                            .getResourceAsStream(name)).asReadOnly());
                    imageBank.put(name, fileBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
