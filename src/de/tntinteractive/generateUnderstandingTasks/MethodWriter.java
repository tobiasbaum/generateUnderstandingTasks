package de.tntinteractive.generateUnderstandingTasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.objectweb.asm.Type;

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.Parameter;

public class MethodWriter {

	private final StringBuilder code = new StringBuilder();

	@Override
	public String toString() {
		return this.code.toString();
	}

	public void addMainPrefix() {
		this.code.append("class Main {\n");
		this.code.append("    public static void main(String[] args) {\n");
	}

	public void addMainSuffix() {
		this.code.append("    }");
		this.code.append("}");
	}

	public void printCreateInstance(MethodSlice slice, Random r) {
		final List<JavaValue> args = new ArrayList<>();
		final ConstructorDeclaration constructor = slice.selectRandomSuitableConstructor(r);
		if (constructor != null) {
			for (final Parameter p : constructor.getParameters()) {
				args.add(JavaValue.generateArg(p.getType(), r));
			}
		}
		this.code.append("        " + slice.getClassName() + " inst = new " + slice.getClassName() + "(" + joinArgCode(args) + ");");
	}

	public void printInstanceCall(MethodSlice slice, final List<JavaValue> args) {
		final String call = "inst." + slice.getMethodName() + "(" + joinArgCode(args) + ")";
		this.code.append("        System.out.print(" + addToString(call, slice.getReturnType()) + ");");
	}

	public void printStaticCall(MethodSlice slice, final List<JavaValue> args) {
		final String call = slice.getMethodName() + "(" + joinArgCode(args) + ")";
		this.code.append("        System.out.print(" + addToString(call, slice.getReturnType()) + ");");
	}

	private static String joinArgCode(List<JavaValue> args) {
		return args.stream()
				.map(JavaValue::getCode)
				.collect(Collectors.joining(", "));
	}

	private static String addToString(String call, Type returnType) {
		switch (returnType.getInternalName()) {
		case "java/lang/String":
			return call;
		case "[Ljava/lang/String;":
			return "java.util.Arrays.toString(" + call + ")";
		case "I":
		case "S":
		case "B":
			return "Integer.toString(" + call + ")";
		case "C":
			return "Character.toString(" + call + ")";
		case "J":
			return "Long.toString(" + call + ")";
		case "Z":
			return "Boolean.toString(" + call + ")";
		default:
			throw new AssertionError("unknown type " + returnType.getInternalName());
		}
	}

}
