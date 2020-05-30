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
 * @author Uwe Pachler
 */
public class Table extends GrtObject {
	private String name;
	private List<Column> columns = new ArrayList<>();
	private List<ForeignKey> foreignKeys = new ArrayList<>();
	private List<Index> indices = new ArrayList<>();
	private Index primaryKey;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public List<ForeignKey> getForeignKeys() {
		return foreignKeys;
	}

	public List<Index> getIndices() {
		return indices;
	}

	public Index getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(Index primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	
}
