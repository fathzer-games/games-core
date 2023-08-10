package com.fathzer.games.util;

import java.util.List;

/** Some utilities on class.
 */
public interface ClassUtils {
	/** Gets the list super classes of a class.
	 * @param aClass a class
	 * @return A list of its super classes except Object.class
	 */
	public static List<Class<?>> getClassHierarchy(Class<?> aClass) {
		final List<Class<?>> result = new java.util.ArrayList<>();
		while (aClass != Object.class) {
			result.add(aClass);
			aClass = aClass.getSuperclass();
		}
		return result;
	}
}
