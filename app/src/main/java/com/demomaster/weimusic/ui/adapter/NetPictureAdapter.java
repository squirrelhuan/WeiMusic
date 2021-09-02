package com.demomaster.weimusic.ui.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.demomaster.weimusic.R;
import com.demomaster.weimusic.model.AudioInfo;
import com.demomaster.weimusic.model.MusicResponse;
import com.demomaster.weimusic.model.NetImage;
import com.demomaster.weimusic.player.service.MC;

import java.io.File;
import java.util.List;

/**
 * Created by Squirrel桓 on 2018/11/11.
 */
public class NetPictureAdapter extends RecyclerView.Adapter<NetPictureAdapter.ViewHolder> {

    private List<MusicResponse.ResultDTO.SongsDTO> musicList;
    private Context context;

    public NetPictureAdapter(Context context, List<MusicResponse.ResultDTO.SongsDTO> lists) {
        this.context = context;
        this.musicList = lists;
    }

    //创建View,被LayoutManager所用
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.audio_cover_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @SuppressWarnings("ResourceType")
    //数据的绑定
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        //viewHolder.tv_index.setText((position + 1) + "");
        //viewHolder.imageView.setImageResource(R.drawable.audio);
        viewHolder.title.setText(musicList.get(position).getName());

        Glide.with(context).load(musicList.get(position).getAlbum().getPicUrl()).into(viewHolder.iv_cover);
        viewHolder.iv_cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.showContextMenu(v, viewHolder.getAdapterPosition());
                }
            }
        });
        viewHolder.btn_use.setOnClickListener(new View.OnClickListener() {
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
        ImageView iv_cover;
        TextView title;
        Button btn_use;

        public ViewHolder(View convertView) {
            super(convertView);
            title = convertView
                    .findViewById(R.id.title);
            iv_cover = convertView
                    .findViewById(R.id.iv_cover);
            btn_use = convertView
                    .findViewById(R.id.btn_use);
        }
    }

    private MusicRecycleViewAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(MusicRecycleViewAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}

