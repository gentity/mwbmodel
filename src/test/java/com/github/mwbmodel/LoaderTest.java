/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mwbmodel;

import com.github.mwbmodel.model.workbench.Document;
import java.io.InputStream;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author count
 */
public class LoaderTest {
	
	public LoaderTest() {
	}

	/**
	 * Test of loadMwb method, of class Loader.
	 */
	@Test
	public void testLoadMwb() throws Exception {
		System.out.println("loadMwb");
		InputStream is = getClass().getClassLoader().getResourceAsStream("test_wb_8.mwb");
		Document result = Loader.loadMwb(is);
		assertNotNull(result);
	}
	
}
