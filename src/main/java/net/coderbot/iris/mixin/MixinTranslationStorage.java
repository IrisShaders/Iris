package net.coderbot.iris.mixin;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.coderbot.iris.Iris;
import net.coderbot.iris.shaderpack.ShaderPack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.resource.ResourceManager;

/**
 * Allows shader packs to provide extra usable language entries outside of resource packs.
 *
 * <p>We "sideload" the language entries with an override system to avoid having to reload the
 * resource manager on shader pack changes, since reloading the resource manager is very slow.</p>
 */
@Mixin(TranslationStorage.class)
public class MixinTranslationStorage {
	private static final String LOAD =
			"load(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;)Lnet/minecraft/client/resource/language/TranslationStorage;";

	// This is needed to keep track of which language code we need to grab our lang files from
	// TODO: Don't make this static. It probably won't break things as-is, but making it an instance variable
	//       is more resilient.
	@Unique
	private static final List<String> languageCodes = new ArrayList<>();

	@Shadow
	@Final
	private Map<String, String> translations;

	@Inject(method = "get", at = @At("HEAD"), cancellable = true)
	private void iris$addLanguageEntries(String key, CallbackInfoReturnable<String> cir) {
		String override = iris$lookupOverriddenEntry(key);

		if (override != null) {
			cir.setReturnValue(override);
		}
	}

	@Inject(method = "hasTranslation", at = @At("HEAD"), cancellable = true)
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
		Map<String, Map<String, String>> languageMap = pack.getLangMap();

		if (translations.containsKey(key)) {
			// TODO: Should we allow shader packs to override existing MC translations?
			return null;
		}

		for (String code : languageCodes) {
			Map<String, String> translations = languageMap.get(code);

			if (translations != null) {
				String translation = translations.get(key);

				if (translation != null) {
					return translation;
				}
			}
		}

		return null;
	}

	@Inject(method = LOAD, at = @At("HEAD"))
	private static void check(ResourceManager resourceManager, List<LanguageDefinition> definitions, CallbackInfoReturnable<TranslationStorage> cir) {
		// Make sure the language codes dont carry over!
		languageCodes.clear();

		// Reverse order due to how minecraft has English and then the primary language in the language definitions list
		new LinkedList<>(definitions).descendingIterator().forEachRemaining(languageDefinition -> {
			languageCodes.add(languageDefinition.getCode());
		});
	}
}
