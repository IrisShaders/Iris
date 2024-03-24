package net.irisshaders.iris;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.irisshaders.iris.config.IrisConfig;
import net.irisshaders.iris.gl.shader.StandardMacros;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class UpdateChecker {
	private final Version currentVersion;
	private CompletableFuture<UpdateInfo> info;
	private CompletableFuture<BetaInfo> betaInfo;
	private boolean shouldShowUpdateMessage;
	private boolean shouldShowBetaUpdateMessage;
	private boolean usedIrisInstaller;

	public UpdateChecker(Version currentVersion) {
		this.currentVersion = currentVersion;
		if (Objects.equals(System.getProperty("iris.installer", "false"), "true")) {
			usedIrisInstaller = true;
		}
	}

	public void checkForUpdates(IrisConfig irisConfig) {
		if (BuildConfig.IS_SHARED_BETA) {
			checkBetaUpdates();
			return;
		}

		if (irisConfig.shouldDisableUpdateMessage()) {
			shouldShowUpdateMessage = false;
			return;
		}

		this.info = CompletableFuture.supplyAsync(() -> {
			try {
				File updateFile = FabricLoader.getInstance().getGameDir().resolve("irisUpdateInfo.json").toFile();
				if (DateUtils.isSameDay(new Date(), new Date(updateFile.lastModified()))) {
					Iris.logger.warn("[Iris Update Check] Cached update file detected, using that!");
					UpdateInfo updateInfo;
					try {
						updateInfo = new Gson().fromJson(FileUtils.readFileToString(updateFile, StandardCharsets.UTF_8), UpdateInfo.class);
					} catch (JsonSyntaxException | NullPointerException e) {
						Iris.logger.error("[Iris Update Check] Cached file invalid, will delete!", e);
						Files.delete(updateFile.toPath());
						return null;
					}
					try {
						if (currentVersion.compareTo(SemanticVersion.parse(updateInfo.semanticVersion)) < 0) {
							shouldShowUpdateMessage = true;
							Iris.logger.warn("[Iris Update Check] New update detected, showing update message!");
							return updateInfo;
						} else {
							return null;
						}
					} catch (VersionParsingException e) {
						Iris.logger.error("[Iris Update Check] Caught a VersionParsingException while parsing semantic versions!", e);
					}
				}

				try (InputStream in = new URL("https://github.com/IrisShaders/Iris-Update-Index/releases/latest/download/updateIndex.json").openStream()) {
					String updateIndex;
					try {
						updateIndex = JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject().get(StandardMacros.getMcVersion()).getAsString();
					} catch (NullPointerException e) {
						Iris.logger.warn("[Iris Update Check] This version doesn't have an update index, skipping.");
						return null;
					}
					String json = IOUtils.toString(new URL(updateIndex), StandardCharsets.UTF_8);
					UpdateInfo updateInfo = new Gson().fromJson(json, UpdateInfo.class);
					BufferedWriter writer = new BufferedWriter(new FileWriter(updateFile));
					writer.write(json);
					writer.close();
					try {
						if (currentVersion.compareTo(SemanticVersion.parse(updateInfo.semanticVersion)) < 0) {
							shouldShowUpdateMessage = true;
							Iris.logger.info("[Iris Update Check] New update detected, showing update message!");
							return updateInfo;
						} else {
							return null;
						}
					} catch (VersionParsingException e) {
						Iris.logger.error("[Iris Update Check] Caught a VersionParsingException while parsing semantic versions!", e);
					}
				}
			} catch (FileNotFoundException e) {
				Iris.logger.warn("[Iris Update Check] Unable to download " + e.getMessage());
			} catch (IOException e) {
				Iris.logger.warn("[Iris Update Check] Failed to get update info!", e);
			}
			return null;
		});
	}

	private void checkBetaUpdates() {
		this.betaInfo = CompletableFuture.supplyAsync(() -> {
			try {
				try (InputStream in = new URL("https://raw.githubusercontent.com/IrisShaders/Iris-Installer-Files/master/betaTag.json").openStream()) {
					BetaInfo updateInfo = new Gson().fromJson(JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject(), BetaInfo.class);
					if (BuildConfig.BETA_VERSION < updateInfo.betaVersion && BuildConfig.BETA_TAG.equalsIgnoreCase(updateInfo.betaTag)) {
						shouldShowUpdateMessage = true;
						Iris.logger.info("[Iris Beta Update Check] New update detected, showing update message!");
						return updateInfo;
					} else {
						return null;
					}
				}
			} catch (FileNotFoundException e) {
				Iris.logger.warn("[Iris Beta Update Check] Unable to download " + e.getMessage());
			} catch (IOException e) {
				Iris.logger.warn("[Iris Beta Update Check] Failed to get update info!", e);
			}
			return null;
		});
	}

	@Nullable
	public UpdateInfo getUpdateInfo() {
		if (info != null && info.isDone()) {
			try {
				return info.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		}

		return null;
	}

	@Nullable
	public Optional<BetaInfo> getBetaInfo() {
		if (betaInfo != null && betaInfo.isDone()) {
			try {
				return Optional.ofNullable(betaInfo.get());
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			}
		}

		return Optional.empty();
	}

	public Optional<Component> getUpdateMessage() {
		if (shouldShowUpdateMessage) {
			UpdateInfo info = getUpdateInfo();

			if (info == null) {
				return Optional.empty();
			}

			String languageCode = Minecraft.getInstance().options.languageCode.toLowerCase(Locale.ROOT);
			String originalText = info.updateInfo.containsKey(languageCode) ? info.updateInfo.get(languageCode) : info.updateInfo.get("en_us");
			String[] textParts = originalText.split("\\{link}");
			if (textParts.length > 1) {
				MutableComponent component1 = Component.literal(textParts[0]);
				MutableComponent component2 = Component.literal(textParts[1]);
				MutableComponent link = Component.literal(usedIrisInstaller ? "the Iris Installer" : info.modHost).withStyle(arg -> arg.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, usedIrisInstaller ? info.installer : info.modDownload)).withUnderlined(true));
				return Optional.of(component1.append(link).append(component2));
			} else {
				MutableComponent link = Component.literal(usedIrisInstaller ? "the Iris Installer" : info.modHost).withStyle(arg -> arg.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, usedIrisInstaller ? info.installer : info.modDownload)).withUnderlined(true));
				return Optional.of(Component.literal(textParts[0]).append(link));
			}
		} else {
			return Optional.empty();
		}
	}

	public Optional<String> getUpdateLink() {
		if (shouldShowUpdateMessage) {
			UpdateInfo info = getUpdateInfo();

			return Optional.of(usedIrisInstaller ? info.installer : info.modDownload);
		} else {
			return Optional.empty();
		}
	}

	static class UpdateInfo {
		public String semanticVersion;
		public Map<String, String> updateInfo;
		public String modHost;
		public String modDownload;
		public String installer;
	}

	public static class BetaInfo {
		public String betaTag;
		public int betaVersion;
	}
}
