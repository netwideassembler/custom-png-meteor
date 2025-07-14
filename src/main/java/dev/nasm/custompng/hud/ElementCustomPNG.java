package dev.nasm.custompng.hud;

import dev.nasm.custompng.CustomPNG;
import dev.nasm.custompng.util.ImageUtils;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ElementCustomPNG extends HudElement {
    private Identifier textureId;
    private int imageWidth = 100;
    private int imageHeight = 100;
    private boolean loading = false;

    public static final HudElementInfo<ElementCustomPNG> INFO = new HudElementInfo<>(CustomPNG.HUD_GROUP, "custom-png", "Render a custom image in the Click GUI.", ElementCustomPNG::new);

    public ElementCustomPNG() {
        super(INFO);
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<String> imageUrl = sgGeneral.add(new StringSetting.Builder()
        .name("image-url")
        .description("URL of the image to display")
        .defaultValue("https://i.ibb.co/ymXKsV61/175103166049165646.png")
        .onChanged(s -> loadImage())
        .build()
    );

    private final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("color")
        .description("The tint and opacity of the image.")
        .defaultValue(new Color(255, 255, 255, 255))
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

    private final Setting<Boolean> clickGUIOnly = sgRender.add(new BoolSetting.Builder()
        .name("Click GUI Only")
        .description("Only render the PNG in the Click GUI")
        .defaultValue(true)
        .build()
    );

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

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

    //retard meteor doesn't have an onActivate() for hud so I need this stupid shit
    int i = 0;
    @Override
    public void render(HudRenderer renderer) {
        if(i == 0) {  loadImage(); i++; }
        if (!(mc.currentScreen instanceof WidgetScreen) && clickGUIOnly.get() || textureId == null || loading) return;

        float scaledWidth = (float)(imageWidth * scale.get());
        float scaledHeight = (float)(imageHeight * scale.get());

        setSize(scaledWidth, scaledHeight);

        renderer.texture(textureId, x, y, scaledWidth, scaledHeight, color.get());
    }
}
