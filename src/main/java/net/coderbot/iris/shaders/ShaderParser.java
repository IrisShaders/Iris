package net.coderbot.iris.shaders;

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
public class ShaderParser {
    private Path shaderPath;
    private Map<ArrayList<Identifier>, String> blockPropertiesMap = Maps.newHashMap();
    private Map<ArrayList<Identifier>, String> itemPropertiesMap = Maps.newHashMap();
    public ShaderParser(Path shaderPath){
        this.shaderPath = shaderPath;
    }
    //TODO call these in the constructor instead of explicitly
    public void parseBlockProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(shaderPath + "/shaders/block.properties"));
        properties.forEach((key, value) -> {
            if (!key.toString().contains("layer")) {
                ArrayList<Identifier> identifiers = new ArrayList<>();
                String[] ar = value.toString().split(" ");
                for (String string : ar) {
                    if (string.contains(":")) {
                        String keyf = string.substring(0, string.indexOf(":"));
                        String path = string.substring(string.indexOf(":") + 1);
                        Identifier identifier = new Identifier(keyf, path);
                        identifiers.add(identifier);
                    } else {
                        identifiers.add(new Identifier(string));
                    }
                }
                blockPropertiesMap.put(identifiers, key.toString());
            }
        });
    }
    public void parseItemProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(shaderPath + "/shaders/item.properties"));
        properties.forEach((key, value) -> {
        ArrayList<Identifier> identifiers = new ArrayList<>();
        String[] ar = value.toString().split(" ");
            for (String string : ar) {
                if (string.contains(":")) {
                    String keyf = string.substring(0, string.indexOf(":"));
                    String path = string.substring(string.indexOf(":") + 1);
                    Identifier identifier = new Identifier(keyf, path);
                    identifiers.add(identifier);
                } else {
                    identifiers.add(new Identifier(string));
                }
            }
        itemPropertiesMap.put(identifiers, key.toString());
        });
    }
    public Map<ArrayList<Identifier>, String> getBlockProperties(){
        return blockPropertiesMap;
    }
    public Map<ArrayList<Identifier>, String> getItemProperties(){
        return itemPropertiesMap;
    }

}
