package net.coderbot.iris.gui.element.widget;

import net.minecraft.network.chat.Component;

import java.util.Optional;

public abstract class CommentedElementWidget extends AbstractElementWidget {
	public abstract Optional<Component> getCommentTitle();

	public abstract Optional<Component> getCommentBody();
}
