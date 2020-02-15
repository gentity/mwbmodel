/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mwbmodel.grt;

import com.github.mwbmodel.grt.test.CoercedFields;
import com.github.mwbmodel.grt.test.TestObject;
import com.github.mwbmodel.grt.test.Youngster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author count
 */
public class GrtUnmarshallerTest {
	
	public GrtUnmarshallerTest() {
	}

	Object unmarshalXml(String xml) throws IOException, ClassNotFoundException {
		
		Package basePackage = getClass().getPackage();
		GrtUnmarshaller u = new GrtUnmarshaller(getClass().getClassLoader(), basePackage);
		
		return u.unmarshal(new ByteArrayInputStream(xml.getBytes("UTF-8")));
	}
	/**
	 * Test of unmarshal method, of class GrtUnmarshaller.
	 */
	@Test
	public void testUnmarshalInt() throws Exception {
		// result should be java.lang.String "Hello"
		String xml =
			"<?xml version='1.0'?>\n" +
			"<data grt_format='2.0' document_type='MySQL Workbench Model' version='1.4.4'>" + 
			"<value type='string'>Hello</value>\n" +
			"</data>";
		Object result = unmarshalXml(xml
		);
		
		assertEquals("Hello", (String)result);
	}
	
	@Test
	public void testUnmarshalString() throws Exception {
		
		// result should be java.lang.Long 42
		String xml =
			"<?xml version='1.0'?>\n" +
			"<data grt_format='2.0' document_type='MySQL Workbench Model' version='1.4.4'>" + 
			"<value type='int'>42</value>\n" +
			"</data>";
		Object result = unmarshalXml(xml
		);
		
		assertEquals(42, (long)result);
	}
	
	@Test
	public void testUnmarshalList() throws Exception {
		// result should be java.lang.Long 42
		String xml =
			"<?xml version='1.0'?>\n" +
			"<data grt_format='2.0' document_type='MySQL Workbench Model' version='1.4.4'>" + 
			"<value type='list'>\n" +
			"    <value type='int'>1</value>\n" +
			"    <value type='int'>2</value>\n" +
			"    <value type='int'>3</value>\n" +
			"</value>\n" +
			"</data>";
		
		Object result = unmarshalXml(xml);
		
		List<Long> o = (List<Long>)result;
		assertEquals(Arrays.asList(1L,2L,3L), o);
	}
	
	@Test
	public void testUnmarshalObject() throws Exception {
		
		// result should be java.lang.Long 42
		String xml =
			"<?xml version='1.0'?>\n" +
			"<data grt_format='2.0' document_type='MySQL Workbench Model' version='1.4.4'>" + 
			"<value type='object' struct-name='test.TestObject' id='1470D384-180C-48DC-8E4B-0DE3332A2841'>\n" +
			"    <value key='intval' type='int'>42</value>\n" +
			"    <value key='stringval' type='string'>Hello</value>\n" +
			"</value>\n" +
			"</data>";
		
		Object result = unmarshalXml(xml);
		
		TestObject o = (TestObject)result;
		assertEquals(UUID.fromString("1470D384-180C-48DC-8E4B-0DE3332A2841"), o.getId());
		assertEquals(o.getIntval(), 42);
		assertEquals(o.getStringval(), "Hello");
	}
	
	@Test
	public void testUnmarshalCoercedObjectFields() throws Exception {
		
		// result should be java.lang.Long 42
		String xml =
			"<?xml version='1.0'?>\n" +
			"<data grt_format='2.0' document_type='MySQL Workbench Model' version='1.4.4'>" + 
			"<value type='object' struct-name='test.CoercedFields' id='33F191AA-6EBC-4735-A168-339C80E29E52'>\n" +
			"    <value key='longfield'  type='int'>10000000000</value>\n" +
			"    <value key='intfield'   type='int'>2000000</value>\n" +
			"    <value key='shortfield' type='int'>30000</value>\n" +
			"    <value key='bytefield'  type='int'>40</value>\n" +
			"    <value key='boolfield'  type='int'>1</value>\n" +
			"    <value key='enumfield'  type='string'>BAR</value>\n" +
			"</value>\n" +
			"</data>";
		
		Object result = unmarshalXml(xml);
		
		CoercedFields o = (CoercedFields)result;
		assertEquals(o.longfield, 10000000000L);
		assertEquals(o.intfield, 2000000);
		assertEquals(o.shortfield, 30000);
		assertEquals(o.bytefield, 40);
		assertEquals(o.boolfield, true);
		assertEquals(o.enumfield, CoercedFields.FooBar.BAR);
	}
	
