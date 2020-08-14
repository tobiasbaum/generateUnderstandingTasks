package de.tntinteractive.generateUnderstandingTasks;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.jar.JarFile;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

public class SuitableMethod implements MethodInfo {

	private final String pathToClass;
	private final String methodName;
	private final boolean isStatic;
	private final Type[] params;
	private final Type returnType;

	public SuitableMethod(String pathToClass, MethodNode method) {
		this.pathToClass = pathToClass;
		this.methodName = method.name;
		this.isStatic = (method.access & Opcodes.ACC_STATIC) != 0;
		this.params = Type.getArgumentTypes(method.desc);
		this.returnType = Type.getReturnType(method.desc);
	}

	public Set<UnderstandingTaskTemplate> generateTasks(int maxCount, JarFile sourceJar, Random r) {
		final Set<UnderstandingTaskTemplate> ret = new LinkedHashSet<>();
		final MethodSlice slice = MethodSlice.create(
				this, new SourceJar(sourceJar));
		for (int i = 0; i < maxCount * 10; i++) {
			final List<JavaValue> args = this.generateArgs(r);
			System.out.println(args);
			final UnderstandingTaskCode u = new UnderstandingTaskCode(slice, args, r);
			final String result = u.determineResult();
			if (this.isSuitableResult(result, args)) {
				ret.add(new UnderstandingTaskTemplate(u, result));
			}
			if (ret.size() >= maxCount) {
				break;
			}
		}
		return ret;
	}

	private boolean isSuitableResult(final String result, List<JavaValue> args) {
		return result != null
			&& !result.isEmpty()
			&& result.indexOf('\t') < 0
			&& result.indexOf('\n') < 0
			&& result.indexOf('\r') < 0
			&& result.trim().equals(result)
			&& !this.isIdentityResult(result, args);
	}

	private boolean isIdentityResult(String result, List<JavaValue> args) {
		return args.size() == 1
			&& result.equals(args.get(0).getPrinted());
	}

	private List<JavaValue> generateArgs(Random r) {
		final List<JavaValue> ret = new ArrayList<>();
		for (final Type type : this.params) {
			ret.add(JavaValue.generateArg(type, r));
		}
		return ret;
	}

	@Override
	public String getMethodName() {
		return this.methodName;
	}

	@Override
	public boolean isStatic() {
		return this.isStatic;
	}

	@Override
	public Type getReturnType() {
		return this.returnType;
	}

	@Override
	public String getClassName() {
		final File f = new File(this.pathToClass);
		final String name = f.getName();
		final int dot = name.indexOf('.');
		return name.substring(0, dot);
	}

	@Override
	public String getPathToClass() {
		return this.pathToClass;
	}

}
