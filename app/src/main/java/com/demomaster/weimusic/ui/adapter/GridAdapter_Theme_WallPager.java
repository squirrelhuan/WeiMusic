package com.demomaster.weimusic.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.demomaster.weimusic.R;
import com.demomaster.weimusic.SystemSetting;
import com.demomaster.weimusic.constant.ThemeConstants;
import com.demomaster.weimusic.util.ThemeUtil;

import java.io.File;
import java.util.List;

/**
 * Created by huan on 2018/1/21.
 */

public class GridAdapter_Theme_WallPager extends BaseAdapter {
    private Context mContext;
    private List datas;//数据
    private ThemeConstants.WallPagerType dataType;

    //适配器初始化
    public GridAdapter_Theme_WallPager(Context context, Object datas, ThemeConstants.WallPagerType type) {
        mContext = context;
        this.dataType = type;
        if (this.dataType == ThemeConstants.WallPagerType.withSystem) {
            this.datas = (List<String>) datas;
        } else if (this.dataType == ThemeConstants.WallPagerType.customPicture){
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

            viewHolder.iv_thumbnail = convertView.findViewById(R.id.iv_thumbnail);
            viewHolder.iv_select = convertView.findViewById(R.id.iv_select);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        ThemeConstants.WallPagerType type1 = ThemeUtil.getWallPagerType();
        if (dataType == ThemeConstants.WallPagerType.withSystem) {//系统预设的壁紙
            int id = (int) datas.get(position);
            int val = SystemSetting.getWallPagerWithSystemValue();
            if (id == val&&dataType==type1) {
                viewHolder.iv_select.setVisibility(View.VISIBLE);
            } else {
                viewHolder.iv_select.setVisibility(View.GONE);
            }
            viewHolder.iv_thumbnail.setBackgroundDrawable(ResourcesCompat.getDrawable(mContext.getResources(),id,null));
        } else {//自定义的壁紙
            String file = (String) datas.get(position);
            try {
                String val =SystemSetting.getWallPagerWithCustomValue();
                if (val != null && dataType==type1 && file.equals(val)) {
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