	@Test
	public void testUnmarshalLinks() throws Exception {
		
		// Tom, Maxine, Falk and Barara are youngsters:
		// * Tom's best friend is Maxine. Otherwise he has no friends
		// * Maxine's best friend is tom, but her other friends are Falk and Barbara
		// * Falk is without best friends, but calls Maxine his friend
		// * Barbara likes herself best, she's her own best friend. She needs no other friends...
		String xml =
			"<?xml version='1.0'?>\n" +
			"<data grt_format='2.0' document_type='MySQL Workbench Model' version='1.4.4'>" + 
			"<value type='list' content-type='object' content-struct-name='test.Youngster'>\n" +
			"    <value type='object' struct-name='test.Youngster' id='C918AD92-61BA-460B-888B-7A362DB582A7'>\n" +	// TOM
			"        <value key='name' type='string'>Tom</value>\n" +
			"        <link type='object' struct-name='GrtObject' key=\"bestFriend\">88F43838-BBE0-4103-854B-8B64F0C1971E</link>" +	// (maxine)
			"        <value key='friends' type='list'> \n" + 
			"        </value>\n" +
			"    </value>\n" +
			"    <value type='object' struct-name='test.Youngster' id='88F43838-BBE0-4103-854B-8B64F0C1971E'>\n" +	// MAXINE
			"        <value key='name' type='string'>Maxine</value>\n" +
			"        <link type='object' struct-name='GrtObject' key=\"bestFriend\">C918AD92-61BA-460B-888B-7A362DB582A7</link>" +	// (tom)
			"        <value key='friends' type='list'> \n" + 
			"            <link type='object' struct-name='GrtObject'>4595B4EA-46F6-4C2D-BD41-6E0D42B3100C</link>" +	// (falk)
			"            <link type='object' struct-name='GrtObject'>F93551FC-EBD6-4412-AEB3-4F9B2D897704</link>" +	// (barbara)
			"        </value>\n" +
			"    </value>\n" +
			"    <value type='object' struct-name='test.Youngster' id='4595B4EA-46F6-4C2D-BD41-6E0D42B3100C'>\n" +	// FALK
			"        <value key='name' type='string'>Falk</value>\n" +
			"        <value key='friends' type='list'> \n" + 
			"            <link type='object' struct-name='GrtObject'>88F43838-BBE0-4103-854B-8B64F0C1971E</link>" +	// (maxine)
			"        </value>\n" +
			"    </value>\n" +
			"    <value type='object' struct-name='test.Youngster' id='F93551FC-EBD6-4412-AEB3-4F9B2D897704'>\n" +	// BARBARA
			"        <value key='name' type='string'>Barbara</value>\n" +
			"        <link type='object' struct-name='GrtObject' key=\"bestFriend\">F93551FC-EBD6-4412-AEB3-4F9B2D897704</link>" +	// (barbara)
			"        <value key='friends' type='list'> \n" + 
			"        </value>\n" +
			"    </value>\n" +
			"</value>\n" +
			"</data>";
		
		Object result = unmarshalXml(xml);
		
		List<Youngster> o = (List<Youngster>)result;
		
		assertEquals(4, o.size());
		
		Youngster tom = o.get(0);
		Youngster maxine = o.get(1);
		Youngster falk = o.get(2);
		Youngster barbara = o.get(3);
		
		assertEquals(UUID.fromString("C918AD92-61BA-460B-888B-7A362DB582A7"), tom.getId());
		assertEquals(UUID.fromString("88F43838-BBE0-4103-854B-8B64F0C1971E"), maxine.getId());
		assertEquals(UUID.fromString("4595B4EA-46F6-4C2D-BD41-6E0D42B3100C"), falk.getId());
		assertEquals(UUID.fromString("F93551FC-EBD6-4412-AEB3-4F9B2D897704"), barbara.getId());
		
		assertSame(maxine, tom.getBestFriend());
		assertTrue(tom.getFriends().isEmpty());
		
		assertSame(tom, maxine.getBestFriend());
		assertEquals(Arrays.asList(falk, barbara), maxine.getFriends());
		
		assertNull(falk.getBestFriend());
		assertEquals(Arrays.asList(maxine), falk.getFriends());
		
		assertSame(barbara, barbara.getBestFriend());
		assertTrue(barbara.getFriends().isEmpty());
		
	}
	
}
