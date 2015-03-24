
package com.example.padim.beans;

/**   
*    
* 项目名称：PadIM   
* 类名称：ChatMsgEntity   
* 类描述：信息对象   
* 创建人：ystgx   
* 创建时间：2014年4月23日 下午2:34:45   
* @version        
*/
public class ChatMsgEntity {
    private static final String TAG = ChatMsgEntity.class.getSimpleName();

    private String name;

    private long time;
    private String text;

    private boolean isComMeg = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean getIsComMsg() {
        return isComMeg;
    }

    public void setIsComMsg(boolean isComMsg) {
    	isComMeg = isComMsg;
    }

    public ChatMsgEntity() {
    }

    public ChatMsgEntity(String name, String text, boolean isComMsg,long time) {
        super();
        this.name = name;
        this.text = text;
        this.isComMeg = isComMsg;
        this.time = time;
    }

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

}
