package net.irisshaders.iris.gui.debug;

import net.irisshaders.iris.Iris;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

public class IrisTrueDebugEntry implements DebugScreenEntry {
	@Override
	public void display(DebugScreenDisplayer debugScreenDisplayer, @Nullable Level level, @Nullable LevelChunk levelChunk, @Nullable LevelChunk levelChunk2) {
		Iris.getPipelineManager().getPipeline().ifPresent(pipeline -> pipeline.addDebugText(debugScreenDisplayer));

	}
}
