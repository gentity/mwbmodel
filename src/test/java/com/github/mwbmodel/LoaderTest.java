/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mwbmodel;

import com.github.mwbmodel.model.db.mysql.Column;
import com.github.mwbmodel.model.db.mysql.ForeignKey;
import com.github.mwbmodel.model.db.mysql.Index;
import com.github.mwbmodel.model.db.mysql.IndexColumn;
import com.github.mwbmodel.model.db.mysql.Schema;
import com.github.mwbmodel.model.db.mysql.Table;
import com.github.mwbmodel.model.workbench.Document;
import com.github.mwbmodel.model.workbench.physical.Model;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Function;
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
		
		// check that we can load 
		
		System.out.println("loadMwb");
		InputStream is = getClass().getClassLoader().getResourceAsStream("test_wb_8.mwb");
		Document result = Loader.loadMwb(is);
		assertNotNull(result);
		
		Model model = result.getPhysicalModels().get(0);
		assertSame(lookup("com.mysql.rdbms.mysql"), model.getRdbms());
		List<Table> tables = model.getCatalog().getSchemata().get(0).getTables();
		
		// check company table is there
		Table companyTable = getNamed(tables, "company", Table::getName);
		Column companyIdColumn = getColumn(companyTable, "id");
		
		// take a closer look at employee table, it also has foreign keys
		Table employeeTable = getNamed(tables, "employee", Table::getName);
		
		// columns and types
		assertEquals(3, employeeTable.getColumns().size());
		Column employeeIdColumn = getColumn(employeeTable, "id");
		{
			Column col = employeeIdColumn;
			assertSame(lookup("com.mysql.rdbms.mysql.datatype.int"), col.getSimpleType());
			assertTrue(col.isAutoIncrement());
			assertTrue(col.isNotNull());
			assertEquals(-1, col.getLength());
			assertEquals(-1, col.getPrecision());
			assertEquals(-1, col.getScale());
		}
		Column employeeNameColumn = getColumn(employeeTable, "name");
		{
			Column col = employeeNameColumn;
			assertSame(lookup("com.mysql.rdbms.mysql.datatype.varchar"), col.getSimpleType());
			assertEquals(45, col.getLength());
			assertFalse(col.isAutoIncrement());
		}
		Column employeeCompanyIdColumn = getColumn(employeeTable, "company_id");
		{
			Column col = employeeCompanyIdColumn;
			assertSame(lookup("com.mysql.rdbms.mysql.datatype.int"), col.getSimpleType());
			assertFalse(col.isAutoIncrement());
			// no need to check the rest, must have been deserialized correctly
			// if that worked for the other columns...
		}
		
		// check foreign key
		ForeignKey fk_employee_company = getNamed(employeeTable.getForeignKeys(), "fk_employee_company", ForeignKey::getName);
		assertSame(companyTable, fk_employee_company.getReferencedTable());
		assertSame(employeeCompanyIdColumn, getNamed(fk_employee_company.getColumns(), "company_id", Column::getName));
		assertSame(companyIdColumn, getNamed(fk_employee_company.getReferencedColumns(), "id", Column::getName));
		assertEquals(ForeignKey.ModificationRule.NO_ACTION, fk_employee_company.getUpdateRule());
		assertEquals(ForeignKey.ModificationRule.NO_ACTION, fk_employee_company.getDeleteRule());
		assertTrue(fk_employee_company.isMandatory());
		assertTrue(fk_employee_company.isMany());
		
		// check index
		Index primary = getNamed(employeeTable.getIndices(), "PRIMARY", Index::getName);
		{
			Index idx = primary;
			assertSame(Index.Type.PRIMARY, idx.getIndexType());
			assertTrue(idx.isPrimary());
			assertFalse(idx.isUnique());
			assertSame(employeeIdColumn, idx.getColumns().get(0).getReferencedColumn());
		}
		
	}
	
	@Test
	public void testPrint() throws IOException {
		InputStream is = getClass().getClassLoader().getResourceAsStream("test_wb_8.mwb");
		printAllTableNamesAndColumns(is);
	}
	
	public void printAllTableNamesAndColumns(InputStream is) throws IOException {
		Document mwbDocument = Loader.loadMwb(is);
		
		// print tables of first schema in first physical model
		
		Schema schema = mwbDocument.getPhysicalModels().get(0).getCatalog().getSchemata().get(0);
		System.out.println("schema: " + schema.getName());
		
		for(Table t : schema.getTables()) {
			System.out.println("\ttable: " + t.getName());
			
			
			for(Column col : t.getColumns()) {
				System.out.println("\t\tcolumn: " + col.getName() + " (" + col.getSimpleType().getName() + ")");
			}
			for(Index idx : t.getIndices()) {
				System.out.print("\t\tindex: " + idx.getName() + " [ ");
				for(IndexColumn icol : idx.getColumns()) {
					System.out.print( icol.getReferencedColumn().getName() + " ");
				}
				System.out.println("]");
			}
			for(ForeignKey fk : t.getForeignKeys()) {
				
				System.out.print("\t\tforeign key: " + fk.getName() + "to " + fk.getReferencedTable().getName() + " [ ");
				for(int i=0; i<fk.getColumns().size(); ++i) {
					System.out.print(fk.getColumns().get(i).getName() + "=>" + fk.getReferencedColumns().get(i).getName());
				}
				System.out.println("]");;
			}

		}
	}
	private <T> T getNamed(List<T> list, String name, Function<T,String> nameFunction) {
		return list.stream().filter(e -> name.equals(nameFunction.apply(e))).findAny().orElseThrow(AssertionError::new);
	}
	private Column getColumn(Table t, String columnName) {
		return getNamed(t.getColumns(), columnName, Column::getName);
	}
	
	private Object lookup(String s) {
		return Loader.CONSTANTS.get(s);
	}
}
