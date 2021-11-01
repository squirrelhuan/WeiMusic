package com.demomaster.weimusic.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.demomaster.weimusic.R;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.model.AudioInfo;
import com.demomaster.weimusic.model.AudioSheet;
import com.demomaster.weimusic.player.service.MC;
import com.demomaster.weimusic.player.service.MusicDataManager;
import com.demomaster.weimusic.ui.adapter.MusicRecycleViewAdapter;
import com.demomaster.weimusic.ui.adapter.MusicRecycleViewAdapter2;
import com.demomaster.weimusic.ui.adapter.MusicRecycleViewAdapter3;
import com.demomaster.weimusic.ui.adapter.SheetBodyAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.demomaster.huan.quickdeveloplibrary.base.activity.QDActivity;
import cn.demomaster.huan.quickdeveloplibrary.helper.PhotoHelper;
import cn.demomaster.huan.quickdeveloplibrary.helper.simplepicture.model.Image;
import cn.demomaster.huan.quickdeveloplibrary.helper.simplepicture.model.UrlType;
import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.util.BlurUtil;
import cn.demomaster.huan.quickdeveloplibrary.util.QDBitmapUtil;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDSheetDialog;
import cn.demomaster.qdlogger_library.QDLogger;
import cn.demomaster.qdrouter_library.actionbar.ACTIONBAR_TYPE;
import cn.demomaster.qdrouter_library.view.appbar.AppBarLayout;
import cn.demomaster.qdrouter_library.view.appbar.CollapsingLayout;

import static cn.demomaster.huan.quickdeveloplibrary.util.QDBitmapUtil.generateColorBitmap;

/**
 * 创建歌单页面
 */
public class SongSheetDetailActivity extends QDActivity implements View.OnClickListener {

