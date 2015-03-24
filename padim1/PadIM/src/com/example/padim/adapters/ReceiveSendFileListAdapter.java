package com.example.padim.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.padim.PadIMApplication;
import com.example.padim.R;
import com.example.padim.model.FileState;
import com.example.padim.util.Constant;
import com.example.padim.util.PadImUtil;

/**
 * =接收文件对话框列表适配器
 * 
 * @author zhuft
 * @version 1.0 2011-11-6
 */
public class ReceiveSendFileListAdapter extends BaseAdapter {
	private ArrayList<FileState> receivedFileNames = null;// 接收到的对方传过来的文件名
	private Context context = null;

	public ReceiveSendFileListAdapter(Context context) {
		this.context = context;
	}

	@Override
	public int getCount() {
		return receivedFileNames.size();
	}

	@Override
	public Object getItem(int position) {
		return receivedFileNames.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View vi = inflater.inflate(R.layout.send_receive_file_layout, null);
		ImageView fileIcon = (ImageView) vi.findViewById(R.id.file_icon);
		TextView fileName = (TextView) vi.findViewById(R.id.file_name);
		TextView fileSize = (TextView) vi.findViewById(R.id.file_size);
		ProgressBar bar = (ProgressBar) vi.findViewById(R.id.file_progress);
		TextView filePercent = (TextView) vi.findViewById(R.id.file_percent);

		FileState fs = receivedFileNames.get(position);// 获得一个文件状态信息
		String ext = fs.fileName.substring(fs.fileName.lastIndexOf(".") + 1);
		Integer srcId = Constant.exts.get(ext);
		if (null == srcId)
			srcId = R.drawable.gdoc;
		fileIcon.setImageResource(srcId);
		fileName.setText(fs.fileName);
		fileSize.setText(PadImUtil.formatFileSize(fs.fileSize));
		bar.setMax(100);
		bar.setProgress(fs.percent);
		filePercent.setText(PadImUtil.formatFileSize(fs.currentSize) + "/"
				+ PadImUtil.formatFileSize(fs.fileSize));
		if (fs.percent == 100){
			filePercent.setTextColor(Color.GREEN);
		}
		return vi;
	}

	public void setResources(ArrayList<FileState> receivedFileNames) {
		this.receivedFileNames = receivedFileNames;
	}
}
