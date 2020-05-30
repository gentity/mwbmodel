/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mwbmodel.grt;

import java.util.UUID;

/**
 * Superclass for all GRT classes. GRT serialization does only work for these.
 * @author Uwe Pachler
 */
public class GrtObject {
	private final UUID id;

	public GrtObject() {
		id = UUID.randomUUID();
	}
	
	public GrtObject(UUID id) {
		this.id = id;
	}
	
	
	public UUID getId() {
		return id;
	}
}
