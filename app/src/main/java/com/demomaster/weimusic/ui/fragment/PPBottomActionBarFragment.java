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
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.activity.MainActivity;
import com.demomaster.weimusic.activity.SportActivity;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.player.service.MC;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDMulSheetDialog;
import cn.demomaster.huan.quickdeveloplibrary.widget.square.SquareImageView;
import cn.demomaster.qdlogger_library.QDLogger;
import cn.demomaster.qdrouter_library.base.fragment.QuickFragment;

public class PPBottomActionBarFragment extends QuickFragment {

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
    @BindView(R.id.bottom_select_cd_open)
    SquareImageView bottom_select_cd_open;
    @BindView(R.id.iv_info)
    ImageView iv_info;

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
            return inflater.inflate(R.layout.layout_bottom_portrait,
                    container);
        } else {
            return inflater.inflate(R.layout.layout_bottom,
                    container);
        }
    }

    @Override
    public void initView(View view) {
        ButterKnife.bind(this, view);
        iv_info.setOnClickListener(this);
        mPrev.setOnClickListener(this);
        bottom_action_bar_play.setSelected(MC.getInstance(getContext()).isPlaying());
        bottom_action_bar_play.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                // MC.getInstance(getContext()).doPauseResume();
                boolean isPlaying = MC.getInstance(getContext()).isPlaying();
                //bottom_action_bar_play.setSelected(!isPlaying);
                if (!isPlaying) {
                    //play();
                    EventBus.getDefault().post(new EventMessage(AudioStation.preToPlay.value()));//????????????
                } else {
                    // pause();
                    EventBus.getDefault().post(new EventMessage(AudioStation.preToPause.value()));//????????????
                }
            }
        });
        mNext.setOnClickListener(this);
        bottom_select_cd_open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).showSheetFragment();
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

    private void showMulMenuDialog() {
        String[] menus = {"?????????", "?????????????????????", "10??????", "20??????", "30??????", "60??????", "90??????", "120??????", "?????????"};
        new QDMulSheetDialog.MenuBuilder(getContext()).setData(menus).setOnDialogActionListener(new QDMulSheetDialog.OnDialogActionListener() {
            @Override
            public void onItemClick(QDMulSheetDialog dialog, int position, List<String> data) {
                dialog.dismiss();
            }
        })
                .create()
                .show();
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

    //????????????????????????????????????????????????????????????
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventMessage message) {
        AudioStation station = AudioStation.getEnum(message.code);
        if (station != null) {
            QDLogger.println("??????:" + station.getDesc());
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
                case REPEATMODE_CHANGED://??????????????????
                    /*mRepeat.setSequenceType(MC.getInstance(getContext()).getRepeatMode());
                    iv_info.setImageResource();*/
                    break;
            }
        } else {
            QDLogger.println("??????2:" + message);
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
                EventBus.getDefault().post(new EventMessage(AudioStation.preToPlayLast.value(), new long[]{id1, id2}));//?????????????????????
                // MC.getInstance(getContext()).playPrev();
                break;
            case R.id.bottom_action_bar_next:
                // MC.getInstance(getContext()).playNext();
                long id3 = MC.getInstance(getContext()).getCurrentAudioId();
                long id4 = MC.getInstance(getContext()).getNextMusicInfo(false).audioId;
                EventBus.getDefault().post(new EventMessage(AudioStation.preToPlayNext.value(), new long[]{id3, id4}));//?????????????????????
                break;
        }
    }
}
