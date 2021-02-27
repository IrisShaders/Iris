package net.coderbot.iris.compat.physicsmod.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class YeetPhysicsMod implements IMixinConfigPlugin {
	@Override
	public void onLoad(String mixinPackage) {

	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return FabricLoader.getInstance().isModLoaded("physicsmod");
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		MethodNode found = null;

		for (MethodNode method : targetClass.methods) {
			if (method.visibleAnnotations == null) {
				continue;
			}

			for (AnnotationNode annotation : method.visibleAnnotations) {
				if (!"Lorg/spongepowered/asm/mixin/transformer/meta/MixinMerged;".equals(annotation.desc)) {
					continue;
				}

				if (!"mixin".equals(annotation.values.get(0))) {
					throw new RuntimeException("Odd MixinMerged annotation with values: " + annotation.values);
				}

				if (!"net.diebuddies.mixins.MixinWorldRenderer".equals(annotation.values.get(1))) {
					continue;
				}

				if (!method.name.contains("renderLayer")) {
					continue;
				}

				// We found it!
				found = method;
				break;
			}
		}

		if (found == null) {
			throw new IllegalStateException("Failed to locate PhysicsMod renderLayer callback in WorldRenderer");
		}

		MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();
		String renderLayer = mappingResolver.mapClassName("intermediary", "net.minecraft.class_1921").replace('.', '/');

		// startDrawing is a member of RenderPhase.
		String startDrawing = mappingResolver.mapMethodName("intermediary", "net.minecraft.class_4668", "method_23516", "()V");

		// endDrawing is a member of RenderPhase.
		String endDrawing = mappingResolver.mapMethodName("intermediary", "net.minecraft.class_4668", "method_23518", "()V");

		redirect(found, renderLayer, startDrawing, "net/coderbot/iris/compat/physicsmod/PhysicsModHooks", "redirectStartDrawing");
		redirect(found, renderLayer, endDrawing, "net/coderbot/iris/compat/physicsmod/PhysicsModHooks", "redirectEndDrawing");
	}

	private static void redirect(MethodNode found, String owner, String name, String replacementOwner, String replacementName) {
		AbstractInsnNode foundInstruction = null;

		for (AbstractInsnNode node : found.instructions) {
			if (!(node instanceof MethodInsnNode)) {
				continue;
			}

			MethodInsnNode methodInsn = (MethodInsnNode) node;

			// INVOKEVIRTUAL net/minecraft/client/render/RenderLayer.endDrawing ()V
			// INVOKEVIRTUAL net/minecraft/client/render/RenderLayer.startDrawing ()V

			System.out.println(methodInsn.owner + "." + methodInsn.name + methodInsn.desc);

			if (owner.equals(methodInsn.owner) && name.equals(methodInsn.name) && "()V".equals(methodInsn.desc)) {
				foundInstruction = node;
				break;
			}
		}

		if (foundInstruction == null) {
			throw new IllegalStateException();
		}

		MethodInsnNode redirection = new MethodInsnNode(Opcodes.INVOKESTATIC, replacementOwner, replacementName, "(L" + owner + ";)V", false);

		found.instructions.insertBefore(foundInstruction, redirection);
		found.instructions.remove(foundInstruction);
	}
}
