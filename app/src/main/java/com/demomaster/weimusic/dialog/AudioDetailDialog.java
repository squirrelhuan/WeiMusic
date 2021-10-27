package com.demomaster.weimusic.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.activity.SongEditActivity;
import com.demomaster.weimusic.activity.SongSheetEditActivity;
import com.demomaster.weimusic.model.AudioInfo;
import com.demomaster.weimusic.model.AudioSheet;
import com.demomaster.weimusic.model.Menu;
import com.demomaster.weimusic.player.service.MC;
import com.demomaster.weimusic.player.service.MusicDataManager;
import com.demomaster.weimusic.ui.adapter.AudioInfoAdapter;
import com.demomaster.weimusic.ui.adapter.AudioMenuAdapter;
import com.demomaster.weimusic.ui.adapter.MusicRecycleViewAdapter;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.demomaster.huan.quickdeveloplibrary.helper.toast.QdToast;
import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.util.QDFileUtil;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.OnClickActionListener;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDDialog;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDDialog2;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDInputDialog;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDSheetDialog;

import static com.demomaster.weimusic.constant.AudioStation.QUEUE_CHANGED;

public class AudioDetailDialog extends QDDialog2 {
    ImageView iv_cover;
    ImageView iv_play;
    TextView tv_index;
    TextView tv_title;
    TextView tv_artist;
    TextView tv_delete;
    TextView tv_rename;
    TextView tv_add_sheet;
    TextView tv_edit;
    TextView tv_duration;
    TextView tv_file_path;
    TextView tv_file_size;
    RecyclerView recycler_menu;

    @Override
    public boolean isHasPadding() {
        return true;
    }

