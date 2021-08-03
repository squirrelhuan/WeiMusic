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
import com.demomaster.weimusic.model.MusicInfo;
import com.demomaster.weimusic.player.service.MC;

import java.util.List;
import java.util.Map;

import cn.demomaster.huan.quickdeveloplibrary.helper.simplepicture.SimplePictureAdapter;
import cn.demomaster.huan.quickdeveloplibrary.helper.simplepicture.model.Image;

/**
 * Created by Squirrel桓 on 2018/11/11.
 */
public class MusicRecycleViewAdapter extends RecyclerView.Adapter<MusicRecycleViewAdapter.ViewHolder> {
    // 帧动画
    private AnimationDrawable mPeakOneAnimation, mPeakTwoAnimation,
            mPeakThreeAnimation;
    private List<MusicInfo> musicList;
    private Context context;

    public MusicRecycleViewAdapter(Context context, List<MusicInfo> lists) {
        this.context = context;
        this.musicList = lists;
    }

    //创建View,被LayoutManager所用
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.music_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @SuppressWarnings("ResourceType")
    //数据的绑定
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        viewHolder.tv_index.setText((position +1)+ "");
        //viewHolder.imageView.setImageResource(R.drawable.audio);
        viewHolder.title.setText(musicList.get(position).getTitle());
        viewHolder.iv_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onItemClickListener!=null){
                   onItemClickListener.showContextMenu(v,viewHolder.getAdapterPosition());
                }
            }
        });
        viewHolder.artist.setText(musicList.get(position).getArtist());
        long currentaudioid = MC.getInstance(context).getCurrentAudioId();
        long audioid = musicList.get(position).getId();
        if (currentaudioid == audioid) {
            viewHolder.mPeakOne
                    .setImageResource(R.anim.peak_white_1);
            viewHolder.mPeakTwo
                    .setImageResource(R.anim.peak_white_2);
            viewHolder.mPeakThree
                    .setImageResource(R.anim.peak_white_3);
            mPeakOneAnimation = (AnimationDrawable) viewHolder.mPeakOne
                    .getDrawable();
            mPeakTwoAnimation = (AnimationDrawable) viewHolder.mPeakTwo
                    .getDrawable();
            mPeakThreeAnimation = (AnimationDrawable) viewHolder.mPeakThree
                    .getDrawable();
            if (MC.getInstance(context).isPlaying()) {
                mPeakOneAnimation.start();
                mPeakTwoAnimation.start();
                mPeakThreeAnimation.start();
            } else {
                mPeakOneAnimation.stop();
                mPeakTwoAnimation.stop();
                mPeakThreeAnimation.stop();
            }
        } else {
            viewHolder.mPeakOne.setImageResource(0);
            viewHolder.mPeakTwo.setImageResource(0);
            viewHolder.mPeakThree.setImageResource(0);
        }
        if(onItemClickListener!=null){
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(v,viewHolder.getAdapterPosition());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }
    //自定义ViewHolder,包含item的所有界面元素
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mPeakOne, mPeakTwo, mPeakThree, iv_more;
        TextView title, tv_index;
        TextView artist;
        public ViewHolder(View convertView) {
            super(convertView);
            title = convertView
                    .findViewById(R.id.title);
            iv_more = convertView
                    .findViewById(R.id.iv_more);
            artist = (TextView) convertView
                    .findViewById(R.id.artist);
            mPeakOne = convertView.findViewById(R.id.peak_one);
            mPeakTwo = convertView.findViewById(R.id.peak_two);
            mPeakThree = convertView.findViewById(R.id.peak_three);
            tv_index = convertView.findViewById(R.id.tv_index);
        }
    }

    private OnItemClickListener onItemClickListener;
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public static interface OnItemClickListener {
        void onItemClick(View view, int position);
        void showContextMenu(View view, int position);
    }
}

