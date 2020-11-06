package net.coderbot.iris.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * A class dedicated to storing the config values of shaderpacks. Right now it only stores the path to the current shaderpack
 */
public class ShaderProperties {
    private Path shaderpath;
    private Path propertiesPath;
    public ShaderProperties() {
        File file = new File(FabricLoader.getInstance().getGameDir() + "/shaderpacks");
        if (!file.exists()){
            file.mkdirs();
        }
    }
    public ShaderProperties setShaderPack(String shaderPackName){
        this.shaderpath = Paths.get(FabricLoader.getInstance().getGameDir() + "/shaderpacks/" + shaderPackName);
        return this;
    }
    public void createAndLoadProperties() throws IOException {
        propertiesPath = Paths.get(FabricLoader.getInstance().getConfigDir() + "/iris.properties");
        Properties properties = new Properties();
        if (new File(String.valueOf(shaderpath)).exists()){
            if (new File(propertiesPath.toString()).exists()) {
                properties.load(new FileInputStream(propertiesPath.toString()));
                shaderpath = Paths.get((String) properties.get("shaderpack"));
            }
        } else {
            throw new IllegalStateException(String.format("The specified shaderpack %s was not found!", shaderpath));
        }
        properties.setProperty("shaderpack", shaderpath.toString());
        properties.store(new FileOutputStream(FabricLoader.getInstance().getConfigDir() + "/iris.properties"), "This file is used to parse iris shader settings.");
    }
    public Path getShaderPackPath(){
        return shaderpath;
    }
}
