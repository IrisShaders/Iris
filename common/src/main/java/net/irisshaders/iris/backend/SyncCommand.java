package net.irisshaders.iris.backend;

public interface SyncCommand {
	default SyncCommand read(Object object) {
		return this.read(object, Access.SHADER);
	}

	default SyncCommand write(Object object) {
		return this.write(object, Access.SHADER);
	}

	SyncCommand read(Object object, Access access);

	SyncCommand write(Object object, Access access);

	SyncCommand nextIs(Stage stage);

	void run();

	enum Stage {
		GRAPHICS,
		COMPUTE,
		TRANSFER
	}

	enum Access {
		SHADER,
		UNIFORM,
		VERTEX,
		INDEX,
		INDIRECT,
		COLOR_ATTACHMENT,
		DEPTH_ATTACHMENT,
		TRANSFER
	}
}
