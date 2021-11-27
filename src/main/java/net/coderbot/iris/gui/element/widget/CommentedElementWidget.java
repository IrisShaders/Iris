package net.coderbot.iris.gui.element.widget;

import net.minecraft.network.chat.Component;

public abstract class CommentedElementWidget extends AbstractElementWidget {
	public abstract Component getCommentTitle();

	public abstract Component getCommentBody();
}
