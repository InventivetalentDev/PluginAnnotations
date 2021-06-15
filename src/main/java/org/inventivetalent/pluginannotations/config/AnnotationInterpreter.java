package org.inventivetalent.pluginannotations.config;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Created by shell on 2018/1/22.
 * <p>
 * Github: https://github.com/shellljx
 */
public interface AnnotationInterpreter {

    <T> T decodeFromYml(ConfigurationSection configuration, Class<T> targetClass);


    void encodeToYml(ConfigurationSection configuration, Object target);
}
