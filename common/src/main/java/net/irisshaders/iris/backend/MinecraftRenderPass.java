package net.irisshaders.iris.backend;

public final class MinecraftRenderPass extends ForwardingRenderPass implements IrisRenderPass {
	private final WorldPassCommands commands;

	public MinecraftRenderPass(WorldPassCommands commands) {
		super(commands);
		this.commands = commands;
	}

	@Override
	public void bindPipeline(GraphicsPipeline pipeline) {
		this.commands.bindPipeline(pipeline);
	}

	@Override
	public void setViewport(int x, int y, int width, int height) {
		this.commands.setViewport(x, y, width, height);
	}

	@Override
	public void close() {
	}
}
