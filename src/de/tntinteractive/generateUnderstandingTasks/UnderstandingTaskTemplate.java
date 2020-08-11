package de.tntinteractive.generateUnderstandingTasks;

public class UnderstandingTaskTemplate {

	private final String code;
	private final String result;

	public UnderstandingTaskTemplate(UnderstandingTaskCode u, String result) {
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

}
