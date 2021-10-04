package net.coderbot.iris.mixin.rendertype;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ItemBlockRenderTypes.class)
public class MixinItemBlockRenderTypes {
    @Shadow
    private static RenderType getChunkRenderType(final BlockState blockState) {
        throw new AssertionError("not shadowed");
    }

	@Overwrite
	public static RenderType getRenderType(final BlockState blockState, final boolean bl) {
        final RenderType renderType = getChunkRenderType(blockState);
        if (renderType != RenderType.translucent()) {
            return RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS);
        }
        if (!Minecraft.useShaderTransparency()) {
            return RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS);
        }
        return bl ? RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS) : RenderType.itemEntityTranslucentCull(TextureAtlas.LOCATION_BLOCKS);
    }
    
	@Overwrite
    public static RenderType getRenderType(final ItemStack itemStack, final boolean bl) {
        final Item item = itemStack.getItem();
        if (item instanceof BlockItem) {
            final Block block = ((BlockItem)item).getBlock();
            return getRenderType(block.defaultBlockState(), bl);
        }
        return bl ? RenderType.entityTranslucentCull(TextureAtlas.LOCATION_BLOCKS) : RenderType.itemEntityTranslucentCull(TextureAtlas.LOCATION_BLOCKS);
    }
}
