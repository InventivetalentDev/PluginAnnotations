package org.inventivetalent.pluginannotations.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class AnnotationUtil {

    private static final String[] BASE_TYPES = {"java.lang.Integer",
            "java.lang.Double",
            "java.lang.Float",
            "java.lang.Long",
            "java.lang.Short",
            "java.lang.Byte",
            "java.lang.Boolean",
            "java.lang.Character",
            "java.lang.String",
            "java.util.List"};

    /**
     * Set a specified Field accessible
     *
     * @param f Field set accessible
     */
    public static Field setAccessible(Field f) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        f.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(f, f.getModifiers() & 0xFFFFFFEF);
        return f;
    }

    /**
     * @param classZ
     * @return
     */
    public static boolean isBaseType(Class classZ) {
        for (String type : BASE_TYPES) {
            if (type.equals(classZ.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param targetClass
     * @return
     */
    public static boolean isMapType(Class targetClass) {
        return targetClass == Map.class || targetClass == HashMap.class;
    }

}
