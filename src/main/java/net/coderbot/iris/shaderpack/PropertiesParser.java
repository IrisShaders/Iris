package net.coderbot.iris.shaderpack;

import com.google.common.collect.Maps;
import net.minecraft.util.Identifier;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * A utility class for parsing entries in item.properties and block.properties files in shaderpacks
 * This is not being used right now. Probably will delete it or reformat to be more efficient
 */
public class PropertiesParser {
    private Path shaderPath;
    private Map<Identifier, Integer> blockPropertiesMap = Maps.newHashMap();
    private Map<Identifier, Integer> itemPropertiesMap = Maps.newHashMap();

    public PropertiesParser(ShaderPack pack){
        this.shaderPath = pack.getPath();
    }
    //TODO call these in the constructor instead of explicitly

    /**
     * fills the block properties map with entries from block.properties
     * Does not fill it with render layer entries
     */
    public void parseBlockProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(shaderPath + "/block.properties"));
        } catch (IOException e){
            System.out.println("A block.properties was not found in the current shaderpack");
            return;
        }
        properties.forEach((key, value) -> {
            if (!key.toString().contains("layer")) {
                final int keyValue = Integer.parseInt(key.toString().substring(key.toString().indexOf(".") + 1));
                if (value.toString().contains(" ")){
                    ArrayList<String> fullNames = new ArrayList<>();
                    ArrayList<Identifier> identifiers = new ArrayList<>();
                    Collections.addAll(fullNames, value.toString().split(" "));
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
                    identifiers.forEach(identifier -> {
                        blockPropertiesMap.put(identifier, keyValue);
                    });
                } else {
                    String namespace;
                    String path;
                    if (value.toString().contains(":")) {
                        namespace = value.toString().substring(0, value.toString().indexOf(":"));
                        path = value.toString().substring(value.toString().indexOf(":") + 1);
                    } else {
                        namespace = "minecraft";
                        path = value.toString();
                    }
                    blockPropertiesMap.put(new Identifier(namespace, path), keyValue);
                }
            }
        });
    }

    /**
     * parses entries from item.properties
     */
    public void parseItemProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(shaderPath + "/shaders/item.properties"));
        } catch (IOException e){
            System.out.println("An item.properties was not found in the current shaderpack");
            return;
        }
        properties.forEach((key, value) -> {
            final int keyValue = Integer.parseInt(key.toString().substring(key.toString().indexOf(".") + 1));
            if (value.toString().contains(" ")) {
                ArrayList<String> fullNames = new ArrayList<>();
                ArrayList<Identifier> identifiers = new ArrayList<>();
                Collections.addAll(fullNames, value.toString().split(" "));
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
                identifiers.forEach(identifier -> {
                    itemPropertiesMap.put(identifier, keyValue);
                });
            } else {
                String namespace;
                String path;
                if (value.toString().contains(":")) {
                    namespace = value.toString().substring(0, value.toString().indexOf(":"));
                    path = value.toString().substring(value.toString().indexOf(":") + 1);
                } else {
                    namespace = "minecraft";
                    path = value.toString();
                }
                itemPropertiesMap.put(new Identifier(namespace, path), keyValue);
            }
        });
    }
    public Map<Identifier, Integer> getBlockProperties(){
        return blockPropertiesMap;
    }
    public Map<Identifier, Integer> getItemProperties(){
        return itemPropertiesMap;
    }

}
