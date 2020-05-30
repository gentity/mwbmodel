/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * The MySQL Workbench GRT metamodel. 
 * MySQL relies on a so-called Generic RunTime (GRT) as its basic object model.
 * Besides other things, the GRT is responsible for (de-/)serializing the
 * in-memory workbench datastructures.
 * 
 * The GRT XML model itself is fully generic and supports serialization of any
 * type that the workbench throws at it. This is visible in the serialized XML,
 * where structs are merely key/value pairs, annotated with type hints.
 * 
 * This java implementation of the GRT serialization mechanism mirrors that 
 * behaviour; it generically allows deserializing to arbitrary Java classes.
 * 
 * See the original documentation of the GRT here in the MySQL Workbench Manual in
 * section 
 * <a href='https://dev.mysql.com/doc/workbench/en/wb-grt-data-organization.html'>C.1 GRT and Workbench Data Organization</a>
 */
package com.github.upachler.mwbmodel.grt;
