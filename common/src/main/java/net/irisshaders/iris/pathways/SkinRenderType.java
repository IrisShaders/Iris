package net.irisshaders.iris.pathways;

import net.irisshaders.iris.mixinterface.AbstractTextureExtended;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

public class SkinRenderType {
	public static RenderType get(Identifier skin) {
		var texture = Minecraft.getInstance().getTextureManager().getTexture(skin);
		var extended = (AbstractTextureExtended) texture;
		var transparency = extended.iris$getTransparency();

		if (transparency == null && texture instanceof DynamicTexture dynamic) {
			transparency = dynamic.getPixels().computeTransparency();
			extended.iris$setTransparency(transparency);
		}

		if (transparency == null || transparency.hasTranslucent()) {
			return RenderTypes.entityTranslucent(skin);
		}

		return transparency.hasTransparent() ? RenderTypes.entityCutout(skin) : RenderTypes.entitySolid(skin);
	}
}
