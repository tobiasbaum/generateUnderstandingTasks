package de.tntinteractive.generateUnderstandingTasks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.objectweb.asm.Type;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.nodeTypes.NodeWithExtends;
import com.github.javaparser.ast.nodeTypes.NodeWithImplements;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;

public class MethodSlice {

	private final MethodInfo info;
	private final CompilationUnit compilationUnit;

	private MethodSlice(MethodInfo info, CompilationUnit cu) {
		this.info = info;
		this.compilationUnit = cu;
	}

	public static MethodSlice create(MethodInfo info, SourceJar sourceJar) {
		return create(info, sourceJar.getSource(info.getPathToClass()), sourceJar);
	}

	public static MethodSlice create(String methodName, boolean isStatic, String code, SourceSource sourceSource) {
		final CompilationUnit cu = StaticJavaParser.parse(code);
		final String className = cu.getTypes().get(0).getName().asString();
		final MethodInfo dummy = new MethodInfo() {
			@Override
			public boolean isStatic() {
				return isStatic;
			}
			@Override
			public Type getReturnType() {
				return Type.getType(String.class);
			}
			@Override
			public String getPathToClass() {
				return className + ".java";
			}

			@Override
			public String getMethodName() {
				return methodName;
			}

			@Override
			public String getClassName() {
				return className;
			}
		};
		return create(dummy, cu, sourceSource);
	}

	private static MethodSlice create(MethodInfo info, CompilationUnit cu, SourceSource sourceSource) {

		final Set<String> newRelevantNames = new HashSet<>();
		newRelevantNames.add(info.getMethodName());
		if (!info.isStatic()) {
			newRelevantNames.add(info.getClassName());
		}

		Set<String> relevantNames = determineRelevantNameHull(newRelevantNames, getClassFrom(cu));
		final List<CompilationUnit> referencedClasses =
				resolveMissingClasses(relevantNames, toPackagePath(info), info.getClassName(), sourceSource);
		removeAllIrrelevantEntries(cu, relevantNames);
		for (final CompilationUnit otherClass : referencedClasses) {
			relevantNames = determineRelevantNameHull(relevantNames, getClassFrom(otherClass));
		}
		for (final CompilationUnit otherClass : referencedClasses) {
			removeAllIrrelevantEntries(otherClass, relevantNames);
			mergeInto(otherClass, cu);
		}
		return new MethodSlice(info, cu);
	}

	private static void mergeInto(CompilationUnit source, CompilationUnit target) {
		for (final ImportDeclaration im : source.getImports()) {
			target.addImport(im);
		}

		for (final TypeDeclaration<?> type : source.getTypes()) {
			type.setModifiers();
			target.addType(type);
		}
	}

	private static String toPackagePath(MethodInfo info) {
		final File parentFile = new File(info.getPathToClass()).getParentFile();
		return parentFile == null ? "" : parentFile.toString() + "/";
	}

	private static List<CompilationUnit> resolveMissingClasses(
			Set<String> relevantNames, String currentPackagePath, String mainClass, SourceSource sourceSource) {

		final List<CompilationUnit> ret = new ArrayList<CompilationUnit>();
		for (final String name : relevantNames) {
			if (Character.isUpperCase(name.charAt(0)) && !name.equals(mainClass)) {
				final CompilationUnit cu = sourceSource.getSource(currentPackagePath + name + ".java");
				if (cu != null) {
					ret.add(cu);
				}
			}
		}
		return ret;
	}

	private static Set<String> determineRelevantNameHull(
			Set<String> entryPointNames, final TypeDeclaration<?> type) {
		Set<String> newRelevantNames = entryPointNames;
		final Set<String> relevantNames = new HashSet<>();
		do {
			final Set<String> nextNewRelevantNames = new HashSet<>();
			for (final String name : newRelevantNames) {
				extractAllUsedNamesFromTypeEntryIfExists(name, type, nextNewRelevantNames);
			}
			relevantNames.addAll(newRelevantNames);
			nextNewRelevantNames.removeAll(relevantNames);
			newRelevantNames = nextNewRelevantNames;
		} while (!newRelevantNames.isEmpty());
		return relevantNames;
	}

