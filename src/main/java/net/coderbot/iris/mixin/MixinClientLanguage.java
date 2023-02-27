package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.coderbot.iris.shaderpack.LanguageMap;
import net.coderbot.iris.shaderpack.ShaderPack;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.locale.Language;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Allows shader packs to provide extra usable language entries outside of resource packs.
 *
 * <p>We "sideload" the language entries with an override system to avoid having to reload the
 * resource manager on shader pack changes, since reloading the resource manager is very slow.</p>
 *
 * Uses a lower priority to inject before Incubus-Core to prevent translations from breaking
 * @see <a href="https://github.com/devs-immortal/Incubus-Core/blob/4edfff0f088bc1b7ea77a1d475f76801a03179a4/src/main/java/net/id/incubus_core/mixin/devel/client/TranslationStorageMixin.java">Incubus-Core translation mixin</a>
 */
@Mixin(value = ClientLanguage.class, priority = 990)
public class MixinClientLanguage {
	private static final String LOAD = "Lnet/minecraft/client/resources/language/ClientLanguage;loadFrom(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;)Lnet/minecraft/client/resources/language/ClientLanguage;";

	// This is needed to keep track of which language code we need to grab our lang files from
	// TODO: Don't make this static. It probably won't break things as-is, but making it an instance variable
	//       is more resilient.
	@Unique
	private static final List<String> languageCodes = new ArrayList<>();

	@Shadow
	@Final
	private Map<String, String> storage;

	@Inject(method = "appendFrom", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILHARD)
	private static void injectFrom(String string, List<Resource> list, Map<String, String> map, CallbackInfo ci) {
		String json = String.format(Locale.ROOT, "lang/%s.json", string);
		if (Iris.class.getResource("/assets/iris/" + json) != null) {
			Language.loadFromJson(Iris.class.getResourceAsStream("/assets/iris/" + json), map::put);
		}
	}

	@Inject(method = "getOrDefault", at = @At("HEAD"), cancellable = true)
	private void iris$addLanguageEntries(String key, String value, CallbackInfoReturnable<String> cir) {
		String override = iris$lookupOverriddenEntry(key);

		if (override != null) {
			cir.setReturnValue(override);
		}
	}

	@Inject(method = "has", at = @At("HEAD"), cancellable = true)
	private void iris$addLanguageEntriesToTranslationChecks(String key, CallbackInfoReturnable<Boolean> cir) {
		String override = iris$lookupOverriddenEntry(key);

		if (override != null) {
			cir.setReturnValue(true);
		}
	}

	@Unique
	private String iris$lookupOverriddenEntry(String key) {
		ShaderPack pack = Iris.getCurrentPack().orElse(null);

		if (pack == null) {
			// If no shaderpack is loaded, do not try to process language overrides.
			//
			// This prevents a cryptic NullPointerException when shaderpack loading fails for some reason.
			return null;
		}

		// Minecraft loads the "en_us" language code by default, and any other code will be right after it.
		//
		// So we also check if the user is loading a special language, and if the shaderpack has support for that
		// language. If they do, we load that, but if they do not, we load "en_us" instead.
		LanguageMap languageMap = pack.getLanguageMap();

		if (storage.containsKey(key)) {
			// TODO: Should we allow shader packs to override existing MC translations?
			return null;
		}

		for (String code : languageCodes) {
			Map<String, String> translations = languageMap.getTranslations(code);

			if (translations != null) {
				String translation = translations.get(key);

				if (translation != null) {
					return translation;
				}
			}
		}

		return null;
	}

	@Inject(method = "loadFrom", at = @At("HEAD"))
	private static void check(ResourceManager resourceManager, List<String> definitions, boolean bl, CallbackInfoReturnable<ClientLanguage> cir) {
		// Make sure the language codes don't carry over!
		languageCodes.clear();

		// Reverse order due to how minecraft has English and then the primary language in the language definitions list
		new LinkedList<>(definitions).descendingIterator().forEachRemaining(languageDefinition -> {
			languageCodes.add(languageDefinition);
		});
	}
}
