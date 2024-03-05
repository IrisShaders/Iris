package net.irisshaders.iris.shadows.frustum.advanced;

public record NeighboringPlaneSet(int plane0, int plane1, int plane2, int plane3) {
	private static final NeighboringPlaneSet FOR_PLUS_X = new NeighboringPlaneSet(2, 3, 4, 5);
	private static final NeighboringPlaneSet FOR_PLUS_Y = new NeighboringPlaneSet(0, 1, 4, 5);
	private static final NeighboringPlaneSet FOR_PLUS_Z = new NeighboringPlaneSet(0, 1, 2, 3);

	private static final NeighboringPlaneSet[] TABLE = new NeighboringPlaneSet[]{
		FOR_PLUS_X,
		FOR_PLUS_Y,
		FOR_PLUS_Z
	};

	public static NeighboringPlaneSet forPlane(int planeIndex) {
		return TABLE[planeIndex >>> 1];
	}


}
