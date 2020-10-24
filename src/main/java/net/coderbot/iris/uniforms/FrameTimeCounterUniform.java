package net.coderbot.iris.uniforms;

import net.minecraft.client.gl.GlProgram;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;

public class FrameTimeCounterUniform extends Uniform {
    private float frameTimeCounter;
    private long lastRecordedTime;
    public FrameTimeCounterUniform(GlProgram program, String name) {
        super(program, name);
        frameTimeCounter = 0;
    }

    @Override
    protected void update(Matrix4f instance) {
        if (lastRecordedTime == 0){
            lastRecordedTime = System.currentTimeMillis();
        }
        long newsys = System.currentTimeMillis() - lastRecordedTime;
        lastRecordedTime = System.currentTimeMillis();
        frameTimeCounter += newsys / 1000F;
        if (frameTimeCounter > 3600){
            frameTimeCounter = 0;
        }
        System.out.println(frameTimeCounter);
        GL21.glUniform1f(this.getUniform(), frameTimeCounter);
    }
}
