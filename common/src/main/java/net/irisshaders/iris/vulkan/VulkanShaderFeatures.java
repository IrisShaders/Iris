package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.api.device.BackendCreationException;
import com.mojang.renderpearl.backend.vulkan.VulkanFeatureSets;
import com.mojang.renderpearl.backend.vulkan.VulkanUtils;
import com.mojang.renderpearl.backend.vulkan.init.FeatureSet;
import com.mojang.renderpearl.backend.vulkan.init.VulkanFeature;
import com.mojang.renderpearl.backend.vulkan.init.VulkanPNextStruct;
import net.irisshaders.iris.mixin.VulkanDeviceAccessor;
import org.lwjgl.vulkan.EXTShaderSubgroupPartitioned;
import org.lwjgl.vulkan.KHRComputeShaderDerivatives;
import org.lwjgl.vulkan.NVComputeShaderDerivatives;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkPhysicalDeviceComputeShaderDerivativesFeaturesKHR;
import org.lwjgl.vulkan.VkPhysicalDeviceShaderSubgroupPartitionedFeaturesEXT;

import java.util.HashSet;
import java.util.Set;

public class VulkanShaderFeatures {
	public static final FeatureSet SAMPLER_UPDATE_AFTER_BIND = new FeatureSet("Iris sampler update after bind", Set.of(),
		Set.of(new VulkanFeature(VulkanFeatureSets.VK12_FEATURES_STRUCT, "descriptorBindingSampledImageUpdateAfterBind"),
			new VulkanFeature(VulkanFeatureSets.VK12_FEATURES_STRUCT, "descriptorBindingUniformTexelBufferUpdateAfterBind")));
	public static final FeatureSet GEOMETRY = new FeatureSet("Iris geometry shaders", Set.of(),
		Set.of(new VulkanFeature(VulkanFeatureSets.VK10_FEATURES_STRUCT, "geometryShader")));
	public static final FeatureSet TESSELLATION = new FeatureSet("Iris tessellation shaders", Set.of(),
		Set.of(new VulkanFeature(VulkanFeatureSets.VK10_FEATURES_STRUCT, "tessellationShader")));
	private static final Set<VulkanFeature> OPTIONAL_FEATURES = Set.of(
		new VulkanFeature(VulkanFeatureSets.VK10_FEATURES_STRUCT, "shaderStorageImageExtendedFormats"),
		new VulkanFeature(VulkanFeatureSets.VK10_FEATURES_STRUCT, "shaderInt64"),
		new VulkanFeature(VulkanFeatureSets.VK10_FEATURES_STRUCT, "shaderInt16"),
		new VulkanFeature(VulkanFeatureSets.VK11_FEATURES_STRUCT, "storageBuffer16BitAccess"),
		new VulkanFeature(VulkanFeatureSets.VK11_FEATURES_STRUCT, "uniformAndStorageBuffer16BitAccess"),
		new VulkanFeature(VulkanFeatureSets.VK11_FEATURES_STRUCT, "storagePushConstant16"),
		new VulkanFeature(VulkanFeatureSets.VK11_FEATURES_STRUCT, "storageInputOutput16"),
		new VulkanFeature(VulkanFeatureSets.VK12_FEATURES_STRUCT, "storageBuffer8BitAccess"),
		new VulkanFeature(VulkanFeatureSets.VK12_FEATURES_STRUCT, "shaderFloat16"),
		new VulkanFeature(VulkanFeatureSets.VK12_FEATURES_STRUCT, "shaderSubgroupExtendedTypes"),
		new VulkanFeature(VulkanFeatureSets.VK12_FEATURES_STRUCT, "shaderInt8"));
	private static final VulkanPNextStruct DERIVATIVES = new VulkanPNextStruct(VkPhysicalDeviceComputeShaderDerivativesFeaturesKHR.class);
	private static final VulkanPNextStruct PARTITIONS = new VulkanPNextStruct(VkPhysicalDeviceShaderSubgroupPartitionedFeaturesEXT.class);
	private static final Set<String> DERIVATIVE_EXTENSIONS = Set.of(
		KHRComputeShaderDerivatives.VK_KHR_COMPUTE_SHADER_DERIVATIVES_EXTENSION_NAME,
		NVComputeShaderDerivatives.VK_NV_COMPUTE_SHADER_DERIVATIVES_EXTENSION_NAME,
		EXTShaderSubgroupPartitioned.VK_EXT_SHADER_SUBGROUP_PARTITIONED_EXTENSION_NAME);

	private VulkanShaderFeatures() {
	}

	public static FeatureSet getOptionalFeatures(VkPhysicalDevice device) throws BackendCreationException {
		var extensions = new HashSet<>(VulkanUtils.enumerateExtensions(device));
		extensions.retainAll(DERIVATIVE_EXTENSIONS);
		var features = new HashSet<>(OPTIONAL_FEATURES);
		if (!extensions.isEmpty()) {
			features.add(new VulkanFeature(DERIVATIVES, "computeDerivativeGroupQuads"));
			features.add(new VulkanFeature(DERIVATIVES, "computeDerivativeGroupLinear"));
			features.add(new VulkanFeature(PARTITIONS, "shaderSubgroupPartitioned"));
		}

		var optional = new FeatureSet("Iris optional shader features", extensions, features);
		return new FeatureSet(optional.name(), extensions, optional.supportedFeatures(device));
	}

	public static boolean isSupported(FeatureSet feature) {
		return ((VulkanDeviceAccessor) VulkanCommandBridge.getDevice()).iris$getEnabledFeatures().contains(feature);
	}
}
