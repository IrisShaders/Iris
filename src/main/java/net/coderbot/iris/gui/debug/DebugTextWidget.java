package net.coderbot.iris.gui.debug;

import com.mojang.blaze3d.vertex.PoseStack;

import net.coderbot.iris.gl.shader.ShaderCompileException;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.GridWidget;
import net.minecraft.client.gui.components.LayoutSettings;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.SpacerWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class DebugTextWidget
	extends AbstractScrollWidget {
	private final Font font;
	private Content content;

	public DebugTextWidget(int i, int j, int k, int l, Font arg, Exception exception) {
		super(i, j, k, l, Component.empty());
		this.font = arg;
		this.content = this.buildContent(exception);
	}

	private Content buildContent(Exception exception) {
		if (exception instanceof ShaderCompileException sce) {
			return buildContentShader(sce);
		}
		ContentBuilder lv = new ContentBuilder(this.containerWidth());
		StackTraceElement[] elements = exception.getStackTrace();
		lv.addHeader(font, Component.literal("Error: "));
		lv.addSpacer(this.font.lineHeight);
		if (exception.getMessage() != null) {
			lv.addLine(font, Component.literal(exception.getMessage()));
		}
		lv.addSpacer(this.font.lineHeight);

		lv.addHeader(font, Component.literal("Stack trace: "));
		lv.addSpacer(this.font.lineHeight);

		for (int i = 0; i < elements.length; ++i) {
			StackTraceElement element = elements[i];
			if (element == null) continue;;
			lv.addLine(font, Component.literal(element.toString()));
			if (i >= elements.length - 1) continue;
			lv.addSpacer(this.font.lineHeight);
		}
		return lv.build();
	}

	private Content buildContentShader(ShaderCompileException sce) {
		ContentBuilder lv = new ContentBuilder(this.containerWidth());
		lv.addHeader(font, Component.literal("Shader compile error in " + sce.getFilename() + ": "));
		lv.addSpacer(this.font.lineHeight);

		lv.addLine(font, Component.literal(sce.getError()));
		return lv.build();
	}

	@Override
	protected int getInnerHeight() {
		return this.content.container().getHeight();
	}

	@Override
	protected boolean scrollbarVisible() {
		return this.getInnerHeight() > this.height;
	}

	@Override
	protected double scrollRate() {
		return this.font.lineHeight;
	}

	@Override
	protected void renderContents(PoseStack arg, int i, int j, float f) {
		int k = this.getY() + this.innerPadding();
		int l = this.getX() + this.innerPadding();
		arg.pushPose();
		arg.translate(l, k, 0.0);
		this.content.container().render(arg, i, j, f);
		arg.popPose();
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput arg) {
		arg.add(NarratedElementType.TITLE, this.content.narration());
	}

	private int containerWidth() {
		return this.width - this.totalInnerPadding();
	}

	record Content(GridWidget container, Component narration) {
	}

	static class ContentBuilder {
		private final int width;
		private final GridWidget grid;
		private final GridWidget.RowHelper helper;
		private final LayoutSettings alignHeader;
		private final MutableComponent narration = Component.empty();

		public ContentBuilder(int i) {
			this.width = i;
			this.grid = new GridWidget();
			this.grid.defaultCellSetting().alignHorizontallyLeft();
			this.helper = this.grid.createRowHelper(1);
			this.helper.addChild(SpacerWidget.width(i));
			this.alignHeader = this.helper.newCellSettings().alignHorizontallyCenter().paddingHorizontal(32);
		}

		public void addLine(Font arg, Component arg2) {
			this.addLine(arg, arg2, 0);
		}

		public void addLine(Font arg, Component arg2, int i) {
			this.helper.addChild(MultiLineTextWidget.create(this.width, arg, arg2), this.helper.newCellSettings().paddingBottom(i));
			this.narration.append(arg2).append("\n");
		}

		public void addHeader(Font arg, Component arg2) {
			this.helper.addChild(MultiLineTextWidget.createCentered(this.width - 64, arg, arg2), this.alignHeader);
			this.narration.append(arg2).append("\n");
		}

		public void addSpacer(int i) {
			this.helper.addChild(SpacerWidget.height(i));
		}

		public Content build() {
			this.grid.pack();
			return new Content(this.grid, this.narration);
		}
	}
}
