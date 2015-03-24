package com.example.padim.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.padim.R;
import com.example.padim.util.PadImUtil;

public class ActionBar extends RelativeLayout {
    LayoutInflater mInflater;

    private View mBarView;

    TextView titleTv;
    TextView titleTvWhenHasLeftBtn;// 左侧有按钮时，的标题，因为动态居中不好做，因此多一套布局

    ImageView titleFlag;
    ImageView actionbar_title_flag2;

    View top_center2;
    View topCenterLayout;

    LinearLayout leftContainer;
    LinearLayout rightContainer;
    int btnWidth = 54;
    int padWidth = 2;
    Context context;

    public LinearLayout getLeftContainer() {
        return leftContainer;
    }

    public void setLeftContainer(LinearLayout leftContainer) {
        this.leftContainer = leftContainer;
    }

    public LinearLayout getRightContainer() {
        return rightContainer;
    }

    public void setRightContainer(LinearLayout rightContainer) {
        this.rightContainer = rightContainer;
    }

    public ActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        btnWidth = PadImUtil.dip2px(context, 54);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mBarView = mInflater.inflate(R.layout.activity_top_bar, null);
        mBarView.setBackgroundColor(getResources().getColor(R.color.orange_top_color));
        ViewGroup.LayoutParams LP = new ViewGroup.LayoutParams(LayoutParams.FILL_PARENT,
                (int) context.getResources().getDimension(R.dimen.actionbar_height));
        addView(mBarView, LP);

        titleTv = (TextView) mBarView.findViewById(R.id.actionbartitle);
        titleTvWhenHasLeftBtn = (TextView) mBarView.findViewById(R.id.actionbartitle2);
        titleFlag = (ImageView) mBarView.findViewById(R.id.actionbar_title_flag);
        top_center2 = mBarView.findViewById(R.id.top_center2);
        actionbar_title_flag2 = (ImageView) mBarView.findViewById(R.id.actionbar_title_flag2);
        topCenterLayout = mBarView.findViewById(R.id.top_center);
        leftContainer = (LinearLayout) mBarView.findViewById(R.id.leftcontainer);
        rightContainer = (LinearLayout) mBarView.findViewById(R.id.rightcontainer);
        padWidth = PadImUtil.dip2px(context, 2);

        /*
         * if (context instanceof LotteryHallActivity) { setTitleGravity(); }
         */
    }

    public View getBarView() {
        return mBarView;
    }

    public void setTitle(String text) {
        titleTv.setText(text);
        titleTvWhenHasLeftBtn.setText(text);
    }
    public void setTitle(String text,int color) {
        titleTv.setText(text);
        titleTvWhenHasLeftBtn.setText(text);
        titleTv.setTextColor(color);
        titleTvWhenHasLeftBtn.setTextColor(color);
    }
    public void setTitle(int text) {
        titleTv.setText(text);
        titleTvWhenHasLeftBtn.setText(text);
    }

    public TextView getTitleTv() {
        return titleTv;
    }

    public TextView getTitleTv2() {
        return titleTvWhenHasLeftBtn;
    }

    public View getTopCenterLayout() {
        return topCenterLayout;
    }

    public ImageView getFlagView() {
        return top_center2.getVisibility() == View.VISIBLE ? actionbar_title_flag2 : titleFlag;
    }

    public void addLeft(String text, View.OnClickListener listener) {
        LinearLayout liner = new LinearLayout(context);
        liner.setOrientation(LinearLayout.HORIZONTAL);
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(18);
        tv.setTextColor(Color.WHITE);
        tv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LayoutParams.FILL_PARENT);
        lp.weight = 1;
        liner.addView(tv, lp);
        lp = new LinearLayout.LayoutParams(padWidth, LayoutParams.FILL_PARENT);
        TextView padline = new TextView(context);

        liner.addView(padline, lp);
        liner.setOnClickListener(listener);
        padline.setBackgroundResource(R.drawable.shuxianpading);
        liner.setBackgroundResource(R.drawable.actionbar_btn_states);

        lp = new LinearLayout.LayoutParams(btnWidth, LayoutParams.FILL_PARENT);
