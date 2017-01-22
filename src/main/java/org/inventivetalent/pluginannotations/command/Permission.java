package org.inventivetalent.pluginannotations.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Permission {

	/**
	 * Permission of the command
	 *
	 * @return permission
	 */
	String value();

	String permissionMessage() default "";

}
