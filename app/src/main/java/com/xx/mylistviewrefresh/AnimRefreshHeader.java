package com.xx.mylistviewrefresh;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.CollapsibleActionView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by XuXiang on 2016/11/28.
 */
public class AnimRefreshHeader extends LinearLayout {
    //根布局
    private LinearLayout mContainer;

    //状态值 0-正常 1-准备刷新  2-正在刷新
    private int mState = STATE_NORMAL;
    public final static int STATE_NORMAL = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_REFRESHING = 2;

    private WaveLoadingView waveLoadingView;
    private boolean mIsStop = false;

    public AnimRefreshHeader(Context context) {
        super(context);
        initView(context);
    }

    public AnimRefreshHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, 0);
        mContainer = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.animal_header, null);
        waveLoadingView = (WaveLoadingView) mContainer.findViewById(R.id.loadingView);
        waveLoadingView.setWaveColor(Color.parseColor("#57bc51"));   //设置填充波浪的颜色
        waveLoadingView.setOriginalImage(R.drawable.yunyunyun);      //设置图片

        addView(mContainer, lp);
        setGravity(Gravity.BOTTOM);
    }

    //设置状态
    public void setState(int state) {
        if (state == mState)
            return;
        switch (state) {
            case STATE_NORMAL:   //下拉加载状态
                waveLoadingView.reset();           // 重置波浪Y坐标
                mIsStop = !mIsStop;
                waveLoadingView.setmStopInvalidate(true);    //暂停、启动动画
                break;
            case STATE_READY:   //释放刷新状态
                if (mState != STATE_READY) {
                    waveLoadingView.setmStopInvalidate(true);    //暂停、启动动画
                }
                break;
            case STATE_REFRESHING:    //正在加载状态
                waveLoadingView.reset();    // 重置波浪Y坐标
                mIsStop = !mIsStop;
                waveLoadingView.setmStopInvalidate(false);    //暂停、启动动画
                break;
            default:
        }
        mState = state;
    }

    /**
     * 设置头部高度
     */
    public void setVisiableHeight(int height) {
        if (height < 0)
            height = 0;
        LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
        lp.height = height;
        mContainer.setLayoutParams(lp);
    }

    public int getVisiableHeight() {
        return mContainer.getHeight();
    }

}
