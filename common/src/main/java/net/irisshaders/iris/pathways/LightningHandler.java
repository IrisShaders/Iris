package net.irisshaders.iris.pathways;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.irisshaders.iris.layer.LightningRenderStateShard;
import net.irisshaders.iris.pipeline.programs.ShaderAccess;
import net.minecraft.util.Util;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.TriState;

import java.util.function.Function;

public abstract class LightningHandler extends RenderType {

	public LightningHandler(String string, int i, boolean bl, boolean bl2, Runnable runnable, Runnable runnable2) {
		super(string, null);
	}
}
