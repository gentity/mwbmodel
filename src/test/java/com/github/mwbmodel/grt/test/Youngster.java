/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mwbmodel.grt.test;

import com.github.mwbmodel.grt.GrtObject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author count
 */
public class Youngster extends GrtObject{
	String name;
	Youngster bestFriend;
	List<Youngster> friends = new ArrayList<>();

	public String getName() {
		return name;
	}

	public Youngster getBestFriend() {
		return bestFriend;
	}

	public List<Youngster> getFriends() {
		return friends;
	}

	@Override
	public String toString() {
		return "Youngster{" + "name=" + name + '}';
	}
	
	
}
