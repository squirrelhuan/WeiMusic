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
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.activity.ThemeActivity;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.constant.ThemeConstants;
import com.demomaster.weimusic.lrc.DefaultLrcBuilder;
import com.demomaster.weimusic.lrc.ILrcBuilder;
import com.demomaster.weimusic.lrc.ILrcViewListener;
import com.demomaster.weimusic.lrc.LrcRow;
import com.demomaster.weimusic.lrc.LrcView;
import com.demomaster.weimusic.model.AudioInfo;
import com.demomaster.weimusic.player.helpers.utils.ThemeUtils;
import com.demomaster.weimusic.player.service.MC;
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
import cn.demomaster.huan.quickdeveloplibrary.bamboo.Bamboo;
import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.operatguid.GuiderView;
import cn.demomaster.huan.quickdeveloplibrary.util.AnimationUtil;
import cn.demomaster.huan.quickdeveloplibrary.util.DisplayUtil;
import cn.demomaster.huan.quickdeveloplibrary.util.QDFileUtil;
import cn.demomaster.huan.quickdeveloplibrary.widget.popup.QDTipPopup;
import cn.demomaster.huan.quickdeveloplibrary.widget.popup.QuickMessage;
import cn.demomaster.qdlogger_library.QDLogger;

import static com.demomaster.weimusic.constant.AudioStation.preToPlayNext;

public class MainFragment2 extends Fragment implements OnClickListener {

    private AudioInfo currentMusic = null;
    @BindView(R.id.button_composer_with)
    Button button_composer_with;
    @BindView(R.id.button_composer_place)
    Button button_composer_place;
    @BindView(R.id.button_composer_music)
    Button button_composer_music;
    @BindView(R.id.iv_music_model)
    AudioPlayerOrderBar mRepeat;


    // ?????????LrcView?????????????????????
    @BindView(R.id.lrcView)
    LrcView mLrcView;
    @BindView(R.id.iv_music_bar3)
    AudioPlayerView audioPlayerView;
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

        audioPlayerView.reseatState();
        if (MC.getInstance(getContext()).isPlaying()) {
            // ???????????????????????????
            final long timePassed = MC.getInstance(getContext()).Position();
            // ????????????
            // mLrcView.seekLrcToTime(timePassed);
        }

        if (MC.getInstance(getContext()).isFavorite(MC.getInstance(getContext()).getCurrentAudioId())) {
            button_composer_place.setBackgroundDrawable(ResourcesCompat.getDrawable(getActivity().getResources(), R.drawable.record_card_like_s, null));
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
        audioPlayerView.reseatState();
        if (MC.getInstance(getContext()).isPlaying()) {
            // ???????????????????????????
            final long timePassed = MC.getInstance(getContext()).Position();//MyApp.natureBinder.getCurrentPosition();
            // ????????????
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
        rel_music.setPadding(0, DisplayUtil.getStatusBarHeight(getContext()), 0, 0);
        audio_player_bar.setOnClickListener(this);
        // audio_player_bar.setOnPlayerStateChangeListener(onPlayerStateChangeListener);
        // iv_music_bar3.setOnPlayerStateChangeListener(onPlayerStateChangeListener2);
        audioPlayerView.setOnClickActionListener(new AudioPlayerView.OnClickActionListener() {
            @Override
            public void onCenterClick() {
                QDLogger.println("???????????? start");
                if (sml_menu.isShowing()) {
                    sml_menu.stopAnimation();
                } else {
                    sml_menu.startAnimation();
                }
            }

            @Override
            public void onEdgeClick() {
                QDLogger.println("???????????? end");
                sml_menu.stopAnimation();
            }
        });
        audioPlayerView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    @SuppressWarnings("deprecation")
                    public void onGlobalLayout() {
                        audioPlayerView.getViewTreeObserver()
                                .removeGlobalOnLayoutListener(this);
                        sml_menu.setStartRadius(audioPlayerView.getHeight() / 2 - DisplayUtil.dip2px(getContext(), 90));
                        sml_menu.setEndRadius(audioPlayerView.getHeight() / 2 - DisplayUtil.dip2px(getContext(), 15));
                        sml_menu.setAnimatDuration(200);
                    }
                });
        mRepeat.setOnClickListener(this);
        /*AnimationUtil.addScaleAnimition(mRepeat, new OnClickListener() {
            @Override
            public void onClick(View view) {
                MC.getInstance(getContext()).doRepeat();
            }
        });*/

