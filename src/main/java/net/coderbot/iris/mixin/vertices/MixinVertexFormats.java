package net.coderbot.iris.mixin.vertices;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;
import org.spongepowered.asm.mixin.*;

@Mixin(VertexFormats.class)
public class MixinVertexFormats {
	@Unique
	private static final VertexFormatElement ENTITY_ELEMENT;

	@Unique
	private static final VertexFormatElement MID_TEXTURE_ELEMENT;

	@Unique
	private static final VertexFormatElement TANGENT_ELEMENT;

	/*@Shadow
	@Final
	@Mutable
	public static final VertexFormat POSITION_COLOR_TEXTURE_LIGHT_NORMAL;*/

	static {
		ENTITY_ELEMENT = new VertexFormatElement(10, VertexFormatElement.Format.FLOAT, VertexFormatElement.Type.GENERIC, 4);
		MID_TEXTURE_ELEMENT = new VertexFormatElement(11, VertexFormatElement.Format.FLOAT, VertexFormatElement.Type.GENERIC, 2);
		// TODO: Store tangent vectors in a more compact way
		TANGENT_ELEMENT = new VertexFormatElement(12, VertexFormatElement.Format.FLOAT, VertexFormatElement.Type.GENERIC, 4);

		ImmutableList.Builder<VertexFormatElement> elements = ImmutableList.builder();

		elements.add(VertexFormats.POSITION_ELEMENT);
		elements.add(VertexFormats.COLOR_ELEMENT);
		elements.add(VertexFormats.TEXTURE_ELEMENT);
		elements.add(VertexFormats.LIGHT_ELEMENT);
		elements.add(VertexFormats.NORMAL_ELEMENT);
		elements.add(VertexFormats.PADDING_ELEMENT);
		elements.add(ENTITY_ELEMENT);
		elements.add(MID_TEXTURE_ELEMENT);
		elements.add(TANGENT_ELEMENT);

		//POSITION_COLOR_TEXTURE_LIGHT_NORMAL = new VertexFormat(elements.build());
	}
}
