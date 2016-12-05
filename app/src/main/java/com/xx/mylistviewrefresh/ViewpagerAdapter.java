package com.xx.mylistviewrefresh;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by XuXiang on 2016/11/16.
 */
public class ViewpagerAdapter extends PagerAdapter {
    private List<ImageView> mList;

    public ViewpagerAdapter(List<ImageView> mList) {
        super();
        this.mList = mList;
    }

    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = mList.get(position % mList.size());
        if (imageView.getParent() != null) {
            ((ViewPager) imageView.getParent()).removeView(imageView);
        }
        container.addView(mList.get(position % mList.size()));
        return mList.get(position % mList.size());
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    }
}
