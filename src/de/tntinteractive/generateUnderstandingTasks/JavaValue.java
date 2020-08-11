package de.tntinteractive.generateUnderstandingTasks;

import java.util.List;
import java.util.stream.Collectors;

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
			return new JavaValue(
					"new " + typeStr + "[0]",
					"[]");
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

}
