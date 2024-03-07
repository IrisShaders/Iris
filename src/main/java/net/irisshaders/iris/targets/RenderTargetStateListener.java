package net.irisshaders.iris.targets;

public interface RenderTargetStateListener {
	RenderTargetStateListener NOP = new RenderTargetStateListener() {
		@Override
		public void beginPostChain() {

		}

		@Override
		public void endPostChain() {

		}

		@Override
		public void setIsMainBound(boolean bound) {

		}
	};

	void beginPostChain();

	void endPostChain();

	void setIsMainBound(boolean bound);
}
