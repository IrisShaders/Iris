package net.irisshaders.iris.compat.dh;

import com.seibel.distanthorizons.api.interfaces.render.renderDef.objects.IDhApiGenericObjectVertexBufferContainer;
import com.seibel.distanthorizons.api.objects.render.DhApiRenderableBox;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class IrisDhGenericBuffer extends IrisDhVertexBuffer implements IDhApiGenericObjectVertexBufferContainer {
	private static final int VERTEX_SIZE = 20;
	private static final int[] CORNERS = {3, 1, 0, 2, 5, 7, 6, 4, 2, 0, 4, 6, 3, 7, 5, 1, 1, 5, 4, 0, 6, 7, 3, 2};

	private ByteBuffer data = ByteBuffer.allocateDirect(0);
	private volatile EState state = EState.NEW;

	public IrisDhGenericBuffer() {
		super("DH generic vertices");
	}

	@Override
	public void updateCpuSideVertexData(List<DhApiRenderableBox> boxes) {
		int size = boxes.size() * CORNERS.length * VERTEX_SIZE;
		if (this.data.capacity() != size) {
			this.data = ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
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
	public void uploadDataToGpu() {
		this.uploadVertexBuffer(this.data, this.data.remaining() / VERTEX_SIZE);
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
