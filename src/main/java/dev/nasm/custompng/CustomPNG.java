package dev.nasm.custompng;

import com.mojang.logging.LogUtils;
import dev.nasm.custompng.hud.ElementCustomPNG;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import org.slf4j.Logger;

public class CustomPNG extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final HudGroup HUD_GROUP = new HudGroup("Custom PNG");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Custom PNG");

        // Modules
        Hud.get().register(ElementCustomPNG.INFO);
    }

    @Override
    public String getPackage() {
        return "dev.nasm.custompng";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("netwideassembler", "custom-png-meteor");
    }
}
