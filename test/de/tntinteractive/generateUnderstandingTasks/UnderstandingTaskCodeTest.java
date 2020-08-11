package de.tntinteractive.generateUnderstandingTasks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.Test;

public class UnderstandingTaskCodeTest {

	@Test
	public void testCompileAndGetStdout1() {
		final String actual = UnderstandingTaskCode.compileAndGetStdout(
				"Foo",
				"public class Foo {\n"
				+ "    public static void main(String[] args) {\n"
				+ "        System.out.print(\"abc\");\n"
				+ "    }\n"
				+ "}\n",
				1000_000);
		assertEquals("abc", actual);
	}

	@Test
	public void testCompileAndGetStdout2() {
		final String actual = UnderstandingTaskCode.compileAndGetStdout(
				"Bar",
				"public class Bar {\n"
				+ "    public static void main(String[] args) {\n"
				+ "        System.out.print(\"x\");\n"
				+ "        System.out.print(\"y\");\n"
				+ "    }\n"
				+ "}\n",
				1000_000);
		assertEquals("xy", actual);
	}

	@Test
	public void testCompileAndGetStdoutSyntaxError() {
		final String actual = UnderstandingTaskCode.compileAndGetStdout(
				"Foo", "wrong syntax", 1000_000);
		assertEquals(null, actual);
	}

	@Test
	public void testCompileAndGetStdoutTimeout() {
		final String actual = UnderstandingTaskCode.compileAndGetStdout(
				"Bar",
				"public class Bar {\n"
				+ "    public static void main(String[] args) throws Exception {\n"
				+ "        System.out.print(\"x\");\n"
				+ "        Thread.sleep(10_000);\n"
				+ "    }\n"
				+ "}\n",
				1);
		assertEquals(null, actual);
	}

	@Test
	public void testCompileAndGetStdoutError() {
		final String actual = UnderstandingTaskCode.compileAndGetStdout(
				"Bar",
				"public class Bar {\n"
				+ "    public static void main(String[] args) throws Exception {\n"
				+ "        throw new AssertionError();\n"
				+ "    }\n"
				+ "}\n",
				1000_000);
		assertEquals(null, actual);
	}

	@Test
	public void testDetermineResult() {
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

		final UnderstandingTaskCode code = new UnderstandingTaskCode(
				slice, Collections.singletonList(JavaValue.intValue("1")));
		assertEquals("2", code.determineResult());
	}

	@Test
	public void testGetCode() {
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

		final UnderstandingTaskCode code = new UnderstandingTaskCode(
				slice, Collections.singletonList(JavaValue.intValue("1")));
		assertEquals(
				"public class Foo {\n"
				+ "\n"
				+ "    /**\n"
				+ "     * Does the incrementation.\n"
				+ "     */\n"
				+ "    public static int increment(int i) {\n"
				+ "        return i + 1;\n"
				+ "    }\n"
				+ "\n"
				+ "    public static void main(String[] args) {\n"
				+ "        System.out.print(increment(1));\n"
				+ "    }\n"
				+ "}\n",
				code.getCode());
	}
}
