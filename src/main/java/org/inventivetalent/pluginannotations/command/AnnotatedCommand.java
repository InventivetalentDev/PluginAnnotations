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
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
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
	public String   fallbackPrefix    = "";

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
		this.fallbackPrefix = commandAnnotation.fallbackPrefix();
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
					if (i == parameterTypes.length - 1) {
						JoinedArg joinedAnnotation = getMethodParameterAnnotation(commandMethod, parameterTypes.length - 1, JoinedArg.class);
						if (joinedAnnotation != null) {
							parsedArguments[parsedArguments.length - 1] = joinArguments(args, i - 1, joinedAnnotation.joiner());
							break;//Break, since we can't use any other arguments
						} else if (String[].class.isAssignableFrom(parameterTypes[parameterTypes.length - 1])) {//Always use the last parameter
							parsedArguments[parsedArguments.length - 1] = getLeftoverArguments(args, i - 1);
							break;
						}
					}
					if (i >= parsedArguments.length) { break; }

					parsedArguments[i] = parseArgument(parameterTypes[i], args[i - 1]);
				}
				parsedArguments[0] = sender;

				if (parameterTypes.length - 1 > args.length) {
					for (int i = args.length; i < parameterTypes.length; i++) {
						if (parsedArguments[i] != null) { continue; }
						OptionalArg optionalAnnotation = getMethodParameterAnnotation(commandMethod, i, OptionalArg.class);
						if (optionalAnnotation != null) {
							if (optionalAnnotation.def() != null && !optionalAnnotation.def().isEmpty()) {
								parsedArguments[i] = parseArgument(parameterTypes[i], optionalAnnotation.def());
							}
						}
					}
				}

				commandMethod.invoke(commandClass, parsedArguments);
			} catch (InvocationTargetException e) {
				Throwable cause = e.getCause();
				if (cause instanceof CommandException) { throw (CommandException) cause; }
				throw new UnhandledCommandException("Unhandled exception while invoking command method in " + this.commandClass + "#" + this.commandMethod.getName(), cause);
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

	<A extends Annotation> A getMethodParameterAnnotation(Method method, int index, Class<A> clazz) {
		Annotation[] annotations = method.getParameterAnnotations()[index];
		if (annotations != null) {
			for (Annotation annotation : annotations) {
				if (clazz.isAssignableFrom(annotation.getClass())) { return (A) annotation; }
			}
		}
		return null;
	}

	<A extends Annotation> A getMethodParameterAnnotation(Method method, Class<A> clazz) {
		Annotation[][] annotations = method.getParameterAnnotations();
		for (Annotation[] annotationA : annotations) {
			for (Annotation annotation : annotationA) {
				if (clazz.isAssignableFrom(annotation.getClass())) {
					return (A) annotation;
				}
			}
		}
		return null;
	}

	List<String> onTabComplete(CommandSender sender, BukkitCommand command, String label, String[] args) {
		if (completionAnnotation == null || completionMethod == null) { return null; }

		if (!hasPermission(sender)) {
			return null;
		}
		try {
			//			if (!List.class.isAssignableFrom(completionMethod.getReturnType())) {
			//				throw new CommandException("Completion method '" + completionMethod.getName() + " in " + commandClass + " does not return a List");
			//			}

			Class<?>[] parameterTypes = completionMethod.getParameterTypes();
			if (parameterTypes.length <= 1) {
				throw new CommandException("Completion method '" + completionMethod.getName() + " in " + commandClass + " is missing the List or CommandSender parameter");
			}
			if (!List.class.isAssignableFrom(parameterTypes[0])) {
				throw new CommandException("First parameter of method '" + completionMethod.getName() + " in " + completionMethod + " is no List");
			}
			if (!CommandSender.class.isAssignableFrom(parameterTypes[1])) {
				throw new CommandException("Second parameter of method '" + completionMethod.getName() + " in " + completionMethod + " is no CommandSender");
			}
			if (Player.class.isAssignableFrom(parameterTypes[0])) {
				if (!(sender instanceof Player)) { return null; }
			}

			Object[] parsedArguments = new Object[parameterTypes.length];
			for (int i = 2; i < args.length + 2; i++) {
				if (i == parameterTypes.length - 1) {
					JoinedArg joinedAnnotation = getMethodParameterAnnotation(completionMethod, parameterTypes.length - 1, JoinedArg.class);
					if (joinedAnnotation != null) {
						parsedArguments[parsedArguments.length - 1] = joinArguments(args, i - 1, joinedAnnotation.joiner());
						break;//Break, since we can't use any other arguments
					} else if (String[].class.isAssignableFrom(parameterTypes[parameterTypes.length - 1])) {//Always use the last parameter
						parsedArguments[parsedArguments.length - 1] = getLeftoverArguments(args, i - 1);
						break;
					}
				}

				if (i >= parsedArguments.length) { break; }
				if (args[i - 2] == null || args[i - 2].isEmpty()) {
					parsedArguments[i] = null;
				} else {
					try {
						parsedArguments[i] = parseArgument(parameterTypes[i], args[i - 2]);
					} catch (Exception e) {
						//Ignore exceptions for tab-completion
					}
				}
			}
			List<String> list = (List) (parsedArguments[0] = new ArrayList<>());
			parsedArguments[1] = sender;

			completionMethod.invoke(commandClass, parsedArguments);
			return getPossibleCompletionsForGivenArgs(args, list.toArray(new String[list.size()]));
		} catch (CommandException commandException) {
			throw commandException;
		} catch (Throwable throwable) {
			throw new UnhandledCommandException("Unhandled exception in " + this.commandClass + "#" + this.completionMethod.getName(), throwable);
		}
	}

	/*
	 * Author: D4rKDeagle
	 */
	public static List<String> getPossibleCompletionsForGivenArgs(String[] args, String[] possibilities) {
		final String argumentToFindCompletionFor = args[args.length - 1];

		final List<String> listOfPossibleCompletions = new ArrayList<>();
		for (int i = 0; i < possibilities.length; i++) {
			try {
				if (possibilities[i] != null && possibilities[i].regionMatches(true, 0, argumentToFindCompletionFor, 0, argumentToFindCompletionFor.length())) {
					listOfPossibleCompletions.add(possibilities[i]);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		Collections.sort(listOfPossibleCompletions);

		return listOfPossibleCompletions;
	}

	boolean hasPermission(CommandSender sender) {
		return permissionAnnotation == null || sender.hasPermission(permission);
	}

	Object parseArgument(Class<?> parameterType, String argument) {
		try {
			if (String.class.isAssignableFrom(parameterType)) {
				return argument;
			}
			try {
				Constructor stringConstructor = parameterType.getConstructor(String.class);
				if (stringConstructor != null) {
					return stringConstructor.newInstance(argument);
				}
			} catch (NoSuchMethodException ignored) {
			}
			if (Number.class.isAssignableFrom(parameterType)) {
				String parseName = parameterType.getSimpleName();
				if (Integer.class.equals(parameterType)) {
					parseName = "Int";/* Why?! */
				}
				return parameterType.getDeclaredMethod("parse" + parseName, String.class).invoke(null, argument);
			}
			if (Enum.class.isAssignableFrom(parameterType)) {
				try {
					return Enum.valueOf((Class<? extends Enum>) parameterType, argument.toUpperCase());
				} catch (Exception e) {
					//Ignore the exception - no enum constant found
				}
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
		if (this.aliases.length != 0) {
			List<String> aliasList = new ArrayList<>();
			for (String s : this.aliases) {
				aliasList.add(s.toLowerCase());
			}
			command.setAliases(aliasList);
		}
		getCommandMap().register(this.fallbackPrefix != null ? this.fallbackPrefix : "", command);
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
