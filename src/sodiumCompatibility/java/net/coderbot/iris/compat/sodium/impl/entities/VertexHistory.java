package net.coderbot.iris.compat.sodium.impl.entities;

import org.joml.Vector3f;

public class VertexHistory {
    public Vector3f[] storedPositions = new Vector3f[8];

    public VertexHistory(int id) {
        for (int i = 0; i < 8; i++) {
            storedPositions[i] = new Vector3f();
        }
    }
}
