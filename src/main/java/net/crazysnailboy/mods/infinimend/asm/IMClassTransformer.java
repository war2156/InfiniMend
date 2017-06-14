package net.crazysnailboy.mods.infinimend.asm;

import java.util.Arrays;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import net.minecraft.launchwrapper.IClassTransformer;


public class IMClassTransformer implements IClassTransformer
{

	private static final String[] classesBeingTransformed = new String[] { "net.minecraft.enchantment.EnchantmentArrowInfinite" };

	@Override
	public byte[] transform(String name, String transformedName, byte[] classBeingTransformed)
	{
		boolean isObfuscated = !name.equals(transformedName);
		int index = Arrays.asList(classesBeingTransformed).indexOf(transformedName);
		return index != -1 ? transform(index, classBeingTransformed, isObfuscated) : classBeingTransformed;
	}

	private static byte[] transform(int index, byte[] classBeingTransformed, boolean isObfuscated)
	{
		try
		{
			ClassNode classNode = new ClassNode();
			ClassReader classReader = new ClassReader(classBeingTransformed);
			classReader.accept(classNode, 0);

			switch(index)
			{
				case 0:
					transformEnchantment(classNode, isObfuscated);
					break;
			}

			ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
			classNode.accept(classWriter);
			return classWriter.toByteArray();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return classBeingTransformed;
	}


	private static void transformEnchantment(ClassNode enchantmentClass, boolean isObfuscated)
	{
		final String METHOD_NAME = isObfuscated ? "a" : "canApplyTogether";
		final String METHOD_DESCRIPTOR = isObfuscated ? "(Lali;)Z" : "(Lnet/minecraft/enchantment/Enchantment;)Z";

		for (MethodNode method : enchantmentClass.methods)
		{
			if (method.name.equals(METHOD_NAME) && method.desc.equals(METHOD_DESCRIPTOR))
			{
				/*
				Replacing:
					return ench instanceof EnchantmentMending ? false : super.canApplyTogether(ench);
				With:
					return super.canApplyTogether(ench);

				BYTECODE
				Replacing:
					mv.visitCode();
					Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitLineNumber(42, l0);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitTypeInsn(INSTANCEOF, "net/minecraft/enchantment/EnchantmentMending");
					Label l1 = new Label();
					mv.visitJumpInsn(IFEQ, l1);
					mv.visitInsn(ICONST_0);
					Label l2 = new Label();
					mv.visitJumpInsn(GOTO, l2);
					mv.visitLabel(l1);
					mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitMethodInsn(INVOKESPECIAL, "net/minecraft/enchantment/Enchantment", "canApplyTogether", "(Lnet/minecraft/enchantment/Enchantment;)Z", false);
					mv.visitLabel(l2);
					mv.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] {Opcodes.INTEGER});
					mv.visitInsn(IRETURN);
					Label l3 = new Label();
					mv.visitLabel(l3);
					mv.visitLocalVariable("this", "Lnet/minecraft/enchantment/EnchantmentArrowInfinite;", null, l0, l3, 0);
					mv.visitLocalVariable("ench", "Lnet/minecraft/enchantment/Enchantment;", null, l0, l3, 1);
					mv.visitMaxs(2, 2);
					mv.visitEnd();
				With:
					mv.visitCode();
					Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitLineNumber(49, l0);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitMethodInsn(INVOKESPECIAL, "net/minecraft/enchantment/Enchantment", "canApplyTogether", "(Lnet/minecraft/enchantment/Enchantment;)Z", false);
					mv.visitInsn(IRETURN);
					Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitLocalVariable("this", "Lnet/crazysnailboy/mods/infinimend/enchantment/EnchantmentArrowInfinite;", null, l0, l1, 0);
					mv.visitLocalVariable("ench", "Lnet/minecraft/enchantment/Enchantment;", null, l0, l1, 1);
					mv.visitMaxs(2, 2);
					mv.visitEnd();
				 */

				AbstractInsnNode targetNode = null;
				for (AbstractInsnNode instruction : method.instructions.toArray())
				{
					if (instruction.getOpcode() == Opcodes.ALOAD)
					{
						if (((VarInsnNode)instruction).var == 1 && instruction.getNext().getOpcode() == Opcodes.INSTANCEOF)
						{
							targetNode = instruction;
							break;
						}
					}
				}

				if (targetNode != null)
				{

					while (targetNode.getOpcode() != Opcodes.IRETURN)
					{
						targetNode = targetNode.getNext();
						method.instructions.remove(targetNode.getPrevious());
					}

					InsnList toInsert = new InsnList();
					toInsert.add(new VarInsnNode(Opcodes.ALOAD, 0));
					toInsert.add(new VarInsnNode(Opcodes.ALOAD, 1));
					toInsert.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, Type.getInternalName(net.minecraft.enchantment.Enchantment.class), (isObfuscated ? "func_77326_a" : "canApplyTogether"), METHOD_DESCRIPTOR, false));
					method.instructions.insertBefore(targetNode, toInsert);

				}

			}

		}
	}


}
