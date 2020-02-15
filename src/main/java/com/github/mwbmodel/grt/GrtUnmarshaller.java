package com.github.mwbmodel.grt;

import com.github.mwbmodel.util.Primitives;
import com.github.mwbmodel.grt.model.Data;
import com.github.mwbmodel.grt.model.Link;
import com.github.mwbmodel.grt.model.ObjectFactory;
import com.github.mwbmodel.grt.model.Type;
import com.github.mwbmodel.grt.model.Value;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author count
 */
public class GrtUnmarshaller {

	private final JAXBContext jaxbc;
	private HashMap<String, Object> idObjectMap;
	private List<UnresolvedLink> linkBacklog;
	private Map<String,Class<?>> grtClassCache;
	
	private GrtUnmarshallerConfig config;
	
	/**
	 * Creates a new GrtUnmarshaller which attempts to find the workbench classes
	 * below the given base packages. When the GrtUnmarshaller attempts to 
	 * deserialize an object with struct-type 'foo.Bar', it will attempt to 
	 * find a Java class by appending the struct-type to a base package. In 
	 * our example, if the base package is 'com.acme', the unmarshaller will
	 * attempt to find the class 'com.acme.foo.Bar'. This is attempted for every
	 * package given to the GrtUnmarshaller. The first match hits; if no match is
	 * found, unmarshalling fails.
	 * 
	 * @param basePackage...	 the package(s) that will appear as the package root
	 * in GRT while unmarshalling
	 */
	public GrtUnmarshaller(ClassLoader cl, Package... basePackage) {
		this(new GrtUnmarshallerConfig(cl, basePackage));
	}
	
