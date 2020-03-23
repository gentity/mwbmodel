/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mwbmodel.model.db;

import com.github.mwbmodel.grt.GrtObject;
import com.github.mwbmodel.model.db.mgmt.Rdbms;
import java.util.List;
import net.karneim.pojobuilder.GeneratePojoBuilder;

/**
 *
 * @author count
 */
@GeneratePojoBuilder
public class SimpleDatatype extends GrtObject {
	
	public enum Flag {
		ZEROFILL,
		UNSIGNED,
	}
	
	int characterMaximumLength;
	int characterOctetLength;
	int dateTimePrecision;
	List<Flag> flags;
	DatatypeGroup group;
	String name;
	int numericPrecision;
	int numericPrecisionRadix;
	int numericScale;
	int parameterFormatType;
	Rdbms owner;
	List<String> synonyms;

	public int getCharacterMaximumLength() {
		return characterMaximumLength;
	}

	public int getCharacterOctetLength() {
		return characterOctetLength;
	}

	public int getDateTimePrecision() {
		return dateTimePrecision;
	}

	public List<Flag> getFlags() {
		return flags;
	}

	public DatatypeGroup getGroup() {
		return group;
	}

	public String getName() {
		return name;
	}

	public int getNumericPrecision() {
		return numericPrecision;
	}

	public int getNumericPrecisionRadix() {
		return numericPrecisionRadix;
	}

	public int getNumericScale() {
		return numericScale;
	}

	public int getParameterFormatType() {
		return parameterFormatType;
	}

	public Rdbms getOwner() {
		return owner;
	}

	public List<String> getSynonyms() {
		return synonyms;
	}

	
	
}
