package com.demomaster.weimusic.ui.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.model.AudioSheet;
import com.demomaster.weimusic.model.Channel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MusicChannelAdapter extends RecyclerView.Adapter<MusicChannelAdapter.ViewHolder> {
    // 帧动画
    private List<Channel> musicList;
    private Context mContext;

    public MusicChannelAdapter(Context mContext, List<Channel> musicList) {
        this.mContext = mContext;
        this.musicList = musicList;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_channel, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        holder.onBind(musicList.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(null, v, position, v.getId());
                }
            }
        });
    }

    private AdapterView.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView title;

        public ViewHolder(View view) {
            super(view);
            this.imageView = itemView
                    .findViewById(R.id.iv_head);
            this.title = itemView
                    .findViewById(R.id.tv_name);
        }

        public void onBind(Channel channel) {
            title.setText(channel.getName());
            imageView.setImageResource(channel.getRes());
        }
    }

}
