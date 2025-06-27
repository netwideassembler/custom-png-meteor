package dev.nasm.custompng;

import com.mojang.logging.LogUtils;
import dev.nasm.custompng.modules.CustomPNGModule;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class CustomPNG extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Custom PNG");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Custom PNG");

        // Modules
        Modules.get().add(new CustomPNGModule());

    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
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
