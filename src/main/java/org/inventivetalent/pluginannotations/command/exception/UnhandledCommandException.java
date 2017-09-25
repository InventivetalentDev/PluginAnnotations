package org.inventivetalent.pluginannotations.command.exception;

@SuppressWarnings({"unused", "WeakerAccess"})
public class UnhandledCommandException extends CommandException {
	public UnhandledCommandException(String message) {
		super(message);
	}

	public UnhandledCommandException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnhandledCommandException(Throwable cause) {
		super(cause);
	}
}
