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
        File shaderPack = new File(shaderpath.toString());
        File propertiesFile = new File(propertiesPath.toString());
        if (shaderPack.exists()){
            if (propertiesFile.exists()) {
                properties.load(new FileInputStream(propertiesPath.toString()));
                shaderpath = Paths.get((String) properties.get("shaderpack"));
            }
        } else {
            properties.setProperty("shaderpack", shaderpath.toString());
            properties.store(new FileOutputStream(FabricLoader.getInstance().getConfigDir() + "/iris.properties"), "This file is used to parse iris shader settings.");
            throw new IllegalStateException(String.format("The specified shaderpack \"%s\" was not found!", shaderpath));
        }
        if (shaderpath != null) {
            properties.setProperty("shaderpack", shaderpath.toString());
            properties.store(new FileOutputStream(FabricLoader.getInstance().getConfigDir() + "/iris.properties"), "This file is used to parse iris shader settings.");
        } else {
            throw new IllegalStateException("There was no specified shaderpack path!");
        }
    }
    public Path getShaderPackPath(){
        return shaderpath;
    }
}
