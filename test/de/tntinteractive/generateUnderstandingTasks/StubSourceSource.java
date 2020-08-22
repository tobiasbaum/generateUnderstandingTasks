package de.tntinteractive.generateUnderstandingTasks;

import java.util.HashMap;
import java.util.Map;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class StubSourceSource implements SourceSource {

	private final Map<String, String> files = new HashMap<>();

	public void put(String path, String source) {
		assert !this.files.containsKey(path);
		this.files.put(path, source);
	}

	@Override
	public CompilationUnit getSource(String pathToClass) {
		if (pathToClass.endsWith(".class")) {
			return this.getSource(pathToClass.replace(".class", ".java"));
		}

		final String source = this.files.get(pathToClass);
		if (source == null) {
			return null;
		}
		return StaticJavaParser.parse(source);
	}

}
