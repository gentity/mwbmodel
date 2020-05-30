package com.github.mwbmodel.grt;

import com.github.mwbmodel.grt.annotations.GrtKey;
import com.github.mwbmodel.grt.annotations.GrtValue;
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
import java.util.HashSet;
import java.util.Set;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 * Generic unmarshaller for GRT object tree serializations.
 * Can unmarshal (i.e. deserialize) GRT object graphs as found in
 * the MySQL Workbench MWB files. However, this unmarshaller is not
 * bound to the predefined to the objects defined in the GRT globals
 * tree, but can deserilize any GRT object serialization. 
 * 
 * To create an object graph, the Unmarshaller needs three inputs:
 * <ul>
 * <li>An {@link java.io.InputStream} providing the XML serialization</li>
 * <li>One or more base packages containing GRT model classes that are looked up 
 * and instantiated during the unmarshalling process</li>
 * <li>A globals list of predefined objects that can be referenced from a GRT serialization (GRT 'globals')</li>
 * </ul>
 * 
 * Note that the unmarshaller does not read MWB files directly, because
 * these are actually Zip files with a well defined internal structure.
 * The {@linkplain GrtUnmarshaller} is designed to read only the 
 * XML serialization contained in that Zip file
 * 
 * <h2>Mapping Rules</h2>
 * 
 * The serialization of a GRT object graph is essentially a tree
 * of key/value dictionaries, serialized in XML. Each value can either
 * be a scalar value (int,String,..), a collection (dictionary/map or list),
 * or another object (see {@link Type}). Nested objects are represented as a nested XML
 * key/value dictionary, however, to implement cyclic references, 
 * links to objects at other locations in the tree are possible.
 * 
 * Each object has a designated struct type. It is these struct types
 * that are modelled by Java classes. Because the struct names used
 * in GRT resemble Java package names (for instance 'db.mysql.Table'), 
 * these are used to look up Java classes that represent the struct types.
 * To prevent GRT files from referencing arbitrary Java classes,
 * lookup is confined to one or more base packages. For instance, 
 * with a base package of 'com.myapp.mybase', a GRT struct type 'foo.Bar' 
 * would be looked up as the 'com.myapp.mybase.foo.Bar' java class.
 *
 * Once a Java class was found for an object's struct type, the keys in
 * the object's key/value pairs are mapped to fields in the Java class.
 * A field matches a key if:
 * <ul>
 * <li>It has a {@link GrtKey} annotation with a matching key name</li>
 * <li>OR its field name matches the key</li>
 * <ul>
 * 
 * Currently, the following GRT basic value types are supported, and mapped
 * to the field type accordingly. Scalar values like 
 * {@link Type#STRING} or {@link Type#INT} are <i>coerced</i>, which means
 * that an attempt is made to transform them to fit into the target field type:
 * <ul>
 *	<li>{@link Type#INT}: Maps to <i>any</i> Java integral type or boolean. values are range 
 *	checked to fit into the target type.</li>
 *	<li>{@link Type#STRING}: Maps to either {@link String} or an enum. An enum 
 *	constant matches either if it has a matching {@link GrtValue} annotation,
 *	or if its name matches the value</li>
 *	<li>{@link Type#LIST}: Maps to {@link java.util.List}</li>
 *	<li>{@link Type#DICT}: Maps to {@link java.util.Map}. <i>currently not supported</i></li>
 *	<li>{@link Type#OBJECT}: Maps to any java object for which a struct class can be looked up</li>
 * </ul>
 * 
 * <h2>Links</h2>
 * 
 * Because the XML serialization is a tree, GRT not only serializes 
 * objects not only as subobjects in a tree, but also allows fields to
 * <i>link</i> to other objects that are located <i>somewhere else</i>.
 * Because each GRT object has a unique identifier (UUID), links can refer
 * to any object in the tree. 
 * Alternatively, links can refer to
 * objects <i>outside</i> the tree. These links do not refer to an
 * object's UUID, but to a designated global name that is assigned to 
 * such an object (we also call them contants). 
 * These objects be passed to the unmarshaller before deserialization is
 * started.
 * 
 * 
 * <h2>Unmarshaller Configuration</h2>
 * 
 * The unmarshaller's constructors allow for configuring base packages
 * and basic unmarshaller behavior. Configuration covers the following
 * aspects:
 * <ul>
 * <li>base packages</li>
 * <li>provided runtime constants</li>
 * <li>fault handling behavior<li>
 * </ul>
 * For details, see {@link GrtUnmarshallerConfig}.
 * 
 * @author Uwe Pachler
 */
public class GrtUnmarshaller {
	
	private static class GrtClassMapping {
		final Class<?> clazz;
		final Map<String,Field> keyMappings;

		public GrtClassMapping(Class<?> clazz, Map<String, Field> keyMappings) {
			this.clazz = clazz;
			this.keyMappings = keyMappings;
		}
		
	}
	
	private static final GrtClassMapping GRT_OBJECT_MAPPING = mkGrtClassMapping(GrtObject.class);
	private final JAXBContext jaxbc;
	private HashMap<String, Object> idObjectMap;
	private List<UnresolvedLink> linkBacklog;
	private Map<String,GrtClassMapping> grtClassCache;
	private Map<Class<?>,Map<String,Enum<?>>> enumMappingCache;
	
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
	 * @param basePackage	 the package(s) that will appear as the package root
	 * in GRT while unmarshalling
	 */
	public GrtUnmarshaller(ClassLoader cl, Package... basePackage) {
		this(new GrtUnmarshallerConfig(cl, basePackage));
	}
	
	public GrtUnmarshaller(GrtUnmarshallerConfig config) {
		this.config = config;
		
		grtClassCache = new HashMap<>();
		grtClassCache.put("GrtObject", mkGrtClassMapping(GrtObject.class));
		enumMappingCache = new HashMap<>();
		
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
				writeGrtObjectField(u.mapping, u.targetObject, u.link.getKey(), linkTarget);
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
	
	private static GrtClassMapping mkGrtClassMapping(Class<?> clazz) {
			
		Map<String,Field> keyMappings = new HashMap<>();
		Map<String,Field> naturalMappings = new HashMap<>();
		Map<String,Field> customMappings = new HashMap<>();
		for(Class<?> cl=clazz; cl!=null; cl=cl.getSuperclass()) {
			for(Field f : cl.getDeclaredFields()) {
				GrtKey grtKey = f.getDeclaredAnnotation(GrtKey.class);
				if(grtKey != null) {
					if(customMappings.containsKey(grtKey.value())) {
						throw new IllegalArgumentException("In class '" + clazz + "' or one of its superclasses, a " + GrtKey.class.getSimpleName() + " annotation is present that maps a field to the same key '" + grtKey.value() + "' as another such annotation");
					}
					customMappings.put(grtKey.value(), f);
				} else {
					naturalMappings.put(f.getName(), f);
				}
			}
		}

		Set<String> overlap = new HashSet<>(naturalMappings.keySet());
		overlap.retainAll(customMappings.keySet());
		if(!overlap.isEmpty()) {
			throw new IllegalArgumentException("in class '" + clazz.getName() + "' the following key mappings made through the " + GrtKey.class.getSimpleName() + " annotation clash with other natural field mappings: " + overlap.toString());
		}
		keyMappings = new HashMap<>(naturalMappings);
		keyMappings.putAll(customMappings);
		return new GrtClassMapping(clazz, keyMappings);
	}
	
	private GrtClassMapping findGrtClassMapping(String grtClassName) throws ClassNotFoundException {
		GrtClassMapping mapping = grtClassCache.get(grtClassName);
		if(mapping == null) {
			Class<?> clazz = null;
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
					break;
				} catch (ClassNotFoundException ex) {
					// attempt next package, if any
					continue;
				}
			}
			
			if(clazz != null) {	
				mapping = mkGrtClassMapping(clazz);
				grtClassCache.put(grtClassName, mapping);
			} else {
				if(!config.isIgnoringMissingClasses()) {
					throw new ClassNotFoundException("GRT class '" + grtClassName + "' not found, tried these candidates: " + Arrays.toString(candidateNames.toArray()));
				}
			}
		}
		
		return mapping;
	}
	
	private Map<String,Enum<?>> enumMappingOf(Class<?> clazz) throws IllegalArgumentException {
		Map<String,Enum<?>> mapping = enumMappingCache.get(clazz);
		if(mapping == null) {
			mapping = new HashMap<>();
			if(!clazz.isEnum()) {
				throw new IllegalArgumentException("class is not an enum");
			}
			Map<String,Enum<?>> naturalMappings = new HashMap<>();
			Map<String,Enum<?>> customMappings = new HashMap<>();
			
			for(Field f : clazz.getFields()) {
				if(!f.isEnumConstant()) {
					continue;
				}
				GrtValue grtValue = f.getDeclaredAnnotation(GrtValue.class);
				Map<String,Enum<?>> mappings;
				String mappedValue;
				Enum<?> enumConstant;
				try {
					enumConstant = (Enum<?>)f.get(null);
				} catch(IllegalAccessException iax) {
					throw new RuntimeException(iax);
				}
				if(grtValue != null) {
					if(customMappings.containsKey(grtValue.value())) {
						throw new IllegalArgumentException("the enum " + clazz.getName() + " has invalid " + GrtValue.class.getName() + " mappings, the value '" + grtValue.value() + "' is mapped multiple times");
					}
					customMappings.put(grtValue.value(), enumConstant);
				} else {
					naturalMappings.put(enumConstant.name(), enumConstant);
				}
			}
			
			HashSet<String> overlap = new HashSet<>(naturalMappings.keySet());
			overlap.retainAll(customMappings.keySet());
			if(!overlap.isEmpty()) {
				throw new IllegalStateException("key names derived from " + GrtValue.class.getSimpleName() + " annotated constants and non-annotated constants overlap: " + overlap.toString());
			}
			mapping = new HashMap<>(naturalMappings);
			mapping.putAll(customMappings);
			
			enumMappingCache.put(clazz, mapping);
		}
		
		return mapping;
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
		
		GrtClassMapping mapping = null;
		mapping = findGrtClassMapping(objectValue.getStructName());
		if(mapping == null) {
			// when we cannot find the class, we are unable to deserialize
			// this object. if we ignore this, all we can do is to return null
			// However, if this problem is not ignored, findGrtClass() will 
			// throw a ClassNotFoundException
			return null;
		}
		Object o;
		try {
			o = mapping.clazz.newInstance();
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
				
				writeGrtObjectField(mapping, o, v.getKey(), instance);
				
			} else if(e.getDeclaredType() == Link.class) {
				Link l = (Link)e.getValue();
				
				linkBacklog.add(new UnresolvedLink(l, o, mapping));
			}
		}
		
		return o;
	}
	
	private void writeGrtObjectField(GrtClassMapping mapping, Object o, String key, Object instance) {
		writeGrtObjectFieldImpl(mapping, o, key, instance);
	}
	
	private void writeGrtObjectId(Object o, UUID uuid) {
		writeGrtObjectFieldImpl(GRT_OBJECT_MAPPING, o, "id", uuid);
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
			Enum<?> v = enumMappingOf(fieldType).get(string);
			if(v == null) {
				throw new IllegalArgumentException("no mapping to enum constant found for value '" + string + "'");
			}
			return v;
		} catch(IllegalArgumentException iax) {
			return string;
		}
	}
	
	private void writeGrtObjectFieldImpl(GrtClassMapping mapping, Object o, String key, Object instance) {
		try {
			Field f = mapping.keyMappings.get(key);
			if(f == null) {
				if(!config.isIgnoringMissingFields()) {
					throw new RuntimeException("cannot resolve key '" + key + "' in GRT class representative '" + o.getClass().getName() + "', no field with that name was found");
				}
				return;
			}
			if(!f.isAccessible()) {
				f.setAccessible(true);
			}
			
			// coercion
			instance = attemptCoercion(f.getType(), instance);
			f.set(o, instance);
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private static class UnresolvedLink {
		public final Link link;
		public final Object targetObject;
		public final GrtClassMapping mapping;	// if the target object is a regular object, this is the mapping to use for storing the resolved link
		public final Integer index;	// if target object is a list, stores the index of the list element to resolve

		public UnresolvedLink(Link link, Object targetObject, GrtClassMapping mapping) {
			this(link, targetObject, mapping, null);
		}
		
		public UnresolvedLink(Link link, Object targetObject, Integer index) {
			this(link, targetObject, null, index);
		}
		
		private UnresolvedLink(Link link, Object targetObject, GrtClassMapping mapping, Integer index) {
			this.link = link;
			this.targetObject = targetObject;
			this.index = index;
			this.mapping = mapping;
		}
		
	}
}
