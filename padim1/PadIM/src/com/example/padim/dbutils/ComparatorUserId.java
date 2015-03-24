package com.example.padim.dbutils;

import java.util.Comparator;

public class ComparatorUserId implements Comparator<String> {

	@Override
	public int compare(String object1, String object2) {
		return object1.compareTo(object2);
	}

}
