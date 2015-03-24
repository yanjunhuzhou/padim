package com.example.padim.dbutils;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
	private static final String DATABASE_NAME = "padim_db";
	private static final int DATABASE_VERSION = 1;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	private RuntimeExceptionDao<MessageData, Integer> messageRuntimeDao = null;
	private RuntimeExceptionDao<Person, Integer> personRuntimeDao = null;

	@Override
	public void onCreate(SQLiteDatabase arg0, ConnectionSource arg1) {
		try {
			TableUtils.createTable(connectionSource, MessageData.class);
			TableUtils.createTable(connectionSource, Person.class);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't create database", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, ConnectionSource arg1, int arg2,
			int arg3) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onUpgrade");
			try {
				TableUtils.dropTable(connectionSource, MessageData.class, true);
				TableUtils.dropTable(connectionSource, Person.class, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao
	 * for our SimpleData class. It will create it or just give the cached
	 * value. RuntimeExceptionDao only through RuntimeExceptions.
	 */
	public RuntimeExceptionDao<MessageData, Integer> getMessageDataDao() {
		if (messageRuntimeDao == null) {
			messageRuntimeDao = getRuntimeExceptionDao(MessageData.class);
		}
		return messageRuntimeDao;
	}

	public RuntimeExceptionDao<Person, Integer> getPersonDao() {
		if (personRuntimeDao == null) {
			personRuntimeDao = getRuntimeExceptionDao(Person.class);
		}
		return personRuntimeDao;
	}

	@Override
	public void close() {
		super.close();
		messageRuntimeDao = null;
		personRuntimeDao = null;
	}
}
