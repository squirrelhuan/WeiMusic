package com.demomaster.weimusic.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.demomaster.weimusic.R;
import com.demomaster.weimusic.model.AudioInfo;
import com.demomaster.weimusic.player.service.MusicDataManager;

import org.greenrobot.eventbus.EventBus;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.images.Artwork;
import org.jaudiotagger.tag.images.ArtworkFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.demomaster.huan.quickdeveloplibrary.base.activity.QDActivity;
import cn.demomaster.huan.quickdeveloplibrary.helper.PhotoHelper;
import cn.demomaster.huan.quickdeveloplibrary.helper.simplepicture.model.Image;
import cn.demomaster.huan.quickdeveloplibrary.helper.simplepicture.model.UrlType;
import cn.demomaster.huan.quickdeveloplibrary.helper.toast.QdToast;
import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.util.QDBitmapUtil;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDSheetDialog;
import cn.demomaster.qdlogger_library.QDLogger;

import static com.demomaster.weimusic.constant.AudioStation.QUEUE_CHANGED;

/**
 * 歌曲信息编辑
 */
public class SongEditActivity extends QDActivity {

    Button btn_creat;
    TextView et_sheet_name;
    long audioId = -1;
    ImageView iv_sheet_img;

    AudioInfo audioInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_song);
        setTitle("封面编辑");
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
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.containsKey("audioId")) {
                audioId = bundle.getLong("audioId");
                btn_creat.setText("保存修改");
                QDLogger.e("audioId=" + audioId);
                audioInfo = MusicDataManager.getInstance(mContext).getMusicInfoById(mContext, audioId);
                if (audioInfo != null) {
                    et_sheet_name.setText(audioInfo.getTitle());
                    Bitmap bitmap = MusicDataManager.getInstance(mContext).getAlbumPicture(mContext, audioInfo);
                    iv_sheet_img.setImageDrawable(new BitmapDrawable(bitmap));
                    //Glide.with(mContext).load(audioInfo.getPath()).into(iv_sheet_img);
                    //iv_theme_color.setBackgroundColor(audioSheet.getThemeColor());
                    //mColor = audioSheet.getThemeColor();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            if (bundle.containsKey("PicUrl")) {
                String url = bundle.getString("PicUrl");
                //image = new Image(url,UrlType.url);
                //Glide.with(mContext).load(image.getPath()).into(iv_sheet_img);
                Glide.with(mContext).asBitmap()
                        .load(url)
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull @NotNull Bitmap resource, Transition<? super Bitmap> transition) {
                                if (resource != null) {
                                    iv_sheet_img.setImageBitmap(resource);
                                    String filePath = QDBitmapUtil.savePhoto(resource, mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath(), "header");//String.valueOf(System.currentTimeMillis())
                                    image = new Image(filePath, UrlType.file);
                                }
                            }
                        });


            }
        }
    }

    private void creatSheet() {
        if (image != null) {
            //EventBus.getDefault().post(new EventMessage(AudioStation.sheet_changed.value()));//准备播放
            AudioInfo audioInfo = MusicDataManager.getInstance(mContext).getMusicInfoById(mContext, audioId);
            try {
                MP3File mp3File = new MP3File(audioInfo.data);
                QDLogger.i("hasID3v2Tag=" + mp3File.hasID3v2Tag());
                if (mp3File.hasID3v2Tag()) {
                    QDLogger.i("ID3v2Tag=" + mp3File.getID3v2Tag());
                    //QDLogger.i("getAudioHeader="+mp3File.getAudioHeader());
                    QDLogger.i("getPath=" + audioInfo.data);
                    AbstractID3v2Tag iD3v2Tag = mp3File.getID3v2Tag();
                    File picFile = new File(image.getPath());
                    Iterator<TagField> tagFieldIterator = iD3v2Tag.getFields();
                    List<String> ids = new ArrayList<>();
                    while (tagFieldIterator.hasNext()) {
                        TagField tagField = (TagField) tagFieldIterator.next();
                        if (tagField.toString().startsWith("MIMEType=")) {
                            ids.add(tagField.getId());
                        }
                        //QDLogger.i("hasNext="+tagField.getId()+",string="+tagField.toString());
                    }
                    for (String id : ids) {
                        iD3v2Tag.removeFrame(id);
                    }
                    Artwork artWork = ArtworkFactory.createArtworkFromFile(picFile);
                    iD3v2Tag.setField(artWork);
                    //mp3File.setID3v2Tag(iD3v2Tag);
                    mp3File.save();
                    QdToast.show("修改成功");
                    EventBus.getDefault().post(new EventMessage(QUEUE_CHANGED.value()));
                    finish();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TagException e) {
                e.printStackTrace();
            } catch (ReadOnlyFileException e) {
                e.printStackTrace();
            } catch (CannotReadException e) {
                e.printStackTrace();
            } catch (InvalidAudioFrameException e) {
                e.printStackTrace();
            }
        } else {
            QdToast.show("请选择图片！");
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
                } else if (position == 1) {//从相册选择
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
                } else if (position == 2) {
                    Intent intent = new Intent(mContext, SelectNetPictureActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("audioName", audioInfo.getTitle());
                    intent.putExtras(bundle);
                    startActivityForResult(intent, 123);
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