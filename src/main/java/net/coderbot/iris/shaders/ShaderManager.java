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
        terrainShaders = new TerrainShaders(Paths.get(Iris.getShaderProperties().getShaderPackPath() + "/shaders/"));
        cloudShaders = new CloudShaders(Paths.get(Iris.getShaderProperties().getShaderPackPath() + "/shaders/"));
        outlineShaders = new OutlineShaders(Paths.get(Iris.getShaderProperties().getShaderPackPath() + "/shaders/"));
    }
    public void useTerrainShaders() throws IOException {
        terrainShaders.useShaders();
    }
    public void useCloudShaders() throws IOException {
        cloudShaders.useShaders();
    }
    public void useBasicShaders() throws IOException {
        outlineShaders.useShaders();
    }
}
