package net.coderbot.iris.layer;

import java.util.Optional;

public interface ProgramRenderLayer {
	Optional<GbufferProgram> getProgram();
}
