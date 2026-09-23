package net.irisshaders.iris.vulkan;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuBuffer;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTexture;
import net.irisshaders.iris.backend.IrisImage;
import net.irisshaders.iris.backend.SyncCommand;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.KHRSynchronization2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VulkanSyncCommand implements SyncCommand {
	private final List<Pending> pending = new ArrayList<>();
	private Stage stage;

	@Override
	public SyncCommand read(Object object, Access access) {
		this.pending.add(new Pending(object, access, false));

		return this;
	}

	@Override
	public SyncCommand write(Object object, Access access) {
		if (access == Access.UNIFORM || access == Access.VERTEX || access == Access.INDEX || access == Access.INDIRECT) {
			throw new IllegalArgumentException("read-only synchronization access: " + access);
		}

		this.pending.add(new Pending(object, access, true));

		return this;
	}

	@Override
	public SyncCommand nextIs(Stage stage) {
		this.stage = Objects.requireNonNull(stage);

		return this;
	}

	@Override
	public void run() {
		if (this.stage == null) {
			throw new IllegalStateException("missing synchronization stage");
		}

		var accesses = new ArrayList<VulkanSynchronization.Access>();
		long shaderStage = switch (this.stage) {
			case COMPUTE -> KHRSynchronization2.VK_PIPELINE_STAGE_2_COMPUTE_SHADER_BIT_KHR;
			case GRAPHICS -> KHRSynchronization2.VK_PIPELINE_STAGE_2_ALL_GRAPHICS_BIT_KHR;
			case TRANSFER -> KHRSynchronization2.VK_PIPELINE_STAGE_2_ALL_TRANSFER_BIT_KHR;
		};

		for (var pending : this.pending) {
			Object object = pending.object();

			if (object instanceof VulkanGraphicsShader.Pipeline pipeline) {
				accesses.addAll(pipeline.accesses());
				continue;
			}

			if (object instanceof GpuTextureView view) {
				object = view.texture();
			} else if (object instanceof GpuBufferSlice slice) {
				object = slice.buffer();
			}

			var resource = switch (object) {
				case VulkanGpuTexture texture -> new VulkanSynchronization.Resource(true, texture.vkImage());
				case IrisImage image -> new VulkanSynchronization.Resource(true, image.getImage());
				case VulkanGpuBuffer buffer -> new VulkanSynchronization.Resource(false, buffer.vkBuffer());
				default -> throw new IllegalArgumentException("unsupported Vulkan synchronization resource: " + object);
			};

			long stages = switch (pending.access()) {
				case VERTEX, INDEX -> KHRSynchronization2.VK_PIPELINE_STAGE_2_VERTEX_INPUT_BIT_KHR;
				case INDIRECT -> KHRSynchronization2.VK_PIPELINE_STAGE_2_DRAW_INDIRECT_BIT_KHR;
				case COLOR_ATTACHMENT -> KHRSynchronization2.VK_PIPELINE_STAGE_2_COLOR_ATTACHMENT_OUTPUT_BIT_KHR;
				case DEPTH_ATTACHMENT ->
					KHRSynchronization2.VK_PIPELINE_STAGE_2_EARLY_FRAGMENT_TESTS_BIT_KHR | KHRSynchronization2.VK_PIPELINE_STAGE_2_LATE_FRAGMENT_TESTS_BIT_KHR;
				case TRANSFER -> KHRSynchronization2.VK_PIPELINE_STAGE_2_ALL_TRANSFER_BIT_KHR;
				default -> shaderStage;
			};
			long mask = switch (pending.access()) {
				case SHADER ->
					pending.write() ? KHRSynchronization2.VK_ACCESS_2_SHADER_READ_BIT_KHR | KHRSynchronization2.VK_ACCESS_2_SHADER_WRITE_BIT_KHR : KHRSynchronization2.VK_ACCESS_2_SHADER_READ_BIT_KHR;
				case UNIFORM -> KHRSynchronization2.VK_ACCESS_2_UNIFORM_READ_BIT_KHR;
				case VERTEX -> KHRSynchronization2.VK_ACCESS_2_VERTEX_ATTRIBUTE_READ_BIT_KHR;
				case INDEX -> KHRSynchronization2.VK_ACCESS_2_INDEX_READ_BIT_KHR;
				case INDIRECT -> KHRSynchronization2.VK_ACCESS_2_INDIRECT_COMMAND_READ_BIT_KHR;
				case COLOR_ATTACHMENT ->
					pending.write() ? KHRSynchronization2.VK_ACCESS_2_COLOR_ATTACHMENT_READ_BIT_KHR | KHRSynchronization2.VK_ACCESS_2_COLOR_ATTACHMENT_WRITE_BIT_KHR : KHRSynchronization2.VK_ACCESS_2_COLOR_ATTACHMENT_READ_BIT_KHR;
				case DEPTH_ATTACHMENT ->
					pending.write() ? KHRSynchronization2.VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_READ_BIT_KHR | KHRSynchronization2.VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_WRITE_BIT_KHR : KHRSynchronization2.VK_ACCESS_2_DEPTH_STENCIL_ATTACHMENT_READ_BIT_KHR;
				case TRANSFER ->
					pending.write() ? KHRSynchronization2.VK_ACCESS_2_TRANSFER_WRITE_BIT_KHR : KHRSynchronization2.VK_ACCESS_2_TRANSFER_READ_BIT_KHR;
			};
			accesses.add(new VulkanSynchronization.Access(resource, stages, mask, pending.write()));
		}

		var encoder = VulkanCommandBridge.encoder(RenderSystem.getDevice().createCommandEncoder());

		try (var stack = MemoryStack.stackPush()) {
			VulkanSynchronization.before(VulkanCommandBridge.commandBuffer(encoder), stack, accesses);
		}

		this.pending.clear();
		this.stage = null;
	}

	public record Pending(Object object, Access access, boolean write) {
	}
}
