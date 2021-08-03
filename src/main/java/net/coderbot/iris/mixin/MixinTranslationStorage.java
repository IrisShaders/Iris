package net.coderbot.iris.mixin;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.coderbot.iris.Iris;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLanguage.class)
public class MixinTranslationStorage {

	// This is needed to keep track of which language code we need to grab our lang files from
	private static List<String> languageCodes = new ArrayList<>();

	private static final String LOAD = "Lnet/minecraft/client/resources/language/ClientLanguage;loadFrom(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;)Lnet/minecraft/client/resources/language/ClientLanguage;";

	@Shadow
	@Final
	private Map<String, String> storage;


	@Inject(method = "getOrDefault", at = @At("HEAD"), cancellable = true)
	private void iris$addLanguageEntries(String key, CallbackInfoReturnable<String> cir) {
		ShaderPack pack = Iris.getCurrentPack().orElse(null);

		if (pack == null) {
			// If no shaderpack is loaded, do not try to process language overrides.
			//
			// This prevents a cryptic NullPointerException when shaderpack loading fails for some reason.
			return;
		}

		// Minecraft loads the "en_us" language code by default, and any other code will be right after it.
		//
		// So we also check if the user is loading a special language, and if the shaderpack has support for that
		// language. If they do, we load that, but if they do not, we load "en_us" instead.
		Map<String, Map<String, String>> languageMap = pack.getLangMap();

		if (!storage.containsKey(key)) {
			languageCodes.forEach(code -> {
				Map<String, String> translations = languageMap.get(code);

				if (translations != null) {
					String translation = translations.get(key);

					if (translation != null) {
						cir.setReturnValue(translation);
					}
				}
			});
		}
	}

	@Inject(method = LOAD, at = @At("HEAD"))
	private static void check(ResourceManager resourceManager, List<LanguageInfo> definitions, CallbackInfoReturnable<ClientLanguage> cir) {
		// make sure the language codes dont carry over!
		languageCodes.clear();

		// Reverse order due to how minecraft has English and then the primary language in the language definitions list
		new LinkedList<>(definitions).descendingIterator().forEachRemaining(languageDefinition -> {
			languageCodes.add(languageDefinition.getCode());
		});
	}
}
