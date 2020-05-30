/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.upachler.mwbmodel.model.db.mysql;

import com.github.upachler.mwbmodel.grt.GrtObject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Uwe Pachler
 */
public class Schema extends GrtObject {
	String name;
	List<Table> tables = new ArrayList<>();

	public List<Table> getTables() {
		return tables;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
