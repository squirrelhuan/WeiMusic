package com.demomaster.weimusic.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.demomaster.weimusic.R;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.model.AudioSheet;
import com.demomaster.weimusic.player.service.MusicDataManager;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.demomaster.huan.quickdeveloplibrary.base.activity.QDActivity;
import cn.demomaster.huan.quickdeveloplibrary.constant.FilePath;
import cn.demomaster.huan.quickdeveloplibrary.helper.PhotoHelper;
import cn.demomaster.huan.quickdeveloplibrary.helper.simplepicture.model.Image;
import cn.demomaster.huan.quickdeveloplibrary.helper.simplepicture.model.UrlType;
import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.util.QDBitmapUtil;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDSheetDialog;
import cn.demomaster.qdlogger_library.QDLogger;
import cn.demomaster.qdrouter_library.base.activity.QuickActivity;

public class AddSongSheetActivity extends QDActivity {

    Button btn_creat;
    TextView et_sheet_name;
    long sheetId = -1;
    ImageView iv_sheet_img;
    LinearLayout ll_theme_color;
    ImageView iv_theme_color;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_song_sheet);

        iv_theme_color = findViewById(R.id.iv_theme_color);
        ll_theme_color = findViewById(R.id.ll_theme_color);
        ll_theme_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext,SelectThemeColorActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("themeColor",mColor);
                intent.putExtras(bundle);
                startActivityForResult(intent,123);
            }
        });
        iv_sheet_img = findViewById(R.id.iv_sheet_img);
        iv_sheet_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuDialog();
            }
        });
        et_sheet_name = findViewById(R.id.et_sheet_name);
        btn_creat = findViewById(R.id.btn_creat);
        btn_creat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                creatSheet();
            }
        });
        Intent intent = getIntent();
        if(intent!=null) {
            Bundle bundle = intent.getExtras();
            if(bundle!=null&&bundle.containsKey("sheetId")){
                sheetId = bundle.getLong("sheetId");
                btn_creat.setText("保存修改");
                QDLogger.e("sheetId="+sheetId);
                AudioSheet audioSheet = MusicDataManager.getInstance(mContext).getSongSheetById(mContext,sheetId);
                if(audioSheet !=null){
                    et_sheet_name.setText(audioSheet.getName());
                    Glide.with(mContext).load(audioSheet.getImgSrc()).into(iv_sheet_img);
                    iv_theme_color.setBackgroundColor(audioSheet.getThemeColor());
                    mColor = audioSheet.getThemeColor();
                }
            }
        }
    }

    int mColor;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            Bundle bundle = data.getExtras();
            mColor = bundle.getInt("themeColor",mColor);
            iv_theme_color.setBackgroundColor(mColor);
        }
    }

    private void creatSheet() {
        EventBus.getDefault().post(new EventMessage(AudioStation.sheet_changed.value()));//准备播放
        AudioSheet audioSheet = new AudioSheet();
        audioSheet.setName(et_sheet_name.getText().toString());
        audioSheet.setThemeColor(mColor);
        audioSheet.setId(sheetId);
        if(image!=null) {
            audioSheet.setImgSrc(image.getPath());
        }
        if(sheetId==-1) {
            MusicDataManager.getInstance(mContext).createSheet(mContext,audioSheet);
            finish();
        }else {
            MusicDataManager.getInstance(mContext).modifySheet(mContext,audioSheet);
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
                                    String filePath = QDBitmapUtil.savePhoto(bitmap, FilePath.APP_PATH_PICTURE, "header");//String.valueOf(System.currentTimeMillis())
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
                            if(images!=null&&images.size()>0) {
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
        if (image.getUrlType() == UrlType.url) {
            Glide.with(mContext).load(image.getPath()).into(iv_sheet_img);
        } else if (image.getUrlType() == UrlType.file) {
            Glide.with(mContext).load(new File(image.getPath())).into(iv_sheet_img);
        }
    }
}