/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mwbmodel.model.db.mysql;

import com.github.mwbmodel.grt.GrtObject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author count
 */
public class Index extends GrtObject{
	
	public enum Type {
		PRIMARY,
		INDEX,
		UNIQUE,
		FULLTEXT
	}
	Type indexType;
	boolean isPrimary;
	String name;
	boolean unique;
	List<IndexColumn> columns = new ArrayList<>();

	public Type getIndexType() {
		return indexType;
	}

	public void setIndexType(Type indexType) {
		this.indexType = indexType;
	}

	public boolean isIsPrimary() {
		return isPrimary;
	}

	public void setIsPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public List<IndexColumn> getColumns() {
		return columns;
	}
	
	
	
}
