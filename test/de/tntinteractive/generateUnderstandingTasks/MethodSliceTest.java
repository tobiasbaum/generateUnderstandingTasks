package de.tntinteractive.generateUnderstandingTasks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MethodSliceTest {

	@Test
	public void testMethodSlice1() {
		final MethodSlice slice = MethodSlice.create(
				"increment",
				"public class Foo {\n"
				+ "    /**\n"
				+ "     * Does the incrementation.\n"
				+ "     */\n"
				+ "    public static int increment(int i) {\n"
				+ "        return i + 1;\n"
				+ "    }\n"
				+ "}\n");

		assertEquals("public class Foo {\n"
				+ "\n"
				+ "    /**\n"
				+ "     * Does the incrementation.\n"
				+ "     */\n"
				+ "    public static int increment(int i) {\n"
				+ "        return i + 1;\n"
				+ "    }\n"
				+ "}\n",
				slice.toString());
	}

	@Test
	public void testMethodSlice2() {
		final MethodSlice slice = MethodSlice.create(
				"increment",
				"public class Foo {\n"
				+ "    public static final int CONSTANT = 42;\n"
				+ "    public static int increment(int i) {\n"
				+ "        return i + 1;\n"
				+ "    }\n"
				+ "    public static int decrement(int i) {\n"
				+ "        return i - 1;\n"
				+ "    }\n"
				+ "}\n");

		assertEquals("public class Foo {\n"
				+ "\n"
				+ "    public static int increment(int i) {\n"
				+ "        return i + 1;\n"
				+ "    }\n"
				+ "}\n",
				slice.toString());
	}

	@Test
	public void testMethodSlice3() {
		final MethodSlice slice = MethodSlice.create(
				"increment",
				"public class Foo {\n"
				+ "    public static final int CONSTANT = 42;\n"
				+ "    private static int add(int i, int j) {\n"
				+ "        return i + j;\n"
				+ "    }\n"
				+ "    public static int increment(int i) {\n"
				+ "        return add(i, CONSTANT);\n"
				+ "    }\n"
				+ "    public static int decrement(int i) {\n"
				+ "        return add(i, -CONSTANT);\n"
				+ "    }\n"
				+ "}\n");

		assertEquals("public class Foo {\n"
				+ "\n"
				+ "    public static final int CONSTANT = 42;\n"
				+ "\n"
				+ "    private static int add(int i, int j) {\n"
				+ "        return i + j;\n"
				+ "    }\n"
				+ "\n"
				+ "    public static int increment(int i) {\n"
				+ "        return add(i, CONSTANT);\n"
				+ "    }\n"
				+ "}\n",
				slice.toString());
	}

	@Test
	public void testMethodSlice4() {
		final MethodSlice slice = MethodSlice.create(
				"foo",
				"package a.b.c;\n"
				+ "import java.io.File;\n"
				+ "import some.other.Type;\n"
				+ "public class Foo {\n"
				+ "    public static int bar() {\n"
				+ "        return new Type();\n"
				+ "    }\n"
				+ "    public static int foo(String s) {\n"
				+ "        return new File(s).getAbsolutePath();\n"
				+ "    }\n"
				+ "}\n");

		assertEquals("import java.io.File;\n"
				+ "\n"
				+ "public class Foo {\n"
				+ "\n"
				+ "    public static int foo(String s) {\n"
				+ "        return new File(s).getAbsolutePath();\n"
				+ "    }\n"
				+ "}\n",
				slice.toString());
	}

	@Test
	public void testMethodSlice5() {
		final MethodSlice slice = MethodSlice.create(
				"xyz",
				"public class Foo {\n"
				+ "    public Foo() {\n"
				+ "    }\n"
				+ "    public static String xyz() {\n"
				+ "        return \"xyz\";\n"
				+ "    }\n"
				+ "}\n");

		assertEquals("public class Foo {\n"
				+ "\n"
				+ "    public static String xyz() {\n"
				+ "        return \"xyz\";\n"
				+ "    }\n"
				+ "}\n",
				slice.toString());
	}

	@Test
	public void testMethodSlice6() {
		final MethodSlice slice = MethodSlice.create(
				"xyz",
				"public class Foo {\n"
				+ "    public Foo() {\n"
				+ "    }\n"
				+ "    public static String xyz() {\n"
				+ "        return new Foo().toString();\n"
				+ "    }\n"
				+ "}\n");

		assertEquals("public class Foo {\n"
				+ "\n"
				+ "    public Foo() {\n"
				+ "    }\n"
				+ "\n"
				+ "    public static String xyz() {\n"
				+ "        return new Foo().toString();\n"
				+ "    }\n"
				+ "}\n",
				slice.toString());
	}

}
