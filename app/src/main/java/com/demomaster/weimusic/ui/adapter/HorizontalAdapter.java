package com.demomaster.weimusic.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.demomaster.weimusic.R;
import com.demomaster.weimusic.model.AudioSheet;

import java.util.ArrayList;
import java.util.List;

import cn.demomaster.huan.quickdeveloplibrary.widget.AutoCenterHorizontalScrollView;

/**
 * Created by Squirrel桓 on 2018/12/15.
 */
public class HorizontalAdapter implements AutoCenterHorizontalScrollView.HAdapter {
    List<AudioSheet> data = new ArrayList<>();
    private Context context;

    public HorizontalAdapter(Context context, List<AudioSheet> data) {
        this.data = data;
        this.context = context;
    }

    @Override
    public void setData(List<String> data) {

    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemView(int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_sheet_header3, null, false);
        HViewHolder hViewHolder = new HViewHolder(v);
        //hViewHolder.iv_icon.setBackgroundColor(Color.BLACK);
        Glide.with(context).load(data.get(i).getImgSrc()).into(hViewHolder.iv_icon);
       // hViewHolder.iv_icon.setText(names.get(i));
        return hViewHolder;
    }

    @Override
    public void onSelectStateChanged(RecyclerView.ViewHolder viewHolder, int position, boolean isSelected) {
       /* if (isSelected) {
            ((HViewHolder) viewHolder).iv_icon.setBackgroundColor(Color.RED);
        } else {
            ((HViewHolder) viewHolder).iv_icon.setBackgroundColor(Color.BLACK);
        }*/
    }

    public static class HViewHolder extends RecyclerView.ViewHolder {
        //public final TextView textView;
        public ImageView iv_icon;
        public HViewHolder(View itemView) {
            super(itemView);
            iv_icon = itemView.findViewById(R.id.iv_icon);
            //textView = itemView.findViewById(R.id.tv_tab_name);
        }
    }
}
