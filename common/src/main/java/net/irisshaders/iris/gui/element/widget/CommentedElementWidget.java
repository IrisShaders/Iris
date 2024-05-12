package net.irisshaders.iris.gui.element.widget;

import net.irisshaders.iris.shaderpack.option.menu.OptionMenuElement;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public abstract class CommentedElementWidget<T extends OptionMenuElement> extends AbstractElementWidget<T> {
	public CommentedElementWidget(T element) {
		super(element);
	}

	public abstract Optional<Component> getCommentTitle();

	public abstract Optional<Component> getCommentBody();
}
