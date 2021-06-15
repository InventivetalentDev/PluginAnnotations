package org.inventivetalent.pluginannotations;

import org.bukkit.plugin.Plugin;
import org.inventivetalent.pluginannotations.command.CommandAnnotations;
import org.inventivetalent.pluginannotations.config.ConfigAnnotations;
import org.inventivetalent.pluginannotations.message.MessageAnnotations;

public class PluginAnnotations {

	public static final ConfigAnnotations  CONFIG  = new ConfigAnnotations();
	public static final MessageAnnotations MESSAGE = new MessageAnnotations();
	public static final CommandAnnotations COMMAND = new CommandAnnotations();

	public static final AnnotationsAbstract[] ALL_ANNOTATIONS = {
			MESSAGE,
			COMMAND };

	public static void loadAll(Plugin plugin, Object classToLoad) {
		for (AnnotationsAbstract annotation : ALL_ANNOTATIONS) {
			annotation.load(plugin, classToLoad);
		}
	}

}
