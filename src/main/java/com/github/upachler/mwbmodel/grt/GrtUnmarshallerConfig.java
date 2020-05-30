/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.upachler.mwbmodel.grt;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Configures the behavour of the unmarshaller. See the 
 * property documentation for details.
 * @author Uwe Pachler
 */
public class GrtUnmarshallerConfig {

	private final ClassLoader classLoader;
	private final List<Package> basePackages;
	private final Map<String,GrtObject> constants;
	private boolean ignoringMissingFields = false;
	private boolean ignoringMissingClasses = false;
	private boolean ignoringBrokenLinks = false;	// NOTE: implied by ignoreMissingClasses
	
	/**
	 * Creates a new unmarshaller config an empty constants pool
	 * @param cl	the class loader to use for class lookup
	 * @param basePackage	list of base packages for looking up GRT classes
	 */
	public GrtUnmarshallerConfig(ClassLoader cl, Package... basePackage) {
		this(cl, Collections.EMPTY_MAP, basePackage);
	}
	
	/**
	 * Creates a new unmarshaller config
	 * @param cl	the class loader to use for class lookup
	 * @param constants a {@linkplain Map} acting as a constant pool, mapping GRT
	 *	global names to predefined objects. 
	 * @param basePackage	list of base packages for looking up GRT classes
	 */
	public GrtUnmarshallerConfig(ClassLoader cl, Map<String,GrtObject> constants, Package... basePackage) {
		classLoader = cl;
		basePackages = Arrays.asList(basePackage);
		this.constants = constants;
	}
	
	/**
	 * The class loader used for loading a GRT class
	 * @return the class loader for GRT class lookup
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}
	
	/**
	 * List of Java base packages that are searched, in order, for
	 * GRT classes.
	 * @return the list of base packages
	 */
	public List<Package> getBasePackages() {
		return basePackages;
	}
	
	/**
	 * Provides the constant pool holding the global constants used during
	 * unmarshalling.
	 * @return	the constant pool.
	 */
	public Map<String, GrtObject> getConstants() {
		return constants;
	}
	
	/**
	 * If {@code true}, missing fields will not raise an error.
	 * Otherwise, unmarshalling will be terminated if a field cannot
	 * be found for a key in a GRT class
	 * @return	{@code true} if missing fields are ignored, {@code false} otherwise.
	 */
	public boolean isIgnoringMissingFields() {
		return ignoringMissingFields;
	}

	public void setIgnoringMissingFields(boolean ignoringMissingFields) {
		this.ignoringMissingFields = ignoringMissingFields;
	}

	/**
	 * If {@code true}, missing classes will not raise an error, but
	 * the object will simply not be deserialized.
	 * Otherwise, unmarshalling will be terminated if a class lookup
	 * cannot find a class for a GRT struct type. 
	 * Note that not deserializing a GRT object will also result in 
	 * not deserializing all of its sub-objects. This can have knock-on
	 * effects like broken links (see {@link #isIgnoringBrokenLinks()}).
	 * Therefore, ignoring missing classes <em>implies</em> ignoring missing links.
	 * @return	{@code true} if missing classes are ignored, {@code false} otherwise.
	 */
	public boolean isIgnoringMissingClasses() {
		return ignoringMissingClasses;
	}

	public void setIgnoringMissingClasses(boolean ignoringMissingClasses) {
		this.ignoringMissingClasses = ignoringMissingClasses;
	}

	/**
	 * If {@code true}, broken links will not raise an error, and the
	 * linking field will be set to {@code null}.
	 * Otherwise, unmarshalling will be terminated if an object with the
	 * link-specified or UUID cannot be found. Note that this may be a
	 * side effect of missing classes or fields (see also {@link #isIgnoringMissingClasses()}
	 * {@link #isIgnoringMissingFields() } ).
	 * @return	{@code true} if broken links are ignored, {@code false} otherwise.
	 */
	public boolean isIgnoringBrokenLinks() {
		// IMPORTANT: ignoring missing classes IMPLIES ignoring missing links
		return ignoringBrokenLinks || ignoringMissingClasses;
	}

	public void setIgnoringBrokenLinks(boolean ignoringBrokenLinks) {
		this.ignoringBrokenLinks = ignoringBrokenLinks;
	}
	
	
}
