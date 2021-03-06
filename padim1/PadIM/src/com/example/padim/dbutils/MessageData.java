package com.example.padim.dbutils;

import com.j256.ormlite.field.DatabaseField;

public class MessageData {

	public MessageData() {
		this.time = System.currentTimeMillis();
		this.unRead = true;
	}

	// id is generated by the database and set on the object automagically
	// android数据库id
	@DatabaseField(generatedId = true)
	int id;

	// 时间
	@DatabaseField
	long time;

	// 内容
	@DatabaseField
	String content;

	// 信息发送者
	@DatabaseField
	String from;

	// 信息接受者，多个人用逗号分隔
	@DatabaseField
	String to;

	@DatabaseField
	boolean unRead;

	// 会话的参与者，多个人用逗号分隔
	@DatabaseField
	String msgPersons;
 
	//为音频消息特设
	@DatabaseField
	boolean play;   //false 时可播放音乐
	
	public boolean isPlay() {
		return play;
	}



	public void setPlay(boolean play) {
		this.play = play;
	}



	public boolean isUnRead() {
		return unRead;
	}

	
	
	public void setUnRead(boolean unRead) {
		this.unRead = unRead;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getMsgPersons() {
		return msgPersons;
	}

	public void setMsgPersons(String msgPersons) {
		this.msgPersons = msgPersons;
	}

}
