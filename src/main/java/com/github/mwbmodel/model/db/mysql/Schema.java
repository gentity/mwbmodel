/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mwbmodel.model.db.mysql;

import com.github.mwbmodel.grt.GrtObject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author count
 */
public class Schema extends GrtObject {
	List<Table> tables = new ArrayList<>();
}
