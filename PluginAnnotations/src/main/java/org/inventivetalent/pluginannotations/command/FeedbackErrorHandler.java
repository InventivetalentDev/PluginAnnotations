/*
 * Copyright 2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

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
		throw exception;
	}
}
