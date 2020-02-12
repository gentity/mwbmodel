/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mwbmodel.grt;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author count
 */
public class GrtUnmarshallerConfig {

	private final ClassLoader classLoader;
	private final List<Package> basePackages;
	private final Map<String,GrtObject> constants;
	private boolean ignoringMissingFields = false;
	private boolean ignoringMissingClasses = false;
	private boolean ignoringBrokenLinks = false;	// NOTE: implied by ignoreMissingClasses
	

	public GrtUnmarshallerConfig(ClassLoader cl, Package... basePackage) {
		this(cl, Collections.EMPTY_MAP, basePackage);
	}
	
	public GrtUnmarshallerConfig(ClassLoader cl, Map<String,GrtObject> constants, Package... basePackage) {
		classLoader = cl;
		basePackages = Arrays.asList(basePackage);
		this.constants = constants;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public List<Package> getBasePackages() {
		return basePackages;
	}

	public Map<String, GrtObject> getConstants() {
		return constants;
	}

	public boolean isIgnoringMissingFields() {
		return ignoringMissingFields;
	}

	public void setIgnoringMissingFields(boolean ignoringMissingFields) {
		this.ignoringMissingFields = ignoringMissingFields;
	}

	public boolean isIgnoringMissingClasses() {
		return ignoringMissingClasses;
	}

	public void setIgnoringMissingClasses(boolean ignoringMissingClasses) {
		this.ignoringMissingClasses = ignoringMissingClasses;
	}

	public boolean isIgnoringBrokenLinks() {
		// IMPORTANT: ignoring missing classes IMPLIES ignoring missing links
		return ignoringBrokenLinks || ignoringMissingClasses;
	}

	public void setIgnoringBrokenLinks(boolean ignoringBrokenLinks) {
		this.ignoringBrokenLinks = ignoringBrokenLinks;
	}
	
	
}