        /*AnimationUtil.addTiltAnimition(iv_music_bar3, new OnClickListener() {
            @Override
            public void onClick(View view) {
                iv_music_bar3.isCustom = !iv_music_bar3.isCustom;
            }
        });*/
        initialButton();
        // ??????????????????LrcView???????????????????????????
        mLrcView.setListener(new ILrcViewListener() {
            // ??????????????????????????????????????????????????????,???????????????????????????????????????
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
        // ???assets?????????????????????????????????
        String lrc = QDFileUtil.getFromAssets(getContext(), "test.lrc");
        //lrc = LrcGet.query("??????", "?????????");
        //String lrc = getFromSDCard(file.getAbsoluteFile().toString());
        // ?????????????????????
        ILrcBuilder builder = new DefaultLrcBuilder();
        // ??????????????????LrcRow??????
        List<LrcRow> rows = builder.getLrcRows(lrc);
        // ??????????????????????????????mLrcView????????????
        mLrcView.setLrc(rows);

        // ???????????????????????????????????????
        //beginLrcPlay();
        hasDownload_lrc = false;
    }

    public void refreshUI() {
        if (MC.getInstance(getContext()).isFavorite(MC.getInstance(getContext()).getCurrentAudioId())) {
            button_composer_place.setBackgroundDrawable(ResourcesCompat.getDrawable(getActivity().getResources(), R.drawable.record_card_like_s, null));
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
            //QDLogger.println("???????????? artistName=" + artistName + ",albumName=" + albumName + ",trackName=" + trackName);

            //String albumId = String.valueOf(MC.getInstance(getContext()).getCurrentAlbumId());
            tv_music_name.setText(trackName);
            tv_singer_name.setText(artistName);
        }

        if (MC.getInstance(getContext()).isPlaying()) {
            if (currentMusic != null) {
                //getLrc(currentMusic.getTitle(), currentMusic.getArtist());
            }
            // ???????????????????????????
            final long timePassed = MC.getInstance(getContext()).Position();/*MyApp.natureBinder.getCurrentPosition()*/
            // ????????????
            // mLrcView.seekLrcToTime(timePassed);
        }
        audio_player_bar.reseatState();
        audioPlayerView.reseatState();
        // animation(1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_music_model:
                MC.getInstance(getContext()).doRepeat();
                QuickMessage qdTipPopup = new QuickMessage.Builder(getContext())
                        .setBackgroundRadius(10)
                        .setBackgroundColor(getResources().getColor(R.color.transparent_dark_77))
                        .setPadding(DisplayUtil.dip2px(getContext(),20))
                        .setMessage(MC.getInstance(getContext()).getRepeatMode().getName())
                        .create();
                qdTipPopup.showTip(v, GuiderView.Gravity.CENTER,2000);
                break;
        }
    }

