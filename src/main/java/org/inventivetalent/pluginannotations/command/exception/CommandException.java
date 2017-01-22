package org.inventivetalent.pluginannotations.command.exception;

public class CommandException extends RuntimeException {
	public CommandException() {
	}

	public CommandException(String message) {
		super(message);
	}

	public CommandException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommandException(Throwable cause) {
		super(cause);
	}
}