//        System.out.println("addleft btnWidth="+btnWidth);
        lp.gravity = Gravity.CENTER;
        leftContainer.addView(liner, lp);

        setTitleGravity();
    }

    public void addLeftIcon(int res, View.OnClickListener listener) {
        LinearLayout liner = new LinearLayout(context);
        liner.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp;
        LinearLayout temp = new LinearLayout(context);
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(res);
        lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        temp.addView(imageView, lp);
        lp = new LinearLayout.LayoutParams(0, LayoutParams.FILL_PARENT);
        lp.weight = 1;
        lp.gravity = Gravity.CENTER;
        liner.addView(temp, lp);
        lp = new LinearLayout.LayoutParams(padWidth, LayoutParams.FILL_PARENT);
        View padline = new View(context);

        liner.addView(padline, lp);

        if (lineRes > 0) {
            padline.setBackgroundResource(R.drawable.shuxianpading);
            liner.setBackgroundResource(R.drawable.actionbar_btn_states);
        }
        lp = new LinearLayout.LayoutParams(btnWidth, LayoutParams.FILL_PARENT);
//        System.out.println("addLeftIcon btnWidth="+btnWidth);
        lp.gravity = Gravity.CENTER;

        liner.setOnClickListener(listener);
        leftContainer.addView(liner, lp);
        leftImages.add(imageView);

        setTitleGravity();
    }

    public void addRightIconGravityTop(int res, View.OnClickListener listener) {
        LinearLayout liner = new LinearLayout(context);
        liner.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(padWidth,
                LayoutParams.FILL_PARENT);
        // View padline = new View(context);
        // padline.setBackgroundResource(R.drawable.shuxianpading);
        // liner.addView(padline, lp);
        LinearLayout temp = new LinearLayout(context);
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(res);
        lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        temp.addView(imageView, lp);
        lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

        lp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        liner.addView(temp, lp);
        lp = new LinearLayout.LayoutParams(btnWidth, LayoutParams.FILL_PARENT);
        lp.gravity = Gravity.CENTER;
        liner.setBackgroundResource(R.drawable.actionbar_btn_states);
        liner.setOnClickListener(listener);
        rightContainer.addView(liner, lp);
        rightImages.add(imageView);
        rightLiner.add(liner);
    }

    public void addRightIcon(int res, View.OnClickListener listener) {
        addRightIcon(res, R.drawable.shuxianpading, listener);
    }

    int lineRes;

    public void addRightIcon(int res, int lineRes, View.OnClickListener listener) {
        this.lineRes = lineRes;
        LinearLayout liner = new LinearLayout(context);
        liner.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(padWidth,
                LayoutParams.FILL_PARENT);
        View padline = new View(context);
        if (lineRes > 0) {
            padline.setBackgroundResource(lineRes);
        }
        liner.addView(padline, lp);
        LinearLayout temp = new LinearLayout(context);
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(res);
        lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        temp.addView(imageView, lp);
        lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

        lp.gravity = Gravity.CENTER;
        liner.addView(temp, lp);
        lp = new LinearLayout.LayoutParams(btnWidth, LayoutParams.FILL_PARENT);
        lp.gravity = Gravity.CENTER;
        if (lineRes > 0) {
            liner.setBackgroundResource(R.drawable.actionbar_btn_states);
        }
        liner.setOnClickListener(listener);
        rightContainer.addView(liner, lp);
        rightImages.add(imageView);
        rightLiner.add(liner);
        // setTitleGravity();
    }

    public void addRight(String text, View.OnClickListener listener) {
        LinearLayout liner = new LinearLayout(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(padWidth,
                LayoutParams.FILL_PARENT);
        View padline = new View(context);
        padline.setBackgroundResource(R.drawable.shuxianpading);
        liner.addView(padline, lp);
        TextView tv = new TextView(context);
//        tv.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));//加粗
        tv.setText(text);
        tv.setGravity(Gravity.CENTER);
//        tv.setTextSize(18);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        tv.setBackgroundResource(R.drawable.actionbar_btn_states);
        lp = new LinearLayout.LayoutParams(0, LayoutParams.FILL_PARENT);
        lp.weight = 1;
        liner.addView(tv, lp);
        liner.setOnClickListener(listener);
        liner.setBackgroundResource(R.drawable.actionbar_btn_states);
        lp = new LinearLayout.LayoutParams(btnWidth, LayoutParams.FILL_PARENT);
        lp.gravity = Gravity.CENTER;
        rightContainer.addView(liner, lp);

        // setTitleGravity();
    }

    private void setTitleGravity() {
        // if (rightContainer.getChildCount() > 0
        // && leftContainer.getChildCount() == 0) {
        // LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
        // LayoutParams.FILL_PARENT);
        // lp.weight = 1;
        // lp.gravity = Gravity.CENTER;
        // topCenterLayout.setLayoutParams(lp);
        // }
        top_center2.setVisibility(View.VISIBLE);
        topCenterLayout.setVisibility(View.GONE);
    }

    public List<ImageView> leftImages = new ArrayList<ImageView>();
    public List<ImageView> rightImages = new ArrayList<ImageView>();

    public List<ViewGroup> rightLiner = new ArrayList<ViewGroup>();
}
