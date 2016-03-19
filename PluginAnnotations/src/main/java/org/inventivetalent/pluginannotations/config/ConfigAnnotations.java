package org.inventivetalent.pluginannotations.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;

public class ConfigAnnotations {

	/**
	 * Load the plugin's configuration values into the class fields
	 *
	 * @param plugin        {@link Plugin} to load the configuration form
	 * @param classesToLoad Array of classes to set the fields in
	 */
	public ConfigAnnotations loadValues(Plugin plugin, Object... classesToLoad) {
		if (plugin == null) { throw new IllegalArgumentException("plugin cannot be null"); }
		if (classesToLoad.length == 0) { throw new IllegalArgumentException("classes cannot be empty"); }
		for (Object toLoad : classesToLoad) {
			loadValues(plugin, toLoad);
		}
		return this;
	}

	/**
	 * Load the plugin's configuration values into the class fields
	 *
	 * @param plugin      {@link Plugin} to load the configuration form
	 * @param classToLoad Class to set the fields in
	 */
	public ConfigAnnotations loadValues(Plugin plugin, Object classToLoad) {
		if (plugin == null) { throw new IllegalArgumentException("plugin cannot be null"); }
		if (classToLoad == null) { throw new IllegalArgumentException("class cannot be null"); }
		Class<?> clazz = classToLoad.getClass();
		FileConfiguration config = plugin.getConfig();

		for (Field field : clazz.getDeclaredFields()) {
			try {
				ConfigValue annotation = field.getAnnotation(ConfigValue.class);
				if (annotation != null) {
					field.setAccessible(true);
					if (config.contains(annotation.path())) {
						field.set(classToLoad, config.get(annotation.path()));
					} else if (!annotation.defaultsTo().isEmpty()) {
						//TODO: Parse value type
						field.set(classToLoad, annotation.defaultsTo());
					}
				}
			} catch (Exception e) {
				throw new RuntimeException("Failed to set config value for field '" + field.getName() + "' in " + clazz, e);
			}
		}

		return this;
	}

}
