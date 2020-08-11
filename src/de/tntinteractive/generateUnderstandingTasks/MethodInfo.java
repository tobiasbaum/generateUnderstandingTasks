package de.tntinteractive.generateUnderstandingTasks;

import org.objectweb.asm.Type;

public interface MethodInfo {

	public abstract String getMethodName();

	public abstract boolean isStatic();

	public abstract Type getReturnType();

	public abstract String getClassName();

	public abstract String getPathToClass();

}
