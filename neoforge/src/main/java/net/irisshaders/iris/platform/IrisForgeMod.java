package net.irisshaders.iris.platform;

import net.irisshaders.iris.gui.screen.ShaderPackScreen;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.ArrayList;
import java.util.List;

@Mod("iris")
public class IrisForgeMod {
	public static List<KeyMapping> KEYLIST = new ArrayList<>();

	public IrisForgeMod(IEventBus bus, ModContainer modContainer) {
		bus.addListener(this::registerKeys);
		modContainer.registerExtensionPoint(IConfigScreenFactory.class, (game, screen) -> new ShaderPackScreen(screen));
	}

	public void registerKeys(RegisterKeyMappingsEvent event) {
		KEYLIST.forEach(event::register);
		KEYLIST.clear();
	}
}
