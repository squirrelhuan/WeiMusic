package com.demomaster.weimusic.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.Audio.Playlists;
import android.provider.MediaStore.MediaColumns;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.activity.AddSongSheetActivity;
import com.demomaster.weimusic.activity.MainActivity;
import com.demomaster.weimusic.activity.SearchActivity;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.model.AudioInfo;
import com.demomaster.weimusic.model.AudioSheet;
import com.demomaster.weimusic.player.service.MC;
import com.demomaster.weimusic.player.service.MusicDataManager;
import com.demomaster.weimusic.ui.adapter.MusicRecycleViewAdapter;
import com.google.android.material.appbar.AppBarLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.util.DisplayUtil;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDDialog;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDSheetDialog;
import cn.demomaster.qdlogger_library.QDLogger;
import cn.demomaster.qdrouter_library.base.fragment.QuickFragment;
import cn.demomaster.quickpermission_library.PermissionHelper;

import static com.demomaster.weimusic.activity.BaseActivity.PERMISSIONS;
import static com.demomaster.weimusic.constant.Constants.EXTERNAL;
import static com.demomaster.weimusic.constant.Constants.MIME_TYPE;
import static com.demomaster.weimusic.constant.Constants.PLAYLIST_FAVORITES;
import static com.demomaster.weimusic.constant.Constants.PLAYLIST_QUEUE;


public class MainFragment3 extends QuickFragment implements AppBarLayout.OnOffsetChangedListener {

    @BindView(R.id.rv_songs)
    RecyclerView rv_songs;
    private List<AudioInfo> musicList = new ArrayList<>();
    private List<AudioInfo> mAudioInfoList = new ArrayList<>();
    //private ClearEditText mClearEditText;

    /**
     * 汉字转换成拼音的类
     */
    //private CharacterParser characterParser;
    //private List<GroupMemberBean> SourceDateList;
    /**
     * 根据拼音来排列ListView里面的数据类
     */
    //private PinyinComparator pinyinComparator;

    LinearLayout mSearchLayout;
    ScrollView mScrollView;
    // ImageView iv_top;
    ImageView ivImg;
    Toolbar toolbar;
    private TransitionSet mSet;

    // Adapter
    //private TrackAdapter mTrackAdapter;
    //private MusicAdapter adapter;
    private MusicRecycleViewAdapter adapter;
    // Playlist ID
    private long mPlaylistId = PLAYLIST_QUEUE;
    // Selected position
    private int mSelectedPosition;
    // Options
    private final int PLAY_SELECTION = 6;
    private final int USE_AS_RINGTONE = 7;
    private final int ADD_TO_PLAYLIST = 8;
    private final int SEARCH = 9;
    private final int REMOVE = 10;
    private boolean mEditMode = false;
    //private RecentlyAddedAdapter mRecentlyAddedAdapter;


