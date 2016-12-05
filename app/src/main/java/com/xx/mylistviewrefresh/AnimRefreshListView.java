package com.xx.mylistviewrefresh;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Scroller;

/**
 * Created by Administrator on 2016/12/2.
 */
public class AnimRefreshListView extends ListView implements AbsListView.OnScrollListener {
    private boolean mEnablePullRefresh = true;
    private boolean mEnablePullLoad;
    public static final int BOTH = 2;//上拉和下拉
    public static final int HEADER = 0;//下拉
    public static final int FOOTER = 1;//上拉

    private float mLastY = -1; // save event y
    private Scroller mScroller; // used for scroll back
    private OnScrollListener mScrollListener; // user's scroll listener

    // the interface to trigger refresh and load more.
    private OnRefreshListener onRefreshListener;

    // -- header view
//    private RefreshHeader mHeaderView;
    private AnimRefreshHeader mHeaderView;
    private LinearLayout mHeaderViewContent;
    private int mHeaderViewHeight; // header view's height
    /**
     * 是否正在刷新
     */
    private boolean mPullRefreshing = false; // is refreashing.

    // -- footer view
    private RefreshFooter mFooterView;
    private boolean mPullLoading;
    private int mScrollBack;
    private final static int SCROLLBACK_HEADER = 0;
    private final static int SCROLLBACK_FOOTER = 1;
    private final static int SCROLL_DURATION = 400;
    private final static int PULL_LOAD_MORE_DELTA = 70;
    private final static float OFFSET_RADIO = 1.8f;
    private boolean isFooterVisible = false;

    public AnimRefreshListView(Context context) {
        super(context);
        init(context);
    }

    public AnimRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mScroller = new Scroller(context, new DecelerateInterpolator());
        super.setOnScrollListener(this);
        //初始化头布局
        mHeaderView = new AnimRefreshHeader(context);
        mHeaderViewContent = (LinearLayout) mHeaderView.findViewById(R.id.headercontent);
        addHeaderView(mHeaderView);
        //初始化底布局
        mFooterView = new RefreshFooter(context);
        addFooterView(mFooterView);

