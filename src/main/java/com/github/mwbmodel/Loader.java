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
import com.github.mwbmodel.model.db.DatatypeGroup;
import com.github.mwbmodel.model.db.DatatypeGroupBuilder;
import com.github.mwbmodel.model.db.SimpleDatatype;
import com.github.mwbmodel.model.db.SimpleDatatypeBuilder;
import com.github.mwbmodel.model.workbench.Document;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import static com.github.mwbmodel.model.db.SimpleDatatype.Flag;

/**
 * The Façade loader class hiding the inner workings of the MWB loader mechanism.
 * @author count
 */
public final class Loader {
	
	private Loader() {
	}
	
	public static final Map<String,GrtObject> CONSTANTS;
	
	
	// common type groups, mapped here to enums so that they're easily 
	// accessible from client code
	// https://github.com/mysql/mysql-workbench/blob/8.0/res/grtdata/db_datatype_groups.xml
	public enum DatatypeGroupKey {
		NUMERIC("Numeric Types", "Datatypes to store numbers of different size"),
		STRING("Strings", "Datatypes to store shorter text"),
		TEXT("Long Text Types", "Datatypes to store long text"),
		BLOB("Blob Types", "Datatypes to store binary data"),
		DATETIME("Date and Time Types", "Datatypes to store date and time values"),
		GIS("Geographical Types", "Datatypes to store geographical information"),
		VARIOUS("Various Types", "Various datatypes"),
		USERDEFINED("Userdefined Types", "Datatypes defined by a user"),
		STRUCTURED("Structured Types", "Structured datatypes consisting of a collection of simple and other structured datatypes"),
		;

		private final DatatypeGroup datatypeGroup;
		
		private DatatypeGroupKey(String caption, String description) {
			datatypeGroup = new DatatypeGroupBuilder()
					.withName(name().toLowerCase(Locale.ROOT))
					.withCaption(caption)
					.withDescription(description)
					.build();
		}
		
		public DatatypeGroup getDatatypeGroup() {
			return datatypeGroup;
		}
		
		public static DatatypeGroupKey valueOf(DatatypeGroup datatypeGroup) {
			for(DatatypeGroupKey key : values()) {
				if(key.datatypeGroup == datatypeGroup) {
					return key;
				}
			}
			throw new IllegalArgumentException("could not find DatatypeGroup because this instance is not part of the predefined groups");
		}
	}
	
	private static SimpleDatatype mkNumeric(String name, int precision, int precisionRadix, int scale) {
		return new SimpleDatatypeBuilder()
				.withName(name)
				.withGroup(DatatypeGroupKey.NUMERIC.getDatatypeGroup())
				.withNumericPrecision(precision)
				.withNumericPrecisionRadix(precisionRadix)
				.withNumericScale(scale)
				.build();
	}
	
	private static SimpleDatatype mkString(String name, int maximumLength) {
		return new SimpleDatatypeBuilder()
				.withName(name)
				.withGroup(DatatypeGroupKey.STRING.getDatatypeGroup())
				.withCharacterMaximumLength(maximumLength)
				.build();
	}
	
	private static SimpleDatatype mkText(String name, int maximumLength) {
		return new SimpleDatatypeBuilder()
				.withName(name)
				.withGroup(DatatypeGroupKey.TEXT.getDatatypeGroup())
				.withCharacterMaximumLength(maximumLength)
				.build();
	}
	
	private static SimpleDatatype mkBlob(String name, int octetLength) {
		return new SimpleDatatypeBuilder()
				.withName(name)
				.withGroup(DatatypeGroupKey.BLOB.getDatatypeGroup())
				.withCharacterOctetLength(octetLength)
				.build();
	}
	
	private static SimpleDatatype mkDatetime(String name, int precision) {
		return new SimpleDatatypeBuilder()
				.withName(name)
				.withGroup(DatatypeGroupKey.BLOB.getDatatypeGroup())
				.withDateTimePrecision(precision)
				.build();
	}
	
	private static SimpleDatatype mkGis(String name) {
		return new SimpleDatatypeBuilder()
				.withName(name)
				.withGroup(DatatypeGroupKey.BLOB.getDatatypeGroup())
				.build();
	}
	
	private static void putSDT(Map<String,GrtObject> constants, SimpleDatatype sdt) {
		putSDT(constants, sdt.getName().toLowerCase(Locale.ROOT), sdt);
	}
	
	private static void putSDT(Map<String,GrtObject> constants, String simpleTypeId, SimpleDatatype sdt) {
		constants.put("com.mysql.rdbms.mysql.datatype."+simpleTypeId, sdt);
	}
	
