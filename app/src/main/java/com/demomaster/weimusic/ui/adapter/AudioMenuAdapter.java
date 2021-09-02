package com.demomaster.weimusic.ui.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.model.Menu;

import java.util.List;

/**
 * Created by Squirrel桓 on 2018/11/11.
 */
public class AudioMenuAdapter extends RecyclerView.Adapter<AudioMenuAdapter.ViewHolder> {
    // 帧动画
    private List<Menu> musicList;
    private Context context;

    public AudioMenuAdapter(Context context, List<Menu> lists) {
        this.context = context;
        this.musicList = lists;
    }

    //创建View,被LayoutManager所用
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_adapter_audio_menu, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @SuppressWarnings("ResourceType")
    //数据的绑定
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        //viewHolder.tv_index.setText((position + 1) + "");
        //viewHolder.imageView.setImageResource(R.drawable.audio);
        viewHolder.tv_name.setText(musicList.get(position).getName());
        viewHolder.iv_menu_icon.setImageResource(musicList.get(position).getResourceId());
        //viewHolder.iv_menu_icon.setText();
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, viewHolder.getAdapterPosition());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    //自定义ViewHolder,包含item的所有界面元素
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_menu_icon;
        TextView tv_name;

        public ViewHolder(View convertView) {
            super(convertView);
            tv_name = convertView
                    .findViewById(R.id.tv_name);
            iv_menu_icon = convertView
                    .findViewById(R.id.iv_menu_icon);
        }
    }

    private MusicRecycleViewAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(MusicRecycleViewAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}

