/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mwbmodel.grt.test;

import com.github.mwbmodel.grt.GrtObject;

/**
 *
 * @author count
 */
public class TestObject extends GrtObject{
	
	private long intval;
	private String stringval;

	public long getIntval() {
		return intval;
	}

	public void setIntval(long intval) {
		this.intval = intval;
	}

	public String getStringval() {
		return stringval;
	}

	public void setStringval(String stringval) {
		this.stringval = stringval;
	}
	
	
}
