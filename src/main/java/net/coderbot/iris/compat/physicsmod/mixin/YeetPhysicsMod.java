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

		redirect(found, renderLayer, startDrawing, "()V", "net/coderbot/iris/compat/physicsmod/PhysicsModHooks", "redirectStartDrawing", "(L" + renderLayer + ";)V");
		redirect(found, renderLayer, endDrawing, "()V", "net/coderbot/iris/compat/physicsmod/PhysicsModHooks", "redirectEndDrawing", "(L" + renderLayer + ";)V");
	}

	private static void redirect(MethodNode found, String owner, String name, String desc, String replacementOwner, String replacementName, String replacementDesc) {
		AbstractInsnNode foundInstruction = null;

		for (AbstractInsnNode node : found.instructions) {
			if (!(node instanceof MethodInsnNode)) {
				continue;
			}

			MethodInsnNode methodInsn = (MethodInsnNode) node;

			if (owner.equals(methodInsn.owner) && name.equals(methodInsn.name) && desc.equals(methodInsn.desc)) {
				foundInstruction = node;
				break;
			}
		}

		if (foundInstruction == null) {
			throw new IllegalStateException("Failed to redirect a method call in PhysicsMod rendering");
		}

		MethodInsnNode redirection = new MethodInsnNode(Opcodes.INVOKESTATIC, replacementOwner, replacementName, replacementDesc, false);

		found.instructions.insertBefore(foundInstruction, redirection);
		found.instructions.remove(foundInstruction);
	}
}
