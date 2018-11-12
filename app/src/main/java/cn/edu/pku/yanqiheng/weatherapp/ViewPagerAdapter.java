package cn.edu.pku.yanqiheng.weatherapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

// viewPager的适配器,对相关的方法进行重写
public class ViewPagerAdapter extends PagerAdapter{
    private List<View> views;
    private Context context;

    public ViewPagerAdapter(List<View> views,Context context){
        this.views=views;
        this.context=context;
    }
    // 要滑动的视图的个数
    @Override
    public int getCount() {
        return views.size();
    }

    @NonNull
    @Override
    // 第一：将当前视图添加到container中，第二：返回当前View
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        container.addView(views.get(position));
        return views.get(position);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view==o;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView(views.get(position));
    }
}

