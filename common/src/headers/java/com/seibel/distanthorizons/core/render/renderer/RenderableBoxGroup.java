package com.seibel.distanthorizons.core.render.renderer;

import com.seibel.distanthorizons.api.interfaces.render.IDhApiRenderableBoxGroup;
import com.seibel.distanthorizons.api.interfaces.render.renderDef.objects.IDhApiGenericObjectVertexBufferContainer;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import com.seibel.distanthorizons.api.objects.render.DhApiRenderableBox;

import java.util.AbstractList;

public abstract class RenderableBoxGroup extends AbstractList<DhApiRenderableBox> implements IDhApiRenderableBoxGroup {
	public IDhApiGenericObjectVertexBufferContainer vertexBufferContainer;

	public void preRender(DhApiRenderParam param) {
		throw new UnsupportedOperationException();
	}

	public void postRender(DhApiRenderParam param) {
		throw new UnsupportedOperationException();
	}

	public void tryUpdateInstancedDataAsync(DhApiRenderParam param) {
		throw new UnsupportedOperationException();
	}
}
