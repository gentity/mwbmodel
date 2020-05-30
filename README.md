# mwbmodel

A library for reading MySQL Workbench (*.mwb) files in Java. 

Comes with a (currently partial) [GRT](https://dev.mysql.com/doc/workbench/en/wb-grt-data-organization.html)-based object model for MySQL Workbench models.

To load a workbench file, simply open an InputStream and pass it to the loader. An example method to print some structures from a workbench file looks like this:

```java
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

```

For a database schema model associating companies and employees, the output of the example above could look like this:

```
schema: mydb
	table: company
		column: id (INT)
		column: name (VARCHAR)
		index: PRIMARY [ id ]
	table: employee
		column: id (INT)
		column: name (VARCHAR)
		column: company_id (INT)
		index: PRIMARY [ id ]
		index: fk_employee_company_idx [ company_id ]
		foreign key: fk_employee_companyto company [ company_id=>id]

```

## About MySQL Workbench files and how mwbmodel loads them

MySQL Workbench's object model is based on an internal system called [GRT](https://dev.mysql.com/doc/workbench/en/wb-grt-data-organization.html). MySQL Workbench files are actually zip archives, containing a couple of binary files and a `document.mwb.xml` file at the top level. This is the file that `mwbmodel` will actually read, the other files in the MWB zip archive are currently ignored.

The GRT implements MySQL Workbench's object model, which is used internally and for its (Python-)plugins. The `document.mwb.xml` file in a MWB archive is a serialization of the GRT runtime objects used to represent a database model. So the file is actually an object graph, which references to objects inside the database model, as well as to objects 'outside' that model. For instance, while column types like bigint, varchar etc. are *referenced* from a database model, there are *not defined* there. Rather, these objects are *predefined* in the MySQL Workbench runtime. Because these objects are not present in the MWB archive, `mwbmodel` also needs to have the definitions of these objects available, so they are built into `mwbmodel` as well - at least a subset that is needed to load and interpret the database structures inside a MWB archive.

The GRT unmarshaller inside `mwbmodel` is built around reflection on Java classes representing GRT objects ('structs'). The type names convenently translate to java class names (FQCNs), which allows a direct mapping from type references in a GRT file. So in essence, the GRT unmarshaller loads key value pairs for objects from the XML file and maps them onto java classes, much like other deserialization technologies (e.g. JAXB).
