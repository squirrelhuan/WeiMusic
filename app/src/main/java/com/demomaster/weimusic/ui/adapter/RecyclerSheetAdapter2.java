package com.demomaster.weimusic.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.demomaster.weimusic.R;
import com.demomaster.weimusic.model.AudioSheet;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RecyclerSheetAdapter2 extends RecyclerView.Adapter<RecyclerSheetAdapter2.ViewHolder> {
    // 帧动画
    private List<AudioSheet> musicList;
    private Context mContext;

    public RecyclerSheetAdapter2(Context mContext, List<AudioSheet> musicList) {
        this.mContext = mContext;
        this.musicList = musicList;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_sheet3, parent, false);
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

        holder.iv_menu.setOnClickListener(new View.OnClickListener() {
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
        ImageView iv_menu;

        public ViewHolder(View view) {
            super(view);
            this.imageView = itemView
                    .findViewById(R.id.iv_head);
            this.title = itemView
                    .findViewById(R.id.tv_name);
            this.iv_menu = itemView
                    .findViewById(R.id.iv_menu);
        }

        public void onBind(AudioSheet audioSheet) {
            title.setText(audioSheet.getName());
            //imageView.setImageResource();
            Glide.with(mContext).load(audioSheet.getImgSrc()).error(R.drawable.ic_launcher_pp).into(imageView);
        }
    }

}
