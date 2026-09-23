package net.irisshaders.iris.mixin;

import com.mojang.renderpearl.backend.vulkan.VulkanFeatureSets;
import com.mojang.renderpearl.backend.vulkan.init.FeatureSet;
import com.mojang.renderpearl.backend.vulkan.init.VulkanFeature;
import com.mojang.renderpearl.backend.vulkan.init.VulkanPNextStruct;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.irisshaders.iris.vulkan.VulkanPipelineLibraries;
import net.irisshaders.iris.vulkan.VulkanShaderFeatures;
import org.lwjgl.system.Platform;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(VulkanFeatureSets.class)
public class MixinVulkanFeatureSets {
	@Unique
	private static final FeatureSet IRIS_RENDERING = new FeatureSet("Iris rendering", Set.of(
		new FeatureSet("Iris attachment blending", Set.of(),
			Set.of(new VulkanFeature(new VulkanPNextStruct(VkPhysicalDeviceFeatures2.class), "independentBlend"),
				new VulkanFeature(new VulkanPNextStruct(VkPhysicalDeviceFeatures2.class), "fragmentStoresAndAtomics"),
				new VulkanFeature(new VulkanPNextStruct(VkPhysicalDeviceFeatures2.class), "vertexPipelineStoresAndAtomics"),
				new VulkanFeature(new VulkanPNextStruct(VkPhysicalDeviceFeatures2.class), "shaderStorageImageWriteWithoutFormat")))));

	@Shadow
	@Final
	@Mutable
	public static FeatureSet REQUIRED_FEATURESET;

	@Shadow
	@Final
	public static VulkanPNextStruct PORTABILITY_SUBSET_FEATURES_STRUCT;
	@Unique
	private static final FeatureSet PORTABILITY_SUBSET_FEATURESET_IRIS = new FeatureSet("Vulkan Portability Subset (Iris)",
		Set.of("VK_KHR_portability_subset"), Set.of(new VulkanFeature(PORTABILITY_SUBSET_FEATURES_STRUCT, "triangleFans"),
		new VulkanFeature(PORTABILITY_SUBSET_FEATURES_STRUCT, "mutableComparisonSamplers"),
		new VulkanFeature(PORTABILITY_SUBSET_FEATURES_STRUCT, "imageViewFormatSwizzle")));

	@Overwrite
	public static Set<FeatureSet> requiredIfExtensionsAvailableFeatureSets() {
		return ObjectOpenHashSet.of(PORTABILITY_SUBSET_FEATURESET_IRIS);
	}

	@Inject(method = "optionalFeatureSets", at = @At("RETURN"))
	private static void iris$optionalRenderingFeatures(CallbackInfoReturnable<Set<FeatureSet>> cir) {
		if (Platform.get() == Platform.MACOSX) {
			cir.getReturnValue().add(VulkanShaderFeatures.SAMPLER_UPDATE_AFTER_BIND);
		}
		cir.getReturnValue().add(VulkanPipelineLibraries.FEATURES);
		cir.getReturnValue().add(VulkanShaderFeatures.GEOMETRY);
		cir.getReturnValue().add(VulkanShaderFeatures.TESSELLATION);
	}

	@Inject(method = "<clinit>", at = @At("RETURN"))
	private static void iris$requireRenderingFeatures(CallbackInfo ci) {
		REQUIRED_FEATURESET = new FeatureSet("Minecraft and Iris Vulkan requirements", Set.of(REQUIRED_FEATURESET, IRIS_RENDERING));
	}
}
