package net.coderbot.iris.config;

import com.google.common.collect.ImmutableList;
import net.coderbot.iris.Iris;
import net.coderbot.iris.gui.GuiUtil;
import net.coderbot.iris.gui.ScreenStack;
import net.coderbot.iris.gui.ShaderPackScreen;
import net.coderbot.iris.gui.UiTheme;
import net.coderbot.iris.gui.element.PropertyDocumentWidget;
import net.coderbot.iris.gui.property.*;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A class dedicated to storing the config values of shaderpacks. Right now it only stores the path to the current shaderpack
 */
public class IrisConfig {
	private static final String COMMENT =
		"This file stores configuration options for Iris, such as the currently active shaderpack";

	/**
	 * The path to the current shaderpack. Null if the internal shaderpack is being used.
	 */
	private String shaderPackName;

	/**
	 * The UI theme to use. Null if the default Iris UI theme is being used.
	 */
	private String uiTheme;

	/**
	 * Whether to display shader pack config screens in "condensed" view.
	 */
	private boolean condenseShaderConfig;

	private Path propertiesPath;

	public IrisConfig() {
		shaderPackName = null;
		propertiesPath = FabricLoader.getInstance().getConfigDir().resolve("iris.properties");
	}

	/**
	 * Initializes the configuration, loading it if it is present and creating a default config otherwise.
	 *
	 * @throws IOException file exceptions
	 */
	public void initialize() throws IOException {
		load();
		if (!Files.exists(propertiesPath)) {
			save();
		}
	}

	/**
	 * returns whether or not the current shaderpack is internal
	 *
	 * @return if the shaderpack is internal
	 */
	public boolean isInternal() {
		return shaderPackName == null;
	}

	/**
	 * Returns the name of the current shaderpack
	 *
	 * @return shaderpack name. If internal it returns "(internal)"
	 */
	public String getShaderPackName() {
		if (shaderPackName == null) {
			return "(internal)";
		}

		return shaderPackName;
	}

	/**
	 * Sets the shader pack name, and tries to save the config file.
	 * Will print an error if unable to save.
	 *
	 * @param name The name of the shader pack
	 */
	public void setShaderPackName(String name) {
		if(name == null) return;
		shaderPackName = name;
		try {
			save();
		} catch (IOException e) {
			Iris.logger.error("Error setting shader pack!");
			e.printStackTrace();
		}
	}

	/**
	 * returns the selected UI Theme
	 *
	 * @return the selected UI Theme, or the default theme if null
	 */
	public UiTheme getUITheme() {
		if(uiTheme == null) this.uiTheme = "IRIS";
		UiTheme theme;
		try {
			theme = UiTheme.valueOf(uiTheme);
		} catch (Exception ignored) {
			theme = UiTheme.IRIS;
		}
		this.uiTheme = theme.name();
		return theme;
	}


	/**
	 * Gets whether to use condensed view for shader pack configuration.
	 *
	 * @return Whether to use condensed view
	 */
	public boolean getIfCondensedShaderConfig() {
		return condenseShaderConfig;
	}

	/**
	 * Sets whether to use condensed view for shader pack configuration.
	 *
	 * @param condense Whether to use condensed view
	 */
	public void setIfCondensedShaderConfig(boolean condense) {
		this.condenseShaderConfig = condense;
		try {
			save();
		} catch (IOException e) {
			Iris.logger.error("Error setting config for condensed shader pack config view!");
			e.printStackTrace();
		}
	}

	/**
	 * Sets config values as read from a Properties object.
	 *
	 * @param properties The Properties object to read and set the config from
	 */
	public void read(Properties properties) {
		shaderPackName = properties.getProperty("shaderPack", this.shaderPackName);
		uiTheme = properties.getProperty("uiTheme", this.uiTheme);
		condenseShaderConfig = Boolean.parseBoolean(properties.getProperty("condenseShaderConfig"));

		if (shaderPackName != null && shaderPackName.equals("(internal)")) {
			shaderPackName = null;
		}
	}

