package com.demomaster.weimusic.ui.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.demomaster.weimusic.R;
import com.demomaster.weimusic.model.AudioSheet;
import com.demomaster.weimusic.player.service.MC;
import com.demomaster.weimusic.player.service.MusicDataManager;

import java.util.ArrayList;
import java.util.List;

public class SheetHeaderAdapter extends PagerAdapter {
    private Context mContext;
    private List<AudioSheet> data = new ArrayList<AudioSheet>();
    public SheetHeaderAdapter(Context mContext, List<AudioSheet> list) {
        this.mContext = mContext;
        this.data = list;
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_sheet_header, null);
        view.setTag(position);
        ViewHolder viewHolder = new ViewHolder(view);
        if (data != null && data.get(position) != null) {
            Glide.with(mContext).load(data.get(position).getImgSrc()).into(viewHolder.iv_icon);
        }
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    class ViewHolder {
        ImageView iv_icon;
        public ViewHolder(View view) {
            iv_icon = view.findViewById(R.id.iv_icon);
        }
    }
}