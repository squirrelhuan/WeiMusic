package com.demomaster.weimusic.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.webkit.ValueCallback;
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

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.demomaster.weimusic.R;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.model.AudioInfo;
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
import cn.demomaster.huan.quickdeveloplibrary.util.QDFileUtil;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDSheetDialog;
import cn.demomaster.qdlogger_library.QDLogger;

import static com.demomaster.weimusic.constant.Constants.APP_PATH_SHEET;

/**
 * 歌单列表页面
 */
public class SongSheetListActivity extends QDActivity implements View.OnClickListener {

    Button btn_import_sheet,btn_creat_sheet,btn_save_sheet;
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
        btn_creat_sheet = findViewById(R.id.btn_creat_sheet);
        btn_creat_sheet.setOnClickListener(this);
        btn_import_sheet = findViewById(R.id.btn_import_sheet);
        btn_import_sheet.setOnClickListener(this);

        btn_save_sheet = findViewById(R.id.btn_save_sheet);
        btn_save_sheet.setOnClickListener(this);

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

    @Override
    protected void onResume() {
        super.onResume();
        audioSheets.clear();
        audioSheets.addAll(MusicDataManager.getInstance(mContext).getSongSheet(mContext));
    }

    int mColor;
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

    private ValueCallback<Uri> uploadFile;
    private ValueCallback<Uri[]> uploadFiles;
    private void openFileChooseProcess() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        //i.setType("*/*");
        i.setType("*/sheet");
        startActivityForResult(Intent.createChooser(i, "上传文件"), 0);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                if (null != uploadFile) {
                    Uri result = data == null ? null
                            : data.getData();
                    uploadFile.onReceiveValue(result);
                    uploadFile = null;
                }
                if (null != uploadFiles) {
                    Uri result = data == null ? null
                            : data.getData();
                    uploadFiles.onReceiveValue(new Uri[]{result});
                    uploadFiles = null;
                }
            } else if (resultCode == RESULT_CANCELED) {
                if (null != uploadFile) {
                    uploadFile.onReceiveValue(null);
                    uploadFile = null;
                }
            }
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
            case R.id.btn_creat_sheet:
                mContext.startActivity(new Intent(mContext, SongSheetEditActivity.class));
                break;
            case R.id.btn_import_sheet:
                openFileChooseProcess();
                break;
            case R.id.btn_save_sheet:
                backSheet();
                //QDFileUtil.writeFileSdcardFile(new File(APP_PATH_SHEET+"/"+System.currentTimeMillis()+".sheet"), JSON.toJSONString(audioSheets),false);
                break;
        }
    }

    private void backSheet() {
        for(AudioSheet audioSheet : audioSheets) {
            List<AudioInfo> audioInfoList = MusicDataManager.getInstance(mContext).getSongSheetListById(mContext, audioSheet.getId());
            audioSheet.setAudioInfoList(audioInfoList);
            QDFileUtil.writeFileSdcardFile(new File(APP_PATH_SHEET+"/"+audioSheet.getName()+"-"+audioSheet.getId()+".sheet"), JSON.toJSONString(audioSheet),false);
        }
    }
}