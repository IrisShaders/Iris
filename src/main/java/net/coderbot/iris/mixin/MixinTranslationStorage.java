package net.coderbot.iris.mixin;

import net.coderbot.iris.Iris;
import net.minecraft.client.resource.language.LanguageDefinition;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mixin(TranslationStorage.class)
public class MixinTranslationStorage {

    //this is needed to keep track of which language code we need to grab our lang files from
    private static List<String> languageCodes = new ArrayList<>();

    private static final String LOAD = "load(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;)Lnet/minecraft/client/resource/language/TranslationStorage;";

    @Shadow @Final private Map<String, String> translations;

    private boolean tested = false;

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void iris$addLanguageEntries(String key, CallbackInfoReturnable<String> cir) {
        //minecraft loads us language code by default, and any other code will be right after it
        //so we also check if the user is loading a special language, and if the shaderpack has support for that language
        //if they do, we load that, but if they do not, we load "en_us" instead
        Map<String, Map<String, String>> languageMap = Iris.getCurrentPack().getLangMap();
        if (!translations.containsKey(key)) {
            if (languageMap.containsKey(languageCodes.get(1)) && languageMap.get(languageCodes.get(1)).containsKey(key)) {
                cir.setReturnValue(languageMap.get(languageCodes.get(1)).get(key));//indicates that the shaderpack has support for the user's selected to language
            } else if (languageMap.containsKey(languageCodes.get(0)) && languageMap.get(languageCodes.get(0)).containsKey(key)){
                cir.setReturnValue(languageMap.get(languageCodes.get(0)).get(key));//indicates that the shaderpack has support for "en_us" or american english
            }
        }
    }
    @Inject(method = LOAD, at = @At("HEAD"))
    private static void check(ResourceManager resourceManager, List<LanguageDefinition> definitions, CallbackInfoReturnable<TranslationStorage> cir) {
        languageCodes.clear();// make sure the language codes dont carry over!
        definitions.forEach(languageDefinition -> languageCodes.add(languageDefinition.getCode()));//will always have 2 entries
    }
}
