/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mwbmodel.model.workbench.physical;

import com.github.mwbmodel.grt.GrtObject;
import com.github.mwbmodel.model.db.mgmt.Rdbms;
import com.github.mwbmodel.model.db.mysql.Catalog;

/**
 *
 * @author Uwe Pachler
 */
public class Model extends GrtObject {
	Catalog catalog;
	Rdbms rdbms;

	public Catalog getCatalog() {
		return catalog;
	}

	public void setCatalog(Catalog catalog) {
		this.catalog = catalog;
	}

	public Rdbms getRdbms() {
		return rdbms;
	}

	public void setRdbms(Rdbms rdbms) {
		this.rdbms = rdbms;
	}
	
}
