package net.irisshaders.iris;

import com.mojang.blaze3d.platform.InputConstants;
import net.irisshaders.iris.compat.sodium.config.ShaderPackScreenPlaceholder;
import net.irisshaders.iris.platform.IrisPlatformHelpers;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class IrisVKOnly {
    public static final KeyMapping.Category irisKeybindCategory = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("iris", "keybinds"));
    private static KeyMapping shaderpackScreenKeybind;

    public static void run() {
        shaderpackScreenKeybind = IrisPlatformHelpers.getInstance().registerKeyBinding(new KeyMapping("iris.keybind.shaderPackSelection", InputConstants.Type.KEYBOARD, InputConstants.KEY_I, irisKeybindCategory));

    }

    public static void handleKeybinds() {
        if (shaderpackScreenKeybind != null) {
            if (shaderpackScreenKeybind.consumeClick()) {
                Minecraft.getInstance().gui.setScreen(new ShaderPackScreenPlaceholder(null));
            }
        }
    }
}
