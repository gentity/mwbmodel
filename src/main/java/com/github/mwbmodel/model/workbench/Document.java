/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mwbmodel.model.workbench;

import com.github.mwbmodel.grt.GrtObject;
import com.github.mwbmodel.model.workbench.physical.Model;
import java.util.List;

/**
 *
 * @author count
 */
public class Document extends GrtObject {
	// NOTE: incomplete, the real thing has lots more fields...
	
	List<Model> physicalModels;
}