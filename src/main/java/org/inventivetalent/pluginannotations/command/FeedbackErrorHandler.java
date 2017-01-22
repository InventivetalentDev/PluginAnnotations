package org.inventivetalent.pluginannotations.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.inventivetalent.pluginannotations.command.exception.*;

public class FeedbackErrorHandler extends CommandErrorHandler {

	@Override
	public void handleCommandException(CommandException exception, CommandSender sender, Command command, String[] args) {
		sender.sendMessage(ChatColor.RED + "Unknown exception, see console for details: " + exception.getMessage());
		throw exception;
	}

	@Override
	public void handlePermissionException(PermissionException exception, CommandSender sender, Command command, String[] args) {
		sender.sendMessage(ChatColor.RED + "You are missing the following permission: " + exception.getPermission());
	}

	@Override
	public void handleIllegalSender(IllegalSenderException exception, CommandSender sender, Command command, String[] args) {
		sender.sendMessage(ChatColor.RED + "This command is only available to players");
	}

	@Override
	public void handleUnhandled(UnhandledCommandException exception, CommandSender sender, Command command, String[] args) {
		sender.sendMessage(ChatColor.RED + "Unhandled exception, see console for details: " + exception.getMessage());
	}

	@Override
	public void handleLength(InvalidLengthException exception, CommandSender sender, Command command, String[] args) {
		sender.sendMessage(ChatColor.RED + exception.getMessage());
		sender.sendMessage(ChatColor.RED + "Usage: /" + command.getName() + " " + command.getUsage());
	}

	@Override
	public void handleArgumentParse(ArgumentParseException exception, CommandSender sender, Command command, String[] args) {
		sender.sendMessage(ChatColor.RED + exception.getMessage());
	}
}
