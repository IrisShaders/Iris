package net.coderbot.iris.shaderpack;

import net.coderbot.iris.Iris;
import net.coderbot.iris.config.IrisConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ShaderPackRef {
    private final String name;
    private static final Map<String, ShaderPackRef> PACKS = new HashMap<>();

    public ShaderPackRef(String fileName) {
        this.name = fileName;
    }

    public String getFileName() {
        return this.name;
    }

    public void applyShaderPack() {
        Iris.getIrisConfig().setShaderPackName(this.name);
        Iris.tryLoadShaderpack();
    }

    public boolean isLoaded() {
        return false;
    }

    public static void refresh() throws IOException {
        Path path = Iris.getShaderPackDir();
        System.out.println("REFRESHING ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        for(Path folder : Files.walk(path, 1).filter(Files::isDirectory).collect(Collectors.toList())) {
            if(Files.exists(folder.resolve("shaders"))) {
                String name = folder.getFileName().toString();
                PACKS.put(name, new ShaderPackRef(name));
            }
        }
    }
}
