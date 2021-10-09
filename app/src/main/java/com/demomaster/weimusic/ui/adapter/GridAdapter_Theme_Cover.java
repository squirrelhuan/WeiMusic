package com.demomaster.weimusic.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.SystemSetting;
import com.demomaster.weimusic.constant.ThemeConstants;
import com.demomaster.weimusic.util.ThemeUtil;
import com.demomaster.weimusic.view.AudioPlayerView;

import java.util.List;

/**
 * Created by huan on 2018/1/21.
 */

public class GridAdapter_Theme_Cover extends BaseAdapter {
    private Context mContext;
    private List datas;//数据
    private ThemeConstants.CoverType dataType = ThemeConstants.CoverType.withSystem;

    //适配器初始化
    public GridAdapter_Theme_Cover(Context context, Object datas, ThemeConstants.CoverType type) {
        mContext = context;
        this.dataType = type;
        if (this.dataType == ThemeConstants.CoverType.customPicture) {
            this.datas = (List<String>) datas;
        } else if (this.dataType == ThemeConstants.CoverType.withSystem){
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_theme_cover_cd, null);

            viewHolder.iv_thumbnail = convertView.findViewById(R.id.iv_thumbnail);
            viewHolder.iv_thumbnail.setEnabled(false);
            viewHolder.iv_select =  convertView.findViewById(R.id.iv_select);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        ThemeConstants.CoverType coverType = ThemeUtil.getCoverType();
        if (dataType == ThemeConstants.CoverType.withSystem) {
            int color = mContext.getResources().getColor((int)datas.get(position));
            try {
                int val = SystemSetting.getCoverWithSystemValue();
                if (coverType == dataType && color == val) {
                    viewHolder.iv_select.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.iv_select.setVisibility(View.GONE);
                }
                viewHolder.iv_thumbnail.setPreviewMode(true);
                viewHolder.iv_thumbnail.setColorStyle(color);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(dataType ==ThemeConstants.CoverType.customPicture){
            String path = (String) datas.get(position);
            try {
                String val = SystemSetting.getCoverWithCustomValue();
                if(coverType==dataType&&val!=null&&path.equals(val)){
                    viewHolder.iv_select.setVisibility(View.VISIBLE);
                }else {
                    viewHolder.iv_select.setVisibility(View.GONE);
                }
                viewHolder.iv_thumbnail.setPreviewMode(true);
                viewHolder.iv_thumbnail.setBitmapPath(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return convertView;
    }

    class ViewHolder {
        ImageView iv_select;
        AudioPlayerView iv_thumbnail;
    }
}