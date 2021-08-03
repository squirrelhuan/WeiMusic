package com.demomaster.weimusic.ui.fragment;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.model.MusicCollection;
import com.demomaster.weimusic.ui.adapter.MusicCollectAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.demomaster.huan.quickdeveloplibrary.util.DisplayUtil;

public class MainFragment1 extends BaseFragment implements OnClickListener,
        OnItemClickListener , LoaderManager.LoaderCallbacks<Cursor> {

    LinearLayout ll_search;
    ListView lv_song_sheet;
    ImageView main_image;
    public Adapter adapter;
    public MusicCollectAdapter musicCollectAdapter;

    @Override
    public View setContentUI(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.fragment_main_01, container, false);
    }

    @Override
    public void initView(View view) {
        ll_search = (LinearLayout) findViewById(R.id.ll_search);
        ll_search.setOnClickListener(this);

        GridView gridview1 = (GridView) rootView.findViewById(R.id.gridview1);//
        ArrayList<HashMap<String, Object>> ItemList = new ArrayList<HashMap<String, Object>>();

        HashMap<String, Object> map_0 = new HashMap<String, Object>();
        //map_0.put("Image", R.drawable.mymusic_icon_allsongs_normal);
        map_0.put("Tag", "本地歌曲");
        ItemList.add(map_0);
        HashMap<String, Object> map_1 = new HashMap<String, Object>();
        //map_1.put("Image", R.drawable.mymusic_icon_download_normal);
        map_1.put("Tag", "下载歌曲");
        ItemList.add(map_1);
        HashMap<String, Object> map_2 = new HashMap<String, Object>();
       // map_2.put("Image", R.drawable.mymusic_icon_history_normal);
        map_2.put("Tag", "最近播放");
        ItemList.add(map_2);
        HashMap<String, Object> map_3 = new HashMap<String, Object>();
        //map_3.put("Image", R.drawable.mymusic_icon_favorite_normal);
        map_3.put("Tag", "我喜欢");
        ItemList.add(map_3);
        HashMap<String, Object> map_4 = new HashMap<String, Object>();
        //map_4.put("Image", R.drawable.mymusic_icon_mv_normal);
        map_4.put("Tag", "下载mv");
        ItemList.add(map_4);
        HashMap<String, Object> map_5 = new HashMap<String, Object>();
        //map_5.put("Image", R.drawable.mymusic_icon_recognizer_normal);
        map_5.put("Tag", "听歌识曲");
        ItemList.add(map_5);

        adapter = new SimpleAdapter(getActivity(), ItemList,
                R.layout.home_item_style, new String[]{"Image", "Tag"},
                new int[]{R.id.ItemImage, R.id.ItemText});
        gridview1.setAdapter((ListAdapter) adapter);
        gridview1.setOnItemClickListener(this);

        lv_song_sheet = (ListView) findViewById(R.id.lv_song_sheet);
        musicCollectAdapter = new MusicCollectAdapter(musicCollections,getActivity());
        lv_song_sheet.setAdapter(musicCollectAdapter);

        //歌单
        //getLoaderManager().initLoader(0, null, this);
        setHead();
    }
    ViewGroup ll_content;
    private void setHead() {
        ll_content = findViewById(R.id.ll_content);
        ll_content.setPadding(ll_content.getPaddingLeft(), DisplayUtil.getStatusBarHeight(getContext()),ll_content.getPaddingRight(),ll_content.getPaddingBottom());
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_search:
                //IntentUtil.jump(getActivity(), SearchActivity.class, null);
                break;

            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Bundle bundle = new Bundle();
        switch (position) {
            case 0:
                bundle.putString("Title", "本地歌曲");
                //IntentUtil.jump(getActivity(), MusicListActivity.class, bundle);
                break;
            case 1:
                bundle.putString("Title", "我的歌单");
                //IntentUtil.jump(getActivity(), SongSheetListActivity.class, bundle);
                break;
            case 2:
                bundle.putString("Title", "最近播放");
                break;
            case 3:
                bundle.putString("Title", "我喜欢");
                break;
            case 4:
                bundle.putString("Title", "下载mv");
                break;
            case 5:
                bundle.putString("Title", "听歌识曲");
                break;
            default:
                break;
        }
    }



    String[] projection01 = new String[]{
            BaseColumns._ID, MediaStore.Audio.PlaylistsColumns.NAME
    };
    private Cursor mCursor;
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //加载歌单
        if (i == 0) {

            Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
            String sortOrder = MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER;
            StringBuilder selection = new StringBuilder();
            if (bundle != null) {
                selection.append(BaseColumns._ID);
                selection.append(" = " + bundle.get("ID") + " ");
            }
            CursorLoader cursorLoader = new CursorLoader(getActivity(), uri, projection01, selection.toString(), null, sortOrder);
            return cursorLoader;
        }
        return null;
    }
    // Aduio columns
    public static int mPlaylistNameIndex, mPlaylistIdIndex;
    private List<MusicCollection> musicCollections = new ArrayList<>();
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null) {
            return;
        }
        //加载歌单
        if (loader.getId() == 0) {
            mPlaylistIdIndex = data.getColumnIndexOrThrow(BaseColumns._ID);
            mPlaylistNameIndex = data.getColumnIndexOrThrow(MediaStore.Audio.PlaylistsColumns.NAME);
            //mPlaylistAdapter.changeCursor(data);
            mCursor = data;

            musicCollections.clear();
            while (data.moveToNext()) {
                MusicCollection musicInfo = new MusicCollection();
                musicInfo.setId(data.getLong(mPlaylistIdIndex));
                musicInfo.setName(data.getString(mPlaylistNameIndex));
                musicCollections.add(musicInfo);
            }
            musicCollectAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
