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
public class Catalog extends GrtObject {
	List<Schema> schemata = new ArrayList<>();

	public List<Schema> getSchemata() {
		return schemata;
	}
	
}
