package org.inventivetalent.pluginannotations.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)

@SuppressWarnings("unused")
public @interface Completion {

	/**
	 * Name of the command. If empty, the method name will be used
	 *
	 * @return command name
	 */
	String name() default "";

}
