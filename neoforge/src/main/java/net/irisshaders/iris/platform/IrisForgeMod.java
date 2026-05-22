package net.irisshaders.iris.platform;

import net.irisshaders.iris.api.v0.IrisApi;
import net.irisshaders.iris.api.v0.IrisProgram;
import net.irisshaders.iris.gui.screen.ShaderPackScreen;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.NeoForgeRenderPipelines;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.ArrayList;
import java.util.List;

@Mod(value = "iris", dist = Dist.CLIENT)
public class IrisForgeMod {
	public static List<KeyMapping> KEYLIST = new ArrayList<>();

	public IrisForgeMod(IEventBus bus, ModContainer modContainer) {
		bus.addListener(this::registerKeys);
		modContainer.registerExtensionPoint(IConfigScreenFactory.class, (game, screen) -> new ShaderPackScreen(screen));
		IrisApi.getInstance().assignPipeline(NeoForgeRenderPipelines.ENTITY_SMOOTH_CUTOUT_CULL, IrisProgram.ENTITIES);
		IrisApi.getInstance().assignPipeline(NeoForgeRenderPipelines.ENTITY_TRANSLUCENT_CULL, IrisProgram.ENTITIES_TRANSLUCENT);
		IrisApi.getInstance().assignPipeline(NeoForgeRenderPipelines.ENTITY_UNLIT_TRANSLUCENT, IrisProgram.ENTITIES_TRANSLUCENT);
	}

	public void registerKeys(RegisterKeyMappingsEvent event) {
		KEYLIST.forEach(event::register);
		KEYLIST.clear();
	}
}
