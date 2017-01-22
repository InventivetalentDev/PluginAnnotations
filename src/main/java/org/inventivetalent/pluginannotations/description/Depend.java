package org.inventivetalent.pluginannotations.description;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Depend {

	/**
	 * @return The plugin's dependencies
	 */
	String[] value();

}
