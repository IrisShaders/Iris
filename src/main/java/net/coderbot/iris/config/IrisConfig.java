package net.coderbot.iris.config;

import com.google.common.collect.Maps;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;

/**
 * A class dedicated to storing the config values of shaderpacks. Right now it only stores the path to the current shaderpack
 */
public class IrisConfig {
    private Map<String, String> stringEntries = Maps.newHashMap();
    private Map<String, Integer> intEntries = Maps.newHashMap();
    private Map<String, Boolean> boolEntries = Maps.newHashMap();
    private Path shaderpath;
    private Path propertiesPath;
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
            if (stringEntries.containsKey(key.toString())){
                String val = value.toString();
                stringEntries.replace(key.toString(), val);
            } else if (intEntries.containsKey(key.toString())){
                Integer val = Integer.parseInt(value.toString());
                intEntries.replace(key.toString(), val);
            } else if (boolEntries.containsKey(key.toString())){
                Boolean val = Boolean.parseBoolean(value.toString());
                boolEntries.replace(key.toString(), val);
            }
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
        stringEntries.keySet().forEach(string -> properties.setProperty(string, stringEntries.get(string)));
        intEntries.keySet().forEach(string -> properties.setProperty(string, intEntries.get(string).toString()));
        boolEntries.keySet().forEach(string -> properties.setProperty(string, boolEntries.get(string).toString()));
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
        properties.store(Files.newOutputStream(propertiesPath), "This file is used to parse Iris Shader config options");
    }

    /**
     * Adds a string entry to the entries list to be parsed
     * @param name the name of the entry
     * @param defaultValue the default value when first serializing
     * @return this
     */
    public IrisConfig addStringEntry(String name, String defaultValue){
        stringEntries.put(name, defaultValue);
        return this;
    }

    /**
     * Adds a integer entry to the entries list to be parsed or gotten.
     * @param name the name of the entry
     * @param defaultValue the default value when first serializing
     * @return this
     */
    public IrisConfig addIntEntry(String name, Integer defaultValue){
        intEntries.put(name, defaultValue);
        return this;
    }

    /**
     * Adds a boolean entry to the entries list to be parsed
     * @param name the name of the entry
     * @param defaultValue the default value of the entry
     * @return this
     */

    public IrisConfig addBoolEntry(String name, Boolean defaultValue){
        boolEntries.put(name, defaultValue);
        return this;
    }

    /**
     * Returns the string entry with the given name
     * @param name the name of the entry
     * @return the string value
     */

    public String getStringEntry(String name){
        return stringEntries.get(name);
    }

    /**
     * Returns the int entry with the given string name
     * @param name the name of the entry
     * @return the value that corresponds to the name
     */

    public int getIntEntry(String name){
        return intEntries.get(name);
    }

    /**
     * Returns the boolean entry with the given string name
     * @param name the name of the entry
     * @return the boolean value of the entry
     */

    public boolean getBoolEntry(String name){
        return boolEntries.get(name);
    }

    /**
     * Sets a old string value with a new one
     * @param name the name of the entry
     * @param newValue the new Value that will replace the old value
     * @return this
     */

    public IrisConfig setStringEntry(String name, String newValue){
        this.stringEntries.replace(name, newValue);
        return this;
    }

    /**
     * Sets a old integer value with a new one
     * @param name the name of the entry
     * @param newValue the new value of the entry
     * @return this
     */

    public IrisConfig setIntEntry(String name, int newValue){
        this.intEntries.replace(name, newValue);
        return this;
    }

    /**
     * Sets a boolean entry with a new one
     * @param name name of the entry
     * @param newValue the new value of the entry
     * @return this
     */

    public IrisConfig setBoolEntry(String name, boolean newValue){
        this.boolEntries.replace(name, newValue);
        return this;
    }
}
