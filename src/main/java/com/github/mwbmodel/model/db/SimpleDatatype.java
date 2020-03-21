/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mwbmodel.model.db;

import com.github.mwbmodel.grt.GrtObject;
import com.github.mwbmodel.model.db.mgmt.Rdbms;
import java.util.List;

/**
 *
 * @author count
 */
public class SimpleDatatype extends GrtObject {
	int characterMaximumLength;
	int characterOctetLength;
	int dateTimePrecision;
	List<String> flags;
	DatatypeGroup group;
	String name;
	int numericPrecision;
	int numericPrecisionRadix;
	int numericScale;
	int parameterFormatType;
	Rdbms owner;
	List<String> synonyms;

}
