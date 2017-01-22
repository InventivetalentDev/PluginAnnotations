package org.inventivetalent.pluginannotations.description;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Database {

	/**
	 * Defaults to <code>true</code>
	 *
	 * @return Whether this plugin uses a database
	 */
	boolean value() default true;

}
