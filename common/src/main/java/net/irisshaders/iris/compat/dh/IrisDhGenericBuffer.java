package net.irisshaders.iris.compat.dh;

import com.seibel.distanthorizons.api.interfaces.render.renderDef.objects.IDhApiGenericObjectVertexBufferContainer;
import com.seibel.distanthorizons.api.objects.render.DhApiRenderableBox;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.util.List;

public class IrisDhGenericBuffer extends IrisDhVertexBuffer implements IDhApiGenericObjectVertexBufferContainer {
	private static final int VERTEX_SIZE = 20;
	private static final int[] CORNERS = {3, 1, 0, 2, 5, 7, 6, 4, 2, 0, 4, 6, 3, 7, 5, 1, 1, 5, 4, 0, 6, 7, 3, 2};

	private ByteBuffer data = MemoryUtil.memAlloc(0);
	private volatile EState state = EState.NEW;

	public IrisDhGenericBuffer() {
		super("DH generic vertices");
	}

	@Override
	public synchronized void updateCpuSideVertexData(List<DhApiRenderableBox> boxes) {
		if (this.data == null) {
			throw new IllegalStateException("DH generic buffer is closed");
		}
		int size = boxes.size() * CORNERS.length * VERTEX_SIZE;
		if (this.data.capacity() != size) {
			var replacement = MemoryUtil.memAlloc(size);
			MemoryUtil.memFree(this.data);
			this.data = replacement;
		}
		this.data.clear();
		for (var box : boxes) {
			for (int corner : CORNERS) {
				this.data.putFloat((float) ((corner & 1) == 0 ? box.minPos.x : box.maxPos.x));
				this.data.putFloat((float) ((corner & 2) == 0 ? box.minPos.y : box.maxPos.y));
				this.data.putFloat((float) ((corner & 4) == 0 ? box.minPos.z : box.maxPos.z));
				this.data.put((byte) box.color.getRed());
				this.data.put((byte) box.color.getGreen());
				this.data.put((byte) box.color.getBlue());
				this.data.put((byte) box.color.getAlpha());
				this.data.put(box.material);
				this.data.put((byte) 0).put((byte) 0).put((byte) 0);
			}
		}
		this.data.flip();
	}

	@Override
	public synchronized void uploadDataToGpu() {
		if (this.data == null) {
			throw new IllegalStateException("DH generic buffer is closed");
		}
		this.uploadVertexBuffer(this.data, this.data.remaining() / VERTEX_SIZE);
	}

	@Override
	public synchronized void close() {
		MemoryUtil.memFree(this.data);
		this.data = null;
		super.close();
	}

	@Override
	public EState getState() {
		return this.state;
	}

	@Override
	public void setState(EState state) {
		this.state = state;
	}
}
