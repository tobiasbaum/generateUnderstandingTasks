package de.tntinteractive.generateUnderstandingTasks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class GenerateUnderstandingTasks {

	public static void main(String[] args) throws Exception {
		final File jar = new File("/home/ich/eclipse-workspace/DreiDBeraterDaten/build/libs/DreiDBeraterDaten-1.0.jar");
		final File sourceJar = jar;
		final List<SuitableMethod> m = findSuitableMethodsInJar(jar);
		final Random r = new Random(1234);
		generateTasks(m, sourceJar, r);
	}

	private static List<SuitableMethod> findSuitableMethodsInJar(File jarFile) throws IOException {
		final List<SuitableMethod> ret = new ArrayList<>();
		try (JarFile jar = new JarFile(jarFile)) {
			final Enumeration<JarEntry> iter = jar.entries();
			while (iter.hasMoreElements()) {
				final JarEntry entry = iter.nextElement();
				if (!entry.getName().endsWith(".class")
						|| entry.getName().contains("$")) {
					continue;
				}
				System.out.println("Analysing " + entry);
				try (InputStream is = jar.getInputStream(entry)) {
					final ClassReader cr = new ClassReader(is);
					final ClassNode classNode = new ClassNode();
					cr.accept(classNode, ClassReader.EXPAND_FRAMES);
					ret.addAll(findSuitableMethodsInClass(entry.getName(), classNode));
				}
			}
		}
		return ret;
	}

	private static List<SuitableMethod> findSuitableMethodsInClass(String path, ClassNode classNode) {
		final List<SuitableMethod> ret = new ArrayList<>();
		for (final MethodNode method : classNode.methods) {
			if (isSuitable(method)) {
				ret.add(new SuitableMethod(path, method));
			}
		}
		return ret;
	}

	private static boolean isSuitable(MethodNode method) {
		final Type[] params = Type.getArgumentTypes(method.desc);
		return params.length >= 1
			&& Arrays.asList(params).stream().allMatch(GenerateUnderstandingTasks::isSuitableType)
			&& isSuitableType(Type.getReturnType(method.desc));
	}

	private static boolean isSuitableType(Type type) {
		switch (type.getDescriptor()) {
		case "Ljava/lang/String;":
		case "[Ljava/lang/String;":
		case "Z":
		case "C":
		case "B":
		case "S":
		case "I":
		case "J":
			return true;
		default:
			return false;
		}
	}

	private static void generateTasks(List<SuitableMethod> ms, File sourceJar, Random r) throws IOException {
		try (JarFile jarFile = new JarFile(sourceJar)) {
			for (final SuitableMethod m : ms) {
				final Set<UnderstandingTaskTemplate> tasks = m.generateTasks(3, jarFile, r);
				//TEST
				for (final UnderstandingTaskTemplate t : tasks) {
					System.out.println(t.toString());
				}
			}
		}
	}

}