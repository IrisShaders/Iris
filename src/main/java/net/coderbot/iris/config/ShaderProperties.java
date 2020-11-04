package net.coderbot.iris.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * A class dedicated to storing the config values of shaderpacks. Right now it only stores the path to the current shaderpack
 * and is hardcoded for the trippy shaderpack
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
    public void createAndLoadProperties() throws IOException {
        //TODO: stop hardcoding for the trippy shaderpack and make a more user friendly approach

        propertiesPath = Paths.get(FabricLoader.getInstance().getConfigDir() + "/iris.properties");
        Properties properties = new Properties();
        if (new File(String.valueOf(shaderpath)).exists()){
            properties.load(new FileInputStream(FabricLoader.getInstance().getConfigDir() + "/iris.properties"));
            shaderpath = Paths.get((String) properties.get("shaderpack"));
        } else{
            shaderpath  = Paths.get(FabricLoader.getInstance().getGameDir() + "/shaderpacks/" + "Trippy-Shaderpack-master" + "/shaders");
        }
        properties.setProperty("shaderpack", shaderpath.toString());
        properties.store(new FileOutputStream(FabricLoader.getInstance().getConfigDir() + "/iris.properties"), "This file is used to parse iris shader settings.");
    }
    public Path getShaderPackPath(){
        return shaderpath;
    }
}
