package com.demomaster.weimusic.player.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.telephony.TelephonyManager;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.constant.SequenceType;
import com.demomaster.weimusic.model.AudioInfo;
import com.demomaster.weimusic.model.AudioRecord;
import com.demomaster.weimusic.player.proxy.QuickProxy;

import org.greenrobot.eventbus.EventBus;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

import cn.demomaster.huan.quickdeveloplibrary.constant.AppConfig;
import cn.demomaster.huan.quickdeveloplibrary.helper.toast.QdToast;
import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.qdlogger_library.QDLogger;
import cn.demomaster.quickpermission_library.PermissionHelper;

import static com.demomaster.weimusic.activity.BaseActivity.PERMISSIONS;
import static com.demomaster.weimusic.constant.AudioStation.PLAYSTATE_CHANGED;
import static com.demomaster.weimusic.constant.AudioStation.Pause;
import static com.demomaster.weimusic.constant.AudioStation.Play;
import static com.demomaster.weimusic.constant.AudioStation.REPEATMODE_CHANGED;
import static com.demomaster.weimusic.constant.AudioStation.audio_ready;
import static com.demomaster.weimusic.constant.AudioStation.audio_source_change_last;
import static com.demomaster.weimusic.constant.AudioStation.audio_source_change_next;
import static com.demomaster.weimusic.constant.AudioStation.song_changed;
import static com.demomaster.weimusic.constant.SequenceType.Normal;
import static com.demomaster.weimusic.constant.SequenceType.REPEAT_ALL;
import static com.demomaster.weimusic.constant.SequenceType.REPEAT_CURRENT;
import static com.demomaster.weimusic.constant.SequenceType.Shuffle;

/**
 * Music Cotroll
 */
public class MC implements AudioManager.OnAudioFocusChangeListener {
    private Context mContext;
    private static QdMediaPlayer mPlayer;
    private static MusicDataManager dataManager;
    private static MC instance;
    private List<AudioInfo> mPlayList;

    public static MC getInstance(Context context) {
        if (instance == null) {
            instance = new MC(context);
        }
        return instance;
    }

    private MC(Context context) {
        mContext = context.getApplicationContext();
        recovery();
    }

    public void init() {
        initMediaPlayer();
        loadMusicList();
    }

    private AudioManager mAudioManager;
    private PowerManager.WakeLock mWakeLock;

    //??????????????????
    private void initMediaPlayer() {
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        initAudioFocus();
        mAudioManager.registerMediaButtonEventReceiver(new ComponentName(
                mContext.getPackageName(), MediaButtonIntentReceiver.class.getName()));

        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this
                .getClass().getName());
        mWakeLock.setReferenceCounted(false);
        mPlayer = new QdMediaPlayer();
        mPlayer.setWakeMode(mContext,
                PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setOnCompletionListener(onCompletionListener);
        mPlayer.setOnErrorListener(errorListener);
        mPlayer.setOnPreparedListener(onPreparedListener);
        mPlayer.setOnPlayerChangeListener(onPlayerChangeListener);
        //mPlayer.setOnCompletionListener(null);
    }

