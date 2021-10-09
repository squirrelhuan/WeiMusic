package com.demomaster.weimusic.ui.fragment;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.model.AudioSheet;
import com.demomaster.weimusic.player.service.MC;
import com.demomaster.weimusic.ui.adapter.MusicCollectAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.demomaster.huan.quickdeveloplibrary.base.activity.QDActivity;
import cn.demomaster.huan.quickdeveloplibrary.util.DisplayUtil;
import cn.demomaster.qdrouter_library.base.fragment.QuickFragment;

public class MainFragment1 extends QuickFragment implements OnClickListener,
        OnItemClickListener , LoaderManager.LoaderCallbacks<Cursor> {

    LinearLayout ll_search;
    ListView lv_song_sheet;
    public Adapter adapter;
    public MusicCollectAdapter musicCollectAdapter;
    EditText et_audio_source;
    Button btn_play_online;

    @Override
    public boolean isUseActionBarLayout() {
        return false;
    }

    @Override
    public View onGenerateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_01, container, false);
    }

    @Override
    public void initView(View view) {
        ll_search = findViewById(R.id.ll_search);
        ll_search.setOnClickListener(this);
        et_audio_source = findViewById(R.id.et_audio_source);
        btn_play_online = findViewById(R.id.btn_play_online);
        btn_play_online.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(et_audio_source.getText())) {
                    MC.getInstance(getContext()).playOnline(et_audio_source.getText().toString());
                }
            }
        });

        GridView gridview1 = findViewById(R.id.gridview1);//
        ArrayList<HashMap<String, Object>> ItemList = new ArrayList<HashMap<String, Object>>();

        HashMap<String, Object> map_0 = new HashMap<String, Object>();
        map_0.put("Image", R.mipmap.ic_qq_music);
        map_0.put("Tag", "qq音乐");
        ItemList.add(map_0);
        HashMap<String, Object> map_1 = new HashMap<String, Object>();
        map_1.put("Image", R.mipmap.ic_cloud_music);
        map_1.put("Tag", "网易云音乐");
        ItemList.add(map_1);
        HashMap<String, Object> map_2 = new HashMap<String, Object>();
        map_2.put("Image", R.mipmap.ic_qianqian_music);
        map_2.put("Tag", "千千音乐");
        ItemList.add(map_2);
        HashMap<String, Object> map_3 = new HashMap<String, Object>();
        map_3.put("Image", R.mipmap.ic_kugou_music);
        map_3.put("Tag", "酷狗音乐");
        ItemList.add(map_3);
        HashMap<String, Object> map_4 = new HashMap<String, Object>();
        map_4.put("Image", R.mipmap.ic_ximalaya);
        map_4.put("Tag", "喜马拉雅");
        ItemList.add(map_4);
        HashMap<String, Object> map_5 = new HashMap<String, Object>();
        map_5.put("Image", R.mipmap.ic_kuwo);
        map_5.put("Tag", "酷我音乐");
        ItemList.add(map_5);

        adapter = new SimpleAdapter(getActivity(), ItemList,
                R.layout.home_item_style, new String[]{"Image", "Tag"},
                new int[]{R.id.iv_logo, R.id.tv_title});
        gridview1.setAdapter((ListAdapter) adapter);
        gridview1.setOnItemClickListener(this);

        lv_song_sheet = (ListView) findViewById(R.id.lv_song_sheet);
        musicCollectAdapter = new MusicCollectAdapter(audioSheets,getActivity());
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
                bundle.putString("Title", "QQ音乐");
                jumpUrl("https://y.qq.com/?ADTAG=myqq#type=index");
                //IntentUtil.jump(getActivity(), MusicListActivity.class, bundle);
                break;
            case 1:
                bundle.putString("Title", "网易云音乐");
                jumpUrl("https://music.163.com/");
                //IntentUtil.jump(getActivity(), SongSheetListActivity.class, bundle);
                break;
            case 2:
                bundle.putString("Title", "最近播放");
                jumpUrl("https://music.taihe.com/");
                break;
            case 3:
                bundle.putString("Title", "我喜欢");
                jumpUrl("https://www.kugou.com/");
                break;
            case 4:
                bundle.putString("Title", "下载mv");
                jumpUrl("https://www.ximalaya.com/yinyue/");
                break;
            case 5:
                bundle.putString("Title", "听歌识曲");http:
                jumpUrl("https://www.kuwo.cn/");
                break;
            default:
                break;
        }
    }

    private void jumpUrl(String url) {
        Bundle bundle = new Bundle();
        bundle.putString("URL", url);
        // webViewFragment.setArguments(bundle);
        // ((QDActivity)v.getContext()).getFragmentHelper().startFragment(webViewFragment);
        ((QDActivity)getActivity()).getFragmentHelper().build(getActivity(), WebViewFragment.class.getName())
                .putExtras(bundle)
                .putExtra("password", 666666)
                .putExtra("name", "小三")
                .navigation();
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
    private List<AudioSheet> audioSheets = new ArrayList<>();
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

            audioSheets.clear();
            while (data.moveToNext()) {
                AudioSheet musicInfo = new AudioSheet();
                musicInfo.setId(data.getLong(mPlaylistIdIndex));
                musicInfo.setName(data.getString(mPlaylistNameIndex));
                audioSheets.add(musicInfo);
            }
            musicCollectAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
