package org.inventivetalent.pluginannotations.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

	/**
	 * Name of the command. If left empty, the method name is used
	 *
	 * @return command name
	 */
	String name() default "";

	/**
	 * Command aliases
	 *
	 * @return aliases
	 */
	String[] aliases() default {};

	/**
	 * @return usage
	 */
	String usage() default "";

	/**
	 * @return description
	 */
	String description() default "";

	/**
	 * Minimum argument length
	 *
	 * @return min argument length
	 */
	int min() default 0;

	/**
	 * Maximum argument length (-1 for no limitation)
	 *
	 * @return max argument length
	 */
	int max() default -1;

	//	/**
	//	 * Whether to allow the console (non-players) as the command sender
	//	 */
	//	boolean allowConsole() default true;

	String fallbackPrefix() default "";

	Class<? extends CommandErrorHandler> errorHandler() default FeedbackErrorHandler.class;

}
