package dev.nasm.custompng.mixin;

import net.minecraft.client.texture.NativeImage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(NativeImage.class)
public interface NativeImageAccessor {
    @Invoker("setColor")
    void invokeSetColor(int x, int y, int color);
}
