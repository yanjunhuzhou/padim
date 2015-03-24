package com.example.padim.dbutils;

import java.util.List;

import android.util.Log;

import com.example.padim.PadIMApplication;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

public class PersonDao {

	private static PersonDao instance;
	RuntimeExceptionDao<Person, Integer> dao;

	private PersonDao() {
		dao = PadIMApplication.getInstance().getHelper().getPersonDao();
	}

	public static PersonDao getInstance() {
		if (instance == null) {
			instance = new PersonDao();
		}
		return instance;
	}

	public long createOrUpdate(Person person) {
		try {
			Person p = getPersonByUserId(person.getUserId());
			if (p != null) {
				p.userName = person.userName;
				p.ipAddress = person.ipAddress;
				p.loginTime = person.loginTime;
				p.onLine = person.onLine;
			} else {
				p = person;
			}
			dao.createOrUpdate(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 1;
	}

	public Person getPersonByUserId(String userId) {
		try {
			QueryBuilder<Person, Integer> qb = dao.queryBuilder();
			qb.where().eq("userId", userId);
			return qb.queryForFirst();
			// mDatabaseManager.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<Person> getAllPersons() {
		try {
			QueryBuilder<Person, Integer> qb = dao.queryBuilder();
			qb.orderBy("onLine", false);
			return qb.query();
			// mDatabaseManager.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<Person> getAllPersons(String me) {
		try {
			QueryBuilder<Person, Integer> qb = dao.queryBuilder();
			qb.where().ne("userId", me);
			qb.orderBy("onLine", false);
			return qb.query();
			// mDatabaseManager.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public List<Person> getOnLineAllPersons(String me) {
		try {
			QueryBuilder<Person, Integer> qb = dao.queryBuilder();
			qb.where().eq("onLine", new Boolean(true)).and().ne("userId", me);
			return qb.query();
			// mDatabaseManager.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
