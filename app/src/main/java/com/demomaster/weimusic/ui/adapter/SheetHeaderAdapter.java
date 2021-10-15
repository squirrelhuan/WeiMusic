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

    public void setCurrent(ViewPager viewPager, int position) {
        View view1 = getCurrentView(viewPager, position - 1);
        if (view1 != null) {
            ViewHolder viewHolder = new ViewHolder(view1);
            //viewHolder.tv_name.setVisibility(View.GONE);
            //viewHolder.iv_play_sheet.setVisibility(View.GONE);
        }

        View view2 = getCurrentView(viewPager, position + 1);
        if (view2 != null) {
            ViewHolder viewHolder2 = new ViewHolder(view2);
            // viewHolder2.tv_name.setVisibility(View.GONE);
            //viewHolder2.iv_play_sheet.setVisibility(View.GONE);
        }

        View view3 = getCurrentView(viewPager, position);
        if(view3!=null) {
            ViewHolder viewHolder3 = new ViewHolder(view3);
            //viewHolder3.tv_name.setVisibility(View.VISIBLE);
            //viewHolder3.iv_play_sheet.setVisibility(View.VISIBLE);
            //viewHolder3.iv_play_sheet.setImageResource(MusicDataManager.getInstance(mContext).getCurrentSheetId() == data.get(position).getId() ? R.drawable.button_music_play01 : R.drawable.button_music_play02);
        }
    }

    public View getCurrentView(ViewPager viewPager, int position) {
        int count = viewPager.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = viewPager.getChildAt(i);
            if (view != null && view.getTag() != null) {
                String tag = ("" + view.getTag());
                if (tag.equals(position + "")) {
                    return view;
                }
            }
        }
        return null;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_sheet_header, null);
        view.setTag(position);
        ViewHolder viewHolder = new ViewHolder(view);

        //viewHolder.tv_play_all = view.findViewById(R.id.tv_play_all);
        if (data != null && data.get(position) != null) {
            Glide.with(mContext).load(data.get(position).getImgSrc()).into(viewHolder.iv_icon);
            //viewHolder.tv_name.setVisibility(View.GONE);
            //viewHolder.tv_name.setText(data.get(position).getName());
            /*viewHolder.iv_play_sheet.setVisibility(View.GONE);
            viewHolder.iv_play_sheet.setImageResource(MusicDataManager.getInstance(mContext).getCurrentSheetId() == data.get(position).getId() ? R.drawable.button_music_play01 : R.drawable.button_music_play02);
            viewHolder.iv_play_sheet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (MusicDataManager.getInstance(mContext).getCurrentSheetId() == data.get(position).getId()) {
                        if (MC.getInstance(mContext).isPlaying()) {
                            MC.getInstance(mContext).pause();
                            //viewHolder.iv_play_sheet.setImageResource(R.drawable.button_music_play02);
                        } else {
                            MC.getInstance(mContext).playSheet(data.get(position).getId());
                            //viewHolder.iv_play_sheet.setImageResource(R.drawable.button_music_play01);
                        }
                    } else {
                        MC.getInstance(mContext).playSheet(data.get(position).getId());
                        //viewHolder.iv_play_sheet.setImageResource(R.drawable.button_music_play01);
                    }
                }
            });*/
        }
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    class ViewHolder {
        ImageView iv_icon;// iv_play_sheet;
        //TextView tv_name;
        public ViewHolder(View view) {
            if (view != null) {
                iv_icon = view.findViewById(R.id.iv_icon);
                //tv_name = view.findViewById(R.id.tv_name);
               // iv_play_sheet = view.findViewById(R.id.iv_play_sheet);
            }
        }
    }
}