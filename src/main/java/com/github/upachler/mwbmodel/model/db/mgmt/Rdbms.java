/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.upachler.mwbmodel.model.db.mgmt;

import com.github.upachler.mwbmodel.grt.GrtObject;
import net.karneim.pojobuilder.GeneratePojoBuilder;

/**
 *
 * @author Uwe Pachler
 */
@GeneratePojoBuilder
public class Rdbms extends GrtObject{
	private String name;
	private String caption;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}
	
}
