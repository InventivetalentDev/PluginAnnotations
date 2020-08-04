package org.inventivetalent.pluginannotations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class AccessUtil {

	/**
	 * Set a specified Field accessible
	 *
	 * @param f Field set accessible
	 */
	public static Field setAccessible(Field field) throws ReflectiveOperationException {
		field.setAccessible(true);
		try {
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
		} catch (NoSuchFieldException e) {
			if (e.getCause().getMessage().equals("modifiers")) {
				System.err.println("Failed to remove final modifier from " + field);
			} else {
				throw e;
			}
		}
		return field;
	}

	/**
	 * Set a specified Method accessible
	 *
	 * @param m Method set accessible
	 */
	public static Method setAccessible(Method m) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		m.setAccessible(true);
		return m;
	}

}
