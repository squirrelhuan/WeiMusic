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
import android.provider.MediaStore;
import android.text.TextUtils;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.constant.SequenceType;
import com.demomaster.weimusic.model.MusicInfo;
import com.demomaster.weimusic.model.MusicRecord;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.helper.QDSharedPreferences;
import cn.demomaster.huan.quickdeveloplibrary.util.QDFileUtil;
import cn.demomaster.qdlogger_library.QDLogger;

import static com.demomaster.weimusic.constant.Constants.EXTERNAL;
import static com.demomaster.weimusic.constant.Constants.PLAYLIST_NAME_FAVORITES;

public class MusicDataManager {

    private static MusicDataManager instance;

    public static MusicDataManager getInstance() {
        if (instance == null) {
            instance = new MusicDataManager();
        }
        return instance;
    }

    public MusicDataManager() {

    }

    public static String playRecord = "playRecord";
    public void savePlayRecord(MusicRecord record) {
        QDSharedPreferences.getInstance().putObject(playRecord, record);
    }

    /**
     * 获取播放记录
     * @return
     */
    public MusicRecord getPlayRecord() {
        MusicRecord record = QDSharedPreferences.getInstance().getObject(playRecord, MusicRecord.class);
        return record;
    }

    /**
     * 获取播放黑名单
     *
     * @return
     */
    public List<MusicInfo> getBlacklist() {
        return null;
    }

    /**
     * 添加到黑名单
     */
    public void addToBlacklist(MusicInfo musicInfo) {

    }

