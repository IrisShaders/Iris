package net.irisshaders.iris.mixinterface;

import com.mojang.blaze3d.platform.Transparency;
import org.jetbrains.annotations.Nullable;

// for stupid player skin fix
public interface AbstractTextureExtended {
	@Nullable Transparency iris$getTransparency();

	void iris$setTransparency(@Nullable Transparency transparency);
}
