package net.coderbot.iris.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * A class dedicated to storing the config values of shaderpacks. Right now it only stores the path to the current shaderpack
 * It will eventually have a sort of builder system for adding settings for it
 */
public class IrisConfig {
    private Path shaderpath;
    private Path propertiesPath;
    private boolean isInternal = false;
    public IrisConfig() {
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
     * loads the current properties for the shaderpack. Currently only supports the shaderpack path
     * @throws IOException file exceptions
     */
    public void createAndLoadProperties() throws IOException {
        propertiesPath = FabricLoader.getInstance().getConfigDir().resolve("iris.properties");
        Properties properties = new Properties();

        if (propertiesPath.toFile().exists()){
            properties.load(Files.newInputStream(propertiesPath));
            shaderpath = Paths.get(properties.get("shaderpack").toString());
        }
        if (!shaderpath.toFile().exists()){
            if (!shaderpath.endsWith("internal")) {
                System.out.println(String.format("The specified shaderpack \"%s\" was not found! Change the value in iris.properties in your config directory! The system path should be \"%s\"", shaderpath.getFileName(), shaderpath));
                System.out.println("falling back to internal shaders...");
            }
            shaderpath = FabricLoader.getInstance().getModContainer("iris")
                    .orElseThrow(() -> new RuntimeException("bruh moment. Something went wrong with iris. I guess it doesnt exists anymore :rofl:")).getRootPath();
            isInternal = true;
        }
        properties.setProperty("shaderpack", shaderpath.toString());
        properties.store(Files.newOutputStream(FabricLoader.getInstance().getConfigDir().resolve("iris.properties")), "This file is used to parse iris shader settings.");
    }

    public Path getShaderPackPath(){
        return shaderpath;
    }

    public Path getPropertiesPath(){
        return propertiesPath;
    }

    public boolean isInternal(){
        return isInternal;
    }

    public String getShaderPackName(){
        if (isInternal){
            return "internal";
        }
        return shaderpath.getFileName().toString();
    }
}