    /**
     * 从黑名单移除
     *
     * @param musicInfo
     */
    public void removeFromBlacklist(MusicInfo musicInfo) {

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
            System.out.println("onQueryComplete ["+token+"]  count=" + (cursor==null?"null":cursor.getCount()));
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
        loadData(context,duration,loadDataListener);
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
                List<MusicInfo> list = null;
                boolean equs = false;
                // 更新mAdapter的Cursor
                if (cursor != null) {
                    list = new ArrayList<>();
                    while (cursor.moveToNext()) {
                        MusicInfo musicInfo = new MusicInfo();
                        musicInfo.id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                        //歌名
                        musicInfo.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                        //歌手
                        musicInfo.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                        musicInfo.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                        musicInfo.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                        musicInfo.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                        //文件路径
                        musicInfo.data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                        //专辑id
                        musicInfo.albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                       /* musicInfo.song = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));

                        //把歌曲名字和歌手切割开
                        if (!TextUtils.isEmpty(musicInfo.song)) {
                            if (musicInfo.song.contains("-")) {
                                String[] str = musicInfo.song.split("-");
                                musicInfo.singer = str[0];
                                musicInfo.song = str[1];
                            }
                        }*/
                        list.add(musicInfo);
                    }
                    //System.out.println("songlist=" + Arrays.toString(list.toArray()));
                    equs =(songlist.size() != list.size());
                }
                cursor.close();
                songlist.clear();
                if (list != null) {
                    songlist.addAll(list);
                }
                if(loadDataListener!=null) {
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

    public MusicInfo loadDataByUri(Context context, Uri uri) {
        ContentResolver resolver = context.getContentResolver();
        String path = QDFileUtil.getFilePathByUri(context,uri);
        String str = MediaStore.Audio.AudioColumns.DATA+"='"+path+"'";
        QDLogger.e(",uri.getQuerystr="+uri.getQuery()+",str="+str);
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,str, null, null);
        if (cursor != null){
            while (cursor.moveToNext()){
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                QDLogger.i("找到title="+title);
                MusicInfo musicInfo = new MusicInfo();
                musicInfo.id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                //歌名
                musicInfo.title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                //歌手
                musicInfo.artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                musicInfo.path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                musicInfo.duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                musicInfo.size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                //文件路径
                musicInfo.data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                //专辑id
                musicInfo.albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                return musicInfo;
            }
        }else {
            QDLogger.e("未找到歌曲 cursor=null");
        }
        return null;
    }

    /**********获取歌曲专辑图片*************/
    public Bitmap getAlbumPicture(Context context,MusicInfo musicInfo,boolean canEmpty) {
        if(musicInfo==null||TextUtils.isEmpty(musicInfo.getData())){
            return null;
        }
        String dataPath = musicInfo.getData();
        android.media.MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(dataPath);
        }catch (Exception e){
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
            if(canEmpty){
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

    public List<MusicInfo> getQueue(Context context) {
        return songlist;
    }

    public static interface OnLoadDataListener {
        void loadComplete(int ret, List<MusicInfo> musicInfoList);
    }

    List<MusicInfo> songlist = new ArrayList<>();

    /**
     * 获取上一首歌曲信息，默认列表顺序
     *
     * @param id
     * @return
     */
    public MusicInfo getPrevMusicInfo(long id) {
        if (songlist != null) {
            int index = indexOfArray(songlist, id);
            QDLogger.i("getNextMusicInfo index=" + index);
            if (index - 1 >= 0) {
                return songlist.get(index - 1);
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
    public MusicInfo getNextMusicInfo(long id) {
        if (songlist != null) {
            int index = indexOfArray(songlist, id);
            QDLogger.println("getNextMusicInfo songlist=" + songlist.size() + ", index=" + index);
            if (index + 1 <= songlist.size() - 1) {
                return songlist.get(index + 1);
            }
        }
        return null;
    }

    /**
     * 获取在列表中的百分比位置，默认列表顺序
     *
     * @param id
     * @return
     */
    public float getIndexInList(long id) {
        int index = 0;
        if (songlist != null) {
            index = indexOfArray(songlist, id);
        } else {
            return 0;
        }
        return (float) (index + 1) / (float) songlist.size();
    }
    public int getIndexInQueue(long id) {
        if (songlist != null) {
            return indexOfArray(songlist, id);
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
        return indexOfArray(songlist, id);
    }

    /**
     * 获取在列表中的索引位置，默认列表顺序
     *
     * @param id
     * @return
     */
    public int indexOfArray(List<MusicInfo> list, long id) {
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
    public MusicInfo getFirstMusicInfo() {
        if (songlist != null && songlist.size() > 0) {
            return songlist.get(0);
        }
        return null;
    }
    /**
     * 获取第一首歌曲信息
     *
     * @return
     */
    public MusicInfo getLastMusicInfo() {
        if (songlist != null && songlist.size() > 0) {
            return songlist.get(songlist.size()-1);
        }
        return null;
    }

    /**
     * 根据id获取歌曲信息
     *
     * @return
     */
    public MusicInfo getMusicInfoById(long songId) {
        for (int i = 0; i < songlist.size(); i++) {
            //QDLogger.i("getMusicInfoById index=" + i + "," + songlist.get(i));
            if (songId == songlist.get(i).getId()) {
                //QDLogger.i("getMusicInfoById id=" + songId + "," + songlist.get(i));
                return songlist.get(i);
            }
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

    /**
     * 收藏列表是否存在
     * @return
     */
    public long initFaroriteTable(Context context) {
        long favorites_id = -1;
        ContentResolver resolver = context.getContentResolver();
        String favorites_where = MediaStore.Audio.PlaylistsColumns.NAME + "='" + PLAYLIST_NAME_FAVORITES + "'";
        String[] favorites_cols = new String[]{
                MediaStore.Audio.Media._ID
        };
        Uri favorites_uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
        Cursor cursor = resolver.query(favorites_uri, favorites_cols, favorites_where, null,
                null);
        if (cursor.getCount() <= 0) {
            favorites_id = createPlaylist(context, PLAYLIST_NAME_FAVORITES);
        } else {
            cursor.moveToFirst();
            favorites_id = cursor.getLong(0);
        }
        cursor.close();
        QDLogger.i("favorites_id=" + favorites_id);

        String[] cols = new String[]{
                MediaStore.Audio.Playlists.Members.AUDIO_ID
        };
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(EXTERNAL, favorites_id);
        Cursor cur = resolver.query(uri, cols, null, null, null);
        while (cur.moveToNext()) {//添加收藏列表
            long id = cur.getLong(0);
            faroriteMap = new LinkedHashMap<>();
            faroriteMap.put(id, id);
        }
        cur.close();
        return favorites_id;
    }

    LinkedHashMap<Long, Long> faroriteMap = new LinkedHashMap<>();
    public boolean isFarorite(Context context, long id) {
        if (faroriteMap != null) {
            return faroriteMap.containsValue(id);
        }
        long favorites_id;
        favorites_id = initFaroriteTable(context);
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
        QDLogger.i("cur.getCount() =" + count);
        return count >= 1;
    }

    public void addFavorite(Context context, Long id) {
        long table_favorites_id;
        table_favorites_id = initFaroriteTable(context);
        if (table_favorites_id == -1) {
            return;
        }
        ContentResolver resolver = context.getContentResolver();

        String[] cols = new String[]{
                MediaStore.Audio.Playlists.Members.AUDIO_ID
        };
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(EXTERNAL, table_favorites_id);
        Cursor cur = resolver.query(uri, cols, null, null, null);
        int base = cur.getCount();
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            if (cur.getLong(0) == id)
                return;
            cur.moveToNext();
        }
        cur.close();

        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, id);
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + 1);
        Uri uri1 = resolver.insert(uri, values);
        if (faroriteMap != null) {
            faroriteMap.put(id, id);
        }
        QDLogger.i("uri1 =" + uri1.getPath());
    }

    public void removeFavorite(Context context, Long id) {
        long table_favorites_id;
        ContentResolver resolver = context.getContentResolver();
        table_favorites_id = initFaroriteTable(context);

        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(EXTERNAL, table_favorites_id);
        resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID + "=" + id, null);
        if (faroriteMap != null) {
            faroriteMap.remove(id);
        }
    }


    /**
     * @param context
     * @param name
     * @return
     */
    public static long createPlaylist(Context context, String name) {
        if (!TextUtils.isEmpty(name)) {
            ContentResolver resolver = context.getContentResolver();
            String[] cols = new String[]{
                    MediaStore.Audio.PlaylistsColumns.NAME
            };
            String whereclause = MediaStore.Audio.PlaylistsColumns.NAME + " = '" + name + "'";
            Cursor cur = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, cols, whereclause,
                    null, null);
            if (cur.getCount() <= 0) {
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Audio.PlaylistsColumns.NAME, name);
                Uri uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);
                return Long.parseLong(uri.getLastPathSegment());
            }
            return -1;
        }
        return -1;
    }
}
