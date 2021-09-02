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

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.WeiApplication;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.constant.SequenceType;
import com.demomaster.weimusic.model.AudioInfo;
import com.demomaster.weimusic.model.AudioRecord;
import com.demomaster.weimusic.model.AudioSheet;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import cn.demomaster.huan.quickdeveloplibrary.helper.QDSharedPreferences;
import cn.demomaster.huan.quickdeveloplibrary.helper.simplepicture.model.Image;
import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.util.QDFileUtil;
import cn.demomaster.qdlogger_library.QDLogger;

import static com.demomaster.weimusic.constant.Constants.EXTERNAL;
import static com.demomaster.weimusic.constant.Constants.PLAYLIST_NAME_FAVORITES;

public class MusicDataManager2 {

    private static MusicDataManager2 instance;

    public static MusicDataManager2 getInstance() {
        if (instance == null) {
            instance = new MusicDataManager2();
        }
        return instance;
    }

    public MusicDataManager2() {

    }

    public static String playRecord = "playRecord";

    public void savePlayRecord(AudioRecord record) {
        QDSharedPreferences.getInstance().putObject(playRecord, record);
    }

    /**
     * 获取播放记录
     *
     * @return
     */
    public AudioRecord getPlayRecord() {
        AudioRecord record = QDSharedPreferences.getInstance().getObject(playRecord, AudioRecord.class);
        return record;
    }

    /**
     * 获取播放黑名单
     *
     * @return
     */
    public List<AudioInfo> getBlacklist() {
        return null;
    }

    /**
     * 添加到黑名单
     */
    public void addToBlacklist(AudioInfo audioInfo) {

    }

    /**
     * 从黑名单移除
     *
     * @param audioInfo
     */
    public void removeFromBlacklist(AudioInfo audioInfo) {

    }

    long currentSheetId = -1;

    public long getCurrentSheetId() {
        return currentSheetId;
    }

    public void setSheetId(Context context, long sheetId) {
        this.currentSheetId = sheetId;
        currentSheetList = getSongSheetListById(context, sheetId);
    }

    // 写一个异步查询类
    private final class QueryHandler extends AsyncQueryHandler {
        OnQueryListener mOnQueryListener;

