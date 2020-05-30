/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.upachler.mwbmodel.model.workbench;

import com.github.upachler.mwbmodel.grt.GrtObject;
import com.github.upachler.mwbmodel.model.workbench.physical.Model;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Uwe Pachler
 */
public class Document extends GrtObject {
	// NOTE: incomplete, the real thing has lots more fields...
	
	List<Model> physicalModels = new ArrayList<>();

	public List<Model> getPhysicalModels() {
		return physicalModels;
	}
}
