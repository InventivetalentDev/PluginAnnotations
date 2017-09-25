package org.inventivetalent.pluginannotations.command;

import org.bukkit.plugin.Plugin;
import org.inventivetalent.pluginannotations.AnnotationsAbstract;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({"unused", "WeakerAccess"})
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
