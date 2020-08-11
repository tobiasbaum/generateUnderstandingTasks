package de.tntinteractive.generateUnderstandingTasks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import org.objectweb.asm.Type;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

public class UnderstandingTaskCode {

	private final String className;
	private final String code;

	public UnderstandingTaskCode(MethodSlice slice, List<JavaValue> args) {
		final CompilationUnit cu = slice.getCopyOfCode();
		this.className = slice.getClassName();
		final ClassOrInterfaceDeclaration cl = cu.getClassByName(slice.getClassName()).get();

		cl.addMember(this.createMainMethod(slice, args));
		this.code = cu.toString();
	}

	private MethodDeclaration createMainMethod(MethodSlice slice, List<JavaValue> args) {
		final String mainPrefix =
				"class Main {"
				+ "    public static void main(String[] args) {";
		final String mainSuffix =
				"    }"
				+ "}";
		final String mainCode;
		if (slice.isStatic()) {
			final String call = slice.getMethodName() + "(" + this.joinArgCode(args) + ")";
			mainCode = mainPrefix
					+ "        System.out.print(" + addToString(call, slice.getReturnType()) + ");"
					+ mainSuffix;
		} else {
			final String call = "inst." + slice.getMethodName() + "(" + this.joinArgCode(args) + ")";
			mainCode = mainPrefix
					+ "        " + slice.getClassName() + " inst = new " + slice.getClassName() + "();"
					+ "        System.out.print(" + addToString(call, slice.getReturnType()) + ");"
					+ mainSuffix;
		}

		final CompilationUnit mainCu = StaticJavaParser.parse(mainCode);
		return mainCu.getClassByName("Main").get().getMethodsByName("main").get(0);
	}

	private String joinArgCode(List<JavaValue> args) {
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
		default:
			throw new AssertionError("unknown type " + returnType.getInternalName());
		}
	}

	public String getCode() {
		return this.code;
	}

	public String determineResult() {
		return compileAndGetStdout(this.className, this.code, 10_000);
	}

	private static class JavaSourceFromString extends SimpleJavaFileObject {
		private final String code;

		JavaSourceFromString(String name, String code) {
			super(URI.create("string:///" + name.replace('.','/') + Kind.SOURCE.extension), Kind.SOURCE);
			this.code = code;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return this.code;
		}
	}

	private static class ExecThread extends Thread {
		private final Method method;
		private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		private volatile boolean successfulEnd;

		public ExecThread(Method method) {
			super("exec of " + method.toString());
			this.setDaemon(true);
			this.method = method;
		}

		@Override
		public void run() {
			final PrintStream oldSyso = System.out;
			try (PrintStream ps = new PrintStream(this.bytes, false, "UTF-8")){
				System.setOut(ps);
				this.method.invoke(null, new Object[] { null });
				this.successfulEnd = true;
			} catch (final Throwable e) {
			} finally {
				System.setOut(oldSyso);
			}
		}

		public String getStdout() {
			if (!this.successfulEnd) {
				return null;
			}
			try {
				return this.bytes.toString("UTF-8");
			} catch (final UnsupportedEncodingException e) {
				throw new AssertionError(e);
			}
		}
	}

	public static String compileAndGetStdout(String classname, String javaCode, long timeout) {
		final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		final JavaFileObject file = new JavaSourceFromString(classname, javaCode);

		final CompilationTask task =
				compiler.getTask(null, null, null, null, null, Collections.singletonList(file));

		final boolean success = task.call();
		if (!success) {
			return null;
		}

		try {
			final URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] { new File("").toURI().toURL() });
			final Method mainMethod = Class.forName(classname, true, classLoader)
				.getDeclaredMethod("main", new Class[] { String[].class });

			final ExecThread thread = new ExecThread(mainMethod);
			thread.start();
			thread.join(timeout);
			final String stdout = thread.getStdout();
			thread.interrupt();
			return stdout;
		} catch (final Throwable e) {
			return null;
		}
	}

}