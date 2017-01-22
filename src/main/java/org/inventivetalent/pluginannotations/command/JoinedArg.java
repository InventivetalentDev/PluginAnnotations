package org.inventivetalent.pluginannotations.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface JoinedArg {

	/**
	 * Joiner for the arguments. Defaults to <code>" "</code>
	 *
	 * @return joiner
	 */
	String joiner() default " ";

}
