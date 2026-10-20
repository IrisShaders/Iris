package net.irisshaders.iris.vulkan;

import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuTexture;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// this system batches clears into the first render pass. Somehow a performance win that is actually notable, even though I was sure it wasn't...
public class VulkanTextureState {
	private final Long2ObjectLinkedOpenHashMap<Clear> clears = new Long2ObjectLinkedOpenHashMap<>();

	public void clear(List<GpuTextureView> views, Vector4f color) {
		if (views.isEmpty()) {
			return;
		}

		var value = new Vector4f(color);

		for (var view : views) {
			if (view.baseMipLevel() == 0 && view.mipLevels() == 1 && view.texture().getDepthOrLayers() == 1 && (view.texture()
				.usage() & GpuTexture.USAGE_RENDER_ATTACHMENT) != 0) {
				this.clears.put(((VulkanGpuTexture) view.texture()).vkImage(), new Clear(view, value));
			} else {
				this.flush();
				VulkanTextureOps.clearColors(List.of(view), color);
			}
		}
	}

	public RenderPassDescriptor takeClears(RenderPassDescriptor descriptor) {
		if (this.clears.isEmpty()) {
			return descriptor;
		}
		var area = descriptor.renderArea();
		for (var attachment : descriptor.colorAttachments()) {
			if (attachment == null) {
				continue;
			}
			var view = attachment.textureView();
			if (this.clears.containsKey(((VulkanGpuTexture) view.texture()).vkImage()) && (view.baseMipLevel() != 0 || view.mipLevels() != 1 || area.x() != 0 || area.y() != 0 || area.width() != view.getWidth(0) || area.height() != view.getHeight(0))) {
				this.flush();
				return descriptor;
			}
		}
		var attachments = new ArrayList<RenderPassDescriptor.Attachment<Optional<Vector4fc>>>();
		boolean changed = false;
		for (var attachment : descriptor.colorAttachments()) {
			if (attachment == null) {
				attachments.add(null);
				continue;
			}

			var view = attachment.textureView();
			if (view.baseMipLevel() == 0 && view.mipLevels() == 1 && area.x() == 0 && area.y() == 0 && area.width() == view.getWidth(
				0) && area.height() == view.getHeight(0)) {
				var clear = this.clears.remove(((VulkanGpuTexture) view.texture()).vkImage());
				if (clear != null && attachment.clearValue().isEmpty()) {
					attachment = new RenderPassDescriptor.Attachment<>(view, Optional.of(clear.color()));
					changed = true;
				}
			}
			attachments.add(attachment);
		}

		return changed ? new RenderPassDescriptor(descriptor.label(), attachments, descriptor.depthAttachment(),
			descriptor.renderArea()) : descriptor;
	}

	public void beforeAccess(GpuTexture texture) {
		if (this.clears.containsKey(((VulkanGpuTexture) texture).vkImage())) {
			this.flush();
		}
	}

	public void release(GpuTexture texture) {
		long image = ((VulkanGpuTexture) texture).vkImage();
		this.clears.remove(image);
	}

	public void flush() {
		if (this.clears.isEmpty()) {
			return;
		}

		var pending = new ArrayList<>(this.clears.values());
		this.clears.clear();

		for (var clear : pending) {
			VulkanTextureOps.clearColors(List.of(clear.view()), clear.color());
		}
	}

	public record Clear(GpuTextureView view, Vector4fc color) {
	}
}
