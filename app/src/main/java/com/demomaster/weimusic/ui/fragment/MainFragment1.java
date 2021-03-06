package com.demomaster.weimusic.ui.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.activity.SongSheetDetailActivity;
import com.demomaster.weimusic.activity.SongSheetListActivity;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.model.AudioSheet;
import com.demomaster.weimusic.model.Channel;
import com.demomaster.weimusic.player.service.MC;
import com.demomaster.weimusic.player.service.MusicDataManager;
import com.demomaster.weimusic.ui.adapter.MusicChannelAdapter;
import com.demomaster.weimusic.ui.adapter.RecyclerSheetAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import cn.demomaster.huan.quickdeveloplibrary.base.activity.QDActivity;
import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.util.DisplayUtil;
import cn.demomaster.qdlogger_library.QDLogger;
import cn.demomaster.qdrouter_library.base.fragment.QuickFragment;

public class MainFragment1 extends QuickFragment implements OnClickListener,
        OnItemClickListener  {

    LinearLayout ll_search;
    RecyclerView recyclerView_song_sheet;
    public MusicChannelAdapter adapter;
    public RecyclerSheetAdapter recyclerSheetAdapter;
    EditText et_audio_source;
    Button btn_play_online;
    ImageView iv_sheet_menu;
    
    @Override
    public boolean isUseActionBarLayout() {
        return false;
    }

    @Override
    public View onGenerateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_01, container, false);
    }
    List<Channel> channelList;
    @Override
    public void initView(View view) {
        EventBus.getDefault().register(this);
        ll_search = findViewById(R.id.ll_search);
        ll_search.setOnClickListener(this);
        iv_sheet_menu = findViewById(R.id.iv_sheet_menu);
        iv_sheet_menu.setOnClickListener(this);

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

        RecyclerView recycler_channel = findViewById(R.id.recycler_channel);//
        channelList = new ArrayList<>();
        Channel channel = new Channel();
        channel.setName("qq??????");
        channel.setRes(R.mipmap.ic_qq_music);
        channel.setUrl("https://y.qq.com/?ADTAG=myqq#type=index");
        channelList.add(channel);

        Channel channel1 = new Channel();
        channel1.setName("???????????????");
        channel1.setRes(R.mipmap.ic_cloud_music);
        channel1.setUrl("https://music.163.com/");
        channelList.add(channel1);

        Channel channel2 = new Channel();
        channel2.setName("????????????");
        channel2.setRes(R.mipmap.ic_qianqian_music);
        channel2.setUrl("https://music.taihe.com/");
        channelList.add(channel2);

        Channel channel3 = new Channel();
        channel3.setName("????????????");
        channel3.setRes(R.mipmap.ic_kugou_music);
        channel3.setUrl("https://www.kugou.com/");
        channelList.add(channel3);

        Channel channel4 = new Channel();
        channel4.setName("????????????");
        channel4.setRes(R.mipmap.ic_ximalaya);
        channel4.setUrl("https://www.ximalaya.com/yinyue/");
        channelList.add(channel4);

        Channel channel5 = new Channel();
        channel5.setName("????????????");
        channel5.setRes(R.mipmap.ic_kuwo);
        channel5.setUrl("https://www.kuwo.cn/");
        channelList.add(channel5);

        recycler_channel.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new MusicChannelAdapter(getContext(),channelList);
        /*adapter = new SimpleAdapter(getActivity(), ItemList,
                R.layout.home_item_style, new String[]{"Image", "Tag"},
                new int[]{R.id.iv_logo, R.id.tv_title});*/
        recycler_channel.setAdapter(adapter);
        adapter.setOnItemClickListener(this);

        recyclerView_song_sheet = findViewById(R.id.recyclerView_song_sheet);

        recyclerView_song_sheet.setLayoutManager(new GridLayoutManager(getContext(), 3));
        audioSheets = new ArrayList<>();
        audioSheets.addAll(MusicDataManager.getInstance(mContext).getSongSheet());
        recyclerSheetAdapter = new RecyclerSheetAdapter(getActivity(),audioSheets);
        recyclerSheetAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putLong("sheetId", audioSheets.get(position).getId());
                Intent intent = new Intent(getContext(), SongSheetDetailActivity.class);
                intent.putExtras(bundle);
                getContext().startActivity(intent);
            }
        });
        recyclerView_song_sheet.setAdapter(recyclerSheetAdapter);

        //??????
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
            case R.id.iv_sheet_menu:
                startActivity(new Intent(mContext, SongSheetListActivity.class));
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Bundle bundle = new Bundle();
        Channel channel = channelList.get(position);
        bundle.putString("Title", channel.getName());
        jumpUrl(channel.getUrl());
        /*switch (position) {
            case 0:
                bundle.putString("Title", "QQ??????");
                jumpUrl("");
                //IntentUtil.jump(getActivity(), MusicListActivity.class, bundle);
                break;
            case 1:
                bundle.putString("Title", "???????????????");
                jumpUrl("https://music.163.com/");
                //IntentUtil.jump(getActivity(), SongSheetListActivity.class, bundle);
                break;
            case 2:
                bundle.putString("Title", "????????????");
                jumpUrl("https://music.taihe.com/");
                break;
            case 3:
                bundle.putString("Title", "?????????");
                jumpUrl("https://www.kugou.com/");
                break;
            case 4:
                bundle.putString("Title", "??????mv");
                jumpUrl("https://www.ximalaya.com/yinyue/");
                break;
            case 5:
                bundle.putString("Title", "????????????");http:
                jumpUrl("https://www.kuwo.cn/");
                break;
            default:
                break;
        }*/
    }

    private void jumpUrl(String url) {
        Bundle bundle = new Bundle();
        bundle.putString("URL", url);
        // webViewFragment.setArguments(bundle);
        // ((QDActivity)v.getContext()).getFragmentHelper().startFragment(webViewFragment);
        ((QDActivity)getActivity()).getFragmentHelper().build(getActivity(), WebViewFragment.class.getName())
                .putExtras(bundle)
                .putExtra("password", 666666)
                .putExtra("name", "??????")
                .navigation();
    }

    String[] projection01 = new String[]{
            BaseColumns._ID, MediaStore.Audio.PlaylistsColumns.NAME
    };
    private Cursor mCursor;
   /* @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        //????????????
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
    }*/
    // Aduio columns
    public static int mPlaylistNameIndex, mPlaylistIdIndex;
    private List<AudioSheet> audioSheets = new ArrayList<>();
    /*@Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null) {
            return;
        }
        //????????????
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
    }*/

    /*@Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }*/

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventMessage message) {
        AudioStation station = AudioStation.getEnum(message.getCode());
        if (station != null) {
            QDLogger.println("????????????:" + station.getDesc());
            switch (station) {
                case sheet_create:
                case sheet_changed:
                    audioSheets.clear();
                    audioSheets.addAll(MusicDataManager.getInstance(mContext).getSongSheet());
                    recyclerSheetAdapter.notifyDataSetChanged();
                    break;
            }
        }
    }
}
