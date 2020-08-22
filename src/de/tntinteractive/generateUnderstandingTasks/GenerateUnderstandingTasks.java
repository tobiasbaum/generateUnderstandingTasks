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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class GenerateUnderstandingTasks {

	public static void main(String[] args) throws Exception {
		final File jar;
		final File sourceJar;
//		jar = new File("/home/ich/eclipse-workspace/DreiDBeraterDaten/build/libs/DreiDBeraterDaten-1.0.jar");
//		sourceJar = jar;

//		jar = new File("/home/ich/.gradle/caches/modules-2/files-2.1/com.eclipsesource.minimal-json/minimal-json/0.9.4/d6e7dd22569de97c2697a4af301a623f35028972/minimal-json-0.9.4.jar");
//		sourceJar = new File("/home/ich/.gradle/caches/modules-2/files-2.1/com.eclipsesource.minimal-json/minimal-json/0.9.4/a7f9afb2417b0126267435586bce893498bd8eff/minimal-json-0.9.4-sources.jar");

		jar = new File("/home/ich/.gradle/caches/modules-2/files-2.1/commons-lang/commons-lang/2.4/16313e02a793435009f1e458fa4af5d879f6fb11/commons-lang-2.4.jar");
		sourceJar = new File("/home/ich/.gradle/caches/modules-2/files-2.1/commons-lang/commons-lang/2.4/2b8c4b3035e45520ef42033e823c7d33e4b4402c/commons-lang-2.4-sources.jar");

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
			&& (method.access & Opcodes.ACC_ABSTRACT) == 0
			&& Arrays.asList(params).stream().allMatch(GenerateUnderstandingTasks::isSuitableType)
			&& isSuitableType(Type.getReturnType(method.desc));
	}

	private static boolean isSuitableType(Type type) {
		switch (type.getDescriptor()) {
		case "Ljava/util/Set;":
		case "Ljava/util/List;":
		case "Ljava/util/Collection;":
		case "Ljava/util/Iterable;":
		case "Ljava/lang/String;":
		case "Z":
		case "C":
		case "B":
		case "S":
		case "I":
		case "J":
		case "[Ljava/lang/String;":
		case "[Z":
		case "[C":
		case "[B":
		case "[S":
		case "[I":
		case "[J":
			return true;
		default:
			return false;
		}
	}

	private static void generateTasks(List<SuitableMethod> ms, File sourceJar, Random r) throws IOException {
		int total = 0;
		try (JarFile jarFile = new JarFile(sourceJar)) {
			for (final SuitableMethod m : ms) {
				final Set<UnderstandingTaskTemplate> tasks = m.generateTasks(3, jarFile, r);

				final File targetRoot = new File("taskdb");
				for (final UnderstandingTaskTemplate t : tasks) {
					t.writeTask(targetRoot);
					total++;
				}
			}
		}
		System.out.println("Created " + total + " tasks");
	}

}
