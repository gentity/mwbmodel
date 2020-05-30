/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mwbmodel.util;

/**
 *
 * @author Uwe Pachler
 */
public abstract class Primitives {
	
	private static final Class[] rawPrimitives =   {void.class, long.class, int.class,     short.class, byte.class, boolean.class, double.class, float.class, char.class};
	private static final Class[] boxedPrimitives = {Void.class, Long.class, Integer.class, Short.class, Byte.class, Boolean.class, Double.class, Float.class, Character.class};

	private Primitives() {}
	
	public static Class<?> toBoxed(Class<?> primitiveType) {
		int i = lastIndexOfClass(rawPrimitives, primitiveType);
		if (i == -1) {
			return null;
		}
		return boxedPrimitives[i];
	}

	public static Class<?> toRaw(Class<?> primitiveType) {
		int i = lastIndexOfClass(boxedPrimitives, primitiveType);
		if (i == -1) {
			return null;
		}
		return rawPrimitives[i];
	}

	public static int lastIndexOfClass(Class[] classes, Class clazz) {
		int i;
		for (i = classes.length - 1; i >= 0 && classes[i] != clazz; --i) {
			;
		}
		return i;
	}
	
}
