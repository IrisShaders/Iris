package net.irisshaders.iris.platform;

import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

import java.util.ArrayList;
import java.util.List;

@Mod("iris")
public class IrisForgeMod {
	public static List<KeyMapping> KEYLIST = new ArrayList<>();

	public IrisForgeMod(IEventBus bus) {
		bus.addListener(this::registerKeys);
	}

	public void registerKeys(RegisterKeyMappingsEvent event) {
		KEYLIST.forEach(event::register);
		KEYLIST.clear();
	}
}
