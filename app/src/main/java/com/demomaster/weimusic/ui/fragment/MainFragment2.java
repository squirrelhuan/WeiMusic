package com.demomaster.weimusic.ui.fragment;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.activity.SettingActivity;
import com.demomaster.weimusic.bamboo.Bamboo;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.constant.ThemeConstants;
import com.demomaster.weimusic.lrc.DefaultLrcBuilder;
import com.demomaster.weimusic.lrc.ILrcBuilder;
import com.demomaster.weimusic.lrc.ILrcViewListener;
import com.demomaster.weimusic.lrc.LrcRow;
import com.demomaster.weimusic.lrc.LrcView;
import com.demomaster.weimusic.model.MusicInfo;
import com.demomaster.weimusic.player.helpers.utils.ThemeUtils;
import com.demomaster.weimusic.player.service.MC;
import com.demomaster.weimusic.util.AnimationUtil;
import com.demomaster.weimusic.util.ThemeUtil;
import com.demomaster.weimusic.view.AudioPlayerBar;
import com.demomaster.weimusic.view.AudioPlayerOrderBar;
import com.demomaster.weimusic.view.AudioPlayerView;
import com.demomaster.weimusic.view.SurroundMenuLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.demomaster.huan.quickdeveloplibrary.base.activity.QDActivity;
import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.util.DisplayUtil;
import cn.demomaster.huan.quickdeveloplibrary.util.QDFileUtil;
import cn.demomaster.qdlogger_library.QDLogger;

//import static com.demomaster.weimusic.constant.Constants.Theme_Cover_Type_ByMusic;
import static com.demomaster.weimusic.constant.AudioStation.preToPlayLast;
import static com.demomaster.weimusic.constant.AudioStation.preToPlayNext;

public class MainFragment2 extends Fragment implements OnClickListener {

    private MusicInfo currentMusic = null;
    @BindView(R.id.button_composer_with)
    Button button_composer_with;
    @BindView(R.id.button_composer_place)
    Button button_composer_place;
    @BindView(R.id.button_composer_music)
    Button button_composer_music;
    @BindView(R.id.iv_music_model)
    AudioPlayerOrderBar mRepeat;


