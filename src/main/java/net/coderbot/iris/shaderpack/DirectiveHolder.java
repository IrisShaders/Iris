package net.coderbot.iris.shaderpack;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.floats.FloatConsumer;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

public interface DirectiveHolder {
	void acceptCommentStringDirective(String name, Consumer<String> consumer);
	void acceptCommentIntDirective(String name, IntConsumer consumer);
	void acceptCommentFloatDirective(String name, FloatConsumer consumer);
	void acceptConstBooleanDirective(String name, BooleanConsumer consumer);
	void acceptConstStringDirective(String name, Consumer<String> consumer);
	void acceptConstIntDirective(String name, IntConsumer consumer);
	void acceptConstFloatDirective(String name, FloatConsumer consumer);
	// TODO: vec4
}
