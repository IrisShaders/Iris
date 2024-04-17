package net.irisshaders.iris.targets;

public interface RenderTargetStateListener {
	RenderTargetStateListener NOP = bound -> {

	};

	void setIsMainBound(boolean bound);
}
