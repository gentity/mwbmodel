# mwbmodel - a MySQL Workbench Model Library

This library allows to load MySQL Workbench (*.mwb) files in Java. 
Comes with a (currently partial) GRT-based object model for MySQL Workbench models.

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
