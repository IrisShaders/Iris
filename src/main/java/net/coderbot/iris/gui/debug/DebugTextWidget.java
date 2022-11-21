package net.coderbot.iris.gui.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.DoubleConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractScrollWidget;
import net.minecraft.client.gui.components.GridWidget;
import net.minecraft.client.gui.components.LayoutSettings;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.SpacerWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DebugTextWidget
	extends AbstractScrollWidget {
	private static final int HEADER_HORIZONTAL_PADDING = 32;
	private static final String TELEMETRY_REQUIRED_TRANSLATION_KEY = "telemetry.event.required";
	private static final String TELEMETRY_OPTIONAL_TRANSLATION_KEY = "telemetry.event.optional";
	private static final Component PROPERTY_TITLE = Component.translatable("telemetry_info.property_title").withStyle(ChatFormatting.UNDERLINE);
	private final Font font;
	private Content content;
	@Nullable
	private DoubleConsumer onScrolledListener;

	public DebugTextWidget(int i, int j, int k, int l, Font arg, Exception exception) {
		super(i, j, k, l, Component.empty());
		this.font = arg;
		this.content = this.buildContent(exception);
	}

	private Content buildContent(Exception exception) {
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
			lv.addSpacer(this.font.lineHeight);
			if (i >= elements.length - 1) continue;
			lv.addSpacer(this.font.lineHeight);
		}
		return lv.build();
	}

	@Override
	protected void setScrollAmount(double d) {
		super.setScrollAmount(d);
		if (this.onScrolledListener != null) {
			this.onScrolledListener.accept(this.scrollAmount());
		}
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

	private void addEventType(ContentBuilder arg, TelemetryEventType arg2) {
		String string = arg2.isOptIn() ? TELEMETRY_OPTIONAL_TRANSLATION_KEY : TELEMETRY_REQUIRED_TRANSLATION_KEY;
		arg.addHeader(this.font, Component.translatable(string, arg2.title()));
		arg.addHeader(this.font, arg2.description().withStyle(ChatFormatting.GRAY));
		arg.addSpacer(this.font.lineHeight / 2);
		arg.addLine(this.font, PROPERTY_TITLE, 2);
		this.addEventTypeProperties(arg2, arg);
	}

	private void addEventTypeProperties(TelemetryEventType arg, ContentBuilder arg2) {
		for (TelemetryProperty<?> lv : arg.properties()) {
			arg2.addLine(this.font, lv.title());
		}
	}

	private int containerWidth() {
		return this.width - this.totalInnerPadding();
	}

	@Environment(value=EnvType.CLIENT)
	record Content(GridWidget container, Component narration) {
	}

	@Environment(value=EnvType.CLIENT)
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
