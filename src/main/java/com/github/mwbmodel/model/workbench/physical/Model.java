/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mwbmodel.model.workbench.physical;

import com.github.mwbmodel.grt.GrtObject;
import com.github.mwbmodel.model.db.mysql.Catalog;

/**
 *
 * @author count
 */
public class Model extends GrtObject {
	Catalog catalog;

	public Catalog getCatalog() {
		return catalog;
	}

	public void setCatalog(Catalog catalog) {
		this.catalog = catalog;
	}
	
}
