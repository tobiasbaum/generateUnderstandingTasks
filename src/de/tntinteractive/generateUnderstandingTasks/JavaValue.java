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
		case "Ljava/lang/String;":
			return JavaValue.stringValue(
				oneOf(r, "", "a", "b", "X", "Y", "Foo", "Bar", "B A Z", "Hello World!", "http://example.com",
						"42", "123-456", "The lazy fox jumps", "tmp\\xy/z", "\"inq\"", "\'inq\'"));
		case "[Ljava/lang/String;":
			final int count = r.nextInt(4);
			return generateArrayArg(count, "String", Type.getType(type.getDescriptor().substring(1)), r);
		case "[[Ljava/lang/String;":
			final int count2 = r.nextInt(4);
			return generateArrayArg(count2, "String[]", Type.getType(type.getDescriptor().substring(1)), r);
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
		default:
			throw new AssertionError("type=" + type.toString());
		}
	}

	private static JavaValue generateArrayArg(int count, String typeStr, Type type, Random r) {
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
