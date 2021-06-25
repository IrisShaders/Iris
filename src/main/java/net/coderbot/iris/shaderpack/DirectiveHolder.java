package net.coderbot.iris.shaderpack;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.coderbot.iris.vendored.joml.Vector4f;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

public interface DirectiveHolder {
	void acceptUniformDirective(String name, Runnable onDetected);
	void acceptCommentStringDirective(String name, Consumer<String> consumer);
	void acceptCommentIntDirective(String name, IntConsumer consumer);
	void acceptCommentFloatDirective(String name, FloatConsumer consumer);
	void acceptConstBooleanDirective(String name, BooleanConsumer consumer);
	void acceptConstStringDirective(String name, Consumer<String> consumer);
	void acceptConstIntDirective(String name, IntConsumer consumer);
	void acceptConstFloatDirective(String name, FloatConsumer consumer);
	void acceptConstVec4Directive(String name, Consumer<Vector4f> consumer);
}
