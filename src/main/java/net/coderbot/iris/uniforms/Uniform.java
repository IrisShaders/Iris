package net.coderbot.iris.uniforms;

import net.minecraft.client.gl.GlProgram;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;

import java.nio.FloatBuffer;

public class Uniform {
    private int uniform;

    public Uniform(GlProgram program, String name){
        this.uniform = GL21.glGetUniformLocation(program.getProgramRef(), name);
    }
    protected void update(Matrix4f instance) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        instance.writeToBuffer(buffer);
        buffer.rewind();

        GL21.glUniformMatrix4fv(uniform, false, buffer);
    }
    public int getUniform(){
        return uniform;
    }

}
