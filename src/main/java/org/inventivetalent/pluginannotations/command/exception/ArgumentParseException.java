package org.inventivetalent.pluginannotations.command.exception;

@SuppressWarnings({"unused", "WeakerAccess"})
public class ArgumentParseException extends CommandException {

	private String   argument;
	private Class<?> type;

	public ArgumentParseException() {
	}

	public ArgumentParseException(String message) {
		super(message);
	}

	public ArgumentParseException(String message, String argument, Class<?> type) {
		super(message);
		this.argument = argument;
		this.type = type;
	}

	public ArgumentParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArgumentParseException(String message, Throwable cause, String argument, Class<?> type) {
		super(message, cause);
		this.argument = argument;
		this.type = type;
	}

	public ArgumentParseException(Throwable cause) {
		super(cause);
	}

	public ArgumentParseException(Throwable cause, String argument, Class<?> type) {
		super(cause);
		this.argument = argument;
		this.type = type;
	}

	public String getArgument() {
		return argument;
	}

	public Class<?> getParameterType() {
		return type;
	}
}
