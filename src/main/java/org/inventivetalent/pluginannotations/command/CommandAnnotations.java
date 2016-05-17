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

package org.inventivetalent.pluginannotations.command;

import org.bukkit.plugin.Plugin;
import org.inventivetalent.pluginannotations.AnnotationsAbstract;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class CommandAnnotations extends AnnotationsAbstract {

	public Set<AnnotatedCommand> registerCommands(Plugin plugin, Object classToRegister) {
		Class<?> clazz = classToRegister.getClass();
		Set<AnnotatedCommand> registeredCommands = new HashSet<>();

		Set<Method> completionMethods = new HashSet<>();
		for (Method method : clazz.getDeclaredMethods()) {
			Completion completionAnnotation = method.getAnnotation(Completion.class);
			if (completionAnnotation != null) { completionMethods.add(method); }
		}

		for (Method method : clazz.getDeclaredMethods()) {
			Command commandAnnotation = method.getAnnotation(Command.class);
			if (commandAnnotation == null) {
				continue;
			}
			Permission permissionAnnotation = method.getAnnotation(Permission.class);

			Method completionMethod = null;
			Completion completionAnnotation = null;
			for (Method method1 : completionMethods) {
				if (method1.getName().equals(method.getName())) {
					completionMethod = method1;
					completionAnnotation = method1.getAnnotation(Completion.class);
				}
			}

			AnnotatedCommand annotatedCommand = new AnnotatedCommand(classToRegister, method, commandAnnotation, permissionAnnotation, completionMethod, completionAnnotation);
			registeredCommands.add(annotatedCommand.register());
		}

		return registeredCommands;
	}

	@Override
	public void load(Plugin plugin, Object clazz) {
		registerCommands(plugin, clazz);
	}
}
