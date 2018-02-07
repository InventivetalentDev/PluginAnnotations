package org.inventivetalent.pluginannotations.config;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.inventivetalent.pluginannotations.utils.AnnotationUtil;
import org.inventivetalent.pluginannotations.utils.ChatColorUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by shell on 2018/1/19.
 * <p>
 * Github: https://github.com/shellljx
 */
public class ValueInterpreter implements AnnotationInterpreter {

    private ConfigValue annotation;
    private Field field;

    public ValueInterpreter(Field field) {
        this.field = field;
        this.annotation = field.getAnnotation(ConfigValue.class);
    }

    @Override
    public <T> T decodeFromYml(ConfigurationSection configuration, Class<T> targetClass) {
        try {
            if (AnnotationUtil.isMapType(targetClass)) {
                return (T)decodeMapValue(configuration);
            } else {
                return (T)decodeSimpleValue(configuration, null);
            }
        } catch (Exception e) {
            throw new RuntimeException("ValueInterpreter decode config path: " + annotation.path() + " failed " + e);
        }
    }

    @Override
    public void encodeToYml(ConfigurationSection configuration, Object target) {
        try {
            Class targetClass = target.getClass();
            if (AnnotationUtil.isMapType(targetClass)) {
                saveMapValue(configuration, target);
            } else {
                saveSimpleValue(configuration, target, null);
            }
        } catch (Exception e) {
            throw new RuntimeException("ValueInterpreter save config path: " + annotation.path() + " failed " + e);
        }
    }

    private void saveMapValue(ConfigurationSection configuration, Object target) {
        if (!AnnotationUtil.isMapType(target.getClass())) {
            return;
        }
        Map<String, ?> map = (Map) target;
        Type genericType = field.getGenericType();
        if (genericType != null && genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            Class<?> valueClass = (Class<?>) pt.getActualTypeArguments()[1];
            if (AnnotationUtil.isBaseType(valueClass)) {
                for (String key : map.keySet()) {
                    saveSimpleValue(configuration, map.get(key), key);
                }
            }
        }
    }

    private void saveSimpleValue(ConfigurationSection configuration, Object target, String key) {
        if (annotation.colorChar() != ' ') {
            if (target instanceof String) {
                target = ChatColorUtils.encodeAlternateColorCodes(annotation.colorChar(), (String) target);
            }
            if (target instanceof List) {
                for (ListIterator iterator = ((List) target).listIterator(); iterator.hasNext(); ) {
                    Object next = iterator.next();
                    if (next instanceof String) {
                        iterator.set(ChatColorUtils.encodeAlternateColorCodes(annotation.colorChar(), (String) next));
                    }
                }
            }
        }
        configuration.set(getValuePath(key), target);
    }

    private Object decodeMapValue(ConfigurationSection configuration) {
        Type genericType = field.getGenericType();
        Map<String, Object> map = new HashMap<>();
        if (genericType != null && genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            Class<?> valueClass = (Class<?>) pt.getActualTypeArguments()[1];
            if (AnnotationUtil.isBaseType(valueClass)) {
                Set<String> keySet = configuration.getConfigurationSection(annotation.path()).getKeys(false);
                for (String key : keySet) {
                    map.put(key, decodeSimpleValue(configuration, key));
                }
                return map;
            }
        }
        return null;
    }

    private Object decodeSimpleValue(ConfigurationSection configuration, String key) {
        if (configuration.contains(annotation.path())) {
            if (field.getType() == ItemStack.class) {
                return configuration.getItemStack(getValuePath(key));
            } else if (field.getType() == Vector.class) {
                return configuration.getVector(getValuePath(key));
            } else {
                Object value = configuration.get(getValuePath(key));
                if (annotation.colorChar() != ' ') {
                    if (value instanceof String) {
                        value = ChatColor.translateAlternateColorCodes(annotation.colorChar(), (String) value);
                    }
                    if (value instanceof List) {
                        for (ListIterator iterator = ((List) value).listIterator(); iterator.hasNext(); ) {
                            Object next = iterator.next();
                            if (next instanceof String) {
                                iterator.set(ChatColor.translateAlternateColorCodes(annotation.colorChar(), (String) next));
                            }
                        }
                    }
                }
                return value;
            }
        } else if (!annotation.defaultsTo().isEmpty()) {
            return annotation.defaultsTo();
        }
        return null;
    }

    private String getValuePath(String key) {
        return key == null ? annotation.path() : annotation.path() + "." + key;
    }
}
