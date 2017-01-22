package org.inventivetalent.pluginannotations.description;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Place this annotation on your plugin's main class. It will automatically generate a plugin.yml file in your jar-package.
 * <p>
 * You can also create a separate plugin.yml file, which will be used as a template (For example if you want to use maven placeholders in it)
 */
@Target(ElementType.TYPE)
public @interface Plugin {

	/**
	 * @return The plugin's name
	 */
	String name();

	/**
	 * @return The plugin version.
	 */
	String version() default "0.0.0";

	/**
	 * Defaults to the class this annotation is used on
	 *
	 * @return The plugin main class
	 */
	String main() default "";

}
