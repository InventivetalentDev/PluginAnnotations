package org.inventivetalent.pluginannotations;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class AccessUtil {

	private static final Object modifiersVarHandle;
	private static final Field modifiersField;
	
	/**
	 * Set a specified Field accessible
	 *
	 * @param field Field to set accessible
	 * @param readOnly Whether removing final modifier should not be attempted
	 */
	public static Field setAccessible(Field field, boolean readOnly) throws ReflectiveOperationException {
		field.setAccessible(true);
		if (readOnly) {
			return field;
		}
		int newModifiers = field.getModifiers() & ~Modifier.FINAL;
		if (modifiersVarHandle != null) {
			((VarHandle) modifiersVarHandle).set(field, newModifiers);
		} else {
			modifiersField.setInt(field, newModifiers);
		}
		return field;
	}

	/**
	 * Set a specified Field accessible
	 *
	 * @param field Field to set accessible
	 */
	public static Field setAccessible(Field field) throws ReflectiveOperationException {
		return setAccessible(field, false);
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

	private static Object initModifiersVarHandle() {
		try {
			VarHandle.class.getName(); // Makes this method fail-fast on JDK 8
			return MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup())
					.findVarHandle(Field.class, "modifiers", int.class);
		} catch (IllegalAccessException | NoClassDefFoundError | NoSuchFieldException ignored) {}
		return null;
	}

	private static Field initModifiersField() {
		try {
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			return modifiersField;
		} catch (NoSuchFieldException ignored) {}
		return null;
	}

	static {
		modifiersVarHandle = initModifiersVarHandle();
		modifiersField = initModifiersField();
	}
}
