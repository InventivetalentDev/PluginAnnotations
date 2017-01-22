package org.inventivetalent.pluginannotations.description;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Author {

	/**
	 * @return The plugin's author(s)
	 */
	String[] value();

}
