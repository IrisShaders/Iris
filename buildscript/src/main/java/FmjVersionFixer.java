import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import io.github.coolcrabs.brachyura.processing.ProcessingEntry;
import io.github.coolcrabs.brachyura.processing.ProcessingSink;
import io.github.coolcrabs.brachyura.processing.Processor;
import io.github.coolcrabs.brachyura.util.GsonUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

public class FmjVersionFixer implements Processor {
	private final Buildscript parent;

	public FmjVersionFixer(Buildscript parent) {
		this.parent = parent;
	}

	@Override
	public void process(Collection<ProcessingEntry> inputs, ProcessingSink sink) throws IOException {
		for (ProcessingEntry e : inputs) {
			if ("fabric.mod.json".equals(e.id.path)) {
				Gson gson = new GsonBuilder().setPrettyPrinting().setLenient().create();
				JsonObject fabricModJson;
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(e.in.get(), StandardCharsets.UTF_8))) {
					fabricModJson = gson.fromJson(reader, JsonObject.class);
				}

				fabricModJson.addProperty("version", parent.getVersion());

				sink.sink(() -> GsonUtil.toIs(fabricModJson, gson), e.id);
			} else {
				sink.sink(e.in, e.id);
			}
		}
	}
}
