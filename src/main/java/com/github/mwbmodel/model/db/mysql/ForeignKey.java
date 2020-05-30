/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mwbmodel.model.db.mysql;

import com.github.mwbmodel.grt.GrtObject;
import com.github.mwbmodel.grt.annotations.GrtValue;
import java.util.List;

/**
 *
 * @author Uwe Pachler
 */
public class ForeignKey extends GrtObject {
	public enum ModificationRule {
		@GrtValue("NO ACTION")
		NO_ACTION,
		CASCADE,
		@GrtValue("SET NULL")
		SET_NULL,
		RESTRICT
	}
	private String name;
	private List<Column> columns;
	private Table referencedTable;
	private List<Column> referencedColumns;
	private ModificationRule deleteRule;
	private ModificationRule updateRule;
	private boolean mandatory;
	private boolean many;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public void setColumns(List<Column> columns) {
		this.columns = columns;
	}

	public Table getReferencedTable() {
		return referencedTable;
	}

	public void setReferencedTable(Table referencedTable) {
		this.referencedTable = referencedTable;
	}

	public List<Column> getReferencedColumns() {
		return referencedColumns;
	}

	public void setReferencedColumns(List<Column> referencedColumns) {
		this.referencedColumns = referencedColumns;
	}

	public ModificationRule getDeleteRule() {
		return deleteRule;
	}

	public void setDeleteRule(ModificationRule deleteRule) {
		this.deleteRule = deleteRule;
	}

	public ModificationRule getUpdateRule() {
		return updateRule;
	}

	public void setUpdateRule(ModificationRule updateRule) {
		this.updateRule = updateRule;
	}

	public boolean isMandatory() {
		return mandatory;
	}

	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	public boolean isMany() {
		return many;
	}

	public void setMany(boolean many) {
		this.many = many;
	}
	
	
}
