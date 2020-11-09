package net.coderbot.iris.shaderpack;

import com.google.common.collect.Maps;
import net.minecraft.util.Identifier;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * A utility class for parsing entries in item.properties and block.properties files in shaderpacks
 * This is not being used right now. Probably will delete it or reformat to be more efficient
 */
public class PropertiesParser {
    private Path shaderPath;
    /**
     * a map that contains the identifier of an item to the integer value parsed in block.properties
     */
    private Map<Identifier, Integer> blockPropertiesMap = Maps.newHashMap();
    /**
     * a map that contains the identifier of an item to the integer value parsed in item.properties
     */
    private Map<Identifier, Integer> itemPropertiesMap = Maps.newHashMap();

    public PropertiesParser(ShaderPack pack){
        this.shaderPath = pack.getPath();
        parseBlockProperties();
        parseItemProperties();
    }

    /**
     * fills the block properties map with entries from block.properties
     * Does not fill it with render layer entries
     */
    private void parseBlockProperties() {
        Properties properties = new Properties();
        try {
            properties.load(Files.newInputStream(shaderPath.resolve("block.properties")));
        } catch (IOException e){
            System.out.println("A block.properties was not found in the current shaderpack");
            return;
        }
        properties.forEach((key, value) -> {
            final int keyValue = Integer.parseInt(key.toString().substring(key.toString().indexOf(".") + 1));
            ArrayList<Identifier> identifiers = parsePropertiesIntoIdentifiers(value.toString());
            identifiers.forEach(identifier -> blockPropertiesMap.put(identifier, keyValue));
        });
    }

    /**
     * Parses values into identifiers to put in property maps
     * @param value the property full value
     * @return the identifiers that have been parsed
     */
    private ArrayList<Identifier> parsePropertiesIntoIdentifiers(String value){
        if (value.contains(" ")){
            ArrayList<String> fullNames = new ArrayList<>();
            ArrayList<Identifier> identifiers = new ArrayList<>();
            Collections.addAll(fullNames, value.split(" "));
            fullNames.forEach(string -> {
                String namespace;
                String path;
                if (string.contains(":")) {
                    namespace = string.substring(0, string.indexOf(":"));
                    path = string.substring(string.indexOf(":") + 1);
                } else {
                    namespace = "minecraft";
                    path = string;
                }
                identifiers.add(new Identifier(namespace, path));
            });
            return identifiers;
        } else {
            String namespace;
            String path;
            if (value.contains(":")) {
                namespace = value.substring(0, value.indexOf(":"));
                path = value.substring(value.indexOf(":") + 1);
            } else {
                namespace = "minecraft";
                path = value;
            }
            ArrayList<Identifier> identifiers = new ArrayList<>();
            identifiers.add(new Identifier(namespace, path));
            return identifiers;
        }
    }


    /**
     * parses entries from item.properties
     */
    private void parseItemProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(shaderPath + "/shaders/item.properties"));
        } catch (IOException e){
            System.out.println("An item.properties was not found in the current shaderpack");
            return;
        }
        properties.forEach((key, value) -> {
            final int keyValue = Integer.parseInt(key.toString().substring(key.toString().indexOf(".") + 1));
            ArrayList<Identifier> identifiers = parsePropertiesIntoIdentifiers(value.toString());
            identifiers.forEach(identifier -> itemPropertiesMap.put(identifier, keyValue));
        });
    }
    public Map<Identifier, Integer> getBlockProperties(){
        return blockPropertiesMap;
    }
    public Map<Identifier, Integer> getItemProperties(){
        return itemPropertiesMap;
    }

}
