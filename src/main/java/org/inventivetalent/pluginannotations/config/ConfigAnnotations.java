/*
 * Copyright 2016 inventivetalent. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and contributors and should not be interpreted as representing official policies,
 *  either expressed or implied, of anybody else.
 */

package org.inventivetalent.pluginannotations.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.inventivetalent.pluginannotations.AnnotationsAbstract;

import java.lang.reflect.Field;
import java.util.List;
import java.util.ListIterator;

public class ConfigAnnotations extends AnnotationsAbstract {

	/**
	 * Load the plugin's configuration values into the class fields
	 *
	 * @param plugin        {@link Plugin} to load the configuration form
	 * @param classesToLoad Array of classes to set the fields in
	 * @return {@link ConfigAnnotations}
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
	 * @return {@link ConfigAnnotations}
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
						Object value = config.get(annotation.path());
						if (annotation.colorChar() != ' ') {
							if (value instanceof String) {
								value = ChatColor.translateAlternateColorCodes(annotation.colorChar(), (String) value);
							}
							if (value instanceof List) {
								for (ListIterator iterator = ((List) value).listIterator(); iterator.hasNext(); ) {
									Object next = iterator.next();
									if (next instanceof String) {
										iterator.set(ChatColor.translateAlternateColorCodes(annotation.colorChar(), (String) next));
									}
								}
							}
						}
						field.set(classToLoad, value);
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

	@Override
	public void load(Plugin plugin, Object clazz) {
		loadValues(plugin, clazz);
	}
}
