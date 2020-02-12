/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mwbmodel.model.db.mysql;

import com.github.mwbmodel.grt.GrtObject;
import com.github.mwbmodel.model.db.SimpleDatatype;
import java.util.List;

/**
 *
 * @author count
 */
public class Column extends GrtObject {
	boolean autoIncrement;
	String characterSetName;
	String collationName;
	String datatypeExplicitParams;
	String defaultValue;
	boolean defaultValueIsNull;
	List<String> flags;
	boolean isNotNull;
	int length;
	int precision;
	int scale;
	String comment;
	String name;
	String oldName;
	GrtObject owner;
	SimpleDatatype simpleType;
}