    @Override
    public boolean isUseActionBarLayout() {
        return false;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @BindView(R.id.sv_search)
    SearchView mSearchView;
    @BindView(R.id.iv_search)
    ImageView iv_search;
    @Override
    public View onGenerateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_03, container, false);
    }

    @Override
    public void initView(View view) {
        ButterKnife.bind(this,view);
        mSearchView.setIconifiedByDefault(true);
        mSearchView.onActionViewExpanded();

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //findResult(query);
                queryChanged(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                QDLogger.println("onQueryTextChange--" + newText);
                queryChanged(newText);
                return false;
            }
        });
        iv_search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SearchActivity.class));
            }
        });

        adapter = new MusicRecycleViewAdapter(getContext(), musicList);
       /* adapter = new MusicAdapter(musicList, getActivity(), new FilterListener() {
            // 回调方法获取过滤后的数据
            public void getFilterData(final List<MusicInfo> list) {
                // 这里可以拿到过滤后数据，所以在这里可以对搜索后的数据进行操作
                Log.e("TAG", "接口回调成功");
                playMusic(list);
            }
        });*/
        //这里使用线性布局像listview那样展示列表,第二个参数可以改为 HORIZONTAL实现水平展示
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        //使用网格布局展示
        rv_songs.setLayoutManager(linearLayoutManager);
        //recy_drag.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        //设置分割线使用的divider
        //rv_songs.addItemDecoration(dividerItemDecoration);
        rv_songs.setAdapter(adapter);
        rv_songs.setOnCreateContextMenuListener(this);

       /* lvSongs = rootView.findViewById(R.id.lvSongs);
        lvSongs.setAdapter(adapter);
        lvSongs.setOnCreateContextMenuListener(this);*/
        //lvSongs.setEmptyView(new TextView(this));
        //mRecentlyAddedAdapter = new RecentlyAddedAdapter(getActivity(),
        //		R.layout.item_song_list02 /* R.layout.listview_items */, null,
        //		new String[] {}, new int[] {}, 0, lvSongs);
        adapter.setOnItemClickListener(new MusicRecycleViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                playMusic(position);
            }

            @Override
            public void showContextMenu(View view, int position) {
                showSongMenu(view, position);
            }
        });

        //通过工具类，获取虚化的bitmap
        // Resources res = getResources();
        //Bitmap originBitmap = BitmapFactory.decodeResource(res, R.drawable.top);
        //Bitmap blurBitmap = BlurUtil.apply(getActivity(), originBitmap, 15);
        // iv_top = (ImageView) findViewById(R.id.iv_top);
        // iv_top.setImageBitmap(blurBitmap);

        ArrayList<String> strs = new ArrayList<String>();

		/*if(musicList_current.size()<1){
			Toast.makeText(getActivity(), "songlist", Toast.LENGTH_SHORT).show();
		};

		for (int i = 0; i < musicList_current.size(); i++) {
			strs.add(musicList_current.get(i).getTitle());
		}
		final String[] sss = strs.toArray(new String[strs.size()]);*/
        //titleLayout = (LinearLayout) findViewById(R.id.title_layout);
        //title = (TextView) this.findViewById(R.id.title_layout_catalog);
        //tvNofriends = (TextView) this
        //		.findViewById(R.id.title_layout_no_friends);

        //mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);
        //mClearEditText.setFocusable(false);
        // 实例化汉字转拼音类
        //characterParser = CharacterParser.getInstance();

        //pinyinComparator = new PinyinComparator();

        //sideBar = (SideBar) findViewById(R.id.sidrbar);
        //dialog = (TextView) findViewById(R.id.dialog);
        //sideBar.setTextView(dialog);

        // 设置右侧触摸监听
		/*sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
			*/    // 该字母首次出现的位置
				/*int position = adapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					lvSongs.setSelection(position);
				}*/
        //}
        //});
        //SourceDateList = filledData(sss,musicList_current);
        // 根据a-z进行排序源数据
        //Collections.sort(SourceDateList, pinyinComparator);
        isEditMode();

        boolean b = PermissionHelper.getInstance().getPermissionStatus(getContext(), PERMISSIONS);
        if (b) {
            MusicDataManager.getInstance(getContext()).loadData(getContext(), new MusicDataManager.OnLoadDataListener() {
                @Override
                public void loadComplete(int ret, List<AudioInfo> audioInfoList) {
                    if (ret == 1) {
                        if (audioInfoList != null) {
                            mAudioInfoList.clear();
                            mAudioInfoList.addAll(audioInfoList);
                            musicList.clear();
                            musicList.addAll(audioInfoList);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }

		/*tvSearch = (EditText) findViewById(R.id.tv_search);
		mSearchLayout = (LinearLayout) findViewById(R.id.ll_search);
		mScrollView = (CustomScrollView) findViewById(R.id.scrollView);
		toolbar = (Toolbar) findViewById(R.id.toolbar);
		ivImg = (ImageView) findViewById(R.id.iv_img);

		//设置toolbar初始透明度为0
		toolbar.getBackground().mutate().setAlpha(0);
		//scrollview滚动状态监听
		mScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
			@Override
			public void onScrollChanged() {
				//改变toolbar的透明度
				changeToolbarAlpha();
				//滚动距离>=大图高度-toolbar高度 即toolbar完全盖住大图的时候 且不是伸展状态 进行伸展操作
				if (mScrollView.getScrollY() >=ivImg.getHeight() - toolbar.getHeight()  && !isExpand) {
					//reduce();
					isExpand = true;
				}
				//滚动距离<=0时 即滚动到顶部时  且当前伸展状态 进行收缩操作
				else if (mScrollView.getScrollY()<=0&& isExpand) {
					//expand();
					isExpand = false;
				}
			}
		});
		setListeners();*/
        setHead();
    }

    private void queryChanged(String query) {
        musicList.clear();
        for (AudioInfo audioInfo : mAudioInfoList) {
            if (TextUtils.isEmpty(query) || audioInfo.getTitle().contains(query)) {
                musicList.add(audioInfo);
            }
        }
        adapter.notifyDataSetChanged();
    }

    QDDialog musicInfoDialog = null;
    private void showSongMenu(View view,final int position) {
        //AudioInfoDialog audioInfoDialog = new AudioInfoDialog(getContext(),musicList.get(position),position);
        //audioInfoDialog.show();

        Intent intent =new Intent();
        Bundle bundle = new Bundle();
        bundle.putInt("selectIndex",position);
        intent.putExtras(bundle);

        Bitmap bitmap = ((MainActivity)getActivity()).getBackagroundBitmap(220);
        //rl_docker_panel.setBackground(new BitmapDrawable(copyBitmap));
        //rl_docker_panel.setBackgroundColor(getResources().getColor(R.color.white));
        //ll_bottom.setBackgroundResource(R.drawable.rect_round_docker_bg);
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte [] bitmapByte =baos.toByteArray();
        intent.putExtra("bitmap", bitmapByte);

        ((MainActivity)getActivity()).startFragment(new AudioInoFragment(),R.id.main_layout,intent);

        /*musicInfoDialog = new QDDialog.Builder(getContext())
                .setBackgroundRadius(50)
                .setContentView(layout)
                .setBackgroundColor(getResources().getColor(R.color.transparent_light_33))
                .setAnimationStyleID(R.style.qd_dialog_animation_center_scale)
                .create();*/
        //musicInfoDialog.show();
    }

    List<AudioSheet> audioSheetList;
    private void showMenuDialog(AudioInfo audioInfo) {
        String[] menus = {"创建歌单"};
        audioSheetList = MusicDataManager.getInstance(getContext()).getSongSheet(getContext());
        if(audioSheetList != null){
            menus=new String[audioSheetList.size()+1];
            menus[0] = "创建歌单";
            for (int i = 0; i< audioSheetList.size(); i++){
                AudioSheet collection = audioSheetList.get(i);
                menus[i+1] = collection.getName();
            }
        }
        new QDSheetDialog.MenuBuilder(getContext())
                .setData(menus)
                .setOnDialogActionListener(new QDSheetDialog.OnDialogActionListener() {
                    @Override
                    public void onItemClick(QDSheetDialog dialog, int position, List<String> data) {
                        dialog.dismiss();
                        if (position == 0) {
                            startActivity(new Intent(getContext(), AddSongSheetActivity.class));
                        }else {
                            MusicDataManager.getInstance(getContext()).addToSheet(getContext(), audioSheetList.get(position-1).getId(), audioInfo.getId());
                        }
                    }
                })
                .create()
                .show();
    }

    private void playMusic(int i) {
        if (i < musicList.size()) {
            MC.getInstance(getContext()).playAudio(musicList.get(i));
        }
        adapter.notifyDataSetChanged();
    }

    ViewGroup ll_content;
    private void setHead() {
        //tv_search_01 = (EditText) findViewById(R.id.tv_search_expand);
        ll_content = findViewById(R.id.ll_content);
        ll_content.setPadding(ll_content.getPaddingLeft(), DisplayUtil.getStatusBarHeight(getContext()), ll_content.getPaddingRight(), ll_content.getPaddingBottom());
        //v_expand_mask = (View) findViewById(R.id.v_expand_mask);
        //tl_expand = (View) findViewById(R.id.tl_expand);
        //tl_collapse = (View) findViewById(R.id.tl_collapse);
        //abl_bar = (AppBarLayout) findViewById(R.id.abl_bar);
        //1abl_bar.addOnOffsetChangedListener(this);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        QDLogger.d("verticalOffset=" + verticalOffset);
        int offset = Math.abs(verticalOffset);
        if (offset <= DisplayUtil.dip2px(getContext(), 108)) {
            //tl_expand.setVisibility(View.VISIBLE);
            //tl_collapse.setVisibility(View.GONE);
            // addListeners(tv_search_01);
            //v_expand_mask.setBackgroundColor(maskColorInDouble);
            //abl_bar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } else {
            //abl_bar.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            //tl_expand.setVisibility(View.GONE);
            //tl_collapse.setVisibility(View.VISIBLE);
            //addListeners(tv_search_02);
            //v_collapse_mask.setBackgroundColor(maskColorOut);
        }
        //  v_pay_mask.setBackgroundColor(maskColorIn);
    }

//adapter.getFilter().filter(s);

    private void changeToolbarAlpha() {
        int scrollY = mScrollView.getScrollY();
        //快速下拉会引起瞬间scrollY<0
        if (scrollY < 0) {
            toolbar.getBackground().mutate().setAlpha(0);
            return;
        }
        //计算当前透明度比率
        float radio = Math.min(1, scrollY / (ivImg.getHeight() - toolbar.getHeight() * 1f));
        //设置透明度
        toolbar.getBackground().mutate().setAlpha((int) (radio * 0xFF));
    }

    private void expand() {
        //设置伸展状态时的布局
        // tvSearch.setHint("搜索歌曲名称");
        RelativeLayout.LayoutParams LayoutParams = (RelativeLayout.LayoutParams) mSearchLayout.getLayoutParams();
        LayoutParams.width = LayoutParams.MATCH_PARENT;
        LayoutParams.setMargins(DisplayUtil.dip2px(getContext(), 10), DisplayUtil.dip2px(getContext(), 10), DisplayUtil.dip2px(getContext(), 10), DisplayUtil.dip2px(getContext(), 10));
        mSearchLayout.setLayoutParams(LayoutParams);
        //开始动画
        beginDelayedTransition(mSearchLayout);
    }

    private void reduce() {
        //设置收缩状态时的布局搜索
        // tvSearch.setHint("");
        // tvSearch.setText("");
        RelativeLayout.LayoutParams LayoutParams = (RelativeLayout.LayoutParams) mSearchLayout.getLayoutParams();
        LayoutParams.width = DisplayUtil.dip2px(getContext(), 80);
        LayoutParams.setMargins(DisplayUtil.dip2px(getContext(), 10), DisplayUtil.dip2px(getContext(), 10), DisplayUtil.dip2px(getContext(), 10), DisplayUtil.dip2px(getContext(), 10));
        mSearchLayout.setLayoutParams(LayoutParams);
        //开始动画
        beginDelayedTransition(mSearchLayout);
    }

    void beginDelayedTransition(ViewGroup view) {
        mSet = new AutoTransition();
        mSet.setDuration(300);
        TransitionManager.beginDelayedTransition(view, mSet);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, PLAY_SELECTION, 0, getResources().getString(R.string.play_all));
        menu.add(0, ADD_TO_PLAYLIST, 0, getResources().getString(R.string.add_to_playlist));
        menu.add(0, USE_AS_RINGTONE, 0, getResources().getString(R.string.use_as_ringtone));
        if (mEditMode) {
            menu.add(0, REMOVE, 0, R.string.remove);
        }
        menu.add(0, SEARCH, 0, getResources().getString(R.string.search));

        AdapterContextMenuInfo mi = (AdapterContextMenuInfo) menuInfo;
        mSelectedPosition = mi.position;
        /*mCursor.moveToPosition(mSelectedPosition);
        try {
            mSelectedId = mCursor.getLong(mMediaIdIndex);
            mSelectedId = adapter.getItemId(mSelectedPosition);
        } catch (IllegalArgumentException ex) {
            mSelectedId = mi.id;
            mSelectedId = adapter.getItemId(mSelectedPosition);
        }
        String title = mCursor.getString(mTitleIndex);
        title = ((MusicInfo) adapter.getItem(mSelectedPosition)).getTitle();
        menu.setHeaderTitle(title);*/
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case PLAY_SELECTION:
                int position = mSelectedPosition;
                //MusicHelper.getInstance().playAll(getActivity(), mCursor, position);
                break;
            case ADD_TO_PLAYLIST: {
               /* Intent intent = new Intent(INTENT_ADD_TO_PLAYLIST);
                long[] list = new long[]{
                        mSelectedId
                };
                intent.putExtra(INTENT_PLAYLIST_LIST, list);
                getActivity().startActivity(intent);*/
                break;
            }
            case USE_AS_RINGTONE:
                // MusicHelper.setRingtone(getActivity(), mSelectedId);
                break;
            case REMOVE: {
                removePlaylistItem(mSelectedPosition);
                break;
            }
            case SEARCH: {
                /*lvSongs.post(new Runnable() {
                    @Override
                    public void run() {
                        lvSongs.setSelectionAfterHeaderView();
                    }
                });*/
                //MusicHelper.doSearch(getActivity(), mCursor, mTitleIndex);
                break;
            }
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * @param which
     */
    private void removePlaylistItem(int which) {
      /*  mCursor.moveToPosition(which);
        long id = mCursor.getLong(mMediaIdIndex);*/
        long id = 0;
        if (mPlaylistId >= 0) {
            Uri uri = Playlists.Members.getContentUri(EXTERNAL, mPlaylistId);
            getActivity().getContentResolver().delete(uri, Playlists.Members.AUDIO_ID + "=" + id,
                    null);
        } else if (mPlaylistId == PLAYLIST_QUEUE) {
            // MusicHelper.getInstance().removeTrack(id);
            reloadQueueCursor();
        } else if (mPlaylistId == PLAYLIST_FAVORITES) {
            // MusicHelper.removeFromFavorites(getActivity(), id);
        }
        // lvSongs.invalidateViews();
    }

    /**
     * Reload the queue after we remove a track
     */
    private void reloadQueueCursor() {
        String str = " != ''";
        /*if (tvSearch != null && tvSearch.getText() != null && !tvSearch.getText().toString().trim().equals("")) {
            str = " like '" + tvSearch.getText().toString() + "' ";
        }*/
        if (mPlaylistId == PLAYLIST_QUEUE) {
            String[] cols = new String[]{
                    BaseColumns._ID, MediaColumns.TITLE, MediaColumns.DATA, AudioColumns.ALBUM,
                    AudioColumns.ARTIST, AudioColumns.ARTIST_ID
            };
            StringBuilder selection = new StringBuilder();
            selection.append(AudioColumns.IS_MUSIC + "=1");
            selection.append(" AND " + MediaColumns.TITLE + str);
            //selection.append(" order by if(instr("+MediaColumns.TITLE +",'"+tvSearch.getText().toString()+"') >0,1,0) desc   ");
            Uri uri = Audio.Media.EXTERNAL_CONTENT_URI;
            /*long[] mNowPlaying = MusicHelper.getInstance().getQueue();
            if (mNowPlaying.length == 0) {
            }
            selection = new StringBuilder();
            selection.append(BaseColumns._ID + " IN (");
            for (int i = 0; i < mNowPlaying.length; i++) {
                selection.append(mNowPlaying[i]);
                if (i < mNowPlaying.length - 1) {
                    selection.append(",");
                }
            }
            selection.append(")");
            mCursor = MusicHelper.query(getActivity(), uri, cols, selection.toString(), null, MediaColumns.TITLE);*/
            //mTrackAdapter.changeCursor(mCursor);
            //lvSongs.invalidateViews();
        }
    }

    /**
     * Check if we're viewing the contents of a playlist
     */
    public void isEditMode() {
        if (getArguments() != null) {
            String mimetype = getArguments().getString(MIME_TYPE);
            if (Playlists.CONTENT_TYPE.equals(mimetype)) {
                mPlaylistId = getArguments().getLong(BaseColumns._ID);
                switch ((int) mPlaylistId) {
                    case (int) PLAYLIST_QUEUE:
                        mEditMode = true;
                        break;
                    case (int) PLAYLIST_FAVORITES:
                        mEditMode = true;
                        break;
                    default:
                        if (mPlaylistId > 0) {
                            mEditMode = true;
                        }
                        break;
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventMessage message) {
        //adapter.notifyDataSetChanged();
        AudioStation station = AudioStation.getEnum(message.code);
        if (station == null) {
            return;
        }
        switch (station) {
            case song_changed:
                adapter.notifyDataSetChanged();
                break;
            case PLAYSTATE_CHANGED:
                adapter.notifyDataSetChanged();
                break;
            case service_ready:
                break;
            case Play:
                adapter.notifyDataSetChanged();
                break;
            case Pause:
                adapter.notifyDataSetChanged();
                break;
            case QUEUE_CHANGED:
                notifyFile();
                break;
            case CURSOR_CHANGED:
                int p = (int) message.getObj();
                rv_songs.scrollToPosition(p);
                LinearLayoutManager mLayoutManager = (LinearLayoutManager) rv_songs.getLayoutManager();
                mLayoutManager.scrollToPositionWithOffset(p, 0);
                break;
        }
    }
    private void notifyFile() {
        boolean b = PermissionHelper.getInstance().getPermissionStatus(getContext(), PERMISSIONS);
        if (b) {
            MusicDataManager.getInstance(getContext()).loadData(getContext(),60000, new MusicDataManager.OnLoadDataListener() {
                @Override
                public void loadComplete(int ret, List<AudioInfo> audioInfoList) {
                    if (ret == 1) {
                        if (audioInfoList != null) {
                            //QdToast.show("共搜索到："+ audioInfoList.size());
                            mAudioInfoList.clear();
                            mAudioInfoList.addAll(audioInfoList);
                            musicList.clear();
                            musicList.addAll(audioInfoList);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (musicInfoDialog != null) {
            musicInfoDialog.dismiss();
        }
    }
}
