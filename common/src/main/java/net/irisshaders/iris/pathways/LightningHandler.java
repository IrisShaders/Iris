package net.irisshaders.iris.pathways;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.layer.InnerWrappedRenderType;
import net.irisshaders.iris.layer.LightningRenderStateShard;
import net.irisshaders.iris.pipeline.programs.ShaderAccess;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;

import java.util.function.Function;

public class LightningHandler extends RenderType {
	public static final RenderType IRIS_LIGHTNING = new InnerWrappedRenderType("iris_lightning2", RenderType.create(
		"iris_lightning",
		DefaultVertexFormat.POSITION_COLOR,
		VertexFormat.Mode.QUADS,
		256,
		false,
		true,
		RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_LIGHTNING_SHADER)
			.setWriteMaskState(COLOR_DEPTH_WRITE)
			.setTransparencyState(LIGHTNING_TRANSPARENCY)
			.setOutputState(WEATHER_TARGET)
			.createCompositeState(false)
	), new LightningRenderStateShard());

	public static final Function<ResourceLocation, RenderType> MEKANISM_FLAME = Util.memoize(resourceLocation -> {
		RenderType.CompositeState state = RenderType.CompositeState.builder()
			.setShaderState(new ShaderStateShard(ShaderAccess.MEKANISM_FLAME))
			.setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, TriState.DEFAULT, false))
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.createCompositeState(true);
		return create("mek_flame", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 256, true, false, state);
	});

	public static final RenderType MEKASUIT = create("mekasuit", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 131_072, true, false,
		RenderType.CompositeState.builder()
			.setShaderState(new ShaderStateShard(ShaderAccess.MEKASUIT))
			.setTextureState(BLOCK_SHEET)
			.setLightmapState(LIGHTMAP)
			.setOverlayState(OVERLAY)
			.createCompositeState(true)
	);

	public LightningHandler(String pRenderType0, VertexFormat pVertexFormat1, VertexFormat.Mode pVertexFormat$Mode2, int pInt3, boolean pBoolean4, boolean pBoolean5, Runnable pRunnable6, Runnable pRunnable7) {
		super(pRenderType0, pVertexFormat1, pVertexFormat$Mode2, pInt3, pBoolean4, pBoolean5, pRunnable6, pRunnable7);
	}
}
