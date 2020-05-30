/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.upachler.mwbmodel.model.db.mysql;

import com.github.upachler.mwbmodel.grt.GrtObject;
import com.github.upachler.mwbmodel.grt.annotations.GrtKey;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Uwe Pachler
 */
public class Index extends GrtObject{
	
	public enum Type {
		PRIMARY,
		INDEX,
		UNIQUE,
		FULLTEXT
	}
	Type indexType;
	@GrtKey("isPrimary")
	boolean primary;
	String name;
	boolean unique;
	List<IndexColumn> columns = new ArrayList<>();

	public Type getIndexType() {
		return indexType;
	}

	public void setIndexType(Type indexType) {
		this.indexType = indexType;
	}

	public boolean isPrimary() {
		return primary;
	}

	public void setPrimary(boolean primary) {
		this.primary = primary;
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
