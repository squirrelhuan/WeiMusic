package com.demomaster.weimusic.ui.adapter;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.model.AudioInfo;
import com.demomaster.weimusic.model.AudioSheet;
import com.demomaster.weimusic.player.service.MC;
import com.demomaster.weimusic.player.service.MusicDataManager;

import java.util.ArrayList;
import java.util.List;

import cn.demomaster.huan.quickdeveloplibrary.helper.QdThreadHelper;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.OnClickActionListener;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDDialog;

public class SheetAdapter extends PagerAdapter {

    private Context mContext;

    private List<AudioSheet> data = new ArrayList<AudioSheet>();
    //SheetFragment fragment;
    public SheetAdapter(Context context, List<AudioSheet> list) {
        //this.fragment = fragment;
        this.mContext = context;
        this.data = list;
    }

    @Override
    public int getCount() {
        return data==null?0:data.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    public View getCurrentView(ViewPager viewPager,int position){
        int count = viewPager.getChildCount();
        for(int i=0;i<count;i++){
           View view = viewPager.getChildAt(i);
           if(view!=null&&view.getTag()!=null){
               String tag = (""+view.getTag());
               if(tag.equals(position+"")) {
                   return view;
               }
           }
        }
        return null;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        ViewHolder viewHolder = new ViewHolder();
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_cd_song_list, null);
        view.setTag(position);
        viewHolder.recyclerView = view.findViewById(R.id.rv_songs);
        MusicRecycleViewAdapter2 adapter;
        List<AudioInfo> musicList = new ArrayList<>();
        if (data != null && data.get(position) != null) {
            long sheetId = data.get(position).getId();
            adapter = new MusicRecycleViewAdapter2(mContext,musicList);
            //这里使用线性布局像listview那样展示列表,第二个参数可以改为 HORIZONTAL实现水平展示
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
            //使用网格布局展示
            viewHolder.recyclerView.setLayoutManager(linearLayoutManager);
            //recy_drag.setLayoutManager(linearLayoutManager);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
            //设置分割线使用的divider
            //rv_songs.addItemDecoration(dividerItemDecoration);
            viewHolder.recyclerView.setAdapter(adapter);
            updata(musicList,adapter,sheetId);
            adapter.setOnItemClickListener(new MusicRecycleViewAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    playMusic(musicList,adapter,position);
                }

                @Override
                public void showContextMenu(View view, int position) {
                    showSongMenu(sheetId,musicList,adapter,position);
                }
            });
        }
        // viewHolder.tv_valuestr.setTextColor(mContext.getResources().getColor((!qiGuanModels.get(position).getValueStr().contains("-")) ? R.color.jps_green_01 : R.color.jps_red_01));
        //AnimationUtil.addScaleAnimition(view,null);
        container.addView(view);
        return view;
    }

    private void updata(List<AudioInfo> musicList, MusicRecycleViewAdapter2 adapter, long sheetId) {
        QdThreadHelper.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                List<AudioInfo> audioInfoList = MusicDataManager.getInstance(mContext).getSongSheetListById(mContext, sheetId);
                //QDLogger.i("musicInfoList:" + musicInfoList.size());
                musicList.clear();
                if (audioInfoList != null && audioInfoList.size() > 0) {
                    musicList.addAll(audioInfoList);
                }
                QdThreadHelper.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void playMusic(List<AudioInfo> musicList, MusicRecycleViewAdapter2 adapter, int i) {
        if(i<musicList.size()) {
            MC.getInstance(mContext).playAudio(musicList.get(i));
        }
        adapter.notifyDataSetChanged();
    }

    QDDialog musicInfoDialog = null;
    private void showSongMenu(long sheetId, List<AudioInfo> musicList, MusicRecycleViewAdapter2 adapter, int position) {
        View layout = ((Activity)mContext).getLayoutInflater().inflate(R.layout.dialog_music_item2,
                null);
        ImageView imageView = layout.findViewById(R.id.iv_cover);
        Bitmap bitmap =MusicDataManager.getInstance(mContext).getAlbumPicture(mContext, musicList.get(position));
        imageView.setImageBitmap(bitmap);
        TextView artist = layout.findViewById(R.id.artist);
        artist.setText(musicList.get(position).getArtist());
        TextView title = layout.findViewById(R.id.title);
        title.setText(musicList.get(position).getTitle());
        TextView tv_add_blacklist = layout.findViewById(R.id.tv_add_blacklist);
        tv_add_blacklist.setVisibility(View.INVISIBLE);
        TextView tv_remove_from_sheet = layout.findViewById(R.id.tv_remove_from_sheet);
        tv_remove_from_sheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicInfoDialog.dismiss();
                showMenuDialog(sheetId,adapter,musicList,musicList.get(position));
            }
        });
        musicInfoDialog = new QDDialog.Builder(mContext)
                .setBackgroundRadius(50)
                .setContentView(layout)
                .setBackgroundColor(mContext.getResources().getColor(R.color.transparent_light_33))
                .setAnimationStyleID(R.style.qd_dialog_animation_center_scale)
                .create();
        musicInfoDialog.show();
    }
    QDDialog qdDialog;
    private void showMenuDialog(long sheetId, MusicRecycleViewAdapter2 adapter, List<AudioInfo> musicList, AudioInfo audioInfo) {
        QDDialog qdDialog = new QDDialog.Builder(mContext)
                .setMessage("确定要从歌单移除"+ audioInfo.getTitle()+"-"+ audioInfo.getArtist()+"吗？")
                .addAction("取消")
                .addAction("确定", new OnClickActionListener() {
                    @Override
                    public void onClick(Dialog dialog, View view, Object tag) {
                        dialog.dismiss();
                        MusicDataManager.getInstance(mContext).removeFromSheet(mContext, sheetId,audioInfo.getId());
                        //updateRecycleView(viewPager.getCurrentItem());
                        updata(musicList,adapter,sheetId);
                    }
                })
                .create();
        qdDialog.show();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    class ViewHolder {
        RecyclerView recyclerView;
    }
}