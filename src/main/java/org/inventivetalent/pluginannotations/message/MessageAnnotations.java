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

package org.inventivetalent.pluginannotations.message;

import org.bukkit.plugin.Plugin;
import org.inventivetalent.pluginannotations.AnnotationsAbstract;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

	public MessageLoader newMessageLoader(@Nonnull Plugin plugin) {
		return new MessageLoader(plugin, null, null, null, null);
	}

	public MessageLoader newMessageLoader(@Nonnull Plugin plugin, @Nullable String messageFile, @Nullable String basePath, @Nullable MessageFormatter messageFormatter) {
		return new MessageLoader(plugin, messageFile, basePath, messageFormatter, null);
	}

	@Override
	public void load(Plugin plugin, Object clazz) {
		loadValues(plugin, clazz);
	}
}