    /**
     * ???????????????????????????????????????
     */
    public void beginLrcPlay() {
        /*
         * mPlayer = new MediaPlayer(); try {
         * mPlayer.setDataSource(MusicLoader.instance(getContentResolver())
         * .getMusicList().get(10).getUrl() getAssets().openFd("test.mp3"
         * ).getFileDescriptor() ); // ????????????????????????
         * mPlayer.setOnPreparedListener(new OnPreparedListener() { // ????????????
         * public void onPrepared(MediaPlayer mp) { mp.start(); if (mTimer ==
         * null) { mTimer = new Timer(); mTask = new LrcTask();
         * mTimer.scheduleAtFixedRate(mTask, 0, mPalyTimerDuration); } } }); //
         * ???????????????????????? mPlayer.setOnCompletionListener(new OnCompletionListener() {
         * public void onCompletion(MediaPlayer mp) { stopLrcPlay(); } }); //
         * ?????????????????? mPlayer.prepare(); // ?????????????????? mPlayer.start(); } catch
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
        if (MC.getInstance(getContext()).getCurrentInfo() != null) {
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

    float lrcView_Y = 0;

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
                    button_composer_place.setBackgroundDrawable(ResourcesCompat.getDrawable(getActivity().getResources(), R.drawable.record_card_like_s, null));
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
                    mLrcView.setY(100 * progress + lrcView_Y);
                } else {
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
                getActivity().startActivity(new Intent(getActivity(), ThemeActivity.class));
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
                    audioPlayerView.startChangeSong(null);
                    break;
                case audio_source_change_last:
                    audioPlayerView.reverseBackChangeSong(null);
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
                    if (sml_menu != null && sml_menu.isShowing()) {
                        MotionEvent ev = (MotionEvent) message.obj;
                        int[] location;
                        location = new int[2];
                        sml_menu.getLocationOnScreen(location);//???????????????????????????????????????
                        if (ev.getX() < location[0] || ev.getY() < location[1]
                                || ev.getX() > (location[0] + sml_menu.getWidth())
                                || ev.getY() > (location[1] + sml_menu.getHeight())) {
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
                case REPEATMODE_CHANGED://??????????????????
                    mRepeat.setSequenceType(MC.getInstance(getContext()).getRepeatMode());
                    break;
            }
        }
    }

    private void playLast() {
        QDLogger.println("??????????????? " + preToPlayNext);
        Bamboo bamboo = new Bamboo();
        bamboo.add(new Bamboo.Node() {
            @Override
            public void doJob(Bamboo.Node node, Object... result) {
                //1??????
                QDLogger.println("1?????? ");
                MC.getInstance(getContext()).pause();
                //2???????????????
                QDLogger.println("2??????????????? ");

                long audioId1 = MC.getInstance(getContext()).getCurrentAudioId();
                long audioId2 = MC.getInstance(getContext()).getNextMusicInfo(true).getAudioId();
                audioPlayerView.changeAudio(audioId1,
                        audioId2);

                MC.getInstance(getContext()).loadLast();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        submit();
                    }
                }, 150);
            }
        }).add(new Bamboo.Node() {
            @Override
            public void doJob(Bamboo.Node node, Object... result) {
                //3??????
                QDLogger.println("3??????");
                MC.getInstance(getContext()).play();
            }
        });
        bamboo.start();
    }

    /**
     * ??????????????????
     */
    private void playNext() {
        /*QDLogger.println("??????????????? "+preToPlayNext);
        audio_player_bar.stop(new AudioPlayerBar.AnimationCall(AudioPlayerBar.ActionType.pause) {
            @Override
            public void call() {
                QDLogger.println("????????????");
                iv_music_bar3.startChangeSong(null);
                audio_player_bar.start(new AudioPlayerBar.AnimationCall(AudioPlayerBar.ActionType.play) {
                    @Override
                    public void call() {
                        QDLogger.println("???????????? ");
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
                //1??????
                QDLogger.println("1?????? ");
                MC.getInstance(getContext()).pause();
                //2???????????????
                QDLogger.println("2??????????????? ");

                long audioId1 = MC.getInstance(getContext()).getCurrentAudioId();
                long audioId2 = MC.getInstance(getContext()).getNextMusicInfo(false).getAudioId();
                audioPlayerView.changeAudio(audioId1, audioId2);
                MC.getInstance(getContext()).loadNext();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        submit();
                    }
                }, 150);
            }
        }).add(new Bamboo.Node() {
            @Override
            public void doJob(Bamboo.Node node, Object... result) {
                //3??????
                QDLogger.println("3??????");
                MC.getInstance(getContext()).play();
            }
        });
        bamboo.start();

        /*Bamboo bamboo = new Bamboo();
        bamboo.add(new Bamboo.Node() {
            @Override
            public void doJob(Bamboo.Node node, Object... result) {
                QDLogger.println("??????????????? "+preToPlayNext);
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
                QDLogger.println("???????????? ??????");
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
     * ????????????
     */
    private void preToPause() {
        MC.getInstance(getContext()).pause();
        QDLogger.println("?????? ??????");
        audioPlayerView.pause(null);
        audio_player_bar.stop(null);
    }

    /**
     * ????????????
     */
    private void preToPlay() {
        Bamboo bamboo = new Bamboo();
        bamboo.add(new Bamboo.Node() {
            @Override
            public void doJob(Bamboo.Node node, Object... result) {
                // QDLogger.println("???????????? canpaly="+MC.getInstance(getContext()).canPlay());
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
                QDLogger.println("???????????? ");
                audioPlayerView.startAnimation();
                MC.getInstance(getContext()).play();
            }
        });
        bamboo.start();
    }

    /**
     * ????????????
     */
    private void refreshCover() {
        ThemeConstants.CoverType coverType = ThemeUtil.getCoverType();
        if (coverType == ThemeConstants.CoverType.withMusic) {
            audioPlayerView.reSeat();
        } else {
            audioPlayerView.init();
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
