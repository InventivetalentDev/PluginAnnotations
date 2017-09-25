package org.inventivetalent.pluginannotations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AccessUtil {

	private AccessUtil() {
	}

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
	 * Set a specified Method accessible
	 *
	 * @param m Method set accessible
	 */
	public static Method setAccessible(Method m) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		m.setAccessible(true);
		return m;
	}

}
