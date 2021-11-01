package com.demomaster.weimusic.ui.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.demomaster.weimusic.R;
import com.demomaster.weimusic.activity.SportActivity;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.player.service.MC;
import com.demomaster.weimusic.player.service.MusicDataManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.widget.square.SquareImageView;
import cn.demomaster.qdlogger_library.QDLogger;
import cn.demomaster.qdrouter_library.base.activity.QuickActivity;
import cn.demomaster.qdrouter_library.base.fragment.QuickFragment;

public class QuickBottomActionBarFragment extends QuickFragment {


    @BindView(R.id.bottom_action_bar_play)
    CheckBox bottom_action_bar_play;
    @BindView(R.id.bottom_select_cd_open)
    SquareImageView bottom_select_cd_open;
    @BindView(R.id.tv_name)
    TextView tv_name;
    @BindView(R.id.iv_cover)
    ImageView iv_cover;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public boolean isUseActionBarLayout() {
        return false;
    }

    @Override
    public View onGenerateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            return inflater.inflate(R.layout.layout_bottom_common,
                    container);
        } else {
            return inflater.inflate(R.layout.layout_bottom_common,
                    container);
        }
    }

    @Override
    public void initView(View view) {
        ButterKnife.bind(this, view);
        //iv_info.setOnClickListener(this);
        bottom_action_bar_play.setSelected(MC.getInstance(getContext()).isPlaying());
        bottom_action_bar_play.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // MC.getInstance(getContext()).doPauseResume();
                boolean isPlaying = MC.getInstance(getContext()).isPlaying();
                //bottom_action_bar_play.setSelected(!isPlaying);
                if (!isPlaying) {
                    //play();
                    EventBus.getDefault().post(new EventMessage(AudioStation.preToPlay.value()));//准备播放
                } else {
                    // pause();
                    EventBus.getDefault().post(new EventMessage(AudioStation.preToPause.value()));//准备播放
                }
            }
        });
        bottom_select_cd_open.setOnClickListener(this);
    }

    private long songId;

    public void refreshUI() {
        try {
            if (MC.getInstance(getContext()).getCurrentInfo() != null) {
                long duration = MC.getInstance(getContext()).getCurrentInfo().getDuration();
                long position = MC.getInstance(getContext()).getPosition();
                int p = (int) (position / (duration * 1f) * 1000);
                bottom_action_bar_play.setSelected(MC.getInstance(getContext()).isPlaying());
                if (songId != MC.getInstance(getContext()).getCurrentInfo().getId()) {
                    tv_name.setText(MC.getInstance(getContext()).getCurrentInfo().title);
                    Glide.with(mContext).load(MusicDataManager.getInstance(mContext).getAlbumPicture(mContext, MC.getInstance(getContext()).getCurrentInfo())).error(R.mipmap.ic_favorite).into(iv_cover);
                }
                songId = MC.getInstance(getContext()).getCurrentInfo().getId();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //QDLogger.i("Handler ...");
            refreshUI();
            if (MC.getInstance(getContext()).isPlaying()) {
                handler.postDelayed(runnable, 1000);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        handler.post(runnable);
    }

    //添加播放倒计时，处理切换歌曲消息通知声音
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventMessage message) {
        AudioStation station = AudioStation.getEnum(message.code);
        if (station != null) {
            QDLogger.println("事件:" + station.getDesc());
            switch (station) {
                case song_changed:
                    refreshUI();
                    break;
                case PLAYSTATE_CHANGED:
                    refreshUI();
                    break;
                case service_ready:
                    refreshUI();
                    break;
                case loadData:
                    refreshUI();
                    break;
                case QUEUE_CHANGED:
                    refreshUI();
                    break;
                case audio_ready:
                    refreshUI();
                    break;
                case Play:
                    handler.removeCallbacks(runnable);
                    handler.post(runnable);
                    break;
                case Pause:
                    handler.removeCallbacks(runnable);
                    handler.post(runnable);
                    break;
                case REPEATMODE_CHANGED://播放顺序变化
                    /*mRepeat.setSequenceType(MC.getInstance(getContext()).getRepeatMode());
                    iv_info.setImageResource();*/
                    break;
            }
        } else {
            QDLogger.println("事件2:" + message);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.iv_info:
                //MC.getInstance(getContext()).doRepeat();
                //showMulMenuDialog();
                Intent intent = new Intent(mContext, SportActivity.class);
                startActivity(intent);
                break;
            case R.id.bottom_action_bar_previous:
                long id1 = MC.getInstance(getContext()).getCurrentAudioId();
                long id2 = MC.getInstance(getContext()).getNextMusicInfo(true).getAudioId();
                EventBus.getDefault().post(new EventMessage(AudioStation.preToPlayLast.value(), new long[]{id1, id2}));//准备播放下一首
                // MC.getInstance(getContext()).playPrev();
                break;
            case R.id.bottom_action_bar_next:
                // MC.getInstance(getContext()).playNext();
                long id3 = MC.getInstance(getContext()).getCurrentAudioId();
                long id4 = MC.getInstance(getContext()).getNextMusicInfo(false).audioId;
                EventBus.getDefault().post(new EventMessage(AudioStation.preToPlayNext.value(), new long[]{id3, id4}));//准备播放下一首
                break;
            case R.id.bottom_select_cd_open:
                SheetRecentFragment sheetRecentFragment = new SheetRecentFragment();
                Intent intent2 = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable("audioSheets",MusicDataManager.getSongSheetById(mContext,MusicDataManager.getInstance(mContext).getCurrentSheetId()));
                intent2.putExtras(bundle);
                //startFragment(sheetRecentFragment, ((QuickActivity)mContext).getContentViewId(), intent2);
                //SheetRecentDialog sheetRecentDialog = new SheetRecentDialog(getActivity());
                //sheetRecentDialog.show();

                SheetListFragment sheetListFragment = new SheetListFragment();
                startFragment(sheetListFragment,((QuickActivity)getContext()).getContentViewId(), intent2);
                break;
        }
    }
}
