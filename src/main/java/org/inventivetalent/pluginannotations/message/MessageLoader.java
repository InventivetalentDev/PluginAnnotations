package org.inventivetalent.pluginannotations.message;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MessageLoader {

	public static final Pattern LINK_PATTERN = Pattern.compile("\\$\\{([^\\}]*)\\}");

	private final Plugin            plugin;
	private final FileConfiguration configuration;
	private final String            basePath;
	private final MessageBase       baseAnnotation;
	private final MessageFormatter  baseFormatter;

	MessageLoader(Plugin plugin, MessageBase baseAnnotation) throws InstantiationException, IllegalAccessException {
		this(plugin, baseAnnotation != null ? baseAnnotation.file() : null, baseAnnotation != null ? baseAnnotation.basePath() : "", baseAnnotation != null ? baseAnnotation.formatter().newInstance() : null, baseAnnotation);
	}

	MessageLoader(Plugin plugin, String messageFile, String basePath, MessageFormatter baseFormatter, MessageBase baseAnnotation) {
		this.plugin = plugin;
		if (messageFile == null || messageFile.isEmpty() || "config.yml".equals(messageFile)) {
			this.configuration = plugin.getConfig();
		} else {
			File file = new File(messageFile);
			if (!file.isAbsolute()) { file = new File(plugin.getDataFolder(), messageFile); }
			this.configuration = YamlConfiguration.loadConfiguration(file);
		}
		this.basePath = basePath == null ? "" : basePath;
		this.baseFormatter = baseFormatter;
		this.baseAnnotation = baseAnnotation;
	}

	public String getMessage(MessageValue annotation) {
		String key = makeKey(annotation.path());
		try {
			return getMessage(key, annotation.defaultsTo(), annotation.allowLinks(), annotation.colorChar(), annotation.formatter());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getMessage(String key, String def, boolean allowLinks, char colorChar, Class<? extends MessageFormatter> formatter) throws InstantiationException, IllegalAccessException {
		return getMessage(key, def, allowLinks, colorChar, formatter != null ? formatter.newInstance() : null);
	}

	public String getMessage(String key, String def, boolean allowLinks, char colorChar, MessageFormatter formatter) {
		key = makeKey(key);
		if (configuration.contains(key)) {
			String message = getMessage0(key, def == null || def.isEmpty() ? null : def);
			if (allowLinks) {
				message = replaceLinks(message);
			}
			if (colorChar != ' ') {
				message = ChatColor.translateAlternateColorCodes(colorChar, message);
			}
			if (formatter != null) {
				message = formatter.format(key, message);
			}
			if (this.baseFormatter != null) {
				message = this.baseFormatter.format(key, message);
			}

			return message;
		}
		return null;
	}

	public String getMessage(String key, String def, MessageFormatter formatter) {
		return getMessage(key, def, true, '&', formatter);
	}

	public String getMessage(String key, String def) {
		return getMessage(key, def, true, '&', (MessageFormatter) null);
	}

	protected String getMessage0(String key, String def) {
		return def != null ? configuration.getString(key, def) : configuration.getString(key);
	}

	public String makeKey(String key) {
		return basePath + (basePath.endsWith(".") ? "" : ".") + key;
	}

	String replaceLinks(String message) {
		Matcher matcher = LINK_PATTERN.matcher(message);
		while (matcher.find()) {
			if (matcher.groupCount() != 1) { continue; }
			String toReplace = matcher.group(0);
			String key = matcher.group(1);

			String fullKey = makeKey(key);
			if (configuration.contains(fullKey)) {
				message = message.replace(toReplace, getMessage0(fullKey, null));
			} else if (configuration.contains(key)) {
				message = message.replace(toReplace, getMessage0(key, null));
			}
		}
		return message;
	}

}
