package net.coderbot.iris.mixin.lang;

import net.coderbot.iris.Iris;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.resource.Resource;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mixin(TranslationStorage.class)
public class MixinTranslationStorage {

    private static final String LANGUAGE_LOAD = "Lnet/minecraft/util/Language;load(Ljava/io/InputStream;Ljava/util/function/BiConsumer;)V";

    @Inject(method = "load(Ljava/util/List;Ljava/util/Map;)V", at = @At(value = "INVOKE", target = LANGUAGE_LOAD), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void iris$addLangEntries(List resources, Map<String, String> translationMap, CallbackInfo ci, Iterator var2, Resource resource, InputStream inputStream) {
        String fileName = resource.getId().getPath().substring(resource.getId().getPath().indexOf("/"));
        String langCode = fileName.substring(1, fileName.lastIndexOf("."));
        try {
            Language.load(Iris.class.getResourceAsStream("/assets/iris/lang" + fileName), translationMap::put);
            Map<String, Map<String, String>> langMap = Iris.getCurrentPack().getLangMap();
            if (langMap.containsKey(langCode)){
                translationMap.putAll(langMap.get(langCode));
            }
            System.out.println(translationMap);
        } catch (NullPointerException e) {//file does not exist
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
