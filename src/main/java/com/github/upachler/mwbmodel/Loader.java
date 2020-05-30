/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.upachler.mwbmodel;

import com.github.upachler.mwbmodel.grt.GrtObject;
import com.github.upachler.mwbmodel.util.LimitInputStream;
import com.github.upachler.mwbmodel.grt.GrtUnmarshaller;
import com.github.upachler.mwbmodel.grt.GrtUnmarshallerConfig;
import com.github.upachler.mwbmodel.model.db.DatatypeGroup;
import com.github.upachler.mwbmodel.model.db.DatatypeGroupBuilder;
import com.github.upachler.mwbmodel.model.db.SimpleDatatype;
import com.github.upachler.mwbmodel.model.db.SimpleDatatypeBuilder;
import com.github.upachler.mwbmodel.model.db.mgmt.Rdbms;
import com.github.upachler.mwbmodel.model.db.mgmt.RdbmsBuilder;
import com.github.upachler.mwbmodel.model.workbench.Document;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.List;

/**
 * The Façade loader class hiding the inner workings of the MWB loader mechanism.
 * @author Uwe Pachler
 */
public final class Loader {
	
	private Loader() {
	}
	
	/**
	 * Constants that the loader uses, like the predefined 
	 * column types, etc.
	 */
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
	
	private static SimpleDatatype mkNumeric(String name, int precision, int precisionRadix, int scale, List<String> synonyms) {
		return new SimpleDatatypeBuilder()
				.withName(name)
				.withGroup(DatatypeGroupKey.NUMERIC.getDatatypeGroup())
				.withNumericPrecision(precision)
				.withNumericPrecisionRadix(precisionRadix)
				.withNumericScale(scale)
				.withSynonyms(synonyms)
				.build();
	}
	
	private static SimpleDatatype mkString(String name, int maximumLength, List<String> synonyms) {
		return new SimpleDatatypeBuilder()
				.withName(name)
				.withGroup(DatatypeGroupKey.STRING.getDatatypeGroup())
				.withCharacterMaximumLength(maximumLength)
				.withSynonyms(synonyms)
				.build();
	}
	
	private static SimpleDatatype mkText(String name, int maximumLength, List<String> synonyms) {
		return new SimpleDatatypeBuilder()
				.withName(name)
				.withGroup(DatatypeGroupKey.TEXT.getDatatypeGroup())
				.withCharacterMaximumLength(maximumLength)
				.withSynonyms(synonyms)
				.build();
	}
	
	private static SimpleDatatype mkBlob(String name, int octetLength, List<String> synonums) {
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
	
	static List<String> syns(String... synonyms) {
		return Arrays.asList(synonyms);
	}
	
	static {
		
		// mysql datatypes:
		// https://github.com/mysql/mysql-workbench/blob/8.0/modules/db.mysql/res/mysql_rdbms_info.xml
		CONSTANTS = new HashMap<String,GrtObject>();
		
		CONSTANTS.put("com.mysql.rdbms.mysql", new RdbmsBuilder()
			.withName("mysql")
			.withCaption("MySQL")
			.build()
		);
		
		putSDT(CONSTANTS, mkNumeric("TINYINT",    3, 0, 0,  syns("BOOL", "BOOLEAN", "INT1")));
		putSDT(CONSTANTS, mkNumeric("SMALLINT",   5, 0, 0,  syns("INT2")));
		putSDT(CONSTANTS, mkNumeric("MEDIUMINT",  8, 0, 0,  syns("INT3", "MIDDLEINT")));
		putSDT(CONSTANTS, mkNumeric("INT",       10, 0, 0,  syns("INTEGER", "INT4")));
		putSDT(CONSTANTS, mkNumeric("BIGINT",    20, 0, 0,  syns("INT8", "SERIAL")));
		putSDT(CONSTANTS, mkNumeric("FLOAT",     53, 0, 30, syns("FLOAT4")));
		putSDT(CONSTANTS, mkNumeric("REAL",      53, 0, 30, syns()));
		putSDT(CONSTANTS, mkNumeric("DOUBLE",    53, 0, 30, syns("FLOAT8")));
		putSDT(CONSTANTS, mkNumeric("DECIMAL",   65, 0, 30, syns("FIXED", "NUMERIC", "DEC")));
		
		putSDT(CONSTANTS, mkString("CHAR",       255, syns("CHARACTER")));
		putSDT(CONSTANTS, mkString("NCHAR",      255, syns("NATIONAL CHAR", "NATIONAL CHARACTER")));
		putSDT(CONSTANTS, mkString("VARCHAR",  65535, syns("CHAR VARYING", "CHARACTER VARYING", "VARCHARACTER")));
		putSDT(CONSTANTS, mkString("NVARCHAR", 65535, syns("NATIONAL VARCHAR", "NATIONAL VARCHARACTER", "NCHAR VARCHAR", "NCHAR VARCHARACTER", "NATIONAL CHAR VARYING", "NATIONAL CHARACTER VARYING", "NCHAR VARYING")));
		
		putSDT(CONSTANTS, mkBlob("BINARY",    255, syns()));
		putSDT(CONSTANTS, mkBlob("VARBINARY", 255, syns()));
		
		putSDT(CONSTANTS, mkText("TINYTEXT",    255,   syns()));
		putSDT(CONSTANTS, mkText("TEXT",        65535, syns()));
		putSDT(CONSTANTS, mkText("MEDIUMTEXT", -24,    syns("LONG", "LONG VARCHAR", "LONG CHAR VARYING")));
		putSDT(CONSTANTS, mkText("LONGTEXT",   -32,    syns()));
		
		putSDT(CONSTANTS, mkBlob("TINYBLOB",   0,     syns()));
		putSDT(CONSTANTS, mkBlob("BLOB",       65535, syns()));
		putSDT(CONSTANTS, mkBlob("MEDIUMBLOB", 0,     syns("LONG VARBINARY")));
		putSDT(CONSTANTS, mkBlob("LONGBLOB",   0,     syns()));
		
		putSDT(CONSTANTS, mkString("JSON", 0, syns()));
		
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
				.withSynonyms(Collections.singletonList("BOOL"))
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
	
	/**
	 * Loads the MWB file from the provided input stream and returns
	 * the root {@link Document} object, which holds all the data.
	 * @param is
	 * @return	a {@link Document} object containing the MySQL Workbench 
	 *	document loaded from the input stream.
	 * @throws IOException 
	 */
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
		
		Package basePackage = com.github.upachler.mwbmodel.model.R.class.getPackage();
		
		
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
