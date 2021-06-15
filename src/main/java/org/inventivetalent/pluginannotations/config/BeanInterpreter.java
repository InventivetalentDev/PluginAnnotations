package org.inventivetalent.pluginannotations.config;

import org.bukkit.configuration.ConfigurationSection;

import java.lang.reflect.Field;

/**
 * Created by shell on 2018/1/23.
 * <p>
 * Github: https://github.com/shellljx
 */
public class BeanInterpreter implements AnnotationInterpreter {


    @Override
    public <T> T decodeFromYml(ConfigurationSection configuration, Class<T> targetClass) {
        try {
            T target = targetClass.newInstance();
            for (Field field : targetClass.getDeclaredFields()) {
                field.setAccessible(true);
                AnnotationInterpreter interpreter = getInterpreter(field);
                if (interpreter == null) {
                    continue;
                }
                ConfigurationSection fieldConfiguration = getFieldConfiguration(interpreter, configuration);
                Object fieldValue = interpreter.decodeFromYml(fieldConfiguration, field.getType());
                if (fieldValue == null) {
                    continue;
                }
                field.set(target, fieldValue);
            }
            return target;
        } catch (Exception e) {
            throw new RuntimeException("BeanInterpreter decode config bean " + targetClass.getName() + " error " + e);
        }
    }

    @Override
    public void encodeToYml(ConfigurationSection configuration, Object target) {
        Class targetClass = target.getClass();
        try {
            for (Field field : targetClass.getDeclaredFields()) {
                field.setAccessible(true);
                AnnotationInterpreter interpreter = getInterpreter(field);
                if (interpreter == null) {
                    continue;
                }
                ConfigurationSection fieldConfiguration = getFieldConfiguration(interpreter, configuration);
                interpreter.encodeToYml(fieldConfiguration, field.get(target));
            }
        } catch (Exception e) {
            throw new RuntimeException("BeanInterpreter save config bean " + targetClass.getName() + "error " + e);
        }
    }

    private AnnotationInterpreter getInterpreter(Field field) {
        ConfigValue configValue = field.getAnnotation(ConfigValue.class);
        ConfigSection configSection = field.getAnnotation(ConfigSection.class);
        if (configValue != null) {
            return new ValueInterpreter(field);
        } else if (configSection != null) {
            return new SectionInterpreter(field);
        }
        return null;
    }

    private ConfigurationSection getFieldConfiguration(AnnotationInterpreter interpreter, ConfigurationSection configuration) {
        if (interpreter instanceof ValueInterpreter) {
            return configuration;
        } else if (interpreter instanceof SectionInterpreter) {
            return configuration.getConfigurationSection(((SectionInterpreter) interpreter).getAnnotation().path());
        }
        return null;
    }
}
