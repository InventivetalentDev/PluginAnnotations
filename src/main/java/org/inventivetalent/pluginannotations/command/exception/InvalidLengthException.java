package org.inventivetalent.pluginannotations.command.exception;

public class InvalidLengthException extends CommandException {

	private int expected;
	private int given;

	public InvalidLengthException(int expected, int given) {
		super((given < expected ? "Missing" : "Too many") + " arguments. (" + given + (given < expected ? "<" : ">") + expected + ")");
		this.expected = expected;
		this.given = given;
	}

	public int getExpectedLength() {
		return expected;
	}

	public int getGivenLength() {
		return given;
	}

	boolean isShorter() {
		return given < expected;
	}

	boolean isLonger() {
		return given > expected;
	}

}
