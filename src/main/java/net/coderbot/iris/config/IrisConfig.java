package net.coderbot.iris.config;

import net.fabricmc.loader.api.FabricLoader;
import net.coderbot.iris.Iris;

import org.jetbrains.annotations.NotNull;

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
        this.shaderpath = FabricLoader.getInstance().getGameDir().resolve("shaderpacks").resolve("internal");
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
        if (!Files.exists(propertiesPath)){
            return;
        }
        Properties properties = new Properties();
        properties.load(Files.newInputStream(propertiesPath));
        this.shaderpath = Paths.get(properties.getProperty("shaderpack"));
    }

    /**
     * Serializes the config into a file. Should be called after refreshing the values of entries
     * @throws IOException file exceptions
     */

    public void serialize() throws IOException {
        Properties properties = new Properties();
        if (!Files.exists(shaderpath)){
            if (!shaderpath.endsWith("internal")) {
                Iris.logger.error(String.format("The specified shaderpack \"%s\" was not found! Change the value in iris.properties in your config directory! The system path should be \"%s\"", shaderpath.getFileName(), shaderpath));
                Iris.logger.error("falling back to internal shaders...");
            }
            shaderpath = FabricLoader.getInstance().getModContainer("iris")
                    .orElseThrow(() -> new RuntimeException("Failed to get the mod container for Iris!")).getRootPath();
            isInternal = true;
        }
        properties.setProperty("shaderpack", shaderpath.toString());
        properties.store(Files.newOutputStream(propertiesPath), "This file stores configuration options for Iris, such as the currently active shaderpack");
    }
}
