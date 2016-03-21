package org.inventivetalent.pluginannotations.message;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageLoader {

	public static final Pattern LINK_PATTERN = Pattern.compile("\\$\\{(.*)\\}");

	private final Plugin            plugin;
	private final FileConfiguration configuration;
	private final String            basePath;
	private final MessageBase       baseAnnotation;
	private final MessageFormatter  baseFormatter;

	MessageLoader(@Nonnull Plugin plugin, @Nullable MessageBase baseAnnotation) throws InstantiationException, IllegalAccessException {
		this(plugin, baseAnnotation != null ? baseAnnotation.file() : null, baseAnnotation != null ? baseAnnotation.basePath() : "", baseAnnotation != null && baseAnnotation.formatter() != null ? baseAnnotation.formatter().newInstance() : null, baseAnnotation);
	}

	MessageLoader(@Nonnull Plugin plugin, @Nullable String messageFile, @Nullable String basePath, @Nullable MessageFormatter baseFormatter, @Nullable MessageBase baseAnnotation) {
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

	@Nullable
	public String getMessage(@Nonnull MessageValue annotation) {
		String key = makeKey(annotation.path());
		try {
			return getMessage(key, annotation.defaultsTo(), annotation.allowLinks(), annotation.colorChar(), annotation.formatter());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	public String getMessage(@Nonnull String key, @Nullable String def, boolean allowLinks, char colorChar, @Nullable Class<? extends MessageFormatter> formatter) throws InstantiationException, IllegalAccessException {
		return getMessage(key, def, allowLinks, colorChar, formatter != null ? formatter.newInstance() : null);
	}

	@Nullable
	public String getMessage(@Nonnull String key, @Nullable String def, boolean allowLinks, char colorChar, @Nullable MessageFormatter formatter) {
		System.out.println(key);
		key = makeKey(key);
		System.out.println(key);
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

	public String getMessage(@Nonnull String key, @Nullable String def, @Nullable MessageFormatter formatter) {
		return getMessage(key, def, true, '&', formatter);
	}

	public String getMessage(@Nonnull String key, @Nullable String def) {
		return getMessage(key, def, true, '&', (MessageFormatter) null);
	}

	protected String getMessage0(@Nonnull String key, @Nullable String def) {
		return def != null ? configuration.getString(key, def) : configuration.getString(key);
	}

	@Nonnull
	public String makeKey(@Nonnull String key) {
		return basePath + (basePath.endsWith(".") ? "" : ".") + key;
	}

	@Nonnull
	String replaceLinks(@Nonnull String message) {
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
