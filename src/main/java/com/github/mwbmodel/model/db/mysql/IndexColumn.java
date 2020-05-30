/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mwbmodel.model.db.mysql;

import com.github.mwbmodel.grt.GrtObject;

/**
 *
 * @author Uwe Pachler
 */
public class IndexColumn extends GrtObject {
	int columnLength;
	String comment;
	boolean descend;
	String expression;
	Column referencedColumn;
	String name;
	GrtObject owner;

	public int getColumnLength() {
		return columnLength;
	}

	public void setColumnLength(int columnLength) {
		this.columnLength = columnLength;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean isDescend() {
		return descend;
	}

	public void setDescend(boolean descend) {
		this.descend = descend;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public Column getReferencedColumn() {
		return referencedColumn;
	}

	public void setReferencedColumn(Column referencedColumn) {
		this.referencedColumn = referencedColumn;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GrtObject getOwner() {
		return owner;
	}

	public void setOwner(GrtObject owner) {
		this.owner = owner;
	}
	
	
}