    // 自定义LrcView，用来展示歌词
    @BindView(R.id.lrcView)
    LrcView mLrcView;
    @BindView(R.id.iv_music_bar3)
    AudioPlayerView iv_music_bar3;
    @BindView(R.id.audio_player_bar)
    AudioPlayerBar audio_player_bar;
    @BindView(R.id.sml_menu)
    SurroundMenuLayout sml_menu;
    @BindView(R.id.tv_music_name)
    TextView tv_music_name;
    @BindView(R.id.tv_singer_name)
    TextView tv_singer_name;
    @BindView(R.id.rel_music)
    RelativeLayout rel_music;
    @BindView(R.id.ll_cd_info)
    ViewGroup ll_cd_info;
    @BindView(R.id.cl_001)
    ViewGroup cl_001;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    private Unbinder unbinder;
    View rootView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            rootView = inflater.inflate(R.layout.fragment_main_02_portrait, container, false);
        } else {
            rootView = inflater.inflate(R.layout.fragment_main_02, container, false);
        }
        unbinder = ButterKnife.bind(this, rootView);
        initView();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUI();
    }

    /**
     * Update what's playing
     */
    private void updateMusicInfo() {
        mRepeat.setSequenceType(MC.getInstance(getContext()).getRepeatMode());
        if (MC.getInstance(getContext()).getCurrentInfo() != null) {
            String artistName = MC.getInstance(getContext()).getCurrentInfo().getArtist();
            String albumName = MC.getInstance(getContext()).getCurrentInfo().getAlbum();
            String trackName = MC.getInstance(getContext()).getCurrentInfo().getTitle();

            tv_music_name.setText(trackName);
            tv_singer_name.setText(albumName + " - " + artistName);
            tv_music_name.setSelected(true);
        }

       /* ImageInfo mInfo = new ImageInfo();
        mInfo.type = TYPE_ALBUM;
        mInfo.size = SIZE_THUMB;
        mInfo.source = SRC_FIRST_AVAILABLE;
        mInfo.data = new String[]{albumId, artistName, albumName};*/

        //ImageProvider.getInstance( getActivity() ).loadImage( mAlbumArt, mInfo );
        // Theme chooser
        ThemeUtils.setTextColor(getActivity(), tv_music_name, "audio_player_text_color");
        ThemeUtils.setTextColor(getActivity(), tv_singer_name, "audio_player_text_color");

        iv_music_bar3.reseatState();
        if (MC.getInstance(getContext()).isPlaying()) {
            // 获取歌曲播放的位置
            final long timePassed = MC.getInstance(getContext()).Position();
            // 滚动歌词
            // mLrcView.seekLrcToTime(timePassed);
        }

        if (MC.getInstance(getContext()).isFavorite(MC.getInstance(getContext()).getCurrentAudioId())) {
            button_composer_place.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.record_card_like_s));
        } else {
            TypedValue typedValue = new TypedValue();
            getActivity().getTheme().resolveAttribute(R.attr.record_card_like_h, typedValue, true);
            button_composer_place.setBackgroundResource(typedValue.resourceId);
            //ThemeUtils.setActionBarItem(this, favorite, "apollo_favorite_normal");
        }
    }

    /**
     * Update state of playing
     */
    private void updateMusicState() {
        iv_music_bar3.reseatState();
        if (MC.getInstance(getContext()).isPlaying()) {
            // 获取歌曲播放的位置
            final long timePassed = MC.getInstance(getContext()).Position();//MyApp.natureBinder.getCurrentPosition();
            // 滚动歌词
            // mLrcView.seekLrcToTime(timePassed);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //WeakReference<VisualizerView> mView = new WeakReference<VisualizerView>((VisualizerView) rootView.findViewById(R.id.visualizerView));
        //VisualizerUtils.updateVisualizerView(mView);
    }

    public void initView() {
        rel_music.setPadding(0,DisplayUtil.getStatusBarHeight(getContext()),0,0);
        audio_player_bar.setOnClickListener(this);
       // audio_player_bar.setOnPlayerStateChangeListener(onPlayerStateChangeListener);
       // iv_music_bar3.setOnPlayerStateChangeListener(onPlayerStateChangeListener2);
        iv_music_bar3.setOnClickActionListener(new AudioPlayerView.OnClickActionListener() {
            @Override
            public void onCenterClick() {
                QDLogger.println("菜单动画 start");
                if(sml_menu.isShowing()){
                    sml_menu.stopAnimation();
                }else {
                    sml_menu.startAnimation();
                }
            }

            @Override
            public void onEdgeClick() {
                QDLogger.println("菜单动画 end");
                sml_menu.stopAnimation();
            }
        });
        iv_music_bar3.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    @SuppressWarnings("deprecation")
                    public void onGlobalLayout() {
                        iv_music_bar3.getViewTreeObserver()
                                .removeGlobalOnLayoutListener(this);
                        sml_menu.setStartRadius(iv_music_bar3.getHeight() / 2 - DisplayUtil.dip2px(getContext(), 90));
                        sml_menu.setEndRadius(iv_music_bar3.getHeight() / 2 - DisplayUtil.dip2px(getContext(), 15));
                        sml_menu.setAnimatDuration(200);
                    }
                });

        AnimationUtil.addScaleAnimition(mRepeat, new OnClickListener() {
            @Override
            public void onClick(View view) {
                MC.getInstance(getContext()).doRepeat();
                view.postInvalidate();
            }
        });

        /*AnimationUtil.addTiltAnimition(iv_music_bar3, new OnClickListener() {
            @Override
            public void onClick(View view) {
                iv_music_bar3.isCustom = !iv_music_bar3.isCustom;
            }
        });*/
        initialButton();
        // 设置自定义的LrcView上下拖动歌词时监听
        mLrcView.setListener(new ILrcViewListener() {
            // 当歌词被用户上下拖动的时候回调该方法,从高亮的那一句歌词开始播放
            public void onLrcSeeked(int newPosition, LrcRow row) {
                /*
                 * if (mPlayer != null) { Log.d(TAG, "onLrcSeeked:" + row.time);
                 * mPlayer.seekTo((int) row.time); }
                 */
            }
        });
    }
    boolean hasDownload_lrc = false;
    public void initLrc() {
        // 从assets目录下读取歌词文件内容
        String lrc = QDFileUtil.getFromAssets(getContext(), "test.lrc");
        //lrc = LrcGet.query("心跳", "王力宏");
        //String lrc = getFromSDCard(file.getAbsoluteFile().toString());
        // 解析歌词构造器
        ILrcBuilder builder = new DefaultLrcBuilder();
        // 解析歌词返回LrcRow集合
        List<LrcRow> rows = builder.getLrcRows(lrc);
        // 将得到的歌词集合传给mLrcView用来展示
        mLrcView.setLrc(rows);

        // 开始播放歌曲并同步展示歌词
        //beginLrcPlay();
        hasDownload_lrc = false;
    }

    public void refreshUI() {
        if (MC.getInstance(getContext()).isFavorite(MC.getInstance(getContext()).getCurrentAudioId())) {
            button_composer_place.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.record_card_like_s));
        } else {
            TypedValue typedValue = new TypedValue();
            getActivity().getTheme().resolveAttribute(R.attr.record_card_like_h, typedValue, true);
            button_composer_place.setBackgroundResource(typedValue.resourceId);
            //buttonFavorite.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.record_card_like_h));
            // Theme chooser
            //ThemeUtils.setActionBarItem(this, favorite, "apollo_favorite_normal");
        }
        mRepeat.setSequenceType(MC.getInstance(getContext()).getRepeatMode());
        if (MC.getInstance(getContext()).getCurrentInfo() != null) {
            String artistName = MC.getInstance(getContext()).getCurrentInfo().getArtist();
            String albumName = MC.getInstance(getContext()).getCurrentInfo().getAlbum();
            String trackName = MC.getInstance(getContext()).getCurrentInfo().getTitle();
            QDLogger.println("当前歌曲 artistName=" + artistName + ",albumName=" + albumName + ",trackName=" + trackName);

            //String albumId = String.valueOf(MC.getInstance(getContext()).getCurrentAlbumId());
            tv_music_name.setText(trackName);
            tv_singer_name.setText(artistName);
        }

        if (MC.getInstance(getContext()).isPlaying()) {
            if (currentMusic != null) {
                //getLrc(currentMusic.getTitle(), currentMusic.getArtist());
            }
            // 获取歌曲播放的位置
            final long timePassed = MC.getInstance(getContext()).Position();/*MyApp.natureBinder.getCurrentPosition()*/
            // 滚动歌词
           // mLrcView.seekLrcToTime(timePassed);
        }
        audio_player_bar.reseatState();
        iv_music_bar3.reseatState();
        // animation(1);
    }

    @Override
    public void onClick(View v) {

    }

    /**
     * 开始播放歌曲并同步展示歌词
     */
    public void beginLrcPlay() {
        /*
         * mPlayer = new MediaPlayer(); try {
         * mPlayer.setDataSource(MusicLoader.instance(getContentResolver())
         * .getMusicList().get(10).getUrl() getAssets().openFd("test.mp3"
         * ).getFileDescriptor() ); // 准备播放歌曲监听
         * mPlayer.setOnPreparedListener(new OnPreparedListener() { // 准备完毕
         * public void onPrepared(MediaPlayer mp) { mp.start(); if (mTimer ==
         * null) { mTimer = new Timer(); mTask = new LrcTask();
         * mTimer.scheduleAtFixedRate(mTask, 0, mPalyTimerDuration); } } }); //
         * 歌曲播放完毕监听 mPlayer.setOnCompletionListener(new OnCompletionListener() {
         * public void onCompletion(MediaPlayer mp) { stopLrcPlay(); } }); //
         * 准备播放歌曲 mPlayer.prepare(); // 开始播放歌曲 mPlayer.start(); } catch
         * (IllegalArgumentException e) { e.printStackTrace(); } catch
         * (IllegalStateException e) { e.printStackTrace(); } catch (IOException
         * e) { e.printStackTrace(); }
         */
    }


    /**
     * @return Share intent
     * @throws RemoteException
     */
    private String shareCurrentTrack() {
        Intent shareIntent = new Intent();
        String currentTrackMessage = null;
        if(MC.getInstance(getContext()).getCurrentInfo()!=null) {
            currentTrackMessage = getResources().getString(R.string.now_listening_to) + " "
                    + MC.getInstance(getContext()).getCurrentInfo().getTitle() + " " + getResources().getString(R.string.by) + " "
                    + MC.getInstance(getContext()).getCurrentInfo().getArtist();

            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, currentTrackMessage);
            startActivity(Intent.createChooser(shareIntent,
                    getResources().getString(R.string.share_track_using)));
        }
        return currentTrackMessage;
    }
    float lrcView_Y =0;
    private void initialButton() {
        mLrcView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    @SuppressWarnings("deprecation")
                    public void onGlobalLayout() {
                        mLrcView.getViewTreeObserver()
                                .removeGlobalOnLayoutListener(this);
                        lrcView_Y = mLrcView.getY();
                    }
                });
        button_composer_with.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sml_menu.stopAnimation();
                shareCurrentTrack();
            }
        });
        button_composer_place.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sml_menu.stopAnimation();
                MC.getInstance(getContext()).toggleFavorite();
                getActivity().invalidateOptionsMenu();
                if (MC.getInstance(getContext()).isFavorite(MC.getInstance(getContext()).getCurrentAudioId())) {
                    button_composer_place.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.record_card_like_s));
                } else {
                    TypedValue typedValue = new TypedValue();
                    getActivity().getTheme().resolveAttribute(R.attr.record_card_like_h, typedValue, true);
                    button_composer_place.setBackgroundResource(typedValue.resourceId);
                    //buttonFavorite.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.record_card_like_h));
                    // Theme chooser
                    //ThemeUtils.setActionBarItem(this, favorite, "apollo_favorite_normal");
                }
            }
        });
        sml_menu.setOnProgressChangeListener(new SurroundMenuLayout.OnProgressChangeListener() {
            @Override
            public void onChanged(float progress) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mLrcView.setY(100 * progress+lrcView_Y);
                }else {
                    // cl_001.setY(ll_Y-100 * progress);
                    cl_001.setPivotX(cl_001.getMeasuredWidth() / 2);
                    cl_001.setPivotY(0);
                    cl_001.setRotationX(-9 * progress);
                }
            }
        });

        button_composer_music.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sml_menu.stopAnimation();
                getActivity().startActivity(new Intent(getActivity(), SettingActivity.class));
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventMessage message) {
        AudioStation station = AudioStation.getEnum(message.code);
        if (station != null) {
            switch (station) {
                case song_changed:
                    refreshUI();
                    refreshCover();
                    break;
                case audio_ready:
                    refreshUI();
                    refreshCover();
                    break;
                case audio_source_change_next:
                    iv_music_bar3.startChangeSong(null);
                    break;
                case audio_source_change_last:
                    iv_music_bar3.reverseBackChangeSong(null);
                    break;
                case PLAYSTATE_CHANGED:
                    refreshUI();
                    break;
                case service_ready:
                    refreshUI();
                    break;
                case Play:
                    refreshUI();
                    break;
                case Pause:
                    refreshUI();
                    break;
                case LoseFocus:
                    if (sml_menu != null&&sml_menu.isShowing()) {
                        MotionEvent ev = (MotionEvent) message.obj;
                        int[] location;
                        location = new int[2];
                        sml_menu.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐标
                        if(ev.getX()<location[0]||ev.getY()<location[1]
                                ||ev.getX()>(location[0]+sml_menu.getWidth())
                                ||ev.getY()>(location[1]+sml_menu.getHeight())){
                            sml_menu.stopAnimation();
                        }
                    }
                    break;
                case loadData:
                    refreshCover();
                    break;
                case ThemeCoverChange:
                    refreshCover();
                    break;
                case preToPlay:
                    preToPlay();
                    break;
                case preToPause:
                    preToPause();
                    break;
                case preToPlayNext:
                    playNext();
                    break;
                case preToPlayLast:
                    playLast();
                    break;
            }
        }
    }

    private void playLast() {
        QDLogger.println("播放上一首 "+preToPlayNext);
        Bamboo bamboo = new Bamboo();
        bamboo.add(new Bamboo.Node() {
            @Override
            public void doJob(Bamboo.Node node, Object... result) {
                //1暂停
                QDLogger.println("1暂停 ");
                MC.getInstance(getContext()).pause();
                //2更改音频源
                QDLogger.println("2更改音频源 ");
                MC.getInstance(getContext()).loadLast();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        submit();
                    }
                },150);
            }
        }).add(new Bamboo.Node() {
            @Override
            public void doJob(Bamboo.Node node, Object... result) {
                //3播放
                QDLogger.println("3播放");
                MC.getInstance(getContext()).play();
            }
        });
        bamboo.start();
    }

    private void playNext() {
        /*QDLogger.println("播放下一首 "+preToPlayNext);
        audio_player_bar.stop(new AudioPlayerBar.AnimationCall(AudioPlayerBar.ActionType.pause) {
            @Override
            public void call() {
                QDLogger.println("停止唱臂");
                iv_music_bar3.startChangeSong(null);
                audio_player_bar.start(new AudioPlayerBar.AnimationCall(AudioPlayerBar.ActionType.play) {
                    @Override
                    public void call() {
                        QDLogger.println("转动唱臂 ");
                        iv_music_bar3.startAnimation();
                        MC.getInstance(getContext()).playNext();
                    }
                });
            }
        });*/

        Bamboo bamboo = new Bamboo();
        bamboo.add(new Bamboo.Node() {
            @Override
            public void doJob(Bamboo.Node node, Object... result) {
                //1暂停
                QDLogger.println("1暂停 ");
                MC.getInstance(getContext()).pause();
                //2更改音频源
                QDLogger.println("2更改音频源 ");
                MC.getInstance(getContext()).loadNext();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        submit();
                    }
                },150);
            }
        }).add(new Bamboo.Node() {
            @Override
            public void doJob(Bamboo.Node node, Object... result) {
                //3播放
                QDLogger.println("3播放");
                MC.getInstance(getContext()).play();
            }
        });
        bamboo.start();

        /*Bamboo bamboo = new Bamboo();
        bamboo.add(new Bamboo.Node() {
            @Override
            public void doJob(Bamboo.Node node, Object... result) {
                QDLogger.println("播放下一首 "+preToPlayNext);
                audio_player_bar.stop(new AudioPlayerBar.AnimationCall(AudioPlayerBar.ActionType.pause) {
                    @Override
                    public void call() {
                        submit();
                    }
                });
            }
        }).add(new Bamboo.Node() {
            @Override
            public void doJob(Bamboo.Node node, Object... result) {
                QDLogger.println("切換歌曲 》》");
                iv_music_bar3.startChangeSong(null);
                audio_player_bar.start(new AudioPlayerBar.AnimationCall(AudioPlayerBar.ActionType.play) {
                    @Override
                    public void call() {
                        iv_music_bar3.startAnimation();
                        MC.getInstance(getContext()).playNext();
                    }
                });
            }
        });
        bamboo.start();*/
    }

    /**
     * 准备暂停
     */
    private void preToPause() {
        MC.getInstance(getContext()).pause();
        QDLogger.println("唱盘 停止");
        iv_music_bar3.pause(null);
        audio_player_bar.stop(null);
    }

    /**
     * 准备播放
     */
    private void preToPlay() {
        Bamboo bamboo = new Bamboo();
        bamboo.add(new Bamboo.Node() {
            @Override
            public void doJob(Bamboo.Node node, Object... result) {
               // QDLogger.println("旋转唱臂 canpaly="+MC.getInstance(getContext()).canPlay());
              // if(MC.getInstance(getContext()).canPlay()) {
                   audio_player_bar.start(new AudioPlayerBar.AnimationCall(AudioPlayerBar.ActionType.play) {
                       @Override
                       public void call() {
                           //QDLogger.println("call1= playMusic");
                           submit(2);
                       }
                   });
               //}
            }
        }).add(new Bamboo.Node() {
            @Override
            public void doJob(Bamboo.Node node, Object... result) {
                QDLogger.println("转动唱盘 ");
                iv_music_bar3.startAnimation();
                MC.getInstance(getContext()).play();
            }
        });
        bamboo.start();
    }

    /**
     * 刷新封面
     */
    private void refreshCover() {
        ThemeConstants.CoverType coverType = ThemeUtil.getCoverType();
        if(coverType== ThemeConstants.CoverType.withMusic){
            iv_music_bar3.reSeat();
        }else {
            iv_music_bar3.init();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
        EventBus.getDefault().unregister(this);
    }
}
