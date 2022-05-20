package net.coderbot.iris;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import net.coderbot.iris.config.IrisConfig;
import net.coderbot.iris.gl.shader.StandardMacros;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class UpdateChecker {
	private final int simpleVersion;
	private CompletableFuture<UpdateInfo> info;
	private boolean shouldShowUpdateMessage;
	private boolean usedIrisInstaller;

	public UpdateChecker(int simpleVersion) {
		this.simpleVersion = simpleVersion;
		if (Objects.equals(System.getProperty("iris.installer", "false"), "true")) {
			usedIrisInstaller = true;
		}
	}

	public void checkForUpdates(IrisConfig irisConfig) {
		this.info = CompletableFuture.supplyAsync(() -> {
			if (!irisConfig.shouldDisableUpdateMessage()) {
				try {
					File updateFile = FabricLoader.getInstance().getGameDir().resolve("irisUpdateInfo.json").toFile();
					if (DateUtils.isSameDay(new Date(), new Date(updateFile.lastModified()))) {
						Iris.logger.warn("Cached update file detected, using that!");
						UpdateInfo updateInfo = new Gson().fromJson(FileUtils.readFileToString(updateFile, StandardCharsets.UTF_8), UpdateInfo.class);
						if (updateInfo.simpleVersion > simpleVersion) {
							shouldShowUpdateMessage = true;
							Iris.logger.warn("New update detected, showing update message!");
							return updateInfo;
						} else {
							return null;
						}
					}

					try (InputStream in = new URL("https://raw.githubusercontent.com/IMS212/Iris-Installer-Files/master/updateindex.json").openStream()) {
						String updateIndex = new JsonParser().parse(new InputStreamReader(in)).getAsJsonObject().get(StandardMacros.getMcVersion()).getAsString();
						String json = IOUtils.toString(new URL(updateIndex), StandardCharsets.UTF_8);
						UpdateInfo updateInfo = new Gson().fromJson(json, UpdateInfo.class);
						BufferedWriter writer = new BufferedWriter(new FileWriter(updateFile));
						writer.write(json);
						writer.close();
						if (updateInfo.simpleVersion > simpleVersion) {
							shouldShowUpdateMessage = true;
							Iris.logger.warn("New update detected, showing update message!");
							return updateInfo;
						}
					}
				} catch (IOException e) {
					Iris.logger.error("Failed to get update info!", e);
				}
			}
			return null;
		});
	}

	public UpdateInfo getUpdateInfo() {
		if (info.isDone()) {
			try {
				return info.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		}

		return null;
	}

	public Component getUpdateMessage() {
		if (shouldShowUpdateMessage) {
			UpdateInfo info = getUpdateInfo();

			String languageCode = Minecraft.getInstance().options.languageCode.toLowerCase(Locale.ROOT);
			String originalText = info.updateInfo.containsKey(languageCode) ? info.updateInfo.get(languageCode) : info.updateInfo.get("en_us");
			String[] textParts = originalText.split("\\{link}");
			if (textParts.length > 1) {
				MutableComponent component1 = new TextComponent(textParts[0]);
				MutableComponent component2 = new TextComponent(textParts[1]);
				MutableComponent link = new TextComponent(usedIrisInstaller ? "the Iris Installer" : info.modHost).withStyle(arg -> arg.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, usedIrisInstaller ? info.installer : info.modDownload)).withUnderlined(true));
				return component1.append(link).append(component2);
			} else {
				MutableComponent link = new TextComponent(usedIrisInstaller ? "the Iris Installer" : info.modHost).withStyle(arg -> arg.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, usedIrisInstaller ? info.installer : info.modDownload)).withUnderlined(true));
				return new TextComponent(textParts[0]).append(link);
			}
		} else {
			return null;
		}
	}

	public String getUpdateLink() {
		UpdateInfo info = getUpdateInfo();

		return usedIrisInstaller ? info.installer : info.modDownload;
	}
}
