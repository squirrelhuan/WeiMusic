package com.demomaster.weimusic.player.service;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.demomaster.weimusic.WeiApplication;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.constant.SequenceType;
import com.demomaster.weimusic.model.AudioInfo;
import com.demomaster.weimusic.model.AudioRecord;
import com.demomaster.weimusic.model.AudioSheet;

import org.greenrobot.eventbus.EventBus;
import org.jaudiotagger.audio.mp3.MP3File;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.demomaster.huan.quickdeveloplibrary.helper.QDSharedPreferences;
import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.util.QDFileUtil;
import cn.demomaster.qdlogger_library.QDLogger;
import cn.demomaster.qdrouter_library.base.activity.QuickActivity;

import static com.demomaster.weimusic.constant.AudioStation.Pause;
import static com.demomaster.weimusic.constant.AudioStation.sheet_changed;
import static com.demomaster.weimusic.constant.AudioStation.sheet_create;
import static com.demomaster.weimusic.constant.Constants.APP_PATH_SHEET;
import static com.demomaster.weimusic.constant.Constants.PLAYLIST_NAME_FAVORITES;

public class MusicDataManager {

    private static MusicDataManager instance;

    public static MusicDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new MusicDataManager(context);
        }
        return instance;
    }
    static Context context;
    public MusicDataManager(Context context) {
        this.context = context.getApplicationContext();
        AudioRecord record = getPlayRecord();
        if (record != null) {
            setSheetId(context, record.getSheetId());
        }
    }

    public static String playRecord = "playRecord";

    public void savePlayRecord(AudioRecord record) {
        QDSharedPreferences.getInstance().putObject(playRecord, record);
    }

    /**
     * ??????????????????
     *
     * @return
     */
    public AudioRecord getPlayRecord() {
        AudioRecord record = QDSharedPreferences.getInstance().getObject(playRecord, AudioRecord.class);
        return record;
    }

    /**
     * ?????????????????????
     *
     * @return
     */
    public List<AudioInfo> getBlacklist() {
        return null;
    }

    /**
     * ??????????????????
     */
    public void addToBlacklist(AudioInfo audioInfo) {

    }

    /**
     * ??????????????????
     *
     * @param audioInfo
     */
    public void removeFromBlacklist(AudioInfo audioInfo) {

    }

    long currentSheetId = -1;//??????????????????id

    public long getCurrentSheetId() {
        return currentSheetId;
    }

    public void setCurrentSheetList(List<AudioInfo> currentSheetList) {
        this.currentSheetList = currentSheetList;
    }

    public void setSheetId(Context context, long sheetId) {
        this.currentSheetId = sheetId;
        currentSheetList = getSongSheetListById(context, sheetId);
    }


    // ????????????????????????
    private final class QueryHandler extends AsyncQueryHandler {
        OnQueryListener mOnQueryListener;

        public QueryHandler(ContentResolver cr, OnQueryListener onQueryListener) {
            super(cr);
            this.mOnQueryListener = onQueryListener;
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            super.onQueryComplete(token, cookie, cursor);
            System.out.println("onQueryComplete [" + token + "]  count=" + (cursor == null ? "null" : cursor.getCount()));
            if (mOnQueryListener != null) {
                mOnQueryListener.onQueryComplete(token, cookie, cursor);
            }
        }
    }

    public static interface OnQueryListener {
        void onQueryComplete(int token, Object cookie, Cursor cursor);
    }

    public static interface OnLoadingListener {
        void onFinish(int result, String msg, Object data);
    }

    public void loadData(Context context, OnLoadDataListener loadDataListener) {
        long duration = 30000;//30s
        loadData(context, duration, loadDataListener);
    }

    /**
     * ??????????????????
     *
     * @return
     */
    public void loadData(Context context, long minDuration, OnLoadDataListener loadDataListener) {
        //??????????????????
        // Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        //        , null, MediaStore.Audio.AudioColumns.DURATION + ">" + duration, null, MediaStore.Audio.AudioColumns.IS_MUSIC);
        QueryHandler queryHandler = new QueryHandler(context.getContentResolver(), new OnQueryListener() {
            @Override
            public void onQueryComplete(int token, Object cookie, Cursor cursor) {
                List<AudioInfo> list = null;
                boolean equs = false;
                // ??????mAdapter???Cursor
                if (cursor != null) {
                    list = new ArrayList<>();
                    while (cursor.moveToNext()) {
                        AudioInfo audioInfo = new AudioInfo();
                        audioInfo.id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                        audioInfo.audioId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                        //??????
                        audioInfo.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                        //??????
                        audioInfo.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                        //audioInfo.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                        audioInfo.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                        audioInfo.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                        //????????????
                        audioInfo.data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                        //??????id
                        audioInfo.albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                       /* musicInfo.song = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));

                        //?????????????????????????????????
                        if (!TextUtils.isEmpty(musicInfo.song)) {
                            if (musicInfo.song.contains("-")) {
                                String[] str = musicInfo.song.split("-");
                                musicInfo.singer = str[0];
                                musicInfo.song = str[1];
                            }
                        }*/
                        list.add(audioInfo);
                    }
                    //System.out.println("songlist=" + Arrays.toString(list.toArray()));
                    equs = (localSonglist.size() != list.size());
                }
                cursor.close();
                localSonglist.clear();
                if (list != null) {
                    localSonglist.addAll(list);
                }
                // String md5 = QDFileUtil.getFileMD5(new File(audioInfo.data));
                //                        audioInfo.setMd5(md5);
                //                        QDLogger.println("????????????:"+audioInfo.data+",md5:"+md5);
                if (loadDataListener != null) {
                    loadDataListener.loadComplete(1, list);
                }
                if (equs) {
                    EventBus.getDefault().post(new EventMessage(AudioStation.QUEUE_CHANGED.value()));
                }
            }
        });
        queryHandler.startQuery(1, null, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                , null, MediaStore.Audio.AudioColumns.DURATION + ">" + minDuration, null, MediaStore.Audio.AudioColumns.IS_MUSIC);
    }

    public AudioInfo loadDataByUri(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        String path = QDFileUtil.getFilePathByUri(context, uri);
        String str = MediaStore.Audio.AudioColumns.DATA + "='" + path + "'";
        QDLogger.e(",uri.getQuerystr=" + uri.getQuery() + ",str=" + str);
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, str, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                QDLogger.i("??????title=" + title);
                AudioInfo audioInfo = new AudioInfo();
                audioInfo.id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                audioInfo.audioId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                //??????
                audioInfo.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                //??????
                audioInfo.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                //audioInfo.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                audioInfo.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                audioInfo.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                //????????????
                audioInfo.data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                /*String md5 = QDFileUtil.getFileMD5(new File(audioInfo.data));
                audioInfo.setMd5(md5);
                QDLogger.println("????????????:"+audioInfo.data+",md5:"+md5);*/
                //??????id
                audioInfo.albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                return audioInfo;
            }
        } else {
            QDLogger.e("???????????????");
        }
        return null;
    }

    /**********????????????????????????*************/
    public Bitmap getAlbumPicture(Context context, AudioInfo audioInfo) {
        if (audioInfo == null || TextUtils.isEmpty(audioInfo.getData())) {
            return null;
        }
        String dataPath = audioInfo.getData();
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(dataPath);
            String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION); // ???????????????????????????
            QDLogger.println("title:" + title + ",album:" + album + ",artist:" + artist + ",duration:" + duration);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        byte[] data = mmr.getEmbeddedPicture();
        Bitmap albumPicture = null;
        if (data != null) {
            //??????bitmap??????
            albumPicture = BitmapFactory.decodeByteArray(data, 0, data.length);
            //????????????
            int width = albumPicture.getWidth();
            int height = albumPicture.getHeight();
            //Log.w("DisplayActivity","width = "+width+" height = "+height);
            // ????????????????????????Matrix??????
            Matrix matrix = new Matrix();
            // ??????????????????
           /* float sx = ((float) 120 / width);
            float sy = ((float) 120 / height);
            // ??????????????????
            matrix.postScale(sx, sy);*/
            // ????????????bitmap?????????????????????bitmap??????????????????
            albumPicture = Bitmap.createBitmap(albumPicture, 0, 0, width, height, matrix, false);
            return albumPicture;
        } else {
            return null;
            /*albumPicture = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg_001);
            return albumPicture;*/
        }
    }

    public List<AudioInfo> getCurrentSheet() {
        return currentSheetList;
    }

    public List<AudioInfo> getLocalSheet() {
        return localSonglist;
    }

    public static interface OnLoadDataListener {
        void loadComplete(int ret, List<AudioInfo> audioInfoList);
    }

    List<AudioInfo> localSonglist = new ArrayList<>();
    List<AudioInfo> currentSheetList = new ArrayList<>();

    /**
     * ????????????????????????????????????????????????
     *
     * @param id
     * @return
     */
    public AudioInfo getPrevMusicInfo(long id) {
        if (currentSheetList != null) {
            int index = indexOfArray(currentSheetList, id);
            if (index - 1 >= 0) {
                return currentSheetList.get(index - 1);
            }
        }
        return null;
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param id
     * @return
     */
    public AudioInfo getNextMusicInfo(long id) {
        if (currentSheetList != null) {
            int index = indexOfArray(currentSheetList, id);
            if (index + 1 <= currentSheetList.size() - 1) {
                return currentSheetList.get(index + 1);
            }
        }
        return null;
    }

    //??????????????????????????????????????????
    public int getIndexInQueue(long id) {
        if (currentSheetList != null) {
            return indexOfArray(currentSheetList, id);
        }
        return 0;
    }

    /**
     * ??????????????????????????????????????????????????????
     *
     * @param id
     * @return
     */
    public int indexOfArray(long id) {
        if (currentSheetList == null) {
            if (localSonglist != null) {
                currentSheetList = localSonglist;
            }
        }
        return indexOfArray(currentSheetList, id);
    }

    /**
     * ??????????????????????????????????????????????????????
     *
     * @param id
     * @return
     */
    public int indexOfArray(List<AudioInfo> list, long id) {
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).id == id) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * ???????????????????????????
     *
     * @return
     */
    public AudioInfo getFirstMusicInfo() {
        if (currentSheetList != null && currentSheetList.size() > 0) {
            return currentSheetList.get(0);
        }
        return null;
    }

    /**
     * ???????????????????????????
     *
     * @return
     */
    public AudioInfo getLastMusicInfo() {
        if (currentSheetList != null && currentSheetList.size() > 0) {
            return currentSheetList.get(currentSheetList.size() - 1);
        }
        return null;
    }

    /**
     * ??????id??????????????????
     *
     * @return
     */
    public AudioInfo getMusicInfoByData(Context context, String data) {
        String str = MediaStore.Audio.Media.DATA + "='" + data + "'";
        return queryMusicInfo(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, str);
    }

    public AudioInfo getMusicInfoById(Context context, long audioId) {
        String str = MediaStore.Audio.Media._ID + "='" + audioId + "'";
        return queryMusicInfo(context, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, str);
    }

    public AudioInfo queryMusicInfo(Context context, Uri uri, String selection) {
        ContentResolver resolver = context.getContentResolver();
        //QDLogger.e(",uri.getQuerystr = " + selection);
        Cursor cursor = resolver.query(uri, null, selection, null, null);
        if (cursor != null) {
            AudioInfo audioInfo = null;
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                QDLogger.println("??????title=" + title);
                audioInfo = new AudioInfo();
                audioInfo.id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                //??????
                audioInfo.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                //??????
                audioInfo.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                //audioInfo.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                audioInfo.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                audioInfo.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                //????????????
                audioInfo.data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                /*String md5 = QDFileUtil.getFileMD5(new File(audioInfo.data));
                audioInfo.setMd5(md5);
                QDLogger.println("????????????:"+audioInfo.data+",md5:"+md5);*/
                //??????id
                audioInfo.albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                break;
            }
            cursor.close();
            return audioInfo;
        } else {
            QDLogger.e("??????????????? cursor=null");
        }
        return null;
    }

    public static String RepeatMode = "RepeatMode";

    /**
     * ?????????????????????????????????
     *
     * @return
     */
    public SequenceType getRepeatMode() {
        int v = QDSharedPreferences.getInstance().getInt(RepeatMode, SequenceType.Normal.value());
        return SequenceType.getEnum(v);
    }

    public void saveRepeatMode(SequenceType sequenceType) {
        QDSharedPreferences.getInstance().putInt(RepeatMode, sequenceType.value());
    }

    public boolean isFarorite(Context context, long audioId) {
        AudioSheet audioSheet = new AudioSheet();
        audioSheet.setName(PLAYLIST_NAME_FAVORITES);
        audioSheet.setSystem(true);
        long favorites_id = createSheet(context, audioSheet);
        AudioInfo audioInfo = ((WeiApplication) context.getApplicationContext()).getDbHelper().findOne("select * from AudioInfo where sheetId='" + favorites_id + "' and audioId=" + audioId, AudioInfo.class);
        return audioInfo != null;
    }

    public void addFavorite(Context context, Long audioId) {
        AudioSheet audioSheet = new AudioSheet();
        audioSheet.setName(PLAYLIST_NAME_FAVORITES);
        long table_favorites_id = createSheet(context, audioSheet);
        if (table_favorites_id != -1) {
            AudioInfo audioInfo = new AudioInfo();
            audioInfo.setAudioId(audioId);
            audioInfo.setSheetId(table_favorites_id);
            ((WeiApplication) context.getApplicationContext()).getDbHelper().insert(audioInfo);
            QDLogger.i("????????????????????????audioId=" + audioId);
        }
    }

    //???????????????
    public void addToSheet(Context context, long sheetId, long audioId) {
        //??????????????????????????????
        AudioInfo audioInfo = getMusicInfoById(context, audioId);
        if (audioInfo != null) {
            //????????????????????????
            audioInfo = ((WeiApplication) context.getApplicationContext()).getDbHelper()
                    .findOne("select * from AudioInfo where sheetId='" + sheetId + "' and audioId=" + audioId, AudioInfo.class);
            if (audioInfo == null) {
                audioInfo = new AudioInfo();
                audioInfo.setAudioId(audioId);
                audioInfo.setSheetId(sheetId);
                ((WeiApplication) context.getApplicationContext()).getDbHelper().insert(audioInfo);
                QDLogger.i("??????????????????" + sheetId + ",audioId=" + audioId);
            } else {
                QDLogger.e("?????????????????????");
            }
        } else {
            QDLogger.e("?????????????????????????????????");
        }
    }

    public void removeFromSheet(Context context, long sheetId, long audioId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("audioId", audioId);
        contentValues.put("sheetId", sheetId);
        QDLogger.e("???????????????" + sheetId + ",audioId=" + audioId);
        //((WeiApplication) context.getApplicationContext()).getDbHelper().getDb().delete("AudioInfo",
        //        "audioId=? and sheetId=?", new String[]{String.valueOf(audioId),String.valueOf(sheetId)});
        ((WeiApplication) context.getApplicationContext()).getDbHelper().delete("AudioInfo", contentValues);
        //((WeiApplication) context.getApplicationContext()).getDbHelper().execDeleteSQL("delete from AudioInfo where audioId='"+audioId+"' and sheetId='"+sheetId+"'");
    }

    public void removeFavorite(Context context, Long audioId) {
        AudioSheet audioSheet = new AudioSheet();
        audioSheet.setName(PLAYLIST_NAME_FAVORITES);
        long favorites_id = createSheet(context, audioSheet);
        ((WeiApplication) context.getApplicationContext()).getDbHelper().execDeleteSQL("delete from AudioInfo where sheetId='" + favorites_id + "' and audioId=" + audioId);
    }

    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);// HH:mm:ss "yyyy???MM???dd??? HH:mm:ss:SSS"

    /**
     * ????????????
     */
    public void backUpSheet(Context context, OnLoadingListener onLoadingListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<AudioSheet> audioInfoList = getSongSheet();
                int count = audioInfoList.size();
                for (int i = 0; i < count; i++) {
                    AudioSheet audioSheet = audioInfoList.get(i);
                    List<AudioInfo> audioInfoList1 = getSongSheetListById(context, audioSheet.getId());
                    if (audioInfoList1 != null) {
                        int count2 = audioInfoList1.size();
                        for (int i2 = 0; i2 < count2; i2++) {
                            AudioInfo audioInfo = audioInfoList1.get(i2);
                            String md5 = QDFileUtil.getFileMD5(new File(audioInfo.data));
                            audioInfo.setMd5(md5);
                            audioInfoList1.set(i, audioInfo);
                        }
                        audioSheet.setAudioInfoList(audioInfoList1);
                    }
                    audioInfoList.set(i, audioSheet);
                }
                QDFileUtil.writeFileSdcardFile(new File(APP_PATH_SHEET + "/" + System.currentTimeMillis() + ".sheet"), JSON.toJSONString(audioInfoList), false);
                if (onLoadingListener != null) {
                    onLoadingListener.onFinish(1, "success", null);
                }
            }
        }).start();
    }

    public void autoImportSheet() {
        String fileParentPath = APP_PATH_SHEET;
        File parentFile = new File(fileParentPath);
        if (parentFile.exists() && parentFile.isDirectory()) {
            QDLogger.i("??????????????????");
            File recentFile = null;//?????????????????????
            for (File childFile : parentFile.listFiles()) {
                if (childFile.getName().endsWith(".sheet")) {
                    if (recentFile == null) {
                        recentFile = childFile;
                    } else {
                        String name1 = recentFile.getName().replace(".sheet", "");
                        String name2 = childFile.getName().replace(".sheet", "");
                        try {
                            long t1 = Long.parseLong(name1);
                            long t2 = Long.parseLong(name2);
                            if (t1 < t2) {
                                recentFile = childFile;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            if (recentFile != null) {
                String str = QDFileUtil.readFileSdcardFile(recentFile.getAbsoluteFile());
                QDLogger.i("???" + recentFile.getAbsoluteFile() + "????????????");
                try {
                    List<AudioSheet> audioSheetList = JSON.parseArray(str, AudioSheet.class);
                    importSheet(context, audioSheetList, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * ????????????
     *
     * @param mContext
     * @param audioSheetList
     */
    public void importSheet(Context mContext, List<AudioSheet> audioSheetList, OnLoadingListener onLoadingListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadData(mContext, 30 * 1000, new OnLoadDataListener() {
                    @Override
                    public void loadComplete(int ret, List<AudioInfo> audioInfoList) {
                        if (audioInfoList != null && audioInfoList.size() > 0) {
                            int count = audioInfoList.size();
                            for (int i = 0; i < count; i++) {
                                AudioInfo audioInfo = audioInfoList.get(i);
                                String md5 = QDFileUtil.getFileMD5(new File(audioInfo.data));
                                audioInfo.setMd5(md5);
                                audioInfoList.set(i, audioInfo);
                            }
                /*String md5 = QDFileUtil.getFileMD5(new File(audioInfo.data));
                audioInfo.setMd5(md5);
                QDLogger.println("????????????:"+audioInfo.data+",md5:"+md5);*/
                            for (AudioSheet audioSheet : audioSheetList) {
                                //????????????????????????????????????????????????id
                                long sheetId = createSheet(mContext, audioSheet);
                                if (audioSheet.getAudioInfoList() != null) {
                                    //????????????????????????
                                    for (AudioInfo audioInfo : audioSheet.getAudioInfoList()) {
                                        QDLogger.i("??????getMd5???" + audioInfo.getMd5());

                                        //????????????md5???????????????
                                        AudioInfo audioInfo2 = null;
                                        for (int i = 0; i < count; i++) {
                                            if (audioInfoList.get(i).getMd5().equals(audioInfo.getMd5())) {
                                                audioInfo2 = audioInfoList.get(i);
                                            }
                                        }
                                        //????????????????????????????????????
                                        if (audioInfo2 != null) {
                                            QDLogger.i("?????????????????????" + audioInfo2.getTitle() + ",????????????" + audioSheet.getName());
                                            addToSheet(mContext, sheetId, audioInfo2.getId());
                                        }
                                    }
                                }
                            }
                            EventBus.getDefault().post(new EventMessage(AudioStation.sheet_changed.value()));
                            if (onLoadingListener != null) {
                                onLoadingListener.onFinish(1, "success", null);
                            }
                        }
                    }
                });

            }
        }).start();

    }

    /**
     * ????????????
     *
     * @param context
     * @return
     */
    public static long createSheet(Context context, AudioSheet audioSheet1) {
        String name = audioSheet1.getName();
        AudioSheet audioSheet = ((WeiApplication) context.getApplicationContext()).getDbHelper().findOne("select * from AudioSheet where name='" + name + "'", AudioSheet.class);
        if (audioSheet != null) {
            return audioSheet.getId();
        }
        ((WeiApplication) context.getApplicationContext()).getDbHelper().insert(audioSheet1);
        EventBus.getDefault().post(new EventMessage(sheet_create.value()));
        return ((WeiApplication) context.getApplicationContext()).getDbHelper().getLastIndex();
    }

    /**
     * ????????????
     *
     * @param context
     * @param audioSheet1
     */
    public void modifySheet(Context context, AudioSheet audioSheet1) {
        AudioSheet audioSheet = ((WeiApplication) context.getApplicationContext()).getDbHelper().findOne("select * from AudioSheet where id=" + audioSheet1.getId(), AudioSheet.class);
        if (audioSheet != null) {
            if (!audioSheet.isSystem()) {//???????????????????????????
                audioSheet.setName(audioSheet1.getName());
            }
            if (!TextUtils.isEmpty(audioSheet1.getImgSrc())) {
                audioSheet.setImgSrc(audioSheet1.getImgSrc());
            }
            audioSheet.setThemeColor(audioSheet1.getThemeColor());
            ((WeiApplication) context.getApplicationContext()).getDbHelper().modify(audioSheet);
            EventBus.getDefault().post(new EventMessage(sheet_changed.value()));
        }
    }

    public static List<AudioSheet> getSongSheet() {
        return ((WeiApplication) context.getApplicationContext()).getDbHelper().findArray("select * from AudioSheet", AudioSheet.class);
    }

    public static AudioSheet getSongSheetById(Context context, long sheetId) {
        return ((WeiApplication) context.getApplicationContext()).getDbHelper().findOne("select * from AudioSheet where id=" + sheetId, AudioSheet.class);
    }

    /**
     * @param context
     * @param sheetId
     * @return
     */
    public static List<AudioInfo> getSongSheetListById(Context context, long sheetId) {
        List<AudioInfo> audioInfoList = ((WeiApplication) context.getApplicationContext()).getDbHelper().findArray("select * from AudioInfo where sheetId='" + sheetId + "'", AudioInfo.class);
        if (audioInfoList != null) {
            List<Long> ids = new ArrayList<>();
            for (int i = 0; i < audioInfoList.size(); i++) {
                ids.add(audioInfoList.get(i).getAudioId());
            }
            if (ids.size() > 0) {
                StringBuilder where = new StringBuilder();
                //where.append(MediaStore.Audio.AudioColumns.IS_MUSIC + "=1").append(" AND " + MediaStore.MediaColumns.TITLE + " != ''");
                where = new StringBuilder();
                where.append(MediaStore.Audio.AudioColumns.IS_MUSIC + "=1");
                where.append(" AND " + MediaStore.MediaColumns.TITLE + " != ''");
                where = new StringBuilder();
                where.append(MediaStore.Audio.AudioColumns.IS_MUSIC + "=1");
                where.append(" AND " + MediaStore.MediaColumns.TITLE + " != ''");
                where.append(" AND " + BaseColumns._ID + " IN (");
                for (long queue_id : ids) {
                    where.append(queue_id + ",");
                }
                where.deleteCharAt(where.length() - 1);
                where.append(")");

                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                ContentResolver resolver = context.getContentResolver();
                //QDLogger.i("where.toString()="+where.toString());
                Cursor cursor = resolver.query(uri, null, where.toString(), null, null);
                if (cursor != null) {
                    audioInfoList = new ArrayList<>();
                    while (cursor.moveToNext()) {//??????????????????
                        AudioInfo audioInfo = new AudioInfo();
                        audioInfo.id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                        audioInfo.audioId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));

                        //QDLogger.i("musicInfo.id="+musicInfo.id);
                        //??????
                        audioInfo.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));

                        QDLogger.i(sheetId + "," + audioInfo.title + "," + audioInfo.id);
                        //??????
                        audioInfo.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                        //audioInfo.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

                        audioInfo.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                        audioInfo.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                        //????????????
                        audioInfo.data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                        //??????id
                        audioInfo.albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                        audioInfo.setSheetId(sheetId);
                        // musicInfo.setAlbum(data.getString(mAlbumIndex));
                        audioInfoList.add(audioInfo);
                    }
                    cursor.close();
                    return audioInfoList;
                }
            }
        }
        return null;
    }

    public static Cursor query(Context context, Uri uri, String[] projection, String selection,
                               String[] selectionArgs, String sortOrder) {
        return query(context, uri, projection, selection, selectionArgs, sortOrder, 0);
    }

    public static Cursor query(Context context, Uri uri, String[] projection, String selection,
                               String[] selectionArgs, String sortOrder, int limit) {
        try {
            ContentResolver resolver = context.getContentResolver();
            if (resolver != null) {
                if (limit > 0) {
                    uri = uri.buildUpon().appendQueryParameter("limit", "" + limit).build();
                }
                return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
            }
        } catch (UnsupportedOperationException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
