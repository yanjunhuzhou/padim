/**
 * 
 */
package com.example.padim.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.padim.dbutils.Person;
import com.example.padim.util.Constant;
import com.example.padim.util.ImReceiver;

/**
 * 聊天界面广播接收器
 * 
 * @author zhuft
 * @version 1.0 2011-11-6
 */
public class ChartMsgBroadcastRecv extends BroadcastReceiver {

	private ImReceiver receiver;

	public ChartMsgBroadcastRecv(ImReceiver receiver) {
		super();
		this.receiver = receiver;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		try {

			if (intent.getAction().equals(Constant.dataReceiveErrorAction)
					|| intent.getAction().equals(Constant.dataSendErrorAction)) {
				receiver.onReceive(intent.getAction(), intent.getExtras()
						.getString("msg"));
			} else if (intent.getExtras() != null) {
				receiver.onReceive(intent.getAction(), (Person) intent
						.getExtras().get("person"));
			} else {
				receiver.onReceive(intent.getAction(), null);
			}

			// if (intent.getAction().equals(Constant.hasMsgUpdatedAction)) {
			// activity.showMsg(activity.person.personId);
			// } else if (intent.getAction().equals(
			// Constant.dataReceiveErrorAction)
			// || intent.getAction().equals(Constant.dataSendErrorAction)) {
			// Toast.makeText(activity, intent.getExtras().getString("msg"),
			// Toast.LENGTH_SHORT).show();
			// } else if (intent.getAction().equals(
			// Constant.fileSendStateUpdateAction)) {// 收到来自服务层的文件发送状态通知
			// activity.updateFileSendState();
			// } else if (intent.getAction().equals(
			// Constant.fileReceiveStateUpdateAction)) {// 收到来自服务层的文件接收状态通知
			// activity.updateFileReceiveState();
			// } else if (intent.getAction().equals(
			// Constant.receivedTalkRequestAction)) {
			// final Person psn = (Person) intent.getExtras().get("person");
			// activity.showReceivedTalkRequestDialog(psn);
			// } else if (intent.getAction().equals(
			// Constant.remoteUserClosedTalkAction)) {
			// activity.isRemoteUserClosed = true;// 如果接收到远程用户关闭通话指令则把该标记置为true
			// Person psn = (Person) intent.getExtras().get("person");
			// activity.showToast("[" + psn.personNickeName + "]关闭与你的通话了...");
			// } else if (intent.getAction().equals(
			// Constant.remoteUserRefuseReceiveFileAction)) {
			// Toast.makeText(activity,
			// activity.getString(R.string.refuse_receive_file),
			// Toast.LENGTH_SHORT).show();
			// } else if (intent.getAction().equals(
			// Constant.receivedSendFileRequestAction)) {
			// activity.showReceivedSendFileRequestDialog();
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
