package com.example.padim.dbutils;

import java.io.Serializable;

import com.example.padim.R;
import com.j256.ormlite.field.DatabaseField;

public class Person implements Serializable {
    private static final long serialVersionUID = 1L;

    @DatabaseField(generatedId = true)
    int id;
    // 用户id
    @DatabaseField
    public String userId;
    // 头像资源id
    @DatabaseField
    private int headIconId = 0;
    // 用户名字
    @DatabaseField
    public String userName = null;
    // ip地址
    @DatabaseField
    public String ipAddress = null;
    @DatabaseField
    public long loginTime;

    @DatabaseField
    public boolean onLine;

    public boolean isOnLine() {
        return onLine;
    }

    public void setOnLine(boolean onLine) {
        this.onLine = onLine;
    }

    public Person(int userId, int headIconResId, String userName, String ipAddress, long loginTime) {
        this.userId = userId + "";
        this.headIconId = headIconResId;
        this.userName = userName;
        this.ipAddress = ipAddress;
        this.loginTime = loginTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public int getIntUserId() {
        return Integer.valueOf(userId);
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getHeadIconId() {
        if (headIconId == 3) {
            return R.drawable.pic_online_male;
        }
        return R.drawable.pic_online_male;
    }

    public void setHeadIconId(int headIconId) {
        this.headIconId = headIconId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(long loginTime) {
        this.loginTime = loginTime;
    }

    public Person() {
    }
}
