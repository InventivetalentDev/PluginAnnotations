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

import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.inventivetalent.pluginannotations.AccessUtil;
import org.inventivetalent.pluginannotations.command.exception.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AnnotatedCommand {

	private final Object              commandClass;
	private final Method              commandMethod;
	private final Command             commandAnnotation;
	private final Permission          permissionAnnotation;
	private final Method              completionMethod;
	private final Completion          completionAnnotation;
	private final CommandErrorHandler errorHandler;

	public String   name              = "";
	public String[] aliases           = new String[0];
	public String   usage             = "";
	public String   description       = "";
	public String   permission        = "";
	public String   permissionMessage = "";

	private BukkitCommand theCommand;

	public AnnotatedCommand(@Nonnull Object commandClass, @Nonnull Method commandMethod, @Nonnull Command commandAnnotation, @Nullable Permission permissionAnnotation, @Nullable Method completionMethod, @Nullable Completion completionAnnotation) {
		this.commandClass = commandClass;
		this.commandMethod = commandMethod;
		this.commandAnnotation = commandAnnotation;
		this.permissionAnnotation = permissionAnnotation;
		this.completionMethod = completionMethod;
		this.completionAnnotation = completionAnnotation;

		if (commandAnnotation.name() != null && !commandAnnotation.name().isEmpty()) {
			this.name = commandAnnotation.name();
		} else {
			this.name = commandMethod.getName();
		}
		this.aliases = commandAnnotation.aliases();
		this.usage = commandAnnotation.usage();
		this.description = commandAnnotation.description();
		if (commandAnnotation.errorHandler() != null) {
			try {
				this.errorHandler = commandAnnotation.errorHandler().newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			this.errorHandler = null;
		}

		if (permissionAnnotation != null) {
			if (permissionAnnotation.value() != null && !permissionAnnotation.value().isEmpty()) {
				this.permission = permissionAnnotation.value();
			}
			if (permissionAnnotation.permissionMessage() != null && !permissionAnnotation.permissionMessage().isEmpty()) {
				this.permissionMessage = permissionAnnotation.permissionMessage();
			}
		}
	}

	boolean onCommand(CommandSender sender, BukkitCommand command, String label, String[] args) {
		try {
			//			//Check sender type
			//			if (!commandAnnotation.allowConsole()) {
			//				if (!(sender instanceof Player)) { throw new IllegalSenderException(); }
			//			}
			//Check permission
			if (!hasPermission(sender)) {
				throw new PermissionException(this.permission);
			}
			//Check argument length
			if (args.length < commandAnnotation.min()) {
				throw new InvalidLengthException(commandAnnotation.min(), args.length);
			}
			if (commandAnnotation.max() != -1 && args.length > commandAnnotation.max()) {
				throw new InvalidLengthException(commandAnnotation.max(), args.length);
			}

			try {
				Class<?>[] parameterTypes = commandMethod.getParameterTypes();
				if (parameterTypes.length == 0) {
					throw new CommandException("Command method '" + commandMethod.getName() + " in " + commandClass + " is missing the CommandSender parameter");
				}
				if (!CommandSender.class.isAssignableFrom(parameterTypes[0])) {
					throw new CommandException("First parameter of method '" + commandMethod.getName() + " in " + commandClass + " is no CommandSender");
				}
				if (Player.class.isAssignableFrom(parameterTypes[0])) {
					if (!(sender instanceof Player)) { throw new IllegalSenderException(); }
				}
				if ((parameterTypes.length - 1/*Ignore the sender*/ < commandAnnotation.min()) || (commandAnnotation.max() != -1 && parameterTypes.length - 1 > commandAnnotation.max())) {
					throw new CommandException("Parameter length of method '" + commandMethod.getName() + " in " + commandClass + " is not in the specified argument length range");
				}

				Object[] parsedArguments = new Object[parameterTypes.length];
				for (int i = 1; i < args.length + 1; i++) {
					System.out.println(i);
					System.out.println(parameterTypes.length);
					if (i == parameterTypes.length - 1) {
						JoinedArg joinedAnnotation = parameterTypes[parameterTypes.length - 1]/* use the last parameter */.getAnnotation(JoinedArg.class);
						if (joinedAnnotation != null) {
							parsedArguments[parsedArguments.length - 1] = joinArguments(args, i - 1, joinedAnnotation.joiner());
							break;//Break, since we can't use any other arguments
						} else if (String[].class.isAssignableFrom(parameterTypes[parameterTypes.length - 1])) {//Always use the last parameter
							parsedArguments[parsedArguments.length - 1] = getLeftoverArguments(args, i - 1);
							break;
						}
					}

					parsedArguments[i] = parseArgument(parameterTypes[i], args[i - 1]);
				}
				parsedArguments[0] = sender;

				commandMethod.invoke(commandClass, parsedArguments);
			} catch (CommandException commandException) {
				throw commandException;
			} catch (Throwable throwable) {
				throw new UnhandledCommandException("Unhandled exception in " + this.commandClass + "#" + this.commandMethod.getName(), throwable);
			}

			return true;
		} catch (PermissionException permissionException) {
			if (errorHandler != null) {
				errorHandler.handlePermissionException(permissionException, sender, command, args);
				return false;
			} else {
				throw permissionException;
			}
		} catch (IllegalSenderException illegalSenderException) {
			if (errorHandler != null) {
				errorHandler.handleIllegalSender(illegalSenderException, sender, command, args);
				return false;
			} /*else {
				throw illegalSenderException;
			}*/
		} catch (ArgumentParseException parseException) {
			if (errorHandler != null) {
				errorHandler.handleArgumentParse(parseException, sender, command, args);
				return false;
			} else {
				throw parseException;
			}
		} catch (InvalidLengthException lengthException) {
			if (errorHandler != null) {
				errorHandler.handleLength(lengthException, sender, command, args);
				return false;
			} else {
				throw lengthException;
			}
		} catch (UnhandledCommandException unhandledException) {
			if (errorHandler != null) {
				errorHandler.handleUnhandled(unhandledException, sender, command, args);
			}
			throw unhandledException;
		} catch (CommandException commandException) {
			if (errorHandler != null) {
				errorHandler.handleCommandException(commandException, sender, command, args);
				return false;
			} else {
				throw commandException;
			}
		}
		return false;
	}

	//TODO
	List<String> onTabComplete(CommandSender sender, BukkitCommand command, String label, String[] args) {
		if (completionAnnotation == null) { return null; }

		//		if (!commandAnnotation.allowConsole()) {
		//			if (!(sender instanceof Player)) { return null; }
		//		}
		if (!hasPermission(sender)) {
			return null;
		}

		return null;
	}

	boolean hasPermission(CommandSender sender) {
		return permissionAnnotation == null || sender.hasPermission(permission);
	}

	Object parseArgument(Class<?> parameterType, String argument) {
		try {
			if (String.class.isAssignableFrom(parameterType)) {
				return argument;
			}
			if (Number.class.isAssignableFrom(parameterType)) {
				String parseName = parameterType.getSimpleName();
				if (Integer.class.equals(parameterType)) {
					parseName = "Int";/* Why?! */
				}
				return parameterType.getDeclaredMethod("parse" + parseName, String.class).invoke(null, argument);
			}
			if (Enum.class.isAssignableFrom(parameterType)) {
				return Enum.valueOf((Class<? extends Enum>) parameterType, argument.toUpperCase());
			}
			throw new ArgumentParseException("Failed to parse argument '" + argument + "' to " + parameterType, argument, parameterType);
		} catch (ReflectiveOperationException e) {
			throw new ArgumentParseException("Exception while parsing argument '" + argument + "' to " + parameterType, e, argument, parameterType);
		}
	}

	String joinArguments(String[] args, int start, String joiner) {
		if (start > args.length) { throw new IllegalArgumentException("start > length"); }

		StringBuilder joined = new StringBuilder();
		for (int i = start; i < args.length; i++) {
			if (i != start) { joined.append(joiner); }
			joined.append(args[i]);
		}
		return joined.toString();
	}

	String[] getLeftoverArguments(String[] args, int start) {
		String[] newArray = new String[args.length - start];
		for (int i = start; i < args.length; i++) {
			newArray[i - start] = args[i];
		}
		return newArray;
	}

	//Internal register methods and classes
	private CommandMap commandMap;

	public final AnnotatedCommand register() {
		BukkitCommand command = new BukkitCommand(this.name);
		this.theCommand = command;
		if (this.description != null) { command.setDescription(this.description); }
		if (this.usage != null) { command.setUsage(this.usage); }
		if (this.permission != null) { command.setPermission(this.permission); }
		if (this.permissionMessage != null) { command.setPermissionMessage(this.permissionMessage); }
		if (this.aliases.length != 0) { command.setAliases(new ArrayList<>(Arrays.asList(this.aliases))); }
		getCommandMap().register("", command);
		return command.executor = this;
	}

	private CommandMap getCommandMap() {
		if (commandMap == null) {
			try {
				commandMap = (CommandMap) AccessUtil.setAccessible(Bukkit.getServer().getClass().getDeclaredField("commandMap")).get(Bukkit.getServer());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return commandMap;
	}

	private class BukkitCommand extends org.bukkit.command.Command {

		private AnnotatedCommand executor;

		protected BukkitCommand(String name) {
			super(name);
		}

		@Override
		public final boolean execute(CommandSender sender, String label, String[] args) {
			if (executor != null) { return executor.onCommand(sender, this, label, args); }
			return false;
		}

		@Override
		public final List<String> tabComplete(CommandSender sender, String label, String[] args) throws IllegalArgumentException {
			if (executor != null) { return executor.onTabComplete(sender, this, label, args); }
			return null;
		}
	}

}
