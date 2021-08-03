package com.demomaster.weimusic.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.demomaster.weimusic.constant.Constants;
import com.demomaster.weimusic.R;

import java.io.FileInputStream;
import java.util.List;

import cn.demomaster.huan.quickdeveloplibrary.helper.QDSharedPreferences;

/**
 * Created by huan on 2018/1/21.
 */

public class GridAdapter_Theme_Font extends BaseAdapter {
    private Context mContext;
    private List datas;//数据
    private int type = 0;

    //适配器初始化
    public GridAdapter_Theme_Font(Context context, Object datas, int type) {
        mContext = context;
        this.type = type;
        if (this.type == 0) {
            this.datas = (List<String>) datas;
        } else {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_theme_font, null);

            viewHolder.iv_thumbnail = (TextView) convertView.findViewById(R.id.iv_thumbnail);
            viewHolder.iv_select = (ImageView) convertView.findViewById(R.id.iv_select);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (type == 0) {
            int id = (int) datas.get(position);
            int type = QDSharedPreferences.getInstance().getInt(Constants.Theme_Font_Type, 0);
            int val = QDSharedPreferences.getInstance().getInt(Constants.Key_Theme_Font_System, 0);
            if (id == val) {
                viewHolder.iv_select.setVisibility(View.VISIBLE);
            } else {
                viewHolder.iv_select.setVisibility(View.GONE);
            }
            if (position == 0) {
                viewHolder.iv_thumbnail.setBackgroundColor(mContext.getResources().getColor(R.color.black));
                viewHolder.iv_thumbnail.setTextColor(mContext.getResources().getColor(R.color.white));
            }else {
                viewHolder.iv_thumbnail.setBackgroundColor(mContext.getResources().getColor(R.color.white));
                viewHolder.iv_thumbnail.setTextColor(mContext.getResources().getColor(R.color.black));
            }
        } else {
            String file = (String) datas.get(position);
            try {
                int type = QDSharedPreferences.getInstance().getInt(Constants.Theme_Welcome_Type, 0);
                String val = QDSharedPreferences.getInstance().getString(Constants.Key_Theme_Welcome_Custom, null);
                if (val != null && type == Constants.Theme_Welcome_Type_Custom && file.equals(val)) {
                    viewHolder.iv_select.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.iv_select.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return convertView;
    }

    class ViewHolder {
        ImageView iv_select;
        TextView iv_thumbnail;
    }
}