    public AudioDetailDialog(Context context, AudioInfo audioInfo, int selectIndex) {
        super(context);
        Window win = getWindow();
        //win.setWindowAnimations(R.style.keybored_anim);
        //win.setWindowAnimations(R.style.qd_dialog_animation_center_scale);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        View layout = getLayoutInflater().inflate(R.layout.dialog_audio_detail,null);
        setContentView(layout,layoutParams);
        tv_index = findViewById(R.id.tv_index);
        iv_cover = findViewById(R.id.iv_cover);
        iv_play = findViewById(R.id.iv_play);
        tv_title = findViewById(R.id.tv_title);
        tv_artist = findViewById(R.id.tv_artist);
        tv_delete = findViewById(R.id.tv_delete);
        tv_rename = findViewById(R.id.tv_rename);
        tv_add_sheet = findViewById(R.id.tv_add_sheet);
        tv_edit = findViewById(R.id.tv_edit);
        tv_duration = findViewById(R.id.tv_duration);
        tv_file_path = findViewById(R.id.tv_file_path);
        tv_file_size = findViewById(R.id.tv_file_size);
        recycler_menu = findViewById(R.id.recycler_menu);
        setCancelable(true);
        tv_file_path.setText("文件路径:"+audioInfo.getData());
        tv_index.setText(selectIndex+"");
        File file = new File(audioInfo.getData());
        tv_file_size.setText("文件大小:"+QDFileUtil.formatFileSize(file.length(),false));

        Bitmap bitmap = MusicDataManager.getInstance(getContext()).getAlbumPicture(getContext(), audioInfo);
        if (bitmap != null) {
            iv_cover.setImageBitmap(bitmap);
        }
        if(MC.getInstance(getContext()).isPlaying()){
            iv_play.setImageResource(MC.getInstance(getContext()).getCurrentAudioId()==audioInfo.getAudioId()?R.drawable.button_music_play01:R.drawable.button_music_play02);
        }else {
            iv_play.setImageResource(R.drawable.button_music_play02);
        }
        iv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MC.getInstance(getContext()).playAudio(audioInfo);
            }
        });

        //AudioMenuAdapter
        tv_artist.setText(audioInfo.getArtist());
        tv_title.setText(audioInfo.getTitle());
        tv_rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(audioInfo.getData());
                if (file.exists()) {
                    showRenameDialog(file);
                } else {
                    QdToast.show("文件不存在");
                }
            }
        });
        tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        tv_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SongEditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putLong("audioId", audioInfo.getAudioId());
                intent.putExtras(bundle);
                getContext().startActivity(intent);
            }
        });

        String[] strs = new String[]{"删除","重命名","收藏","加入歌单"};//,"分享"
        int[] ids = new int[]{R.drawable.ic_delete,R.drawable.ic_write,R.drawable.ic_like,R.drawable.ic_add_3,R.drawable.ic_share,R.drawable.ic_add_1};

        if (MC.getInstance(getContext()).isFavorite(audioInfo.getAudioId())) {
            ids[2] = R.drawable.ic_likefill;
        } else {
            ids[2] = R.drawable.ic_like;
        }
        List<Menu> menuList = new ArrayList<>();
        for(int i=0;i<strs.length;i++){
            Menu menu = new Menu();
            menu.setName(strs[i]);
            menu.setResourceId(ids[i]);
            menuList.add(menu);
        }
        AudioMenuAdapter adapter = new AudioMenuAdapter(getContext(),menuList);
        //这里使用线性布局像listview那样展示列表,第二个参数可以改为 HORIZONTAL实现水平展示
        // LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        //使用网格布局展示
        recycler_menu.setLayoutManager(new GridLayoutManager(getContext(), 4));
        //recy_drag.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        //设置分割线使用的divider
        //rv_songs.addItemDecoration(dividerItemDecoration);
        recycler_menu.setAdapter(adapter);
        adapter.setOnItemClickListener(new MusicRecycleViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                switch (position){
                    case 0:
                        showDeleteDialog(audioInfo);
                        break;
                    case 1:
                        File file = new File(audioInfo.getData());
                        if (file.exists()) {
                            showRenameDialog(file);
                        } else {
                            QdToast.show("文件不存在");
                        }
                        break;
                    case 2:
                        if (MC.getInstance(getContext()).isFavorite(audioInfo.getAudioId())) {
                            MusicDataManager.getInstance(getContext()).removeFavorite(getContext(), audioInfo.getAudioId());
                            ((ImageView)view.findViewById(R.id.iv_menu_icon)).setImageResource(R.drawable.ic_like);
                        } else {
                            MusicDataManager.getInstance(getContext()).addFavorite(getContext(), audioInfo.getAudioId());
                            ((ImageView)view.findViewById(R.id.iv_menu_icon)).setImageResource(R.drawable.ic_likefill);
                        }
                        break;
                    case 3:
                        selectSongSheetDialog(audioInfo);
                        break;
                    case 4:
                        //shareCurrentTrack(audioInfo);
                        break;
                }
            }

            @Override
            public void showContextMenu(View view, int position) {

            }
        });

    }


    List<AudioSheet> audioSheetList;
    private void selectSongSheetDialog(AudioInfo audioInfo) {
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
                            getContext().startActivity(new Intent(getContext(), SongSheetEditActivity.class));
                        }else {
                            MusicDataManager.getInstance(getContext()).addToSheet(getContext(), audioSheetList.get(position-1).getId(), audioInfo.getId());
                        }
                    }
                })
                .create()
                .show();
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
    QDDialog qdDialog;
    private void showDeleteDialog(AudioInfo audioInfo) {
        qdDialog = new QDDialog.Builder(getContext())
                .setMessage("确定要删除歌曲《"+ audioInfo.getTitle()+"-"+ audioInfo.getArtist()+"》吗？（同时删除文件）")
                .addAction("取消")
                .addAction("确定", new OnClickActionListener() {
                    @Override
                    public void onClick(Dialog dialog, View view, Object tag) {
                        dialog.dismiss();
                        AudioDetailDialog.this.dismiss();
                        QDFileUtil.delete(audioInfo.getData());
                        QdToast.show("删除成功");
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
