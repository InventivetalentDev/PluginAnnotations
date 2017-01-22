package org.inventivetalent.pluginannotations.description;

import org.bukkit.plugin.PluginLoadOrder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface Load {

	/**
	 * Defaults to {@link PluginLoadOrder#POSTWORLD}
	 *
	 * @return The plugin load order
	 */
	PluginLoadOrder value() default PluginLoadOrder.POSTWORLD;

}
