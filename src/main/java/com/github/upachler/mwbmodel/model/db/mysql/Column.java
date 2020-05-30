/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.upachler.mwbmodel.model.db.mysql;

import com.github.upachler.mwbmodel.grt.GrtObject;
import com.github.upachler.mwbmodel.grt.annotations.GrtKey;
import com.github.upachler.mwbmodel.model.db.SimpleDatatype;
import java.util.List;

/**
 *
 * @author Uwe Pachler
 */
public class Column extends GrtObject {
	boolean autoIncrement;
	String datatypeExplicitParams;
	String defaultValue;
	boolean defaultValueIsNull;
	List<String> flags;
	@GrtKey("isNotNull")
	boolean notNull;
	int length;
	int precision;
	int scale;
	String comment;
	String name;
	SimpleDatatype simpleType;

	public boolean isAutoIncrement() {
		return autoIncrement;
	}

	public void setAutoIncrement(boolean autoIncrement) {
		this.autoIncrement = autoIncrement;
	}

	public String getDatatypeExplicitParams() {
		return datatypeExplicitParams;
	}

	public void setDatatypeExplicitParams(String datatypeExplicitParams) {
		this.datatypeExplicitParams = datatypeExplicitParams;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean isDefaultValueIsNull() {
		return defaultValueIsNull;
	}

	public void setDefaultValueIsNull(boolean defaultValueIsNull) {
		this.defaultValueIsNull = defaultValueIsNull;
	}

	public List<String> getFlags() {
		return flags;
	}

	public void setFlags(List<String> flags) {
		this.flags = flags;
	}

	public boolean isNotNull() {
		return notNull;
	}

	public void setNotNull(boolean notNull) {
		this.notNull = notNull;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getPrecision() {
		return precision;
	}

	public void setPrecision(int precision) {
		this.precision = precision;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SimpleDatatype getSimpleType() {
		return simpleType;
	}

	public void setSimpleType(SimpleDatatype simpleType) {
		this.simpleType = simpleType;
	}
	
	
}
