package org.inventivetalent.pluginannotations.description;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface LoadBefore {

	/**
	 * @return Plugins to load after this plugin
	 */
	String[] value();

}
