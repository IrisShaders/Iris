package net.coderbot.iris.gl.buffer;

import com.mojang.blaze3d.platform.GlStateManager;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2LongArrayMap;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gl.IrisRenderSystem;
import org.lwjgl.opengl.GL43C;

import java.util.Collections;

public interface ShaderStorageBufferHolder {
	int getBuffer(int location);
	void destroyBuffers();
    void onNewFrame();
}
