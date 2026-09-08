package net.irisshaders.iris.pipeline.programs;

public final class IrisBindings {
	public static final int RESOURCE_COUNT = 32;

	public static final int ALBEDO_TEXTURE = 0;
	public static final int OVERLAY_TEXTURE = 1;
	public static final int LIGHTMAP_TEXTURE = 2;
	public static final int AUX_TEXTURE = 3;

	public static final int DYNAMIC_TRANSFORMS = 0;
	public static final int CLOUD_INFO = 1;
	public static final int PROJECTION = 2;
	public static final int GLOBALS = 3;
	public static final int FOG = 4;
	public static final int LIGHTING = 5;
	public static final int PUSH_CONSTANTS = 6;
	public static final int SODIUM_GLOBALS = 0;

	private IrisBindings() {
	}
}
