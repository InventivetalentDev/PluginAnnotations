package org.inventivetalent.pluginannotations.description;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Website {

	/**
	 * @return The plugin website
	 */
	String value();

}
