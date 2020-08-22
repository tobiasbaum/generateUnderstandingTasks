package de.tntinteractive.generateUnderstandingTasks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MethodSliceTest {

	@Test
	public void testMethodSlice1() {
		final MethodSlice slice = MethodSlice.create(
				"increment",
				true,
				"public class Foo {\n"
				+ "    /**\n"
				+ "     * Does the incrementation.\n"
				+ "     */\n"
				+ "    public static int increment(int i) {\n"
				+ "        return i + 1;\n"
				+ "    }\n"
				+ "}\n",
				new StubSourceSource());

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
				true,
				"public class Foo {\n"
				+ "    public static final int CONSTANT = 42;\n"
				+ "    public static int increment(int i) {\n"
				+ "        return i + 1;\n"
				+ "    }\n"
				+ "    public static int decrement(int i) {\n"
				+ "        return i - 1;\n"
				+ "    }\n"
				+ "}\n",
				new StubSourceSource());

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
				true,
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
				+ "}\n",
				new StubSourceSource());

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
				true,
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
				+ "}\n",
				new StubSourceSource());

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
				true,
				"public class Foo {\n"
				+ "    public Foo() {\n"
				+ "    }\n"
				+ "    public static String xyz() {\n"
				+ "        return \"xyz\";\n"
				+ "    }\n"
				+ "}\n",
				new StubSourceSource());

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
		final StubSourceSource sss = new StubSourceSource();
		sss.put("Foo.java",
				"public class Foo {\n"
				+ "    public Foo() {\n"
				+ "    }\n"
				+ "    public static String xyz() {\n"
				+ "        return new Foo().toString();\n"
				+ "    }\n"
				+ "}\n");
		final MethodSlice slice = MethodSlice.create(
				"xyz",
				true,
				sss.getSource("Foo.java").toString(),
				sss);

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

	@Test
	public void testMethodSliceNested() {
		final MethodSlice slice = MethodSlice.create(
				"foo",
				true,
				"public class Foo {\n"
				+ "    private static int a;\n"
				+ "    private static int b;\n"
				+ "    private static int c;\n"
				+ "    public static int foo() {\n"
				+ "        return bar() + a;\n"
				+ "    }\n"
				+ "    public static int bar() {\n"
				+ "        return b;\n"
				+ "    }\n"
				+ "    public static int baz() {\n"
				+ "        return c;\n"
				+ "    }\n"
				+ "}\n",
				new StubSourceSource());

		assertEquals("public class Foo {\n" +
				"\n" +
				"    private static int a;\n" +
				"\n" +
				"    private static int b;\n" +
				"\n" +
				"    public static int foo() {\n" +
				"        return bar() + a;\n" +
				"    }\n" +
				"\n" +
				"    public static int bar() {\n" +
				"        return b;\n" +
				"    }\n" +
				"}\n",
				slice.toString());
	}

	@Test
	public void testMethodSliceNested2() {
		final MethodSlice slice = MethodSlice.create(
				"bar",
				false,
				"public class Foo {\n"
				+ "    private int a;\n"
				+ "    private int b;\n"
				+ "    private int c;\n"
				+ "    public Foo() {\n"
				+ "        a = 5;\n"
				+ "        b = 10;\n"
				+ "    }\n"
				+ "    public int bar() {\n"
				+ "        return b;\n"
				+ "    }\n"
				+ "    public int baz() {\n"
				+ "        return c;\n"
				+ "    }\n"
				+ "}\n",
				new StubSourceSource());

		assertEquals("public class Foo {\n" +
				"\n" +
				"    private int a;\n" +
				"\n" +
				"    private int b;\n" +
				"\n" +
				"    public Foo() {\n" +
				"        a = 5;\n" +
				"        b = 10;\n" +
				"    }\n" +
				"\n" +
				"    public int bar() {\n" +
				"        return b;\n" +
				"    }\n" +
				"}\n",
				slice.toString());
	}

	@Test
	public void testMethodSliceMultipleClasses1() {
		final StubSourceSource otherSources = new StubSourceSource();
		otherSources.put("Constants.java",
				"public class Constants {\n"
				+ "    public static final String ABC = \"123\";\n"
				+ "    public static final String XYZ = \"asdf\";\n"
				+ "}\n");

		final MethodSlice slice = MethodSlice.create(
				"baz",
				false,
				"public class Foo {\n"
				+ "    public static String baz() {\n"
				+ "        return Constants.XYZ;\n"
				+ "    }\n"
				+ "}\n",
				otherSources);

		assertEquals(
				"public class Foo {\n"
				+ "\n"
				+ "    public static String baz() {\n"
				+ "        return Constants.XYZ;\n"
				+ "    }\n"
				+ "}\n"
				+ "\n"
				+ "class Constants {\n"
				+ "\n"
				+ "    public static final String XYZ = \"asdf\";\n"
				+ "}\n",
				slice.toString());
	}
	@Test
	public void testMethodSliceMultipleClasses2() {
		final StubSourceSource otherSources = new StubSourceSource();
		otherSources.put("Constants.java",
				"import java.io.File;\n"
				+ "import java.util.Date;\n"
				+ "\n"
				+ "public class Constants {\n"
				+ "    public static final String XYZ = new Date().toString() + new File(\"x\");\n"
				+ "}\n");

		final MethodSlice slice = MethodSlice.create(
				"baz",
				false,
				"import java.util.Date;\n"
				+ "\n"
				+ "public class Foo {\n"
				+ "    public static String baz() {\n"
				+ "        return Constants.XYZ + new Date();\n"
				+ "    }\n"
				+ "}\n",
				otherSources);

		assertEquals(
				"import java.util.Date;\n"
				+ "import java.io.File;\n"
				+ "\n"
				+ "public class Foo {\n"
				+ "\n"
				+ "    public static String baz() {\n"
				+ "        return Constants.XYZ + new Date();\n"
				+ "    }\n"
				+ "}\n"
				+ "\n"
				+ "class Constants {\n"
				+ "\n"
				+ "    public static final String XYZ = new Date().toString() + new File(\"x\");\n"
				+ "}\n",
				slice.toString());
	}
}
