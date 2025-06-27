package dev.nasm.custompng.modules;

import dev.nasm.custompng.CustomPNG;
import dev.nasm.custompng.util.ImageUtils;
import me.x150.renderer.render.Renderer2d;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import java.util.UUID;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomPNGModule extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgPosition = settings.createGroup("Position");

    private final Setting<String> imageUrl = sgGeneral.add(new StringSetting.Builder()
        .name("image-url")
        .description("URL of the image to display")
        .defaultValue("https://i.ibb.co/ymXKsV61/175103166049165646.png")
        .onChanged(s -> loadImage())
        .build()
    );

    private final Setting<Boolean> clickGUIOnly = sgRender.add(new BoolSetting.Builder()
        .name("ClickGUI Only")
        .description("Only render the PNG in the Click GUI")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> opacity = sgRender.add(new DoubleSetting.Builder()
        .name("opacity")
        .description("Opacity of the image")
        .defaultValue(1.0)
        .min(0.0)
        .max(1.0)
        .build()
    );

    private final Setting<Double> scale = sgRender.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Scale of the image")
        .defaultValue(0.6)
        .min(0.1)
        .max(5.0)
        .build()
    );

    private final Setting<Double> posX = sgPosition.add(new DoubleSetting.Builder()
        .name("x-position")
        .description("Horizontal position (0-100%) - 0 is left, 100 is right")
        .defaultValue(86.0)
        .min(0.0)
        .max(100.0)
        .sliderRange(0.0, 100.0)
        .build()
    );

    private final Setting<Double> posY = sgPosition.add(new DoubleSetting.Builder()
        .name("y-position")
        .description("Vertical position (0-100%) - 0 is top, 100 is bottom")
        .defaultValue(70.0)
        .min(0.0)
        .max(100.0)
        .sliderRange(0.0, 100.0)
        .build()
    );

    private Identifier textureId;

    // fallback sizes if it cant fetch the shit
    private int imageWidth = 100;
    private int imageHeight = 100;
    private boolean loading = false;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CustomPNGModule() {
        super(CustomPNG.CATEGORY, "custom-png", "Renders a custom image from URL.");
    }

    @Override
    public void onActivate() {
        loadImage();
    }



    private void loadImage() {
        if (loading) return;
        loading = true;

        executor.execute(() -> {
            try {
                BufferedImage bufferedImage = ImageUtils.loadBufferedImage(imageUrl.get());

                if (bufferedImage == null) {
                    System.err.println("Failed to load image from URL: " + imageUrl.get());
                    loading = false;
                    return;
                }

                imageWidth = bufferedImage.getWidth();
                imageHeight = bufferedImage.getHeight();

                NativeImage nativeImage = ImageUtils.bufferedImageToNativeImage(bufferedImage);

                Util.getMainWorkerExecutor().execute(() -> {
                    if (textureId != null) {
                        mc.getTextureManager().destroyTexture(textureId);
                        textureId = null;
                    }

                    textureId = Identifier.of("custompng", "img_" + UUID.randomUUID());
                    mc.getTextureManager().registerTexture(
                        textureId,
                        new NativeImageBackedTexture(nativeImage)
                    );

                    loading = false;
                });

            } catch (IOException e) {
                System.err.println("Error loading image from URL: " + e.getMessage());
                loading = false;
            }
        });
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (!(mc.currentScreen instanceof WidgetScreen) && clickGUIOnly.get() || textureId == null || loading) return;

        float screenWidth = mc.getWindow().getWidth();
        float screenHeight = mc.getWindow().getHeight();

        float scaledWidth = (float)(imageWidth * scale.get());
        float scaledHeight = (float)(imageHeight * scale.get());

        float xPos = (float)((posX.get() / 100.0) * (screenWidth - scaledWidth));
        float yPos = (float)((posY.get() / 100.0) * (screenHeight - scaledHeight));

        xPos = Math.max(0, Math.min(xPos, screenWidth - scaledWidth));
        yPos = Math.max(0, Math.min(yPos, screenHeight - scaledHeight));

        Renderer2d.renderTexture(
            event.drawContext.getMatrices(),
            textureId,
            xPos,
            yPos,
            scaledWidth,
            scaledHeight
        );
    }

    @Override
    public void onDeactivate() {
        if (textureId != null) {
            mc.getTextureManager().destroyTexture(textureId);
            textureId = null;
        }
    }
}
