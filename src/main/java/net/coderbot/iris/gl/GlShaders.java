package net.coderbot.iris.gl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.texture.TextureUtil;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL21;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class GlShaders {

    public static GlShader createFromResource(GlShader.Type type, String name, InputStream sourceCode, String string) throws IOException {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        String string2 = TextureUtil.readAllToString(sourceCode);
        if (string2 == null) {
            throw new IOException("Could not load program " + type.getName());
        } else {
            int i = GlStateManager.createShader(type.getGlType());
            safeShaderSource(i, string2);
            GlStateManager.compileShader(i);
            if (GlStateManager.getShader(i, 35713) == 0) {
                String string3 = StringUtils.trim(GlStateManager.getShaderInfoLog(i, 32768));
                throw new IOException("Couldn't compile " + type.getName() + " program (" + string + ", " + name + ") : " + string3);
            } else {
                GlShader glShader = new GlShader(type, i, name);
                type.getLoadedShaders().put(name, glShader);
                return glShader;
            }
        }
    }

    /**
     * Identical in function to {@link GL20C#glShaderSource(int, CharSequence)} but
     * passes a null pointer for string length to force the driver to rely on the null
     * terminator for string length.  This is a workaround for an apparent flaw with some
     * AMD drivers that don't receive or interpret the length correctly, resulting in
     * an access violation when the driver tries to read past the string memory.
     *
     * <p>Hat tip to fewizz for the find and the fix.
     */
    private static void safeShaderSource(@NativeType("GLuint") int glId, @NativeType("GLchar const **") CharSequence source) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);

        final MemoryStack stack = MemoryStack.stackGet();
        final int stackPointer = stack.getPointer();

        try {
            final ByteBuffer sourceBuffer = MemoryUtil.memUTF8(source, true);
            final PointerBuffer pointers = stack.mallocPointer(1);
            pointers.put(sourceBuffer);

            GL21.nglShaderSource(glId, 1, pointers.address0(), 0);
            org.lwjgl.system.APIUtil.apiArrayFree(pointers.address0(), 1);
        } finally {
            stack.setPointer(stackPointer);
        }
    }

}
