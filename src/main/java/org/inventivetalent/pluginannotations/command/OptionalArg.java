package org.inventivetalent.pluginannotations.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface OptionalArg {

	/**
	 * Default value when nothing is specified (if this is empty, <code>null</code> will be passed to the command method)
	 *
	 * @return default value
	 */
	String def() default "";

}
