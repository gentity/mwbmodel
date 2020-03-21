/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mwbmodel;

import com.github.mwbmodel.grt.GrtObject;
import com.github.mwbmodel.util.LimitInputStream;
import com.github.mwbmodel.grt.GrtUnmarshaller;
import com.github.mwbmodel.grt.GrtUnmarshallerConfig;
import com.github.mwbmodel.model.db.SimpleDatatype;
import com.github.mwbmodel.model.workbench.Document;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * The Façade loader class hiding the inner workings of the MWB loader mechanism.
 * @author count
 */
public final class Loader {
	
	private Loader() {
	}
	
	public static Map<String,GrtObject> CONSTANTS;
	
	
	static {
		// common type groups
		// https://github.com/mysql/mysql-workbench/blob/8.0/res/grtdata/db_datatype_groups.xml
		
		// mysql datatypes:
		// https://github.com/mysql/mysql-workbench/blob/8.0/modules/db.mysql/res/mysql_rdbms_info.xml
		
		CONSTANTS = new HashMap<String,GrtObject>(){{
			put("com.mysql.rdbms.mysql.datatype.tinyint", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.smallint", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.mediumint", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.int", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.bigint", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.float", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.double", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.decimal", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.char", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.varchar", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.binary", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.varbinary", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.tinytext", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.text", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.mediumtext", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.longtext", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.tinyblob", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.blob", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.mediumblob", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.longblob", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.datetime", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.datetime_f", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.date", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.time", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.time_f", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.year", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.timestamp", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.timestamp_f", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.geometry", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.point", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.real", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.nchar", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.nvarchar", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.json", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.linestring", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.polygon", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.geometrycollection", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.multipoint", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.multilinestring", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.multipolygon", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.bit", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.boolean", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.enum", new SimpleDatatype());
			put("com.mysql.rdbms.mysql.datatype.set", new SimpleDatatype());
		}};
	}
	
	public static Document loadMwb(InputStream is) throws IOException {
		ZipInputStream zis = new ZipInputStream(is);
		for(ZipEntry e=zis.getNextEntry(); e!=null; e=zis.getNextEntry()) {
			if(e.getName().equals("document.mwb.xml")) {
				long size = e.getSize();
				InputStream subInputStream = new LimitInputStream(zis, size, true);
				return loadDocumentMwbXml(subInputStream);
			}
		}
		throw new IOException("no document.mwb.xml file present in this ZIP file - is this really a MySQL Workbench file?");
	}
	
	private static Document loadDocumentMwbXml(InputStream is) throws IOException {
		
		Package basePackage = com.github.mwbmodel.model.R.class.getPackage();
		
		
		GrtUnmarshallerConfig cfg = new GrtUnmarshallerConfig(Loader.class.getClassLoader(), CONSTANTS, basePackage);
		cfg.setIgnoringMissingClasses(true);
		cfg.setIgnoringMissingFields(true);
		
		GrtUnmarshaller u = new GrtUnmarshaller(cfg);
		try {
			return (Document)u.unmarshal(is);
		} catch (ClassNotFoundException ex) {
			throw new RuntimeException(ex);
		}
	}
	
}
