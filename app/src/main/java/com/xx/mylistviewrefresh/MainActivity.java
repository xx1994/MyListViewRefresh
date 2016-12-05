package com.xx.mylistviewrefresh;

import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private AnimRefreshListView listView;

    private int img_arr[] = new int[]{R.drawable.p1, R.drawable.p2, R.drawable.p3};
    // 上一个页面的位置
    protected int lastPosition = 0;
    private ViewPager viewPager;
    private LinearLayout pointLinear;  //圆点
    private List<ImageView> mImgList = new ArrayList<ImageView>();  //装载Viewpage图片

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 10000) {
                //刷新结束回调
                listView.complete();
            } else {
                // 执行滑动到下一个页面
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                // 再发一个handler延时
                handler.sendEmptyMessageDelayed(0, 1000);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //模拟数据源
        List<String> dataList = new ArrayList<String>();
        for (int i = 1; i <= 20; i++) {
            String str = "我是第" + i + "个数据";
            dataList.add(str);
        }
        listView = (AnimRefreshListView) findViewById(R.id.listview);

        View headView = getLayoutInflater().inflate(R.layout.viewpager_header, null);
        pointLinear = (LinearLayout) headView.findViewById(R.id.point);
        viewPager = (ViewPager) headView.findViewById(R.id.viewpager);
        initViewPagerView();
        viewPager.setAdapter(new ViewpagerAdapter(mImgList));
        viewPager.setCurrentItem(0);
        viewPager.addOnPageChangeListener(this);

        listView.addHeaderView(headView);   //添加顶部Viewpager为HeaderView

        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList));

        //延时进行轮播
        handler.sendEmptyMessageDelayed(0, 1000);


        listView.setListViewMode(AnimRefreshListView.BOTH);
        listView.setOnRefreshListener(new AnimRefreshListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Message msg = new Message();
                        msg.what = 10000;
                        handler.sendMessage(msg);
                    }
                }).start();
            }

            @Override
            public void onLoadMore() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Message msg = new Message();
                        msg.what = 10000;
                        handler.sendMessage(msg);
                    }
                }).start();
            }
        });
    }

    //初始化Viewpager页面
    private void initViewPagerView() {
        for (int i : img_arr) {
            //初始化图片
            ImageView image = new ImageView(this);
            image.setBackgroundResource(i);
            mImgList.add(image);

            //初始化小点
            ImageView point = new ImageView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(15, 15);
            params.leftMargin = 10;
            params.rightMargin = 10;
            point.setLayoutParams(params);
            point.setBackgroundResource(R.drawable.dot_normal);
            if (i == R.drawable.p1) {
                //默认第一张
                point.setEnabled(true);
                point.setBackgroundResource(R.drawable.dot_focused);
            } else {
                point.setEnabled(false);
            }
            pointLinear.addView(point);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        // 页面切换后调用， position是新的页面位置
        position %= mImgList.size();
        // 把当前点设置为true,将上一个点设为false；并设置point_group图标
        pointLinear.getChildAt(position).setEnabled(true);
        pointLinear.getChildAt(position).setBackgroundResource(R.drawable.dot_focused);
        pointLinear.getChildAt(lastPosition).setEnabled(false);
        pointLinear.getChildAt(lastPosition).setBackgroundResource(R.drawable.dot_normal);
        lastPosition = position;
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