	public GrtUnmarshaller(GrtUnmarshallerConfig config) {
		this.config = config;
		
		grtClassCache = new HashMap<>();
		grtClassCache.put("GrtObject", GrtObject.class);
		
		try {
			jaxbc = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
		} catch (JAXBException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public Object unmarshal(InputStream is) throws IOException, ClassNotFoundException {
		
		idObjectMap = new HashMap<>();
		idObjectMap.putAll(config.getConstants());
		linkBacklog = new ArrayList<>();
		
		Data metamodel;
		try {
			metamodel = (Data)jaxbc.createUnmarshaller().unmarshal(is);
		} catch (JAXBException ex) {
			throw new IOException(ex);
		}
		
		
		return deserializeData(metamodel);
	}
	
	private Object deserializeData(Data d) throws ClassNotFoundException {
		Object o = deserializeValue(d.getValue());
		resolveLinks();
		return o;
	}
	
	private void resolveLinks() {
		for(UnresolvedLink u : linkBacklog) {
			String oid = u.link.getValue();
			Object linkTarget = idObjectMap.get(oid);
			if(linkTarget == null) {
				if(config.isIgnoringBrokenLinks()) {
					continue;
				} else {
					throw new RuntimeException("link target object with id '" + oid + "' not found");
				}
			}
			// if there is an index, we assume the target object is a list
			if(u.index != null) {
				((List)u.targetObject).set(u.index, linkTarget);
			} else {
				writeGrtObjectField(u.targetObject, u.link.getKey(), linkTarget);
			}
		}
	}
	
	private Object deserializeValue(Value v) throws ClassNotFoundException {
		Object o = null;
		switch (v.getType()) {
			case DICT:
				// not ever seen now this looks filled...
				o = Collections.EMPTY_MAP;
				break;
			case INT:
				o = Long.parseLong(v.getContent().get(0).toString().trim());
				break;
			case STRING:
				o = v.getContent().isEmpty()
					?	""
					:	v.getContent().get(0).toString();
				break;
			case OBJECT:{
				o = deserializeObjectValue(v);
			}	break;
			case LIST:{
				o = deserializeListValue(v);
				break;
			}
			default:
				throw new UnsupportedOperationException();
		}
		
		return o;
	}
	
	private Class<?> findGrtClass(String grtClassName) throws ClassNotFoundException {
		Class<?> clazz = grtClassCache.get(grtClassName);
		if(clazz == null) {
			List<String> candidateNames = new ArrayList<>();
			for(Package p : config.getBasePackages()) {
				String baseName = p.getName();
				String candidateName;
				if(!baseName.isEmpty()) {
					candidateName = baseName + "." + grtClassName;
				} else {
					candidateName = grtClassName;
				}
				candidateNames.add(candidateName);
			}
			
			for(String candidateName : candidateNames) {
				try {
					clazz = config.getClassLoader().loadClass(candidateName);
					if(!GrtObject.class.isAssignableFrom(clazz)) {
						throw new RuntimeException("found class '" + clazz.getName() + "' when searching for GRT class '" + grtClassName + "', but that class is not a subclass of GrtObject");
					}
					grtClassCache.put(grtClassName, clazz);
					break;
				} catch (ClassNotFoundException ex) {
					// attempt next package, if any
					continue;
				}
			}
			
			if(clazz == null) {
				throw new ClassNotFoundException("GRT class '" + grtClassName + "' not found, tried these candidates: " + Arrays.toString(candidateNames.toArray()));
			}
		}
		
		return clazz;
	}
	
	private Object deserializeListValue(Value objectValue) throws ClassNotFoundException {
		assert objectValue.getType() == Type.LIST;
		
		List o = new ArrayList<>();
		
		for(Object content : objectValue.getContent()) {
			if(!(content instanceof JAXBElement)) {
				continue;
			}
			JAXBElement e = (JAXBElement)content;
			if(e.getDeclaredType() == Value.class) {
				Value v = (Value)e.getValue();
				o.add(deserializeValue(v));
			} else {
				assert e.getDeclaredType() == Link.class;
				Link l = (Link)e.getValue();
				linkBacklog.add(new UnresolvedLink(l, o, o.size()));
				o.add(null);
			}
		}
		
		return o;
	}
	
	private Object deserializeObjectValue(Value objectValue) throws ClassNotFoundException {
		assert objectValue.getType() == Type.OBJECT;
		
		Class clazz = null;
		try {
			clazz = findGrtClass(objectValue.getStructName());
		} catch(ClassNotFoundException cnfx) {
			if(config.isIgnoringMissingClasses()) {
				// when we cannot find the class, we are unable to deserialize
				// this object. if we ignore this, all we can do is to return null
				return null;
			} else {
				// re-throw
				throw cnfx;
			}
		}
		Object o;
		try {
			o = clazz.newInstance();
			UUID id = UUID.fromString(objectValue.getId());
			writeGrtObjectId(o, id);
		} catch (InstantiationException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
		
		if(objectValue.getId() != null) {
			idObjectMap.put(objectValue.getId(), o);
		}
		
		for(Object content : objectValue.getContent()) {
			if(!(content instanceof JAXBElement)) {
				continue;
			}
			JAXBElement e = (JAXBElement)content;
			
			if(e.getDeclaredType() == Value.class) {
				Value v = (Value)e.getValue();
				
				Object instance = deserializeValue(v);
				
				writeGrtObjectField(o, v.getKey(), instance);
				
			} else if(e.getDeclaredType() == Link.class) {
				Link l = (Link)e.getValue();
				
				linkBacklog.add(new UnresolvedLink(l, o));
			}
		}
		
		return o;
	}
	
	private void writeGrtObjectField(Object o, String key, Object instance) {
		writeGrtObjectFieldImpl(o.getClass(), o, key, instance);
	}
	
	private void writeGrtObjectId(Object o, UUID uuid) {
		writeGrtObjectFieldImpl(GrtObject.class, o, "id", uuid);
	}
	
	
	private static Class<?> toBoxedType(Class<?> primitiveType) {
		return Primitives.toBoxed(primitiveType);
	}
	
	private void checkInRangeForCoercion(long value, Class targetType, long min, long max) {
		if(value > max || value < min) {
			throw new ArithmeticException("value '" + value + "' is out of range for " + targetType + " target field while attempting coercion");
		}
	}
	
	private Object attemptCoercion(Class fieldType, Object instance) {
		if(instance instanceof Long) {
			return attemptLongCoercion(fieldType, (Long)instance);
		} else if(instance instanceof String) {
			return attemptStringCoercion(fieldType, (String)instance);
		} else {
			return instance;
		}
	}
	
	private Object attemptLongCoercion(Class fieldType, Long longbox) {
		long longval = (long)longbox;
		Class targetType = toBoxedType(fieldType);
		if(targetType == null) {
			targetType = fieldType;
		}
		if(targetType == Boolean.class) {
			checkInRangeForCoercion(longval, targetType, 0, 1);
			if(longval == 0) {
				return Boolean.FALSE;
			} else {
				return Boolean.TRUE;
			}
		} else if(Integer.class.isAssignableFrom(targetType)) {
			checkInRangeForCoercion(longval, targetType, Integer.MIN_VALUE, Integer.MAX_VALUE);
			return longbox.intValue();
		} else if(Short.class.isAssignableFrom(targetType)) {
			checkInRangeForCoercion(longval, targetType, Short.MIN_VALUE, Short.MAX_VALUE);
			return longbox.shortValue();
		} else if(Byte.class.isAssignableFrom(targetType)) {
			checkInRangeForCoercion(longval, targetType, Byte.MIN_VALUE, Byte.MAX_VALUE);
			return longbox.byteValue();
		} else {
			return longbox;
		}
	}
	
	private Object attemptStringCoercion(Class fieldType, String string) {
		if(!fieldType.isEnum()) {
			return string;
		}
		try {
			return Enum.valueOf(fieldType, string);
		} catch(IllegalArgumentException iax) {
			return string;
		}
	}
	
	private void writeGrtObjectFieldImpl(Class clazz, Object o, String key, Object instance) {
		try {
			Field f = clazz.getDeclaredField(key);
			if(!f.isAccessible()) {
				f.setAccessible(true);
			}
			
			// coercion
			instance = attemptCoercion(f.getType(), instance);
			f.set(o, instance);
		} catch (NoSuchFieldException ex) {
			// try base class
			if(clazz.getSuperclass() != null) {
				writeGrtObjectFieldImpl(clazz.getSuperclass(), o, key, instance);
			} else {
				if(!config.isIgnoringMissingFields()) {
					throw new RuntimeException("cannot resolve key '" + key + "' in GRT class representative '" + o.getClass().getName() + "', no field with that name was found");
				}
			}
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private static class UnresolvedLink {
		public final Link link;
		public final Object targetObject;
		public final Integer index;	// if target object is a list, stores the index of the list element to resolve

		public UnresolvedLink(Link link, Object targetObject) {
			this(link, targetObject, null);
		}
		
		public UnresolvedLink(Link link, Object targetObject, Integer index) {
			this.link = link;
			this.targetObject = targetObject;
			this.index = index;
		}
		
	}
}
