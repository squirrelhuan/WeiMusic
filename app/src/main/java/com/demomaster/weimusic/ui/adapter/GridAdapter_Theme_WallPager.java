package com.demomaster.weimusic.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.demomaster.weimusic.constant.Constants;
import com.demomaster.weimusic.R;
import com.demomaster.weimusic.constant.ThemeConstants;
import com.demomaster.weimusic.util.ThemeUtil;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import cn.demomaster.huan.quickdeveloplibrary.helper.QDSharedPreferences;

/**
 * Created by huan on 2018/1/21.
 */

public class GridAdapter_Theme_WallPager extends BaseAdapter {
    private Context mContext;
    private List datas;//数据
    private ThemeConstants.WallPagerType type = ThemeConstants.WallPagerType.withSystem;

    //适配器初始化
    public GridAdapter_Theme_WallPager(Context context, Object datas, ThemeConstants.WallPagerType type) {
        mContext = context;
        this.type = type;
        if (this.type == ThemeConstants.WallPagerType.withSystem) {
            this.datas = (List<String>) datas;
        } else if (this.type == ThemeConstants.WallPagerType.customPicture){
            this.datas = (List<Integer>) datas;
        }
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_theme_01, null);

            viewHolder.iv_thumbnail = (ImageView) convertView.findViewById(R.id.iv_thumbnail);
            viewHolder.iv_select = (ImageView) convertView.findViewById(R.id.iv_select);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        ThemeConstants.WallPagerType type1 = ThemeUtil.getWallPagerType();
        if (type == ThemeConstants.WallPagerType.withSystem) {
            int id = (int) datas.get(position);
            int val = QDSharedPreferences.getInstance().getInt(Constants.Key_Theme_WallPager_System, 0);
            if (id == val&&type==type1) {
                viewHolder.iv_select.setVisibility(View.VISIBLE);
            } else {
                viewHolder.iv_select.setVisibility(View.GONE);
            }
            viewHolder.iv_thumbnail.setBackgroundDrawable(mContext.getResources().getDrawable(id));
        } else {
            String file = (String) datas.get(position);
            try {
                String val = QDSharedPreferences.getInstance().getString(Constants.Key_Theme_WallPager_Custom, null);
                if (val != null && type==type1 && file.equals(val)) {
                    viewHolder.iv_select.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.iv_select.setVisibility(View.GONE);
                }
                Glide.with(mContext).load(new File(file)).into(viewHolder.iv_thumbnail);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return convertView;
    }

    class ViewHolder {
        ImageView iv_select;
        ImageView iv_thumbnail;
    }
}