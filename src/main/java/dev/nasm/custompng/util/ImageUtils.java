package dev.nasm.custompng.util;

import dev.nasm.custompng.mixin.NativeImageAccessor;
import net.minecraft.client.texture.NativeImage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class ImageUtils {

    public static BufferedImage loadBufferedImage(String url) throws IOException {
        URL imageUrl = new URL(url);
        return ImageIO.read(imageUrl);
    }

    public static NativeImage bufferedImageToNativeImage(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, width, height, true);
        NativeImageAccessor accessor = (NativeImageAccessor) (Object) nativeImage;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = bufferedImage.getRGB(x, y);
                if ((argb >> 24) == 0x00) {
                    accessor.invokeSetColor(x, y, 0);
                } else {
                    accessor.invokeSetColor(x, y, ARGBtoABGR(argb));
                }
            }
        }

        return nativeImage;
    }

    //retard minecraft uses ABGR
    public static int ARGBtoABGR(int argb) {
        int a = (argb >> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }
}
