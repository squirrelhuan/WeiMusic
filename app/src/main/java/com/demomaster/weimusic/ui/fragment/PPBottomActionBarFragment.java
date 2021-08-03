package com.demomaster.weimusic.ui.fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.player.helpers.MusicHelper;
import com.demomaster.weimusic.player.service.MC;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.widget.square.SquareImageView;
import cn.demomaster.qdlogger_library.QDLogger;

public class PPBottomActionBarFragment extends BaseFragment {

    @BindView(R.id.audio_player_total_time)
    TextView mTotalTime;
    @BindView(R.id.audio_player_current_time)
    TextView mCurrentTime;
    @BindView(R.id.bottom_action_bar_play)
    CheckBox bottom_action_bar_play;
    @BindView(R.id.bottom_action_bar_previous)
    SquareImageView mPrev;
    @BindView(R.id.bottom_action_bar_next)
    SquareImageView mNext;
    @BindView(R.id.sb_music_process)
    SeekBar mProgress;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public View setContentUI(LayoutInflater inflater, ViewGroup container) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            return inflater.inflate(R.layout.layout_bottom_portrait,
                    container);
        } else {
            return inflater.inflate(R.layout.layout_bottom,
                    container);
        }
    }

    @Override
    public void initView(View view) {
        mPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new EventMessage(AudioStation.preToPlayLast.value()));//准备播放下一首
                // MC.getInstance(getContext()).playPrev();
            }
        });
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
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // MC.getInstance(getContext()).playNext();
                EventBus.getDefault().post(new EventMessage(AudioStation.preToPlayNext.value()));//准备播放下一首
            }
        });

        mProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b)
                    MC.getInstance(getContext()).seekProgress((float) i / mProgress.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mProgress.setMax(1000);
    }

    public void refreshUI() {
        try {
            if (MC.getInstance(getContext()).getCurrentInfo() != null) {
                long duration = MC.getInstance(getContext()).getCurrentInfo().getDuration();
                long position = MC.getInstance(getContext()).getPosition();
                mCurrentTime.setText(MC.makeTimeString(getActivity(), position / 1000));
                mTotalTime.setText(MC.getInstance(getContext()).makeTimeString(getActivity(), duration / 1000));
                int p = (int) (position / (duration * 1f) * 1000);
                mProgress.setProgress(p);
                bottom_action_bar_play.setSelected(MC.getInstance(getContext()).isPlaying());
            }
        } catch (Exception e) {
            mCurrentTime.setText("0:00");
            mProgress.setProgress(1000);
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
}
