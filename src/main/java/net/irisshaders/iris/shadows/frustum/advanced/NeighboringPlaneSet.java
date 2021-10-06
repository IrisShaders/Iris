package net.irisshaders.iris.shadows.frustum.advanced;

public class NeighboringPlaneSet {
	private static final NeighboringPlaneSet FOR_PLUS_X = new NeighboringPlaneSet(2, 3, 4, 5);
	private static final NeighboringPlaneSet FOR_PLUS_Y = new NeighboringPlaneSet(0, 1, 4, 5);
	private static final NeighboringPlaneSet FOR_PLUS_Z = new NeighboringPlaneSet(0, 1, 2, 3);

	private static final NeighboringPlaneSet[] TABLE = new NeighboringPlaneSet[] {
		FOR_PLUS_X,
		FOR_PLUS_Y,
		FOR_PLUS_Z
	};

	private final int plane0;
	private final int plane1;
	private final int plane2;
	private final int plane3;

	public NeighboringPlaneSet(int plane0, int plane1, int plane2, int plane3) {
		this.plane0 = plane0;
		this.plane1 = plane1;
		this.plane2 = plane2;
		this.plane3 = plane3;
	}

	public static NeighboringPlaneSet forPlane(int planeIndex) {
		return TABLE[planeIndex >>> 1];
	}

	public int getPlane0() {
		return plane0;
	}

	public int getPlane1() {
		return plane1;
	}

	public int getPlane2() {
		return plane2;
	}

	public int getPlane3() {
		return plane3;
	}
}