	static {
		
		// mysql datatypes:
		// https://github.com/mysql/mysql-workbench/blob/8.0/modules/db.mysql/res/mysql_rdbms_info.xml
		CONSTANTS = new HashMap<String,GrtObject>(){{
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

		putSDT(CONSTANTS, mkNumeric("TINYINT",    3, 0, 0));
		putSDT(CONSTANTS, mkNumeric("SMALLINT",   5, 0, 0));
		putSDT(CONSTANTS, mkNumeric("MEDIUMINT",  8, 0, 0));
		putSDT(CONSTANTS, mkNumeric("INT",       10, 0, 0));
		putSDT(CONSTANTS, mkNumeric("BIGINT",    20, 0, 0));
		putSDT(CONSTANTS, mkNumeric("FLOAT",     53, 0, 30));
		putSDT(CONSTANTS, mkNumeric("REAL",      53, 0, 30));
		putSDT(CONSTANTS, mkNumeric("DOUBLE",    53, 0, 30));
		putSDT(CONSTANTS, mkNumeric("DECIMAL",   65, 0, 30));
		
		putSDT(CONSTANTS, mkString("CHAR",       255));
		putSDT(CONSTANTS, mkString("NCHAR",      255));
		putSDT(CONSTANTS, mkString("VARCHAR",  65535));
		putSDT(CONSTANTS, mkString("NVARCHAR", 65535));
		
		putSDT(CONSTANTS, mkBlob("BINARY",    255));
		putSDT(CONSTANTS, mkBlob("VARBINARY", 255));
		
		putSDT(CONSTANTS, mkText("TINYTEXT", 255));
		putSDT(CONSTANTS, mkText("TEXT", 65535));
		putSDT(CONSTANTS, mkText("MEDIUMTEXT", -24));
		putSDT(CONSTANTS, mkText("LONGTEXT", -32));
		
		putSDT(CONSTANTS, mkBlob("TINYBLOB", 0));
		putSDT(CONSTANTS, mkBlob("BLOB", 65535));
		putSDT(CONSTANTS, mkBlob("MEDIUMBLOB", 0));
		putSDT(CONSTANTS, mkBlob("LONGBLOB", 0));
		
		putSDT(CONSTANTS, mkString("JSON", 0));
		
		putSDT(CONSTANTS,                mkDatetime("DATETIME", 8));
		putSDT(CONSTANTS, "datetime_f",  mkDatetime("DATETIME", 8));
		putSDT(CONSTANTS,                mkDatetime("DATE", 0));
		putSDT(CONSTANTS,                mkDatetime("TIME", 3));
		putSDT(CONSTANTS, "time_f",      mkDatetime("TIME", 3));
		putSDT(CONSTANTS,                mkDatetime("YEAR", 1));
		putSDT(CONSTANTS,                mkDatetime("TIMESTAMP", 4));
		putSDT(CONSTANTS, "timestamp_f", mkDatetime("TIMESTAMP", 4));
		
		putSDT(CONSTANTS, mkGis("GEOMETRY"));
		putSDT(CONSTANTS, mkGis("POINT"));
		putSDT(CONSTANTS, mkGis("LINESTRING"));
		putSDT(CONSTANTS, mkGis("POLYGON"));
		putSDT(CONSTANTS, mkGis("GEOMETRYCOLLECTION"));
		putSDT(CONSTANTS, mkGis("MULTIPOINT"));
		putSDT(CONSTANTS, mkGis("MULTILINESTRING"));
		putSDT(CONSTANTS, mkGis("MULTIPOLYGON"));
		
		putSDT(CONSTANTS, new SimpleDatatypeBuilder()
				.withName("BIT")
				.withGroup(DatatypeGroupKey.VARIOUS.getDatatypeGroup())
				.withNumericPrecision(2)
				.build()
		);
		putSDT(CONSTANTS, new SimpleDatatypeBuilder()
				.withName("BOOLEAN")
				.withGroup(DatatypeGroupKey.VARIOUS.getDatatypeGroup())
				.withNumericPrecision(2)
				.build()
		);
		putSDT(CONSTANTS, new SimpleDatatypeBuilder()
				.withName("ENUM")
				.withGroup(DatatypeGroupKey.VARIOUS.getDatatypeGroup())
				.build()
		);
		putSDT(CONSTANTS, new SimpleDatatypeBuilder()
				.withName("SET")
				.withGroup(DatatypeGroupKey.VARIOUS.getDatatypeGroup())
				.build()
		);
		
		for(DatatypeGroupKey key : DatatypeGroupKey.values()) {
			CONSTANTS.put("com.mysql.rdbms.common.typegroup."+key.name(), key.getDatatypeGroup());
		}
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
