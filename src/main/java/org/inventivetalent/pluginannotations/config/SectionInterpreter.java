package org.inventivetalent.pluginannotations.config;

import org.bukkit.configuration.ConfigurationSection;
import org.inventivetalent.pluginannotations.utils.AnnotationUtil;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by shell on 2018/1/19.
 * <p>
 * Github: https://github.com/shellljx
 */
public class SectionInterpreter implements AnnotationInterpreter {

    private ConfigSection annotation;
    private Field field;

    public SectionInterpreter(Field field) {
        this.field = field;
        this.annotation = field.getAnnotation(ConfigSection.class);
    }

    @Override
    public <T> T decodeFromYml(ConfigurationSection configuration, Class<T> targetClass) {
        try {
            if (AnnotationUtil.isMapType(targetClass)) {
                Map map = new HashMap<>();
                Type genericType = field.getGenericType();
                if (genericType != null && genericType instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) genericType;
                    Class<?> valueClass = (Class<?>) pt.getActualTypeArguments()[1];
                    if (!AnnotationUtil.isBaseType(valueClass)) {
                        Set<String> keySet = configuration.getKeys(false);
                        for (String key : keySet) {
                            AnnotationInterpreter interpreter = new BeanInterpreter();
                            Object value = interpreter.decodeFromYml(configuration.getConfigurationSection(key), valueClass);
                            if (value != null) {
                                map.put(key, value);
                            }
                        }
                        return (T) map;
                    }
                }
            } else if (!AnnotationUtil.isBaseType(targetClass)) {
                AnnotationInterpreter interpreter = new BeanInterpreter();
                Object value = interpreter.decodeFromYml(configuration, targetClass);
                if (value != null) {
                    return (T) value;
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("SectionInterpreter decode path: " + annotation.path() + " failed " + e);
        }
    }

    @Override
    public void encodeToYml(ConfigurationSection configuration, Object target) {
        if (AnnotationUtil.isMapType(target.getClass())) {
            Map<String, ?> map = (Map<String, ?>) target;
            Type genericType = field.getGenericType();
            if (genericType != null && genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                Class<?> valueClass = (Class<?>) pt.getActualTypeArguments()[1];
                if (!AnnotationUtil.isBaseType(valueClass)) {
                    for (String key : map.keySet()) {
                        AnnotationInterpreter interpreter = new BeanInterpreter();
                        interpreter.encodeToYml(configuration.getConfigurationSection(key), map.get(key));
                    }
                }
            }
        } else if (!AnnotationUtil.isBaseType(target.getClass())) {
            AnnotationInterpreter interpreter = new BeanInterpreter();
            interpreter.encodeToYml(configuration, target);
        }
    }

    public ConfigSection getAnnotation() {
        return annotation;
    }

}
