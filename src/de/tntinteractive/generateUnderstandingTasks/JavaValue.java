package de.tntinteractive.generateUnderstandingTasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.objectweb.asm.Type;

public class JavaValue {

	private final String code;
	private final String printed;

	private JavaValue(String code, String printed) {
		this.code = code;
		this.printed = printed;
	}

	public static JavaValue intValue(String value) {
		return new JavaValue(value, value);
	}

	public static JavaValue longValue(String value) {
		return new JavaValue(value, value);
	}

	public static JavaValue shortValue(String value) {
		return new JavaValue("(short) " + value, value);
	}

	public static JavaValue byteValue(String value) {
		return new JavaValue("(byte) " + value, value);
	}

	public static JavaValue booleanValue(boolean value) {
		final String s = Boolean.toString(value);
		return new JavaValue(s, s);
	}

	public static JavaValue charValue(String value) {
		return new JavaValue(
				"\'" + value.replace("\\", "\\\\").replace("\'", "\\\'").replace("\"", "\\\"") + "\'",
				value);
	}

	public static JavaValue stringValue(String value) {
		return new JavaValue(
				"\"" + value.replace("\\", "\\\\").replace("\'", "\\\'").replace("\"", "\\\"") + "\"",
				value);
	}

	public static JavaValue arrayValue(String typeStr, List<JavaValue> elements) {
		if (elements.isEmpty()) {
			final int index = typeStr.indexOf('[');
			if (index >= 0) {
				return new JavaValue("new " + typeStr.substring(0, index) + "[0]" + typeStr.substring(index), "[]");
			} else {
				return new JavaValue("new " + typeStr + "[0]", "[]");
			}
		}

		return new JavaValue(
				elements.stream().map(JavaValue::getCode).collect(Collectors.joining(", ", "new " + typeStr + "[] {", "}")),
				elements.stream().map(JavaValue::getPrinted).collect(Collectors.joining(", ", "[", "]")));
	}

	public String getCode() {
		return this.code;
	}

	public String getPrinted() {
		return this.printed;
	}

	@Override
	public String toString() {
		return this.printed;
	}

	public static JavaValue generateArg(Type type, Random r) {
		switch (type.getDescriptor()) {
		case "I":
			return JavaValue.intValue(oneOf(r, "0", "1", "-1", "2", "3", "4", "5", "10", "15", "20", "100", "1000"));
		case "J":
			return JavaValue.longValue(oneOf(r, "0", "1", "-1", "2", "3", "4", "5", "10", "15", "20", "100", "1000"));
		case "S":
			return JavaValue.shortValue(oneOf(r, "0", "1", "-1", "2", "3", "4", "5", "10", "15", "20", "100", "1000"));
		case "B":
			return JavaValue.byteValue(oneOf(r, "0", "1", "-1", "2", "3", "4", "5", "10", "15", "20", "100", "1000"));
		case "Z":
			return JavaValue.booleanValue(r.nextBoolean());
		case "C":
			return JavaValue.charValue(oneOf(r, "a", "b", "c", "x", "y", "z", "A", "B", "C", "Y", "Y", "Z", "!", "\"", "\'", " ", "_", ",", ".", "0", "1", "2", "9"));
		case "Ljava/lang/String;":
			return JavaValue.stringValue(
				oneOf(r, "", "a", "b", "X", "Y", "Foo", "Bar", "B A Z", "Hello World!", "http://example.com",
						"42", "123-456", "The lazy fox jumps", "tmp\\xy/z", "\"inq\"", "\'inq\'", "23.0", "72.1", "UTF-8"));
		case "[I":
			return generateArrayArg("int", Type.getType(type.getDescriptor().substring(1)), r);
		case "[J":
			return generateArrayArg("long", Type.getType(type.getDescriptor().substring(1)), r);
		case "[S":
			return generateArrayArg("short", Type.getType(type.getDescriptor().substring(1)), r);
		case "[B":
			return generateArrayArg("byte", Type.getType(type.getDescriptor().substring(1)), r);
		case "[Z":
			return generateArrayArg("boolean", Type.getType(type.getDescriptor().substring(1)), r);
		case "[C":
			return generateArrayArg("char", Type.getType(type.getDescriptor().substring(1)), r);
		case "[Ljava/lang/String;":
			return generateArrayArg("String", Type.getType(type.getDescriptor().substring(1)), r);
		case "[[Ljava/lang/String;":
			return generateArrayArg("String[]", Type.getType(type.getDescriptor().substring(1)), r);
		default:
			throw new AssertionError("unsupported type " + type.getDescriptor());
		}
	}

	public static JavaValue generateArg(com.github.javaparser.ast.type.Type type, Random r) {
		return generateArg(mapType(type), r);
	}

	public static boolean isSuitableType(com.github.javaparser.ast.type.Type type) {
		return mapType(type) != null;
	}

	private static Type mapType(com.github.javaparser.ast.type.Type type) {
		switch (type.toString()) {
		case "String[][]":
			return Type.getType("[[Ljava/lang/String;");
		case "String[]":
			return Type.getType("[Ljava/lang/String;");
		case "String":
			return Type.getType("Ljava/lang/String;");
		case "int":
			return Type.getType("I");
		default:
			return null;
		}
	}

	private static JavaValue generateArrayArg(String typeStr, Type type, Random r) {
		final int count = r.nextInt(4);
		final List<JavaValue> elements = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			elements.add(generateArg(type, r));
		}
		return arrayValue(typeStr, elements);
	}

	private static String oneOf(Random r, String... strings) {
		return strings[r.nextInt(strings.length)];
	}

}
