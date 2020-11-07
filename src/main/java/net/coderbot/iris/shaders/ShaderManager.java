package net.coderbot.iris.shaders;

import net.coderbot.iris.Iris;
import org.lwjgl.opengl.GL21;

import java.io.IOException;
import java.nio.file.Paths;

public class ShaderManager {
    private TerrainShaders terrainShaders;
    private CloudShaders cloudShaders;
    private OutlineShaders outlineShaders;
    public ShaderManager(){
        terrainShaders = new TerrainShaders();
        cloudShaders = new CloudShaders();
        outlineShaders = new OutlineShaders();
    }
    public void useTerrainShaders() {
        terrainShaders.useShaders();
    }
    public void useCloudShaders() {
        cloudShaders.useShaders();
    }
    public void useBasicShaders() {
        outlineShaders.useShaders();
    }
}
