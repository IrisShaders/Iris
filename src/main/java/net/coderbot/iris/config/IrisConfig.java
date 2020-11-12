package net.coderbot.iris.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * A class dedicated to storing the config values of shaderpacks. Right now it only stores the path to the current shaderpack
 */
public class IrisConfig {
    private Path shaderpath;
    private Path propertiesPath;
    /**
     * Represents if the current loaded shaderpack is in internal one or not
     */
    private boolean isInternal = false;

    public IrisConfig() {
        propertiesPath = FabricLoader.getInstance().getConfigDir().resolve("iris.properties");
        try {
            Files.createDirectories(FabricLoader.getInstance().getGameDir().resolve("shaderpacks"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This sets the default shaderpack name for the shader properties to set.
     * This value will only be used if the iris.properties file is not found
     * @param shaderPackName the file name of the pack
     * @return the shaderproperties with the set value
     */
    public IrisConfig setDefaultPack(String shaderPackName){
        this.shaderpath = FabricLoader.getInstance().getGameDir().resolve("shaderpacks").resolve(shaderPackName);
        return this;
    }

    /**
     * Deserializes and serializes the config
     * @throws IOException file exceptions
     */
    public void createAndLoadProperties() throws IOException {
        deserialize();
        serialize();
    }

    /**
     * returns the path of the current shaderpack
     * @return the path of the shaderpack
     */
    public Path getShaderPackPath(){
        return shaderpath;
    }

    /**
     * The path of the config file
     * @return the path to config file
     */
    public Path getPropertiesPath(){
        return propertiesPath;
    }

    /**
     * returns whether or not the current shaderpack is internal
     * @return if the shaderpack is internal
     */

    public boolean isInternal(){
        return isInternal;
    }

    /**
     * Returns the name of the shaderpack
     * @return shaderpack name. If internal it returns "internal"
     */

    public String getShaderPackName(){
        if (isInternal){
            return "internal";
        }
        return shaderpath.getFileName().toString();
    }

    /**
     * loads the config file and then populates the string, int, and boolean entries with the parsed entries
     * @throws IOException if the file cannot be loaded
     */

    public void deserialize() throws IOException {
        if (!propertiesPath.toFile().exists()){
            return;
        }
        Properties properties = new Properties();
        properties.load(Files.newInputStream(propertiesPath));
        properties.forEach((key, value) -> {
           if (key.toString().equals("shaderpack")){
                this.shaderpath = Paths.get(value.toString());
            }
        });
    }

    /**
     * Serializes the config into a file. Should be called after refreshing the values of entries
     * @throws IOException file exceptions
     */

    public void serialize() throws IOException {
        Properties properties = new Properties();
        if (!shaderpath.toFile().exists()){
            if (!shaderpath.endsWith("internal")) {
                System.out.println(String.format("The specified shaderpack \"%s\" was not found! Change the value in iris.properties in your config directory! The system path should be \"%s\"", shaderpath.getFileName(), shaderpath));
                System.out.println("falling back to internal shaders...");
            }
            shaderpath = FabricLoader.getInstance().getModContainer("iris")
                    .orElseThrow(() -> new RuntimeException("Iris doesnt exists anymore I guess")).getRootPath();
            isInternal = true;
        }
        properties.setProperty("shaderpack", shaderpath.toString());
        properties.store(Files.newOutputStream(propertiesPath), "This file is used to parse Iris Shader config options");
    }
}