    TextView tv_sheet_name;
    long sheetId = -1;
    ImageView iv_sheet_img;
    CollapsingLayout collapsingLayout;
    View toolbar_layout_01;
    //FrameLayout framelayout_header;
    RecyclerView recyclerView;
    MusicRecycleViewAdapter3 musicRecycleViewAdapter3;
    List<AudioInfo> musicList;
    Button btn_play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_song_sheet_detail);
        getActionBarTool().setActionBarType(ACTIONBAR_TYPE.NO_ACTION_BAR_NO_STATUS);
        getActionBarTool().setHeaderBackgroundColor(Color.TRANSPARENT);

        findViewById(R.id.it_actionbar_common_right).setOnClickListener(this);
        //framelayout_header = findViewById(R.id.framelayout_header);
        iv_sheet_img = findViewById(R.id.iv_sheet_img);
        iv_sheet_img.setOnClickListener(this);
        tv_sheet_name = findViewById(R.id.tv_sheet_name);
        btn_play = findViewById(R.id.btn_play);
        btn_play.setOnClickListener(this);
        recyclerView = findViewById(R.id.recyclerView);
        toolbar_layout_01 = findViewById(R.id.toolbar_layout_01);
        collapsingLayout = findViewById(R.id.collapsingLayout);
        collapsingLayout.setOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int top = appBarLayout.getMeasuredHeight() - Math.abs(verticalOffset);
                float h = toolbar_layout_01.getMeasuredHeight();
                //toolbar_layout_01.setAlpha(Math.abs(verticalOffset/h));
                int a = (int) ((1 - (Math.abs(MathUtils.clamp(1 - top / h, -1, 1)))) * 0xff);
                int color = a << 24 | 0xffffff;
                //toolbar_layout_01.setBackgroundColor(color);
                //toolbar_layout_01.setAlpha(top < 2*h?(2*h-top)/h*1f:0f);
                Drawable drawable = toolbar_layout_01.getBackground();

                //Drawable drawable = findViewById(R.id.headerView).getBackground();
                if (drawable != null) {
                    int alpha = (int) (255f * ((top < 2 * h ? (2 * h - top) / h * 1f : 0f)));
                    QDLogger.println("verticalOffset=" + top / h + "," + toolbar_layout_01.getMeasuredHeight() + ",a=" + a + ",color=" + color + ",alpha=" + alpha);
                    drawable.mutate().setAlpha(alpha);
                }
                // toolbar_layout_01.setBackground(drawable);
            }
        });
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.containsKey("sheetId")) {
                sheetId = bundle.getLong("sheetId");
                setUI();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUI();
    }

    private void setUI() {
        QDLogger.e("sheetId=" + sheetId);
        AudioSheet audioSheet = MusicDataManager.getInstance(mContext).getSongSheetById(mContext, sheetId);
        if (audioSheet != null) {
            tv_sheet_name.setText(audioSheet.getName());
            //Glide.with(mContext).load(audioSheet.getImgSrc()).into(iv_sheet_img);
            Image image1 = new Image(audioSheet.getImgSrc(), UrlType.file);
            updateHeader(image1);
            //Glide.with(mContext).load(audioSheet.getImgSrc()).error(R.drawable.ic_launcher_pp).into(iv_sheet_img);
            mColor = audioSheet.getThemeColor();
        }

        musicList = new ArrayList<>();
        musicRecycleViewAdapter3 = new MusicRecycleViewAdapter3(mContext, musicList);
        musicRecycleViewAdapter3.setOnItemClickListener(new MusicRecycleViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MC.getInstance(mContext).playAudio(musicList.get(position));
            }

            @Override
            public void showContextMenu(View view, int position) {
                //showSongMenu(viewHolder,sheetId,musicList,adapter,position);
            }
        });

        //这里使用线性布局像listview那样展示列表,第二个参数可以改为 HORIZONTAL实现水平展示
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        //使用网格布局展示
        recyclerView.setLayoutManager(linearLayoutManager);
        //recy_drag.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        //设置分割线使用的divider
        //rv_songs.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(musicRecycleViewAdapter3);
        List<AudioInfo> audioInfoList = MusicDataManager.getInstance(mContext).getSongSheetListById(mContext, sheetId);
        //QDLogger.i("musicInfoList:" + audioInfoList.size());
        musicList.clear();
        if (audioInfoList != null && audioInfoList.size() > 0) {
            musicList.addAll(audioInfoList);
            musicRecycleViewAdapter3.notifyDataSetChanged();
        }
    }

    int mColor;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            mColor = bundle.getInt("themeColor", mColor);
        }
    }

    private void creatSheet() {
        EventBus.getDefault().post(new EventMessage(AudioStation.sheet_changed.value()));//准备播放
        AudioSheet audioSheet = new AudioSheet();
        audioSheet.setName(tv_sheet_name.getText().toString());
        audioSheet.setThemeColor(mColor);
        audioSheet.setId(sheetId);
        if (image != null) {
            audioSheet.setImgSrc(image.getPath());
        }
        if (sheetId == -1) {
            MusicDataManager.getInstance(mContext).createSheet(mContext, audioSheet);
            finish();
        } else {
            MusicDataManager.getInstance(mContext).modifySheet(mContext, audioSheet);
            finish();
        }
    }

    private Image image;

    private void showMenuDialog() {
        String[] menus = getResources().getStringArray(cn.demomaster.huan.quickdeveloplibrary.R.array.select_picture_items);
        new QDSheetDialog.MenuBuilder(mContext).setData(menus).setOnDialogActionListener(new QDSheetDialog.OnDialogActionListener() {
            @Override
            public void onItemClick(QDSheetDialog dialog, int position, List<String> data) {
                dialog.dismiss();
                if (position == 0) {
                    ((QDActivity) mContext).getPhotoHelper().takePhoto(null, new PhotoHelper.OnTakePhotoResult() {
                        @Override
                        public void onSuccess(Intent data, String path) {
                            if (data == null) {
                                image = new Image(path, UrlType.file);
                                updateHeader(image);
                            } else {
                                Bundle extras = data.getExtras();
                                if (extras != null) {
                                    Bitmap bitmap = extras.getParcelable("data");
                                    String filePath = QDBitmapUtil.savePhoto(bitmap, mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath(), "header");//String.valueOf(System.currentTimeMillis())
                                    image = new Image(filePath, UrlType.file);
                                    updateHeader(image);
                                }
                            }
                        }

                        @Override
                        public void onFailure(String error) {

                        }
                    });
                } else {//从相册选择
                    ((QDActivity) mContext).getPhotoHelper().selectPhotoFromMyGallery(new PhotoHelper.OnSelectPictureResult() {
                        @Override
                        public void onSuccess(Intent data, ArrayList<Image> images) {
                            if (images != null && images.size() > 0) {
                                image = images.get(0);
                                updateHeader(image);
                            }
                        }

                        @Override
                        public void onFailure(String error) {

                        }

                        @Override
                        public int getImageCount() {
                            return 1;
                        }
                    });
                }
            }
        }).create().show();
    }

    private void updateHeader(Image image) {
        Object imgObj = null;
        if (image.getUrlType() == UrlType.url) {
            imgObj = image.getPath();
        } else if (image.getUrlType() == UrlType.file) {
            if (!TextUtils.isEmpty(image.getPath())) {
                imgObj = new File(image.getPath());
            } else {
                imgObj = R.mipmap.ic_favorite;
            }
        }
        if (imgObj != null) {
            Glide.with(mContext).asBitmap()
                    .load(imgObj)
                    .error(R.mipmap.ic_favorite)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull @NotNull Bitmap resource, Transition<? super Bitmap> transition) {
                            if (resource != null) {
                                iv_sheet_img.setImageBitmap(resource);
                                Bitmap heightBitmap = QDBitmapUtil.zoomImage(resource, 100, 100);
                                Bitmap bitmap = BlurUtil.doBlur(heightBitmap, 20, 0.2f);
                                Bitmap colorBitmap = generateColorBitmap(bitmap.getWidth(), bitmap.getHeight(), getResources().getColor(R.color.transparent_dark_33));
                                heightBitmap = QDBitmapUtil.mergeBitmap(bitmap, colorBitmap);
                                Drawable drawable = new BitmapDrawable(heightBitmap);
                                findViewById(R.id.headerView).setBackground(drawable);
                                if (findViewById(R.id.headerView).getBackground() != null) {
                                    Drawable drawable2 = new BitmapDrawable(heightBitmap);
                                    toolbar_layout_01.setBackground(drawable2);
                                }
                            }
                        }
                    });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_theme_color:
                Intent intent = new Intent(mContext, SelectThemeColorActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("themeColor", mColor);
                intent.putExtras(bundle);
                startActivityForResult(intent, 123);
                break;
            case R.id.iv_sheet_img:
                showMenuDialog();
                break;
            case R.id.btn_creat:
                creatSheet();
                break;
            case R.id.btn_play:
                MC.getInstance(mContext).playSheet(sheetId);
                break;
            case R.id.it_actionbar_common_right:
                Bundle bundle2 = new Bundle();
                bundle2.putLong("sheetId", sheetId);
                Intent intent2 = new Intent(mContext, SongSheetEditActivity.class);
                intent2.putExtras(bundle2);
                mContext.startActivity(intent2);
                break;
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
            case PLAYSTATE_CHANGED:
            case service_ready:
            case Play:
            case Pause:
            case QUEUE_CHANGED:
                musicRecycleViewAdapter3.notifyDataSetChanged();
                break;
            case CURSOR_CHANGED:
                break;
        }
    }
}