package org.asutarisucu.mixin.TheThirdEye;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftClient.class)
public interface IMixinMinecraftClient {
    @Mutable
    @Accessor("window")
    abstract void setWindow(Window window);
}
