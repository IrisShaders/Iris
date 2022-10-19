import io.github.coolcrabs.brachyura.dependency.JavaJarDependency;
import io.github.coolcrabs.brachyura.fabric.FabricLoader;
import io.github.coolcrabs.brachyura.maven.MavenId;

public class FabricLoader2 extends FabricLoader {
	public FabricLoader2(String maven, MavenId id, JavaJarDependency jar) {
		super(maven, id);
	}
}
