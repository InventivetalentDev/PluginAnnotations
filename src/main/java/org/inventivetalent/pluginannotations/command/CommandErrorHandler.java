package org.inventivetalent.pluginannotations.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.inventivetalent.pluginannotations.command.exception.*;

public class CommandErrorHandler {

	public static final Class<? extends CommandErrorHandler> VOID     = VoidErrorHandler.class;
	public static final Class<? extends CommandErrorHandler> FEEDBACK = FeedbackErrorHandler.class;

	public void handleCommandException(CommandException exception, CommandSender sender, Command command, String[] args) {
	}

	public void handlePermissionException(PermissionException exception, CommandSender sender, Command command, String[] args) {
	}

	public void handleIllegalSender(IllegalSenderException exception, CommandSender sender, Command command, String[] args) {
	}

	public void handleUnhandled(UnhandledCommandException exception, CommandSender sender, Command command, String[] args) {
	}

	public void handleLength(InvalidLengthException exception, CommandSender sender, Command command, String[] args) {
	}

	public void handleArgumentParse(ArgumentParseException exception, CommandSender sender, Command command, String[] args) {
	}

}
