package net.coderbot.iris.shaders;

import net.coderbot.iris.Iris;

import java.io.IOException;

public class ShaderManager {
    private static TerrainShaders terrainShaders;
    private static CloudShaders cloudShaders;
    private static SkyShaders skyShaders;
    private static OutlineShaders outlineShaders;
    public static void useTerrainShaders() throws IOException {
        terrainShaders = new TerrainShaders(Iris.getShaderProperties().getShaderPackPath());
        terrainShaders.useShaders();
    }
    public static void useCloudShaders() throws IOException {
        cloudShaders = new CloudShaders(Iris.getShaderProperties().getShaderPackPath());
        cloudShaders.useShaders();
    }
    public static void useSkyShaders() throws IOException {
        skyShaders = new SkyShaders(Iris.getShaderProperties().getShaderPackPath());
        skyShaders.useShaders();
    }
    public static void useBasicShaders() throws IOException {
        outlineShaders = new OutlineShaders(Iris.getShaderProperties().getShaderPackPath());
        outlineShaders.useShaders();
    }
}
