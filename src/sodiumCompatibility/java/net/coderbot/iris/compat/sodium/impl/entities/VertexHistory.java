package net.coderbot.iris.compat.sodium.impl.entities;

import org.joml.Vector3f;

public class VertexHistory {
    public Vector3f[] storedPositions;
	public int lastFrame;

    public VertexHistory(int id, int size) {
		storedPositions = new Vector3f[size];
        for (int i = 0; i < size; i++) {
            storedPositions[i] = new Vector3f();
        }
    }
}
