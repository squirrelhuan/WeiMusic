package com.demomaster.weimusic.dialog;

import android.app.Dialog;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.viewpager.widget.ViewPager;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.model.AudioInfo;
import com.demomaster.weimusic.player.service.MusicDataManager;
import com.demomaster.weimusic.ui.adapter.AudioInfoAdapter;
import com.demomaster.weimusic.ui.adapter.MusicRecycleViewAdapter;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.util.QDFileUtil;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.OnClickActionListener;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDDialog;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDDialog2;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDInputDialog;

import static com.demomaster.weimusic.constant.AudioStation.QUEUE_CHANGED;

public class AudioInfoDialog extends QDDialog2 {
    private ViewPager viewPager2;
    private int selectIndex;
    public AudioInfoDialog(Context context, AudioInfo audioInfo,int selectIndex) {
        super(context);
        this.selectIndex = selectIndex;
        Window win = getWindow();
        //win.setWindowAnimations(R.style.keybored_anim);
        //win.setWindowAnimations(R.style.qd_dialog_animation_center_scale);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        View layout = getLayoutInflater().inflate(R.layout.dialog_music_info,null);
        //setContentView(R.layout.dialog_music_item);
        setContentView(layout,layoutParams);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        viewPager2 = findViewById(R.id.viewpager_audioinfo);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.setPageTransformer(true,new ScaleTransformer());
        AudioInfoAdapter adater2 = new AudioInfoAdapter(context, MusicDataManager.getInstance(getContext()).getLocalSheet());
        viewPager2.setAdapter(adater2);
        viewPager2.setCurrentItem(selectIndex);
        setCancelable(true);
        adater2.setOnItemClickListener(new MusicRecycleViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                dismiss();
            }

            @Override
            public void showContextMenu(View view, int position) {

            }
        });
        //setBackgroundColor(context.getResources().getColor(R.color.transparent_light_33));
        //setAnimationStyleID(R.style.qd_dialog_animation_center_scale);
        /*ImageView imageView = findViewById(R.id.iv_cover);
        Bitmap bitmap = MusicDataManager.getInstance().getAlbumPicture(getContext(), audioInfo, true);
        if(bitmap!=null) {
            imageView.setImageBitmap(bitmap);
        }
        
        ImageView iv_play = findViewById(R.id.iv_play);
        iv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MC.getInstance(getContext()).playAudio(audioInfo);
            }
        });
        TextView artist = findViewById(R.id.artist);
        artist.setText(audioInfo.getArtist());
        TextView title = findViewById(R.id.title);
        title.setText(audioInfo.getTitle());
        TextView tv_rename = findViewById(R.id.tv_rename);
        tv_rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                File file = new File(audioInfo.getData());
                if(file.exists()) {
                    //showRenameDialog(file);
                }else {
                    QdToast.show("文件不存在");
                }
            }
        });
        TextView tv_add_sheet = findViewById(R.id.tv_add_sheet);
        tv_add_sheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                //showMenuDialog(musicList.get(position));
            }
        });
        TextView tv_delete = findViewById(R.id.tv_delete);
        tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                //showDeleteDialog(musicList.get(position));
            }
        });
        TextView tv_edit = findViewById(R.id.tv_edit);
        tv_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                Intent intent = new Intent(getContext(), SongEditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putLong("audioId",audioInfo.getAudioId());
                intent.putExtras(bundle);
                getContext().startActivity(intent);
            }
        });*/
    }

    @Override
    public boolean isCanSliding() {
        return false;
    }
    private MusicRecycleViewAdapter.OnItemClickListener onItemClickListener;
    public void setOnItemClickListener(MusicRecycleViewAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public static interface OnItemClickListener {
        void onItemClick(View view, int position);
        void showContextMenu(View view, int position);
    }
    private void showRenameDialog(File file) {
        new QDInputDialog.Builder(getContext()).setTitle("重命名")
                .setMessage(file.getName())
                .setHint("请输入名称")
                .setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD)
                .addAction("修改", new OnClickActionListener() {
                    @Override
                    public void onClick(Dialog dialog, View view, Object tag) {
                        dialog.dismiss();
                        String oldPath = file.getPath();
                        String newPath = file.getParentFile().getPath()+File.separator+tag;
                        QDFileUtil.renameFile(oldPath, newPath);
                        if(file.exists()) {
                            file.delete();
                        }
                        File file1 = new File(newPath);
                        if(file1.exists()) {
                            QDFileUtil.updateMediaFile(getContext(), file1.getAbsolutePath(), new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    EventBus.getDefault().post(new EventMessage(QUEUE_CHANGED.value()));
                                }
                            });
                        }
                    }
                }).addAction("取消")
                .setGravity_foot(Gravity.RIGHT)
                .create().show();
    }

    private void showDeleteDialog(AudioInfo audioInfo) {
        QDDialog qdDialog = new QDDialog.Builder(getContext())
                .setMessage("确定要删除歌曲《"+ audioInfo.getTitle()+"-"+ audioInfo.getArtist()+"》吗？（同时删除文件）")
                .addAction("取消")
                .addAction("确定", new OnClickActionListener() {
                    @Override
                    public void onClick(Dialog dialog, View view, Object tag) {
                        dialog.dismiss();
                        QDFileUtil.delete(audioInfo.getData());
                        QDFileUtil.updateMediaFile(getContext(), audioInfo.getData(), new MediaScannerConnection.OnScanCompletedListener() {
                            @Override
                            public void onScanCompleted(String path, Uri uri) {
                                EventBus.getDefault().post(new EventMessage(QUEUE_CHANGED.value()));
                            }
                        });
                    }
                })
                .create();
        qdDialog.show();
    }

    public static class ScaleTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.55f;
        private static final float MIN_ALPHA = 0.3f;

        @Override
        public void transformPage(View page, float position) {
                float progress =0;
                if (position < -1 ) {
                    //progress = position+2;
                    progress = 0;
                }else if(position<0){
                    progress = 1+position;
                }else if(position<1){//当有View1左滑到View2时，由transformPage函数的日志获得以下数据(注意顺序)：view2的posion由1 -> 0;view1的posion由0 -> -1;
                    progress = 1-position;
                }else if(position<2){
                    //progress = position-1;
                    progress = 0;
                }
                page.setAlpha(progress*(1-MIN_SCALE)+MIN_SCALE);
                page.setScaleX(progress*(1-MIN_SCALE)+MIN_SCALE);
                page.setScaleY(progress*(1-MIN_SCALE)+MIN_SCALE);
        }
    }

}
