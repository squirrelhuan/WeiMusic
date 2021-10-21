package com.demomaster.weimusic.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.demomaster.weimusic.R;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.model.AudioSheet;
import com.demomaster.weimusic.player.service.MusicDataManager;
import com.demomaster.weimusic.ui.adapter.RecyclerSheetAdapter;
import com.demomaster.weimusic.ui.adapter.RecyclerSheetAdapter2;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.demomaster.huan.quickdeveloplibrary.base.activity.QDActivity;
import cn.demomaster.huan.quickdeveloplibrary.helper.PhotoHelper;
import cn.demomaster.huan.quickdeveloplibrary.helper.simplepicture.model.Image;
import cn.demomaster.huan.quickdeveloplibrary.helper.simplepicture.model.UrlType;
import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.util.QDBitmapUtil;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDSheetDialog;
import cn.demomaster.qdlogger_library.QDLogger;

/**
 * 歌单列表页面
 */
public class SongSheetListActivity extends QDActivity implements View.OnClickListener {

    Button btn_import;
    RecyclerView recyclerView_song_sheet;
    public RecyclerSheetAdapter2 recyclerSheetAdapter;
    private List<AudioSheet> audioSheets = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_sheet_list);
        //getActionBarTool().setActionBarType(ACTIONBAR_TYPE.NO_ACTION_BAR_NO_STATUS);
        //getActionBarTool().setHeaderBackgroundColor(Color.TRANSPARENT);
        setTitle("自建歌单");

        recyclerView_song_sheet = findViewById(R.id.recyclerView_song_sheet);

        recyclerView_song_sheet.setLayoutManager(new LinearLayoutManager(mContext));
        audioSheets = new ArrayList<>();
        audioSheets.addAll(MusicDataManager.getInstance(mContext).getSongSheet(mContext));
        recyclerSheetAdapter = new RecyclerSheetAdapter2(mContext,audioSheets);
        recyclerSheetAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(id==R.id.iv_menu) {
                    showMenuDialog();
                }else {
                    Bundle bundle = new Bundle();
                    bundle.putLong("sheetId", audioSheets.get(position).getId());
                    Intent intent = new Intent(mContext, SongSheetDetailActivity.class);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });
        recyclerView_song_sheet.setAdapter(recyclerSheetAdapter);
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
       /* EventBus.getDefault().post(new EventMessage(AudioStation.sheet_changed.value()));//准备播放
        AudioSheet audioSheet = new AudioSheet();
        audioSheet.setName(et_sheet_name.getText().toString());
        audioSheet.setThemeColor(mColor);
        audioSheet.setId(sheetId);
        if (image != null) {
            audioSheet.setImgSrc(image.getPath());
        }
        if (sheetId == -1) {
            MusicDataManager.getInstance(mContext).createSheet(mContext, audioSheet);
        } else {
            MusicDataManager.getInstance(mContext).modifySheet(mContext, audioSheet);
        }
        finish();*/
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
            }
        }
        if (imgObj != null) {
            Glide.with(mContext).asBitmap()
                    .load(imgObj)
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull @NotNull Bitmap resource, Transition<? super Bitmap> transition) {
                            if (resource != null) {
                               // iv_sheet_img.setImageBitmap(resource);
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
        }
    }
}