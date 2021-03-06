package de.tntinteractive.generateUnderstandingTasks;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

public class SourceJar implements SourceSource {

	private final JarFile jar;

	public SourceJar(JarFile sourceJar) {
		this.jar = sourceJar;
	}

	@Override
	public CompilationUnit getSource(String pathToClass) {
		if (pathToClass.endsWith(".class")) {
			return this.getSource(pathToClass.replace(".class", ".java"));
		}

		final ZipEntry entry = this.jar.getEntry(pathToClass);
		if (entry == null) {
			System.out.println("entry not found " + pathToClass);
			return null;
		}
		try (InputStream stream = this.jar.getInputStream(entry)) {
			//TEST TODO das ist eine statische Einstellung und sollte nicht hier gesetzt werden
			StaticJavaParser.getConfiguration().setAttributeComments(false);
			return StaticJavaParser.parse(stream);
		} catch (final IOException e) {
			return null;
		}
	}

}