        //header的高度
        mHeaderView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mHeaderViewHeight = mHeaderViewContent.getHeight();
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mLastY == -1) {//获取上次y轴坐标
            mLastY = ev.getRawY();
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY;
                mLastY = ev.getRawY();
                if (getFirstVisiblePosition() == 0 && (mHeaderView.getVisiableHeight() > 0 || deltaY > 0)) {
                    updateHeaderHeight(deltaY / OFFSET_RADIO);
                    invokeOnScrolling();
                } else if ((mFooterView.getBottomMargin() > 0 || deltaY < 0) && getLastVisiblePosition() == (getCount() - 1)) {
                    if (mEnablePullLoad) {
                        updateFooterHeight(-deltaY / OFFSET_RADIO);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                mLastY = -1;// reset
                if (isFooterVisible && mEnablePullLoad && mFooterView.getHeight() > 0 && mFooterView.getBottomMargin() > PULL_LOAD_MORE_DELTA) {
                    startLoadMore();
                    resetFooterHeight();
                    new ResetHeaderHeightTask().execute();
                } else if (getFirstVisiblePosition() == 0) {
                    // invoke refresh
                    if (mEnablePullRefresh && mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
                        mPullRefreshing = true;
                        mHeaderView.setState(AnimRefreshHeader.STATE_REFRESHING);
                        if (onRefreshListener != null) {
                            onRefreshListener.onRefresh();
                        }
                    }
                    resetHeaderHeight();
                }
                break;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (mScrollBack == SCROLLBACK_HEADER) {
                mHeaderView.setVisiableHeight(mScroller.getCurrY());
            } else {
                mFooterView.setBottomMargin(mScroller.getCurrY());
            }
            postInvalidate();
            invokeOnScrolling();
        }
        super.computeScroll();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (mScrollListener != null) {
            mScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        mScrollListener = l;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // send to user's listener
        if (mScrollListener != null) {
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
        if (firstVisibleItem + visibleItemCount >= totalItemCount) {
            isFooterVisible = true;
        } else {
            isFooterVisible = false;
        }
    }

    class ResetHeaderHeightTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            mPullRefreshing = false;
            mHeaderView.setState(AnimRefreshHeader.STATE_NORMAL);
            resetHeaderHeight();
        }
    }

    private void startLoadMore() {
        mPullLoading = true;
        mFooterView.setState(RefreshFooter.STATE_LOADING);
        if (onRefreshListener != null) {
            onRefreshListener.onLoadMore();
        }
    }

    /**
     * 更新头部高度，设置状态值
     */
    private void updateHeaderHeight(float delta) {
        mHeaderView.setVisiableHeight((int) delta + mHeaderView.getVisiableHeight());
        if (mEnablePullRefresh && !mPullRefreshing) {
            if (mHeaderView.getVisiableHeight() > mHeaderViewHeight) {
                mHeaderView.setState(AnimRefreshHeader.STATE_READY);
            } else {
                mHeaderView.setState(AnimRefreshHeader.STATE_NORMAL);
            }
        }
        setSelection(0); // scroll to top each time
    }

    /**
     * 更新底部高度
     */
    private void updateFooterHeight(float delta) {
        int height = mFooterView.getBottomMargin() + (int) delta;
        if (mEnablePullLoad && !mPullLoading) {
            if (height > PULL_LOAD_MORE_DELTA) { // height enough to invoke load
                // more.
                mFooterView.setState(RefreshFooter.STATE_READY);
            } else {
                mFooterView.setState(RefreshFooter.STATE_NORMAL);
            }
        }
        mFooterView.setBottomMargin(height);
        if (mPullLoading) {
            resetFooterHeight();
        }
    }

    /**
     * reset header view's height.
     */
    private void resetHeaderHeight() {
        int height = mHeaderView.getVisiableHeight();
        if (height == 0) // not visible.
            return;
        // refreshing and header isn't shown fully. do nothing.
        if (mPullRefreshing && height <= mHeaderViewHeight) {
            return;
        }
        int finalHeight = 0; // default: scroll back to dismiss header.
        // is refreshing, just scroll back to show all the header.
        if (mPullRefreshing && height > mHeaderViewHeight) {
            finalHeight = mHeaderViewHeight;
        }
        mScrollBack = SCROLLBACK_HEADER;
        mScroller.startScroll(0, height, 0, finalHeight - height, SCROLL_DURATION);
        // trigger computeScroll
        invalidate();
    }

    private void resetFooterHeight() {
        int bottomMargin = mFooterView.getBottomMargin();
        if (bottomMargin > 0) {
            mScrollBack = SCROLLBACK_FOOTER;
            mScroller.startScroll(0, bottomMargin, 0, -bottomMargin, SCROLL_DURATION);
            invalidate();
        }
    }

    private void invokeOnScrolling() {
        if (mScrollListener instanceof OnXScrollListener) {
            OnXScrollListener l = (OnXScrollListener) mScrollListener;
            l.onXScrolling(this);
        }
    }

    /**
     * you can listen ListView.OnScrollListener or this one. it will invoke
     * onXScrolling when header/footer scroll back.
     */
    public interface OnXScrollListener extends OnScrollListener {
        public void onXScrolling(View view);
    }

    public void setOnRefreshListener(OnRefreshListener l) {
        onRefreshListener = l;
    }

    /**
     * implements this interface to get refresh/load more event.
     */
    public interface OnRefreshListener {
        public void onRefresh();

        public void onLoadMore();
    }

    /**
     * 上拉加载和下拉刷新请求完毕
     */
    public void complete() {
        stopLoadMore();
        stopRefresh();
    }

    /**
     * stop refresh, reset header view.
     * 停止刷新,重置头部控件
     */
    private void stopRefresh() {
        if (mPullRefreshing == true) {
            mPullRefreshing = false;
            resetHeaderHeight();
        }
    }

    /**
     * stop load more, reset footer view.
     * 停止加载更多,重置尾部控件
     */
    private void stopLoadMore() {
        if (mPullLoading == true) {
            mPullLoading = false;
            mFooterView.setState(RefreshFooter.STATE_NORMAL);
        }
    }

    /**
     * 设置ListView的模式,上拉和下拉
     */
    public void setListViewMode(int mode) {
        if (mode == BOTH) {
            setPullRefreshEnable(true);
            setPullLoadEnable(true);
        } else if (mode == FOOTER) {
            setPullLoadEnable(true);
        } else if (mode == HEADER) {
            setPullRefreshEnable(true);
        }
    }

    /**
     * 设置刷新可用
     */
    private void setPullRefreshEnable(boolean enable) {
        mEnablePullRefresh = enable;
        if (!mEnablePullRefresh) { // disable, hide the content
            mHeaderViewContent.setVisibility(View.INVISIBLE);
        } else {
            mHeaderViewContent.setVisibility(View.VISIBLE);
        }
    }

    /**
     * enable or disable pull up load more feature.
     * 设置加载可用
     */
    private void setPullLoadEnable(boolean enable) {
        mEnablePullLoad = enable;
        if (!mEnablePullLoad) {
            mFooterView.hide();
            mFooterView.setOnClickListener(null);
        } else {
            mPullLoading = false;
            mFooterView.show();
            mFooterView.setState(RefreshFooter.STATE_NORMAL);
            // both "pull up" and "click" will invoke load more.
            mFooterView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    startLoadMore();
                }
            });
        }
    }
}
