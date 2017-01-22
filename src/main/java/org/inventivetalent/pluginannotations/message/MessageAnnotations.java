package org.inventivetalent.pluginannotations.message;

import org.bukkit.plugin.Plugin;
import org.inventivetalent.pluginannotations.AnnotationsAbstract;

import java.lang.reflect.Field;

public class MessageAnnotations extends AnnotationsAbstract {

	/**
	 * Load the plugin's message values into the class fields
	 *
	 * @param plugin        {@link Plugin} to load the messages form
	 * @param classesToLoad Array of classes to set the fields in
	 * @return {@link MessageAnnotations}
	 */
	public MessageAnnotations loadValues(Plugin plugin, Object... classesToLoad) {
		if (plugin == null) { throw new IllegalArgumentException("plugin cannot be null"); }
		if (classesToLoad.length == 0) { throw new IllegalArgumentException("classes cannot be empty"); }
		for (Object toLoad : classesToLoad) {
			loadValues(plugin, toLoad);
		}
		return this;
	}

	/**
	 * Load the plugin's message values into the class fields
	 *
	 * @param plugin      {@link Plugin} to load the messages form
	 * @param classToLoad Class to set the fields in
	 * @return {@link MessageAnnotations}
	 */
	public MessageAnnotations loadValues(Plugin plugin, Object classToLoad) {
		if (plugin == null) { throw new IllegalArgumentException("plugin cannot be null"); }
		if (classToLoad == null) { throw new IllegalArgumentException("class cannot be null"); }
		Class<?> clazz = classToLoad.getClass();
		MessageBase baseAnnotation = clazz.getAnnotation(MessageBase.class);

		try {
			MessageLoader messageLoader = new MessageLoader(plugin, baseAnnotation);
			for (Field field : clazz.getDeclaredFields()) {
				try {
					MessageValue annotation = field.getAnnotation(MessageValue.class);
					if (annotation != null) {
						field.setAccessible(true);
						String message = messageLoader.getMessage(annotation);
						if (message != null) {
							field.set(classToLoad, message);
						} else if (!annotation.defaultsTo().isEmpty()) {
							field.set(classToLoad, annotation.defaultsTo());
						}
					}
				} catch (Exception e) {
					throw new RuntimeException("Failed to set config value for field '" + field.getName() + "' in " + clazz, e);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to load " + classToLoad.getClass(), e);
		}

		return this;
	}

	public MessageLoader newMessageLoader(Plugin plugin) {
		return new MessageLoader(plugin, null, null, null, null);
	}

	public MessageLoader newMessageLoader(Plugin plugin, String messageFile, String basePath, MessageFormatter messageFormatter) {
		return new MessageLoader(plugin, messageFile, basePath, messageFormatter, null);
	}

	@Override
	public void load(Plugin plugin, Object clazz) {
		loadValues(plugin, clazz);
	}
}
