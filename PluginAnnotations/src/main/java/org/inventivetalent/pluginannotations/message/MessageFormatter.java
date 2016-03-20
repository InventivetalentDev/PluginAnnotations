package org.inventivetalent.pluginannotations.message;

public class MessageFormatter {

	public static final Class<MessageFormatter> DEFAULT = MessageFormatter.class;

	public String format(String key, String message) {
		return message;
	}

}
