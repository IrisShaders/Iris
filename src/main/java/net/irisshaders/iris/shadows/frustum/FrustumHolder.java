package net.irisshaders.iris.shadows.frustum;

import net.minecraft.client.renderer.culling.Frustum;

public class FrustumHolder {
	private Frustum frustum;
	private String distanceInfo = "(unavailable)";
	private String cullingInfo = "(unavailable)";

	public FrustumHolder setInfo(Frustum frustum, String distanceInfo, String cullingInfo) {
		this.frustum = frustum;
		this.distanceInfo = distanceInfo;
		this.cullingInfo = cullingInfo;
		return this;
	}

	public Frustum getFrustum() {
		return frustum;
	}

	public String getDistanceInfo() {
		return distanceInfo;
	}

	public String getCullingInfo() {
		return cullingInfo;
	}
}
