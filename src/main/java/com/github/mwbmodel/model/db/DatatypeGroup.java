/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mwbmodel.model.db;

import com.github.mwbmodel.grt.GrtObject;
import net.karneim.pojobuilder.GeneratePojoBuilder;

/**
 *
 * @author Uwe Pachler
 */
@GeneratePojoBuilder
public class DatatypeGroup extends GrtObject{
	String caption;
	String description;
	String name;

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
