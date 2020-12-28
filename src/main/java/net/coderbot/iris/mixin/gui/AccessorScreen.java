package net.coderbot.iris.mixin.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(Screen.class)
public interface AccessorScreen {
    @Accessor(value = "children")
    List<Element> getChildren();

    @Accessor(value = "client")
    MinecraftClient getClient();
}
