package de.tntinteractive.generateUnderstandingTasks;

import com.github.javaparser.ast.CompilationUnit;

public interface SourceSource {

	public abstract CompilationUnit getSource(String pathToClass);

}
