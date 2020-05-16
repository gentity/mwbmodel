/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mwbmodel.grt.test;

import com.github.mwbmodel.grt.GrtObject;
import com.github.mwbmodel.grt.annotations.GrtValue;

/**
 *
 * @author count
 */
public class CoercedFields extends GrtObject{
	public long longfield;
	public int intfield;
	public short shortfield;
	public byte bytefield;
	public boolean boolfield;
	public FooBar enumfield1;
	public FooBar enumfield2;
	
	public enum FooBar {
		@GrtValue("F O O")
		FOO,
		BAR
	}
}
