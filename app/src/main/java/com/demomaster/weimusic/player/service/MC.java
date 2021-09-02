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

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
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

    //初始化播放器
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

    boolean loss_transient = false;//音频焦点临时丢失
    boolean isLoss_transient_isplaying;//丢失焦点时是否在播放音频

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
            QDLogger.e("音频焦点获取失败");
        } else if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            QDLogger.i("音频焦点获取成功");
        } else if (res == AudioManager.AUDIOFOCUS_REQUEST_DELAYED) {
            QDLogger.i("音频焦点延迟授权");
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        QDLogger.w("onAudioFocusChange:" + focusChange);
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                //用于指示持续时间未知的音频聚焦增益或音频聚焦请求。
                if (loss_transient) {
                    QDLogger.w("音频焦点临时丢失后恢复了");
                    loss_transient = false;
                    if (isLoss_transient_isplaying) {//如果丢失焦点前是播放状态，是被打断意外终止的。则继续播放
                        play();
                    }
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS://音频失去焦点
                QDLogger.w("音频焦点丢失时间未知长期");
                loss_transient = false;
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                QDLogger.w("音频焦点暂时丢失");
                loss_transient = true;
                isLoss_transient_isplaying = isPlaying();
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                //用于指示音频焦点暂时丢失，此时音频焦点丢失者可能
                //*如果要继续播放（也称为“闪避”），请降低其输出音量，如下所示：
                //*新的焦点所有者不要求其他人保持沉默。
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
                        case TelephonyManager.CALL_STATE_IDLE:// 电话挂断
                            QDLogger.w("电话挂断...");
                            break;
                        case TelephonyManager.CALL_STATE_OFFHOOK: //电话通话的状态
                            QDLogger.w("正在通话...");
                            break;
                        case TelephonyManager.CALL_STATE_RINGING: //电话响铃的状态
                            QDLogger.w("电话响铃...");
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
     * 当app的音频使用完毕，应该主动释放，这样其他app才能及时响应
     */
    private void playComplete() {
        QDLogger.w("playComplete");
        mAudioManager.abandonAudioFocus(this);
    }

    //获取播放列表
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
                        QDLogger.e("播放列表加載完成");
                        EventBus.getDefault().post(new EventMessage(AudioStation.loadData.value()));
                    }
                }
            });
        }
    }

    /**
     * 完成和reset都會触发
     */
    MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            trackEnded();
        }
    };

    //播放结束
    private void trackEnded() {
        QDLogger.println("播放结束");
        if (mRepeatMode == REPEAT_CURRENT) {//单曲循环
            seek(0);
            play();
        } else {//播放下一首
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
            QDLogger.println("音频准备完成");
            request();
            savePlayState();
            EventBus.getDefault().post(new EventMessage(audio_ready.value()));
            //QDLogger.println("音频准备完成  播放");
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

    //获取播放模式
    public SequenceType getRepeatMode() {
        return mRepeatMode;
    }

    public void setRepeatMode(SequenceType mRepeatMode) {
        this.mRepeatMode = mRepeatMode;
    }

    public boolean isPlaying() {
        if (mPlayer == null || !mPlayer.hasPrepared) {
            return false;
        }
        return mPlayer.isPlaying();
    }

    AudioInfo currentAudioInfo;

    /**
     * 获取当前播放歌曲信息
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
     * 切换播放模式
     */
    public void doRepeat() {
        int mode = getRepeatMode().value();
        QDLogger.println("切换播放模式：" + mode);
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

    //恢复播放状态
    public void recovery() {
        //恢复播放模式
        SequenceType sequenceType = MusicDataManager.getInstance(mContext).getRepeatMode();
        setRepeatMode(sequenceType);
        //恢复上次播放的歌单，索引&id，进度
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
            QDLogger.println("恢复上次播放的歌单索引:" + record);
            if (audioInfo != null) {
                seek = (int) (record.getProgress() * audioInfo.duration);
            }
        }else {
            MusicDataManager.getInstance(mContext).setCurrentSheetList(mPlayList);
        }
        if (audioInfo == null) {
            audioInfo = MusicDataManager.getInstance(mContext).getFirstMusicInfo();
        }

        if (audioInfo != null) {
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
     * 保存播放进度
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
        QDLogger.println("停止播放");
        mPlayer.stop();
    }

    public void pause() {
        QDLogger.println("暂停播放");
        if (isPlaying()) {
            mPlayer.pause();
        }
    }

    //播放指定歌曲
    public void playByAudioByData(String data) {
        QDLogger.println("播放歌曲id=" + data);
        if (currentAudioInfo == null || currentAudioInfo.getData() != data) {
            AudioInfo info = MusicDataManager.getInstance(mContext).getMusicInfoByData(mContext, data);
            if (info != null) {
                setDataSourceImpl(info);
            } else {
                QDLogger.e("播放歌曲" + data + "不存在");
            }
        }
    }

    //设置音频源
    private boolean setDataSourceImpl(AudioInfo info) {
        try {
            mPlayer.reset();
            currentAudioInfo = info;
            if (info == null) {
                QDLogger.e("设置音频路径 爲空");
                return false;
            }
            String path = info.getData();
            QDLogger.println("设置音频路径 path=" + path + ",info.position=" + info.position);
            if (path.startsWith("content://")) {
                mPlayer.setDataSource(mContext, Uri.parse(path));
            } else {
                mPlayer.setDataSource(path);
            }
            mPlayer.setOnCompletionListener(onCompletionListener);
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //QDLogger.println("准备加载音频文件");
            mPlayer.prepare();
            mPlayer.seekTo(info.position);
        } catch (Exception ex) {
            if (ex instanceof FileNotFoundException) {
                QdToast.show("音频文件不存在");
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

    //播放
    public void play() {
        request();
        if (!mPlayer.hasPrepared) {
            if (mPlayList != null && mPlayList.size() > 0) {
                setDataSourceImpl(getCurrentInfo());
            }
        }
        if (mPlayList != null && mPlayList.size() > 0) {
            QDLogger.println("播放");
            if (!mPlayer.isPlaying()) {
                mPlayer.start();
            }
            EventBus.getDefault().post(new EventMessage(Play.value()));
        } else {
            QDLogger.e("播放列表为空 ");
        }
    }

    //播放上一首
    public void playPrev() {
        QDLogger.println("MC", "播放上一首:" + (currentAudioInfo == null ? "" : currentAudioInfo.title));
        loadLast();
        play();
    }

    //加载 上一首音频源
    public void loadLast() {
        loadAudioSource(true);
        EventBus.getDefault().post(new EventMessage(audio_source_change_last.value()));
    }

    //播放下一首
    public void playNext() {
        loadNext();
        QDLogger.println("播放下一首:" + (currentAudioInfo == null ? "" : currentAudioInfo.title));
        play();
    }

    /**
     * 切换音频源 到 上一首/下一首
     *
     * @param isLast 上一首
     */
    public void loadAudioSource(boolean isLast) {
        QDLogger.e("切换音频源 到 上一首/下一首");
        AudioInfo audioInfo = getNextMusicInfo(isLast);
        if (audioInfo != null) {
            setDataSourceImpl(audioInfo);
        } else {
            QDLogger.e("歌曲列表为空");
        }
    }

    /**
     * 获取下一个要播放的歌曲
     *
     * @param isLast true上一曲，false下一曲
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
                        if (isLast) {//true上一曲，false下一曲
                            audioInfo = MusicDataManager.getInstance(mContext).getLastMusicInfo();
                        } else {
                            audioInfo = MusicDataManager.getInstance(mContext).getNextMusicInfo(currentAudioInfo.id);
                        }
                    } else if (playIndex == songCount - 1) {
                        if (isLast) {//true上一曲，false下一曲
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
                        if (isLast) {//true上一曲，false下一曲
                            audioInfo = MusicDataManager.getInstance(mContext).getLastMusicInfo();
                        } else {
                            audioInfo = MusicDataManager.getInstance(mContext).getNextMusicInfo(currentAudioInfo.id);
                        }
                    } else if (playIndex == songCount - 1) {
                        if (isLast) {//true上一曲，false下一曲
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
                        if (isLast) {//true上一曲，false下一曲
                            audioInfo = MusicDataManager.getInstance(mContext).getLastMusicInfo();
                        } else {
                            audioInfo = MusicDataManager.getInstance(mContext).getNextMusicInfo(currentAudioInfo.id);
                        }
                    } else if (playIndex == songCount - 1) {
                        if (isLast) {//true上一曲，false下一曲
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
                        if (isLast) {//true上一曲，false下一曲
                            audioInfo = MusicDataManager.getInstance(mContext).getLastMusicInfo();
                        } else {
                            audioInfo = MusicDataManager.getInstance(mContext).getNextMusicInfo(currentAudioInfo.id);
                        }
                    } else if (playIndex == songCount) {
                        if (isLast) {//true上一曲，false下一曲
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
     * 切换到下一首音频源
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
     * 是否可以播放
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
            currentAudioInfo.setTitle("获取中");
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