        public QueryHandler(ContentResolver cr, OnQueryListener onQueryListener) {
            super(cr);
            mOnQueryListener = onQueryListener;
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

    public void loadData(Context context, OnLoadDataListener loadDataListener) {
        long duration = 30000;//30s
        loadData(context, duration, loadDataListener);
    }

    /**
     * 获取播放队列
     *
     * @return
     */
    public void loadData(Context context, long minDuration, OnLoadDataListener loadDataListener) {
        //获取当前列表
        // Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        //        , null, MediaStore.Audio.AudioColumns.DURATION + ">" + duration, null, MediaStore.Audio.AudioColumns.IS_MUSIC);
        QueryHandler queryHandler = new QueryHandler(context.getContentResolver(), new OnQueryListener() {
            @Override
            public void onQueryComplete(int token, Object cookie, Cursor cursor) {
                List<AudioInfo> list = null;
                boolean equs = false;
                // 更新mAdapter的Cursor
                if (cursor != null) {
                    list = new ArrayList<>();
                    while (cursor.moveToNext()) {
                        AudioInfo audioInfo = new AudioInfo();
                        audioInfo.id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                        //歌名
                        audioInfo.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                        //歌手
                        audioInfo.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                        //audioInfo.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                        audioInfo.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                        audioInfo.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                        //文件路径
                        audioInfo.data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                        //专辑id
                        audioInfo.albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                       /* musicInfo.song = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));

                        //把歌曲名字和歌手切割开
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
                QDLogger.i("找到title=" + title);
                AudioInfo audioInfo = new AudioInfo();
                audioInfo.id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                //歌名
                audioInfo.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                //歌手
                audioInfo.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                //audioInfo.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                audioInfo.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                audioInfo.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                //文件路径
                audioInfo.data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                //专辑id
                audioInfo.albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                return audioInfo;
            }
        } else {
            QDLogger.e("未找到歌曲 cursor=null");
        }
        return null;
    }

    /**********获取歌曲专辑图片*************/
    public Bitmap getAlbumPicture(Context context, AudioInfo audioInfo, boolean canEmpty) {
        if (audioInfo == null || TextUtils.isEmpty(audioInfo.getData())) {
            return null;
        }
        String dataPath = audioInfo.getData();
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(dataPath);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        byte[] data = mmr.getEmbeddedPicture();
        Bitmap albumPicture = null;
        if (data != null) {
            //获取bitmap对象
            albumPicture = BitmapFactory.decodeByteArray(data, 0, data.length);
            //获取宽高
            int width = albumPicture.getWidth();
            int height = albumPicture.getHeight();
            //Log.w("DisplayActivity","width = "+width+" height = "+height);
            // 创建操作图片用的Matrix对象
            Matrix matrix = new Matrix();
            // 计算缩放比例
           /* float sx = ((float) 120 / width);
            float sy = ((float) 120 / height);
            // 设置缩放比例
            matrix.postScale(sx, sy);*/
            // 建立新的bitmap，其内容是对原bitmap的缩放后的图
            albumPicture = Bitmap.createBitmap(albumPicture, 0, 0, width, height, matrix, false);
            return albumPicture;
        } else {
            if (canEmpty) {
                return null;
            }
            albumPicture = BitmapFactory.decodeResource(context.getResources(), R.drawable.bg_001);
            //music1是从歌曲文件读取不出来专辑图片时用来代替的默认专辑图片
            int width = albumPicture.getWidth();
            int height = albumPicture.getHeight();
            //Log.w("DisplayActivity","width = "+width+" height = "+height);
            // 创建操作图片用的Matrix对象
            Matrix matrix = new Matrix();
            // 计算缩放比例
            /*float sx = ((float) 120 / width);
            float sy = ((float) 120 / height);
            // 设置缩放比例
            matrix.postScale(sx, sy);*/
            // 建立新的bitmap，其内容是对原bitmap的缩放后的图
            albumPicture = Bitmap.createBitmap(albumPicture, 0, 0, width, height, matrix, false);
            return albumPicture;
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
     * 获取上一首歌曲信息，默认列表顺序
     *
     * @param id
     * @return
     */
    public AudioInfo getPrevMusicInfo(long id) {
        if (currentSheetList != null) {
            int index = indexOfArray(currentSheetList, id);
            QDLogger.i("getNextMusicInfo index=" + index);
            if (index - 1 >= 0) {
                return currentSheetList.get(index - 1);
            }
        }
        return null;
    }

    /**
     * 获取下一首歌曲，默认列表顺序
     *
     * @param id
     * @return
     */
    public AudioInfo getNextMusicInfo(long id) {
        if (currentSheetList != null) {
            int index = indexOfArray(currentSheetList, id);
            QDLogger.println("getNextMusicInfo songlist=" + currentSheetList.size() + ", index=" + index);
            if (index + 1 <= currentSheetList.size() - 1) {
                return currentSheetList.get(index + 1);
            }
        }
        return null;
    }

    public int getIndexInQueue(long id) {
        if (currentSheetList != null) {
            return indexOfArray(currentSheetList, id);
        }
        return 0;
    }

    /**
     * 获取在列表中的索引位置，默认列表顺序
     *
     * @param id
     * @return
     */
    public int indexOfArray(long id) {
        return indexOfArray(currentSheetList, id);
    }

    /**
     * 获取在列表中的索引位置，默认列表顺序
     *
     * @param id
     * @return
     */
    public int indexOfArray(List<AudioInfo> list, long id) {
        for (int i = 0; i < list.size(); i++) {
            //QDLogger.i("indexOfArray:"+i+", id="+id+",c="+list.get(i).id);
            if (list.get(i).id == id) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 获取第一首歌曲信息
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
     * 获取第一首歌曲信息
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
     * 根据id获取歌曲信息
     *
     * @return
     */
    public AudioInfo getMusicInfoByData(Context context, String data) {
        ContentResolver resolver = context.getContentResolver();
        String str = MediaStore.Audio.Media.DATA + "='" + data + "'";
        QDLogger.e(",uri.getQuerystr = " + str);
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, str, null, null);
        if (cursor != null) {
            AudioInfo audioInfo = null;
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                QDLogger.i("找到title=" + title);
                audioInfo = new AudioInfo();
                audioInfo.id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                //歌名
                audioInfo.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                //歌手
                audioInfo.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                //audioInfo.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                audioInfo.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                audioInfo.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                //文件路径
                audioInfo.data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                //专辑id
                audioInfo.albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                break;
            }
            cursor.close();
            return audioInfo;
        } else {
            QDLogger.e("未找到歌曲 cursor=null");
        }
        return null;
    }

    public AudioInfo getMusicInfoById(Context context, long audioId) {
        ContentResolver resolver = context.getContentResolver();
        String str = MediaStore.Audio.Media._ID + "='" + audioId + "'";
        QDLogger.e(",uri.getQuerystr = " + str);
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, str, null, null);
        if (cursor != null) {
            AudioInfo audioInfo = null;
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                QDLogger.i("找到title=" + title);
                audioInfo = new AudioInfo();
                audioInfo.id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                //歌名
                audioInfo.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                //歌手
                audioInfo.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                //audioInfo.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                audioInfo.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                audioInfo.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                //文件路径
                audioInfo.data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                //专辑id
                audioInfo.albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                break;
            }
            cursor.close();
            return audioInfo;
        } else {
            QDLogger.e("未找到歌曲 cursor=null");
        }
        return null;
    }

    public static String RepeatMode = "RepeatMode";

    /**
     * 获取上次记录的播放模式
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

    public boolean isFarorite(Context context, long id) {
        long favorites_id = createSheet(context, PLAYLIST_NAME_FAVORITES, null);
        if (favorites_id == -1) {
            return false;
        }
        ContentResolver resolver = context.getContentResolver();
        String[] cols = new String[]{
                MediaStore.Audio.Playlists.Members.AUDIO_ID
        };
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(EXTERNAL, favorites_id);
        Cursor cur = resolver.query(uri, cols, " " + MediaStore.Audio.Playlists.Members.AUDIO_ID + "=" + id, null, null);
        int count = cur.getCount();
        cur.close();
        return count >= 1;
    }

    public void addFavorite(Context context, Long audioId) {
        long table_favorites_id = createSheet(context, PLAYLIST_NAME_FAVORITES, null);
        if (table_favorites_id != -1) {
            QDLogger.i("addFavorite table_favorites_id" + table_favorites_id);
            ContentResolver resolver = context.getContentResolver();
            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(EXTERNAL, table_favorites_id);
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);
            values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, 1);
            Uri uri1 = resolver.insert(uri, values);
            QDLogger.i("addFavorite " + uri1.getPath());
        }
    }

    public void addToSheet(Context context, long sheetId, long audioId) {
        ContentResolver resolver = context.getContentResolver();
        //MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(EXTERNAL, sheetId);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, 1);
        Uri uri1 = resolver.insert(uri, values);
        QDLogger.i("添加到歌单：" + (uri1 == null ? "null" : uri1.getPath()));
    }

    public void removeFromSheet(Context context, AudioInfo audioInfo) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(EXTERNAL, audioInfo.getSheetId());
        resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID + "=" + audioInfo.getId(), null);
    }

    public void removeFavorite(Context context, Long id) {
        ContentResolver resolver = context.getContentResolver();
        long table_favorites_id = createSheet(context, PLAYLIST_NAME_FAVORITES, null);
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(EXTERNAL, table_favorites_id);
        resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID + "=" + id, null);
    }

    /**
     * 创建歌单
     *
     * @param context
     * @param name
     * @param image
     * @return
     */
    public static long createSheet(Context context, String name, Image image) {
        if (!TextUtils.isEmpty(name)) {
            AudioSheet audioSheet = new AudioSheet();
            audioSheet.setName(name);
            if (image != null) {
                audioSheet.setImgSrc(image.getPath());
            }
            ((WeiApplication) context.getApplicationContext()).getDbHelper().insert(audioSheet);
            return ((WeiApplication) context.getApplicationContext()).getDbHelper().getLastIndex();
            /*Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
            ContentResolver resolver = context.getContentResolver();
            String[] cols = new String[]{
                    MediaStore.Audio.PlaylistsColumns.NAME,
                    MediaStore.Audio.PlaylistsColumns._ID,
                    MediaStore.Audio.PlaylistsColumns.DATA
            };
            //MediaStore.Audio.Playlists.Members.AUDIO_ID
            String whereclause = MediaStore.Audio.PlaylistsColumns.NAME + " = '" + name + "'";
            Cursor cur = resolver.query(uri, cols, whereclause,
                    null, null);
            if (cur.getCount() <= 0) {
                cur.close();
                ContentValues values = new ContentValues();
                values.put(MediaStore.Audio.PlaylistsColumns.NAME, name);
                QDLogger.i("createSheet " + name);
                //values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, name);
                Uri uri2 = resolver.insert(uri, values);
                if (uri2 != null) {
                    long id = Long.parseLong(uri2.getLastPathSegment());
                    if (image != null) {
                        AudioSheet audioSheet = new AudioSheet();
                        audioSheet.setPlaylist_id(id);
                        audioSheet.setName(name);
                        audioSheet.setImgSrc(image.getPath());
                        ((WeiApplication) context.getApplicationContext()).getDbHelper().insert(audioSheet);
                    }
                    return id;
                }
            } else {
                if (cur.moveToNext()) {
                    long id = cur.getLong(cur.getColumnIndexOrThrow(MediaStore.Audio.PlaylistsColumns._ID));
                    cur.close();
                    return id;
                }
            }*/
        }
        return -1;
    }

    public void modifySheet(Context context, long sheetId, String sheetName, Image image) {
        /*Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.PlaylistsColumns.NAME, sheetName);
        String whereclause = BaseColumns._ID + " = '" + sheetId + "'";
        context.getContentResolver().update(uri, values, whereclause, null);*/
        AudioSheet audioSheet = ((WeiApplication) context.getApplicationContext()).getDbHelper().findOne("select * from AudioSheet where playlist_id=" + sheetId, AudioSheet.class);
        if (audioSheet != null) {
            audioSheet.setName(sheetName);
            if (image != null) {
                audioSheet.setImgSrc(image.getPath());
            }
            ((WeiApplication) context.getApplicationContext()).getDbHelper().modify(audioSheet);
        }
    }

    public static List<AudioSheet> getSongSheet(Context context) {
        return  ((WeiApplication) context.getApplicationContext()).getDbHelper().findArray("select * from AudioSheet",AudioSheet.class);
        /*
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        //context.getContentResolver().delete(uri, null, null);
        ContentResolver resolver = context.getContentResolver();
        String[] projection = new String[]{
                BaseColumns._ID,
                //MediaStore.Audio.Playlists.Members.AUDIO_ID,
                MediaStore.Audio.PlaylistsColumns.NAME
        };
        String whereclause = null;//MediaStore.Audio.PlaylistsColumns.NAME + " = '" + name + "'";
        String sortOrder = MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER;
        Cursor cursor = resolver.query(uri, null, whereclause,
                null, sortOrder);
        List<AudioSheet> audioSheets = null;
        if (cursor != null) {
            audioSheets = new ArrayList<>();
            while (cursor.moveToNext()) {
                AudioSheet collection = new AudioSheet();
                int mPlaylistIdIndex = cursor.getColumnIndexOrThrow(BaseColumns._ID);
                int mPlaylistNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.PlaylistsColumns.NAME);
                collection.setId(cursor.getLong(mPlaylistIdIndex));
                collection.setName(cursor.getString(mPlaylistNameIndex));
                audioSheets.add(collection);
                *//*int c = cursor.getCount();
                QDLogger.e("ColumnNames="+Arrays.toString(cursor.getColumnNames()));
                for(int i=0;i<c;i++){
                    QDLogger.e(cursor.getColumnNames()[i]+"="+cursor.getColumnName(i)+
                            ","+cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.PlaylistsColumns.DATA)));
                }*//*
            }
            cursor.close();
        }
        if(audioSheets!=null&&audioSheets.size()>0){
            for(int i=0;i<audioSheets.size();i++){
                AudioSheet audioSheet = audioSheets.get(i);
                audioSheet =getSongSheetById(context,audioSheet.getId());
                audioSheets.set(i,audioSheet);
            }
        }
        return audioSheets;*/
    }

    public static AudioSheet getSongSheetById(Context context, long sheetId) {
        Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        //context.getContentResolver().delete(uri, null, null);
        ContentResolver resolver = context.getContentResolver();
        String[] projection = new String[]{
                BaseColumns._ID,
                //MediaStore.Audio.Playlists.Members.AUDIO_ID,
                MediaStore.Audio.PlaylistsColumns.NAME
        };
        String whereclause = BaseColumns._ID + " = '" + sheetId + "'";
        String sortOrder = MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER;
        Cursor cursor = resolver.query(uri, projection, whereclause,
                null, sortOrder);
        AudioSheet collection = null;
        if (cursor != null) {
            int mPlaylistIdIndex = cursor.getColumnIndexOrThrow(BaseColumns._ID);
            int mPlaylistNameIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.PlaylistsColumns.NAME);
            if (cursor.moveToFirst()) {
                collection = new AudioSheet();
                collection.setId(cursor.getLong(mPlaylistIdIndex));
                collection.setName(cursor.getString(mPlaylistNameIndex));
            }
            cursor.close();
        }
        if(collection!=null){
            AudioSheet audioSheet = ((WeiApplication) context.getApplicationContext()).getDbHelper().findOne("select * from AudioSheet where playlist_id="+collection.getId(),AudioSheet.class);
            if(audioSheet!=null) {
                collection.setImgSrc(audioSheet.getImgSrc());
            }
        }
        return collection;
    }

    /**
     * @param context
     * @param sheetId
     * @return
     */
    public static List<AudioInfo> getSongSheetListById(Context context, long sheetId) {
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(EXTERNAL, sheetId);
        String sortOrder = MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER;
        String[] projection = new String[]{
                //MediaStore.Audio.Playlists.Members._ID,
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Playlists.Members.AUDIO_ID,
                MediaStore.MediaColumns.TITLE,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.AudioColumns.ARTIST,
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.SIZE,
                MediaStore.Audio.AudioColumns.ALBUM_ID,
                MediaStore.Audio.AudioColumns.DURATION
        };
        StringBuilder where = new StringBuilder();
        //where.append(MediaStore.Audio.AudioColumns.IS_MUSIC + "=1").append(" AND " + MediaStore.MediaColumns.TITLE + " != ''");
        where = new StringBuilder();
        where.append(MediaStore.Audio.AudioColumns.IS_MUSIC + "=1");
        where.append(" AND " + MediaStore.MediaColumns.TITLE + " != ''");

        ContentResolver resolver = context.getContentResolver();
        if (sheetId >= 0) {
            Cursor cursor1 = resolver.query(uri, projection, where.toString(), null, sortOrder);
            List<Long> ids = new ArrayList<>();
            if (cursor1 != null) {
                while (cursor1.moveToNext()) {//添加收藏列表
                    long id = cursor1.getLong(cursor1.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID));
                    ids.add(id);
                }
                cursor1.close();
                if (ids.size() > 0) {
                    where = new StringBuilder();
                    where.append(MediaStore.Audio.AudioColumns.IS_MUSIC + "=1");
                    where.append(" AND " + MediaStore.MediaColumns.TITLE + " != ''");
                    where.append(" AND " + BaseColumns._ID + " IN (");
                    for (long queue_id : ids) {
                        where.append(queue_id + ",");
                    }
                    where.deleteCharAt(where.length() - 1);
                    where.append(")");
                }
            }
        }
        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        //QDLogger.i("where.toString()="+where.toString());
        Cursor cursor = resolver.query(uri, null, where.toString(), null, null);
        if (cursor != null) {
            List<AudioInfo> audioInfoList = new ArrayList<>();
            while (cursor.moveToNext()) {//添加收藏列表
                AudioInfo audioInfo = new AudioInfo();
                audioInfo.id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                //QDLogger.i("musicInfo.id="+musicInfo.id);
                //歌名
                audioInfo.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                //歌手
                audioInfo.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                audioInfo.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                audioInfo.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                //文件路径
                audioInfo.data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                //专辑id
                audioInfo.albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                audioInfo.setSheetId(sheetId);
                // musicInfo.setAlbum(data.getString(mAlbumIndex));
                audioInfoList.add(audioInfo);
            }
            cursor.close();
            return audioInfoList;
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
