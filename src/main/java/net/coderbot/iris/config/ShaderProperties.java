package net.coderbot.iris.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * A class dedicated to storing the config values of shaderpacks. Right now it only stores the path to the current shaderpack
 * It will eventually have a sort of builder system for adding settings for it
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

    /**
     * This sets the default shaderpack name for the shader properties to set.
     * @param shaderPackName the file name of the pack
     * @return the shaderproperties with the set value
     */
    public ShaderProperties setDefaultPack(String shaderPackName){
        this.shaderpath = Paths.get(FabricLoader.getInstance().getGameDir() + "/shaderpacks/" + shaderPackName);
        return this;
    }
    public void createAndLoadProperties() throws IOException {
        propertiesPath = Paths.get(FabricLoader.getInstance().getConfigDir() + "/iris.properties");
        Properties properties = new Properties();
        File propertiesFile = new File(propertiesPath.toString());
        if (!propertiesFile.exists()){
            properties.setProperty("shaderpack", shaderpath.toString());
            properties.store(new FileOutputStream(FabricLoader.getInstance().getConfigDir() + "/iris.properties"), "This file is used to parse iris shader settings");
        } else {
            properties.load(new FileInputStream(propertiesPath.toFile()));
            shaderpath = Paths.get(properties.get("shaderpack").toString());
        }
        if (!shaderpath.toFile().exists()){
            throw new IllegalStateException(String.format("The specified shaderpack \"%s\" was not found! Change the value in iris.properties in your config directory!", shaderpath));
        }
        properties.setProperty("shaderpack", shaderpath.toString());
        properties.store(new FileOutputStream(FabricLoader.getInstance().getConfigDir() + "/iris.properties"), "This file is used to parse iris shader settings.");
    }
    public Path getShaderPackPath(){
        return shaderpath;
    }
}