	/**
	 * Puts config values to a Properties object.
	 *
	 * @return the Properties object written to
	 */
	public Properties write() {
		Properties properties = new Properties();

		properties.setProperty("shaderPack", getShaderPackName());
		properties.setProperty("uiTheme", getUITheme().name());
		properties.setProperty("condenseShaderConfig", Boolean.toString(getIfCondensedShaderConfig()));

		return properties;
	}

	/**
	 * loads the config file and then populates the string, int, and boolean entries with the parsed entries
	 *
	 * @throws IOException if the file cannot be loaded
	 */
	public void load() throws IOException {
		if (!Files.exists(propertiesPath)) {
			return;
		}

		Properties properties = new Properties();
		properties.load(Files.newInputStream(propertiesPath));

		this.read(properties);
	}

	/**
	 * Serializes the config into a file. Should be called whenever any config values are modified.
	 *
	 * @throws IOException file exceptions
	 */
	public void save() throws IOException {
		Properties properties = write();
		properties.store(Files.newOutputStream(propertiesPath), COMMENT);
	}

	/**
	 * Creates a set of pages for the config screen
	 *
	 * @return pages for the config screen as a String to PropertyList map
	 */
	public Map<String, PropertyList> createDocument(TextRenderer tr, Screen parent, PropertyDocumentWidget widget, int width) {
		Map<String, PropertyList> document = new HashMap<>();
		PropertyList page = new PropertyList();
		page.add(new TitleProperty(new TranslatableText("property.iris.title.configScreen").formatted(Formatting.BOLD),
				0x82ffffff, 0x82ff0000, 0x82ff8800, 0x82ffd800, 0x8288ff00, 0x8200d8ff, 0x823048ff, 0x829900ff, 0x82ffffff
		));
		page.add(new FunctionalButtonProperty(widget, () -> MinecraftClient.getInstance().openScreen(new ShaderPackScreen(parent)), new TranslatableText("options.iris.shaderPackSelection.title"), LinkProperty.Align.CENTER_LEFT));
		int optionTextWidthFull = (int)(width * 0.6) - 21;
		int optionTextWidthHalf = (int)((width * 0.5) * 0.6) - 21;
		page.addAllPairs(ImmutableList.of(
				new StringOptionProperty(ImmutableList.of(UiTheme.IRIS.name(), UiTheme.VANILLA.name(), UiTheme.AQUA.name()), 0, widget, "uiTheme", GuiUtil.trimmed(tr, "property.iris.uiTheme", optionTextWidthHalf, true, true), false, false),
				new BooleanOptionProperty(widget, false, "condenseShaderConfig", GuiUtil.trimmed(tr, "property.iris.condenseShaderConfig", optionTextWidthHalf, true, true), false)
		));
		document.put("main", page);
		widget.onSave(() -> {
			Properties ps = new Properties();
			widget.getPage(widget.getCurrentPage()).forEvery(property -> {
				if(property instanceof ValueProperty) {
					ValueProperty<?> vp = ((ValueProperty<?>)property);
					ps.setProperty(vp.getKey(), vp.getValue().toString());
				}
			});
			this.read(ps);
			try {
				this.save();
			} catch (IOException e) {
				Iris.logger.error("Failed to save config!");
				e.printStackTrace();
			}
		});
		widget.onLoad(() -> {
			try {
				this.load();
			} catch (IOException e) {
				Iris.logger.error("Failed to load config!");
				e.printStackTrace();
			}
			Properties properties = this.write();
			for(String k : widget.getPages()) {
				widget.getPage(k).forEvery(property -> {
					if(property instanceof ValueProperty) {
						ValueProperty<?> vp = ((ValueProperty<?>)property);
						vp.setValue(properties.getProperty(vp.getKey()));
						vp.resetValueText();
					}
				});
			}
		});
		return document;
	}
}
