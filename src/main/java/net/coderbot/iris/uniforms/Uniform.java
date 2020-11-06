package net.coderbot.iris.uniforms;

import jdk.internal.jline.internal.Nullable;
import net.minecraft.client.gl.GlProgram;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;

import java.nio.FloatBuffer;

public class Uniform {
    private int uniform;

    public Uniform(GlProgram program, String name){
        this.uniform = GL21.glGetUniformLocation(program.getProgramRef(), name);
    }
    protected void update(@Nullable Matrix4f instance) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        instance.writeToBuffer(buffer);
        buffer.rewind();

        GL21.glUniformMatrix4fv(uniform, false, buffer);
    }
    protected void updateVector(Vec3d instance){
        GL21.glUniform3f(this.getUniform(), (float) instance.x, (float) instance.y, (float) instance.z);
    }
    public final int getUniform(){
        return uniform;
    }
    protected void updateVector(Vector3f instance){
        GL21.glUniform3f(this.getUniform(), instance.getX(), instance.getY(), instance.getZ());
    }

}
