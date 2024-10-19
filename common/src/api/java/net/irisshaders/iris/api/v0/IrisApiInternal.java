package net.irisshaders.iris.api.v0;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class IrisApiInternal {
	static final IrisApi INSTANCE;

	static {
		try {
			INSTANCE = (IrisApi) Class.forName("net.irisshaders.iris.apiimpl.IrisApiV0Impl").getField("INSTANCE").get(null);
		} catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
