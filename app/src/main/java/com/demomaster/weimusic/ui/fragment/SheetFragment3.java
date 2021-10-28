package com.demomaster.weimusic.ui.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.demomaster.weimusic.R;
import com.demomaster.weimusic.activity.MainActivity;
import com.demomaster.weimusic.activity.SongSheetDetailActivity;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.model.AudioInfo;
import com.demomaster.weimusic.model.AudioSheet;
import com.demomaster.weimusic.player.service.MC;
import com.demomaster.weimusic.player.service.MusicDataManager;
import com.demomaster.weimusic.ui.adapter.MusicRecycleViewAdapter;
import com.demomaster.weimusic.ui.adapter.MusicRecycleViewAdapter2;
import com.demomaster.weimusic.ui.adapter.SheetBodyAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import cn.demomaster.huan.quickdeveloplibrary.helper.QdThreadHelper;
import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.util.DisplayUtil;
import cn.demomaster.huan.quickdeveloplibrary.view.banner.BannerCursorView;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.OnClickActionListener;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDDialog;
import cn.demomaster.huan.quickdeveloplibrary.widget.layout.LoadLayout;
import cn.demomaster.qdlogger_library.QDLogger;
import cn.demomaster.qdrouter_library.base.fragment.QuickFragment;

public class SheetFragment3 extends QuickFragment {

    RecyclerView recyclerView;
    ImageView iv_sheet_cover;
    ImageView iv_sheet_playall;
    TextView tv_sheet_name;
    LoadLayout loadlayout_sheet;
    AudioSheet audioSheet;
    MusicRecycleViewAdapter2 adapter;

    @Override
    public boolean isUseActionBarLayout() {
        return false;
    }

    @Override
    public View onGenerateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.item_cd_song_list, null);
        return mView;
    }

    List<AudioInfo> musicList = new ArrayList<>();

    @Override
    public void initView(View rootView) {
        /*getActionBarTool().setActionBarType(ACTIONBAR_TYPE.NO_ACTION_BAR);*/
        Bundle bundle = getArguments();
        EventBus.getDefault().register(this);
        audioSheet = (AudioSheet) bundle.getSerializable("audioSheets");
        recyclerView = findViewById(R.id.rv_songs);
        tv_sheet_name = findViewById(R.id.tv_sheet_name);
        iv_sheet_cover = findViewById(R.id.iv_sheet_cover);
        iv_sheet_playall = findViewById(R.id.iv_sheet_playall);
        loadlayout_sheet = findViewById(R.id.loadlayout_sheet);
        tv_sheet_name.setText(audioSheet.getName());
        iv_sheet_cover.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putLong("sheetId", audioSheet.getId());
                Intent intent = new Intent(mContext, SongSheetDetailActivity.class);
                intent.putExtras(bundle);
                mContext.startActivity(intent);
                return true;
            }
        });
        iv_sheet_playall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MC.getInstance(mContext).playSheet(audioSheet.getId());
            }
        });

        Glide.with(mContext).load(audioSheet.getImgSrc()).error(R.drawable.ic_launcher_pp).into(iv_sheet_cover);
        adapter = new MusicRecycleViewAdapter2(mContext, musicList);
        //这里使用线性布局像listview那样展示列表,第二个参数可以改为 HORIZONTAL实现水平展示
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        //使用网格布局展示
        recyclerView.setLayoutManager(linearLayoutManager);
        //recy_drag.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        //设置分割线使用的divider
        //rv_songs.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(adapter);
        updata();
        adapter.setOnItemClickListener(new MusicRecycleViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                playMusic(musicList, adapter, position);
                //fragment.finish();
            }

            @Override
            public void showContextMenu(View view, int position) {
                showSongMenu(position);
            }
        });
    }

    private void updata() {
        QdThreadHelper.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                List<AudioInfo> audioInfoList = MusicDataManager.getInstance(mContext).getSongSheetListById(mContext, audioSheet.getId());
                //QDLogger.i("musicInfoList:" + musicInfoList.size());
                musicList.clear();
                if (audioInfoList != null && audioInfoList.size() > 0) {
                    musicList.addAll(audioInfoList);
                }
                QdThreadHelper.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (musicList.size() == 0) {
                            loadlayout_sheet.setRetryText("添加歌曲");
                            loadlayout_sheet.loadFailWithRetry("添加歌曲", "歌单空空如也，快添加歌曲吧", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Bundle bundle = new Bundle();
                                    bundle.putLong("sheetId",  audioSheet.getId());
                                    Intent intent = new Intent(mContext, SongSheetDetailActivity.class);
                                    intent.putExtras(bundle);
                                    mContext.startActivity(intent);
                                }
                            });
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private void playMusic(List<AudioInfo> musicList, MusicRecycleViewAdapter2 adapter, int i) {
        if (i < musicList.size()) {
            MC.getInstance(mContext).playAudio(musicList.get(i));
        }
        adapter.notifyDataSetChanged();
    }

    QDDialog musicInfoDialog = null;

    private void showSongMenu(int position) {
        View layout = ((Activity) mContext).getLayoutInflater().inflate(R.layout.dialog_music_item2,
                null);
        ImageView imageView = layout.findViewById(R.id.iv_cover);
        Bitmap bitmap = MusicDataManager.getInstance(mContext).getAlbumPicture(mContext, musicList.get(position));
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
                showMenuDialog(musicList.get(position));
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

    private void showMenuDialog(AudioInfo audioInfo) {
        qdDialog = new QDDialog.Builder(mContext)
                .setMessage("确定要从歌单移除" + audioInfo.getTitle() + "-" + audioInfo.getArtist() + "吗？")
                .addAction("取消")
                .addAction("确定", new OnClickActionListener() {
                    @Override
                    public void onClick(Dialog dialog, View view, Object tag) {
                        dialog.dismiss();
                        MusicDataManager.getInstance(mContext).removeFromSheet(mContext, audioSheet.getId(), audioInfo.getId());
                        //updateRecycleView(viewPager.getCurrentItem());
                        updata();
                    }
                })
                .create();
        qdDialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventMessage message) {
        AudioStation station = AudioStation.getEnum(message.code);
        if (station != null) {
            switch (station) {
                case song_changed:
                case Play:
                case PLAYSTATE_CHANGED:
                case audio_ready:
                    adapter.notifyDataSetChanged();
                    break;
                case sheet_create:
                case sheet_changed:
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//默认只处理回退事件
            finish();
            return true;//当返回true时，表示已经完整地处理了这个事件，并不希望其他的回调方法再次进行处理，而当返回false时，表示并没有完全处理完该事件，更希望其他回调方法继续对其进行处理
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((MainActivity) getActivity()).hideSheetFragment();
    }
}
