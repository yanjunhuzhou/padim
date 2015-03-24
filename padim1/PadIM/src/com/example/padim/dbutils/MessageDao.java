package com.example.padim.dbutils;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.example.padim.PadIMApplication;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

public class MessageDao {

	private static MessageDao instance;
	RuntimeExceptionDao<MessageData, Integer> dao;

	private MessageDao() {
		dao = PadIMApplication.getInstance().getHelper().getMessageDataDao();
	}

	public static MessageDao getInstance() {
		if (instance == null) {
			instance = new MessageDao();
		}
		return instance;
	}

	public long createOrUpdate(MessageData messageVO) {
		try {
			dao.createOrUpdate(messageVO);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 1;
	}

	public long delete(MessageData messageVO) {
		try {
			Log.i("msg","hao"+messageVO.getMsgPersons());
			QueryBuilder<MessageData, Integer> qb = dao.queryBuilder();
			qb.where().eq("msgPersons", messageVO.getMsgPersons());
			List<MessageData> deleteData = qb.query();
			dao.delete(deleteData);
			Log.i("msg","delete succuss");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 1;
	}
	
	public List<MessageData> getMessages(String msgPersons) {
		try {
			QueryBuilder<MessageData, Integer> qb = dao.queryBuilder();
			qb.where().eq("msgPersons", msgPersons);
			qb.orderBy("time", false);
			qb.limit(20);
			return qb.query();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	

	
	public List<MessageData> getHisMessagesGroupByHF() {
		try {
			QueryBuilder<MessageData, Integer> qb = dao.queryBuilder();
		//	qb.where().eq("msgPersons", msgPersons);
			qb.groupBy("msgPersons");
			qb.orderBy("time", false); 
			return qb.query();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<MessageData> getHisMessagesGroupByHF(String msgPersons) {
		try {
			QueryBuilder<MessageData, Integer> qb = dao.queryBuilder();
			List<MessageData> list = new ArrayList<MessageData>();
			List<MessageData> resultlist = new ArrayList<MessageData>();
		//	qb.where().eq("msgPersons", msgPersons);
			qb.groupBy("msgPersons");
			qb.orderBy("time", false); 
			list = qb.query();
			
			for(int i=0;i<list.size();i++) {
				if(!msgPersons.equals(list.get(i).msgPersons) ) {
					resultlist.add(list.get(i));
				}
			}
			return resultlist;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<MessageData> getSpecialHisMessagesGroupByHF(String msgPersons) {
		try {
			QueryBuilder<MessageData, Integer> qb = dao.queryBuilder();
			List<MessageData> list = new ArrayList<MessageData>();
			List<MessageData> resultlist = new ArrayList<MessageData>();
		//	qb.where().eq("msgPersons", msgPersons);
			qb.groupBy("msgPersons");
			qb.orderBy("time", false); 
			list = qb.query();
			for(int i=0;i<list.size();i++) {
				if(msgPersons.equals(list.get(i).msgPersons)) {
					resultlist.add(list.get(i));
				}
			}
			return resultlist;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	
}
