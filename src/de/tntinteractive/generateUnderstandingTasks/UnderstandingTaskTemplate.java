package de.tntinteractive.generateUnderstandingTasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public class UnderstandingTaskTemplate {

	private final String family;
	private final String code;
	private final String result;

	public UnderstandingTaskTemplate(String family, UnderstandingTaskCode u, String result) {
		this.family = family;
		this.code = u.getCode();
		this.result = result;
	}

	@Override
	public String toString() {
		return "Expecting " + this.result + " for:\n" + this.code;
	}

	@Override
	public int hashCode() {
		return this.code.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof UnderstandingTaskTemplate)) {
			return false;
		}
		final UnderstandingTaskTemplate other = (UnderstandingTaskTemplate) o;
		return this.result.equals(other.result)
			&& this.code.equals(other.code);
	}

	public void writeTask(File targetRoot) throws IOException {
		final File dir = new File(targetRoot, this.md5(this.code));
		dir.mkdir();

		Files.writeString(new File(dir, "source").toPath(), this.code);

		final Properties p = new Properties();
		p.setProperty("type", "understanding");
		p.setProperty("family", this.family);
		p.setProperty("expectedAnswer", this.result);
		try (FileOutputStream out = new FileOutputStream(new File(dir, "task.properties"))) {
			p.store(out, null);
		}
	}

	private String md5(String s) {
		try {
			final MessageDigest md = MessageDigest.getInstance("MD5");
			final byte[] hash = md.digest(s.getBytes("UTF-8"));
			final BigInteger bigInt = new BigInteger(1, hash);
			return bigInt.toString(16);
		} catch (final UnsupportedEncodingException | NoSuchAlgorithmException e) {
			throw new AssertionError(e);
		}
	}

}
