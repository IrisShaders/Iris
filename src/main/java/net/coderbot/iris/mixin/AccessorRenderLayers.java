package net.coderbot.iris.mixin;

import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(RenderLayers.class)
public interface AccessorRenderLayers {
    @Accessor("BLOCKS")
    static Map<Block, RenderLayer> getBlockRenderLayers(){
        throw new AssertionError("Accessor Mixin \"AccessorRenderLayers\" Failed to apply!");
    }
}