    private void initAudioFocus() {
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            playbackAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(this, new Handler())
                    .build();
        }
    }

    AudioAttributes playbackAttributes;
    AudioFocusRequest focusRequest;

    boolean loss_transient = false;//????????????????????????
    boolean isLoss_transient_isplaying;//????????????????????????????????????

    private void request() {
        int res = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            res = mAudioManager.requestAudioFocus(focusRequest);
        } else {
            res = mAudioManager.requestAudioFocus(this,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN);
        }
        //QDLogger.i("request="+res);
        loss_transient = false;
        if (res == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            QDLogger.e("????????????????????????");
        } else if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            QDLogger.i("????????????????????????");
        } else if (res == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) {
            QDLogger.i("????????????????????????");
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        QDLogger.w("onAudioFocusChange:" + focusChange);
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                //???????????????????????????????????????????????????????????????????????????
                if (loss_transient) {
                    QDLogger.w("????????????????????????????????????");
                    loss_transient = false;
                    if (isLoss_transient_isplaying) {//????????????????????????????????????????????????????????????????????????????????????
                        play();
                    }
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS://??????????????????
                QDLogger.w("????????????????????????????????????");
                loss_transient = false;
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                QDLogger.w("????????????????????????");
                loss_transient = true;
                isLoss_transient_isplaying = isPlaying();
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                //????????????????????????????????????????????????????????????????????????
                //*?????????????????????????????????????????????????????????????????????????????????????????????
                //*??????????????????????????????????????????????????????
                break;
        }
    }

    TelephonyManager mTelephonyManager;

    private void telephony() {
        if (mTelephonyManager == null)
            mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        BroadcastReceiver mPanelBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
                    int state = mTelephonyManager.getCallState();
                    switch (state) {
                        case TelephonyManager.CALL_STATE_IDLE:// ????????????
                            QDLogger.w("????????????...");
                            break;
                        case TelephonyManager.CALL_STATE_OFFHOOK: //?????????????????????
                            QDLogger.w("????????????...");
                            break;
                        case TelephonyManager.CALL_STATE_RINGING: //?????????????????????
                            QDLogger.w("????????????...");
                            break;
                    }
                    //sendMessage(jsonObject.toString());
                }
            }
        };

        IntentFilter panelFilter = new IntentFilter();
        panelFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        panelFilter.setPriority(Integer.MAX_VALUE);
        mContext.registerReceiver(mPanelBroadcastReceiver, panelFilter, null, null);
    }

    /**
     * ???app?????????????????????????????????????????????????????????app??????????????????
     */
    private void playComplete() {
        QDLogger.w("playComplete");
        mAudioManager.abandonAudioFocus(this);
    }

    //??????????????????
    public void loadMusicList() {
        dataManager = MusicDataManager.getInstance(mContext);
        boolean b = PermissionHelper.getInstance().getPermissionStatus(mContext, PERMISSIONS);
        if (b) {
            dataManager.loadData(mContext, new MusicDataManager.OnLoadDataListener() {
                @Override
                public void loadComplete(int ret, List<AudioInfo> audioInfoList) {
                    if (ret == 1) {
                        if (audioInfoList != null) {
                            mPlayList = new ArrayList<>();
                            mPlayList.addAll(audioInfoList);
                        }
                        recovery();
                        QDLogger.e("????????????????????????");
                        EventBus.getDefault().post(new EventMessage(AudioStation.loadData.value()));
                    }
                }
            });
        }
    }

    /**
     * ?????????reset????????????
     */
    MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            trackEnded();
        }
    };

    //????????????
    private void trackEnded() {
        QDLogger.println("????????????");
        if (mRepeatMode == REPEAT_CURRENT) {//????????????
            seek(0);
            play();
        } else {//???????????????
            playNext();
        }
    }

    MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            QDLogger.i("play onError" + what + "," + extra);
            switch (what) {
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                    mPlayer.release();
                    initMediaPlayer();
                    return true;
                default:
                    break;
            }
            return false;
        }
    };

    MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            QDLogger.println("??????????????????");
            request();
            savePlayState();
            EventBus.getDefault().post(new EventMessage(audio_ready.value()));
            //QDLogger.println("??????????????????  ??????");
            mp.start();
            EventBus.getDefault().post(new EventMessage(Play.value()));
        }
    };

    QdMediaPlayer.OnPlayerChangeListener onPlayerChangeListener = new QdMediaPlayer.OnPlayerChangeListener() {
        @Override
        public void onStart() {
            MusicNotification.getInstance(mContext).updateNotification();
        }

        @Override
        public void onPause() {
            EventBus.getDefault().post(new EventMessage(Pause.value()));
            savePlayState();
            MusicNotification.getInstance(mContext).updateNotification();
        }

        @Override
        public void onStop() {
            savePlayState();
            MusicNotification.getInstance(mContext).updateNotification();
        }

        @Override
        public void onComplete() {
            savePlayState();
            MusicNotification.getInstance(mContext).updateNotification();
        }

        @Override
        public void onError() {
            savePlayState();
            MusicNotification.getInstance(mContext).updateNotification();
        }

        @Override
        public void onDataSourceChanged() {
            EventBus.getDefault().post(new EventMessage(song_changed.value()));
        }
    };

    private SequenceType mRepeatMode = SequenceType.Normal;

    //??????????????????
    public SequenceType getRepeatMode() {
        return mRepeatMode;
    }

    public void setRepeatMode(SequenceType mRepeatMode) {
        this.mRepeatMode = mRepeatMode;
        EventBus.getDefault().post(new EventMessage(REPEATMODE_CHANGED.value()));
    }

    public boolean isPlaying() {
        if (mPlayer == null || !mPlayer.hasPrepared) {
            return false;
        }
        return mPlayer.isPlaying();
    }

    AudioInfo currentAudioInfo;

    /**
     * ??????????????????????????????
     *
     * @return
     */
    public AudioInfo getCurrentInfo() {
        return currentAudioInfo;
    }

    public long Position() {
        return -1;
    }

    /**
     * ??????????????????
     */
    public void doRepeat() {
        int mode = getRepeatMode().value();
        QDLogger.println("?????????????????????" + mode);
        //int shuffle = MusicHelper.mService.getShuffleMode();
        switch (SequenceType.getEnum(mode)) {
            case Unknow:
                setRepeatMode(Normal);
                break;
            case Normal:
                setRepeatMode(REPEAT_ALL);
                break;
            case REPEAT_ALL:
                setRepeatMode(Shuffle);
                break;
            case Shuffle:
                setRepeatMode(REPEAT_CURRENT);
                break;
            case REPEAT_CURRENT:
                setRepeatMode(Normal);
                break;
        }
    }

    //??????????????????
    public void recovery() {
        //??????????????????
        SequenceType sequenceType = MusicDataManager.getInstance(mContext).getRepeatMode();
        setRepeatMode(sequenceType);
        //????????????????????????????????????&id?????????
        AudioRecord record = MusicDataManager.getInstance(mContext).getPlayRecord();
        AudioInfo audioInfo = null;
        int seek = 0;
        if (record != null) {
            MusicDataManager.getInstance(mContext).setSheetId(mContext, record.getSheetId());
            if(record.getSheetId()==-1){
                MusicDataManager.getInstance(mContext).setCurrentSheetList(mPlayList);
            }
            audioInfo = MusicDataManager.getInstance(mContext).getMusicInfoByData(mContext, record.getData());
            if (record.getSongId() != -1) {
                audioInfo = MusicDataManager.getInstance(mContext).getMusicInfoById(mContext, record.getSongId());
            }
            QDLogger.println("?????????????????????????????????:" + record);
            if (audioInfo != null) {
                seek = (int) (record.getProgress() * audioInfo.duration);
            }
        }else {
            MusicDataManager.getInstance(mContext).setCurrentSheetList(mPlayList);
        }
        if (audioInfo == null) {
            audioInfo = MusicDataManager.getInstance(mContext).getFirstMusicInfo();
        }

        if (audioInfo != null&&record!=null) {
            if (record.getSheetId() != -1) {
                audioInfo.setSheetId(record.getSheetId());
            }
            currentAudioInfo = audioInfo;
            currentAudioInfo.setPosition(seek);
            EventBus.getDefault().post(new EventMessage(PLAYSTATE_CHANGED.value()));
        }
    }

    public long getCurrentAudioId() {
        if (currentAudioInfo == null) {
            return -1;
        }
        return currentAudioInfo.id;
    }

    public boolean isFavorite(Long id) {
        return MusicDataManager.getInstance(mContext).isFarorite(mContext, id);
    }

    public void toggleFavorite() {
        if (mPlayer != null && currentAudioInfo != null) {
            Long id = currentAudioInfo.getId();
            boolean b = isFavorite(id);
            if (!b) {
                MusicDataManager.getInstance(mContext).addFavorite(mContext, id);
            } else {
                MusicDataManager.getInstance(mContext).removeFavorite(mContext, id);
            }
        }
    }

    public void doPauseResume() {
        if (!isPlaying()) {
            play();
        } else {
            pause();
        }
    }

    public long getDuration() {
        //QDLogger.d("MC", "  ----->  getDuration");
        if (mPlayer != null) {
            if (mPlayer.hasPrepared) {
                return mPlayer.getDuration();
            }
        }
        return 0;
    }

    /**
     * ??????????????????
     */
    public void savePlayState() {
        MusicDataManager.getInstance(mContext).saveRepeatMode(mRepeatMode);
        if (currentAudioInfo != null) {
            AudioRecord record = new AudioRecord();
            record.setIndex(MusicDataManager.getInstance(mContext).indexOfArray(currentAudioInfo.id));
            if (mPlayer.getDuration() > 0) {
                record.setProgress((float) mPlayer.getCurrentPosition() / (float) mPlayer.getDuration());
            }
            record.setSheetId(currentAudioInfo.sheetId);
            record.setSongId(currentAudioInfo.id);
            record.setData(currentAudioInfo.getData());
            MusicDataManager.getInstance(mContext).savePlayRecord(record);
        }
    }

    public void stop() {
        QDLogger.println("????????????");
        mPlayer.stop();
    }

    public void pause() {
        QDLogger.println("????????????");
        if (isPlaying()) {
            mPlayer.pause();
        }
    }

    //??????????????????
    public void playByAudioByData(String data) {
        QDLogger.println("????????????id=" + data);
        if (currentAudioInfo == null || currentAudioInfo.getData() != data) {
            AudioInfo info = MusicDataManager.getInstance(mContext).getMusicInfoByData(mContext, data);
            if (info != null) {
                setDataSourceImpl(info);
            } else {
                QDLogger.e("????????????" + data + "?????????");
            }
        }
    }

    //???????????????
    private boolean setDataSourceImpl(AudioInfo info) {
        try {
            mPlayer.reset();
            currentAudioInfo = info;
            if (info == null) {
                QDLogger.e("?????????????????? ??????");
                return false;
            }
            String path = info.getData();
            QDLogger.println("?????????????????? path=" + path + ",info.position=" + info.position);
            if (path.startsWith("content://")) {
                mPlayer.setDataSource(mContext, Uri.parse(path));
            } else {
                mPlayer.setDataSource(path);
            }
            mPlayer.setOnCompletionListener(onCompletionListener);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //QDLogger.println("????????????????????????");
            mPlayer.prepare();
            mPlayer.seekTo(info.position);
        } catch (Exception ex) {
            if (ex instanceof FileNotFoundException) {
                QdToast.show("?????????????????????");
            } else {
                ex.printStackTrace();
            }
            return false;
        }
       /* Intent i = new Intent(
                AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
        i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, mContext.getPackageName());
        sendBroadcast(i);*/

        //VisualizerUtils.initVisualizer(player);
        return true;
    }

    //??????
    public void play() {
        request();
        if (!mPlayer.hasPrepared) {
            if (mPlayList != null && mPlayList.size() > 0) {
                setDataSourceImpl(getCurrentInfo());
            }
        }
        if (mPlayList != null && mPlayList.size() > 0) {
            QDLogger.println("??????");
            if (!mPlayer.isPlaying()) {
                mPlayer.start();
            }
            EventBus.getDefault().post(new EventMessage(Play.value()));
        } else {
            QDLogger.e("?????????????????? ");
        }
    }

    //???????????????
    public void playPrev() {
        QDLogger.println("MC", "???????????????:" + (currentAudioInfo == null ? "" : currentAudioInfo.title));
        loadLast();
        play();
    }

    //?????? ??????????????????
    public void loadLast() {
        loadAudioSource(true);
        EventBus.getDefault().post(new EventMessage(audio_source_change_last.value()));
    }

    //???????????????
    public void playNext() {
        loadNext();
        QDLogger.println("???????????????:" + (currentAudioInfo == null ? "" : currentAudioInfo.title));
        play();
    }

    /**
     * ??????????????? ??? ?????????/?????????
     *
     * @param isLast ?????????
     */
    public void loadAudioSource(boolean isLast) {
        QDLogger.e("??????????????? ??? ?????????/?????????");
        AudioInfo audioInfo = getNextMusicInfo(isLast);
        if (audioInfo != null) {
            setDataSourceImpl(audioInfo);
        } else {
            QDLogger.e("??????????????????");
        }
    }

    /**
     * ?????????????????????????????????
     *
     * @param isLast true????????????false?????????
     * @return
     */
    public AudioInfo getNextMusicInfo(boolean isLast) {
        AudioInfo audioInfo = null;
        if (mPlayList != null && mPlayList.size() > 0) {
            if (currentAudioInfo == null) {
                return MusicDataManager.getInstance(mContext).getFirstMusicInfo();
            }
            if (MusicDataManager.getInstance(mContext).getCurrentSheet() == null || MusicDataManager.getInstance(mContext).getCurrentSheet().size() < 1) {
                return null;
            }
            int songCount = MusicDataManager.getInstance(mContext).getCurrentSheet().size();
            int playIndex = MusicDataManager.getInstance(mContext).getIndexInQueue(currentAudioInfo.id);
            //QDLogger.i("gotoNext progress=" + playIndex + ",mRepeatMode=" + mRepeatMode);
            switch (mRepeatMode) {
                case Normal:
                    if (playIndex == 0) {
                        if (isLast) {//true????????????false?????????
                            audioInfo = MusicDataManager.getInstance(mContext).getLastMusicInfo();
                        } else {
                            audioInfo = MusicDataManager.getInstance(mContext).getNextMusicInfo(currentAudioInfo.id);
                        }
                    } else if (playIndex == songCount - 1) {
                        if (isLast) {//true????????????false?????????
                            audioInfo = MusicDataManager.getInstance(mContext).getPrevMusicInfo(currentAudioInfo.id);
                        } else {
                            audioInfo = MusicDataManager.getInstance(mContext).getFirstMusicInfo();
                        }
                    } else {
                        if (isLast) {
                            audioInfo = MusicDataManager.getInstance(mContext).getPrevMusicInfo(currentAudioInfo.id);
                        } else {
                            audioInfo = MusicDataManager.getInstance(mContext).getNextMusicInfo(currentAudioInfo.id);
                        }
                    }
                    break;
                case REPEAT_ALL:
                    if (playIndex == 0) {
                        if (isLast) {//true????????????false?????????
                            audioInfo = MusicDataManager.getInstance(mContext).getLastMusicInfo();
                        } else {
                            audioInfo = MusicDataManager.getInstance(mContext).getNextMusicInfo(currentAudioInfo.id);
                        }
                    } else if (playIndex == songCount - 1) {
                        if (isLast) {//true????????????false?????????
                            audioInfo = MusicDataManager.getInstance(mContext).getPrevMusicInfo(currentAudioInfo.id);
                        } else {
                            audioInfo = MusicDataManager.getInstance(mContext).getFirstMusicInfo();
                        }
                    } else {
                        if (isLast) {
                            audioInfo = MusicDataManager.getInstance(mContext).getPrevMusicInfo(currentAudioInfo.id);
                        } else {
                            audioInfo = MusicDataManager.getInstance(mContext).getNextMusicInfo(currentAudioInfo.id);
                        }
                    }
                    break;
                case Shuffle:
                    if (playIndex == 0) {
                        if (isLast) {//true????????????false?????????
                            audioInfo = MusicDataManager.getInstance(mContext).getLastMusicInfo();
                        } else {
                            audioInfo = MusicDataManager.getInstance(mContext).getNextMusicInfo(currentAudioInfo.id);
                        }
                    } else if (playIndex == songCount - 1) {
                        if (isLast) {//true????????????false?????????
                            audioInfo = MusicDataManager.getInstance(mContext).getPrevMusicInfo(currentAudioInfo.id);
                        } else {
                            audioInfo = MusicDataManager.getInstance(mContext).getFirstMusicInfo();
                        }
                    } else {
                        if (isLast) {
                            audioInfo = MusicDataManager.getInstance(mContext).getPrevMusicInfo(currentAudioInfo.id);
                        } else {
                            audioInfo = MusicDataManager.getInstance(mContext).getNextMusicInfo(currentAudioInfo.id);
                        }
                    }
                    break;
                case REPEAT_CURRENT:
                    audioInfo = currentAudioInfo;
                    break;
                case Unknow:
                    if (playIndex == 0) {
                        if (isLast) {//true????????????false?????????
                            audioInfo = MusicDataManager.getInstance(mContext).getLastMusicInfo();
                        } else {
                            audioInfo = MusicDataManager.getInstance(mContext).getNextMusicInfo(currentAudioInfo.id);
                        }
                    } else if (playIndex == songCount) {
                        if (isLast) {//true????????????false?????????
                            audioInfo = MusicDataManager.getInstance(mContext).getPrevMusicInfo(currentAudioInfo.id);
                        } else {
                            audioInfo = MusicDataManager.getInstance(mContext).getFirstMusicInfo();
                        }
                    } else {
                        if (isLast) {
                            audioInfo = MusicDataManager.getInstance(mContext).getPrevMusicInfo(currentAudioInfo.id);
                        } else {
                            audioInfo = MusicDataManager.getInstance(mContext).getNextMusicInfo(currentAudioInfo.id);
                        }
                    }
                    break;
            }
            // int pos = getNextPosition(force);
        }
        return audioInfo;
    }


    /**
     * ???????????????????????????
     */
    public void loadNext() {
        loadAudioSource(false);
        EventBus.getDefault().post(new EventMessage(audio_source_change_next.value()));
    }

    public long getPosition() {
        if (mPlayer != null && mPlayer.hasPrepared) {
            savePlayState();
            return mPlayer.getCurrentPosition();
        } else if (currentAudioInfo != null) {
            return currentAudioInfo.getPosition();
        }
        return 0;
    }

    public int seek(int pos) {
        if (mPlayer.hasPrepared) {
            mPlayer.seekTo(pos);
        } else {
            payCurrent();
        }
        return pos;
    }

    public void seekProgress(float pos) {
        QDLogger.println("seekProgress:" + pos);
        seek((int) (pos * getDuration()));
    }

    // Used to make number of albums/songs/time strings
    private final static StringBuilder sFormatBuilder = new StringBuilder();
    private final static Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
    private static final Object[] sTimeArgs = new Object[5];

    /**
     * @param context
     * @param secs
     * @return time String
     */
    public static String makeTimeString(Context context, long secs) {
        String durationformat = context.getString(secs < 3600 ? R.string.durationformatshort
                : R.string.durationformatlong);
        /*
         * Provide multiple arguments so the format can be changed easily by
         * modifying the xml.
         */
        sFormatBuilder.setLength(0);

        final Object[] timeArgs = sTimeArgs;
        timeArgs[0] = secs / 3600;
        timeArgs[1] = secs / 60;
        timeArgs[2] = secs / 60 % 60;
        timeArgs[3] = secs;
        timeArgs[4] = secs % 60;
        return sFormatter.format(durationformat, timeArgs).toString();
    }

    public void onDestroy() {
        savePlayState();
        if (mPlayer != null) {
            mPlayer.release();
        }
    }

    /**
     * ??????????????????
     *
     * @return
     */
   /* public boolean canPlay() {
        return mPlayer != null && mPlayer.hasPrepared;
    }*/
    public void playUri(Uri uri) {
        AudioInfo audioInfo = MusicDataManager.getInstance(mContext).loadDataByUri(mContext, uri);
        if (audioInfo != null) {
            currentAudioInfo = audioInfo;
            payCurrent();
        }
    }

    private void payCurrent() {
        try {
            QDLogger.e("playUri");
            if (mPlayer != null) {
                mPlayer.setOnCompletionListener(null);
                mPlayer.setOnErrorListener(null);
                mPlayer.reset();
                mPlayer.setOnErrorListener(errorListener);
                mPlayer.setOnCompletionListener(onCompletionListener);
            } else {
                initMediaPlayer();
            }
            String path = currentAudioInfo.getData();
            if (path.startsWith("content://")) {
                mPlayer.setDataSource(mContext, Uri.parse(path));
            } else {
                mPlayer.setDataSource(path);
            }
            mPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playOnline(String url) {
        String localProxyUrl = null;
        try {
            String savePath = (String) AppConfig.getInstance().getConfigMap().get("DownloadFilePath");
            QuickProxy quickProxy = new QuickProxy(mContext, url, savePath);
            localProxyUrl = quickProxy.getProxyUrl();
            quickProxy.pre();
            quickProxy.start();
            QDLogger.e("playUrl=" + localProxyUrl);

            currentAudioInfo = new AudioInfo();
            currentAudioInfo.setData(localProxyUrl);
            currentAudioInfo.setResourceType(1);
            currentAudioInfo.setTitle("?????????");
            if (mPlayer != null) {
                mPlayer.setOnCompletionListener(null);
                mPlayer.setOnErrorListener(null);
                mPlayer.reset();
                mPlayer.setOnErrorListener(errorListener);
                mPlayer.setOnCompletionListener(onCompletionListener);
            } else {
                initMediaPlayer();
            }
            mPlayer.setDataSource(mContext, Uri.parse(localProxyUrl));
            QDLogger.i("playOnline prepare");
            mPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playSheet(long sheetId) {
        pause();
        if (mPlayer != null) {
            mPlayer.setOnCompletionListener(null);
            mPlayer.setOnErrorListener(null);
            mPlayer.reset();
            mPlayer.setOnErrorListener(errorListener);
            mPlayer.setOnCompletionListener(onCompletionListener);
        } else {
            initMediaPlayer();
        }
        AudioRecord record = new AudioRecord();
        if (mPlayer.getDuration() > 0) {
            record.setProgress((float) mPlayer.getCurrentPosition() / (float) mPlayer.getDuration());
        }
        record.setSheetId(sheetId);
        record.setSongId(-1);
        MusicDataManager.getInstance(mContext).savePlayRecord(record);
        recovery();
        setRepeatMode(REPEAT_ALL);
        play();
    }

    public void playAudio(AudioInfo audioInfo) {
        if (MusicDataManager.getInstance(mContext).currentSheetId != audioInfo.getSheetId()) {
            pause();
            if (mPlayer != null) {
                mPlayer.setOnCompletionListener(null);
                mPlayer.setOnErrorListener(null);
                mPlayer.reset();
                mPlayer.setOnErrorListener(errorListener);
                mPlayer.setOnCompletionListener(onCompletionListener);
            } else {
                initMediaPlayer();
            }
            AudioRecord record = new AudioRecord();
            if (mPlayer.getDuration() > 0) {
                record.setProgress((float) mPlayer.getCurrentPosition() / (float) mPlayer.getDuration());
            }
            record.setSheetId(audioInfo.getSheetId());
            record.setSongId(audioInfo.getId());
            record.setData(audioInfo.getData());
            MusicDataManager.getInstance(mContext).savePlayRecord(record);
            recovery();
            play();
        } else {
            playByAudioByData(audioInfo.getData());
        }
    }
}