	private static TypeDeclaration<?> getClassFrom(CompilationUnit cu) {
		for (final TypeDeclaration<?> t : cu.getTypes()) {
			if (t.isPublic()) {
				return t;
			}
		}
		return cu.getTypes().get(0);
	}

	private static void extractAllUsedNamesFromTypeEntryIfExists(
			String typeEntryName, final TypeDeclaration<?> typeDeclaration, Set<String> relevantNames) {
		for (final MethodDeclaration method : typeDeclaration.getMethodsByName(typeEntryName)) {
			extractAllUsedNames(method, relevantNames);
		}
		final Optional<FieldDeclaration> field = typeDeclaration.getFieldByName(typeEntryName);
		if(field.isPresent()) {
			extractAllUsedNames(field.get(), relevantNames);
		}
		if (typeEntryName.equals(typeDeclaration.getNameAsString())) {
			for (final ConstructorDeclaration cons : typeDeclaration.getConstructors()) {
				extractAllUsedNames(cons, relevantNames);
			}
		}
	}

	private static void extractAllUsedNames(Node method, Set<String> relevantNames) {
		for (final Node node : method.findAll(Node.class, (Node n) -> n instanceof NodeWithSimpleName)) {
			relevantNames.add(((NodeWithSimpleName<?>) node).getNameAsString());
		}
	}

	private static void removeAllIrrelevantEntries(CompilationUnit cu, Set<String> relevantNames) {
		cu.removePackageDeclaration();

		for (final ImportDeclaration imp : new ArrayList<>(cu.getImports())) {
			final String importString = imp.getNameAsString();
			if (!(importString.startsWith("java")
					&& (relevantNames.contains(imp.getName().getIdentifier()) || importString.indexOf('*') >= 0))) {
				imp.remove();
			}
		}

		for (final TypeDeclaration<?> type : cu.getTypes()) {
			if (type instanceof NodeWithImplements) {
				((NodeWithImplements) type).setImplementedTypes(new NodeList<>());
			}
			if (type instanceof NodeWithExtends) {
				((NodeWithExtends) type).setExtendedTypes(new NodeList<>());
			}
			type.setModifiers(Keyword.PUBLIC);
			type.setAnnotations(new NodeList<>());

			for (final MethodDeclaration method : new ArrayList<>(type.getMethods())) {
				if (!relevantNames.contains(method.getNameAsString())) {
					method.remove();
				}
				method.setAnnotations(new NodeList<>());
			}
			for (final ConstructorDeclaration cons : new ArrayList<>(type.getConstructors())) {
				if (!relevantNames.contains(cons.getNameAsString())) {
					cons.remove();
				}
				cons.setAnnotations(new NodeList<>());
			}
			for (final FieldDeclaration field : new ArrayList<>(type.getFields())) {
				for (final VariableDeclarator v : new ArrayList<>(field.getVariables())) {
					if (!relevantNames.contains(v.getNameAsString())) {
						v.removeForced();
					}
				}
				field.setAnnotations(new NodeList<>());
				if (field.getVariables().isEmpty()) {
					field.remove();
				}
			}
		}
	}

	public CompilationUnit getCopyOfCode() {
		return this.compilationUnit.clone();
	}

	public boolean isStatic() {
		return this.info.isStatic();
	}

	public String getClassName() {
		return this.info.getClassName();
	}

	public String getMethodName() {
		return this.info.getMethodName();
	}

	public ConstructorDeclaration selectRandomSuitableConstructor(Random r) {
		final TypeDeclaration<?> type = this.compilationUnit.getType(0);
		final List<ConstructorDeclaration> suitableConstructors = new ArrayList<>();
		for (final ConstructorDeclaration d : type.getConstructors()) {
			if (this.hasSuitableTypes(d)) {
				suitableConstructors.add(d);
			}
		}
		if (suitableConstructors.isEmpty()) {
			return null;
		}
		return suitableConstructors.get(r.nextInt(suitableConstructors.size()));
	}

	private boolean hasSuitableTypes(ConstructorDeclaration d) {
		for (final Parameter p : d.getParameters()) {
			if (!JavaValue.isSuitableType(p.getType())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return this.compilationUnit.toString();
	}

	public Type getReturnType() {
		return this.info.getReturnType();
	}

}
