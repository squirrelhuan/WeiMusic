package com.demomaster.weimusic.ui.adapter;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.activity.AddSongSheetActivity;
import com.demomaster.weimusic.activity.SongEditActivity;
import com.demomaster.weimusic.model.AudioInfo;
import com.demomaster.weimusic.model.AudioSheet;
import com.demomaster.weimusic.model.Menu;
import com.demomaster.weimusic.player.service.MC;
import com.demomaster.weimusic.player.service.MusicDataManager;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.demomaster.huan.quickdeveloplibrary.helper.toast.QdToast;
import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.util.QDFileUtil;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.OnClickActionListener;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDDialog;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDInputDialog;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDSheetDialog;

import static com.demomaster.weimusic.constant.AudioStation.QUEUE_CHANGED;

public class AudioInfoAdapter extends PagerAdapter {

    private Context mContext;
    private List<AudioInfo> data = new ArrayList<AudioInfo>();
    public AudioInfoAdapter(Context context, List<AudioInfo> list) {
        this.mContext = context;
        this.data = list;
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_adapter_audio, null);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.tv_index.setText(""+(position+1));
        AudioInfo audioInfo = data.get(position);
        Bitmap bitmap = MusicDataManager.getInstance(mContext).getAlbumPicture(mContext, audioInfo);
        if (bitmap != null) {
            viewHolder.iv_cover.setImageBitmap(bitmap);
        }
        if(MC.getInstance(mContext).isPlaying()){
            viewHolder.iv_play.setImageResource(MC.getInstance(mContext).getCurrentAudioId()==audioInfo.getAudioId()?R.drawable.button_music_play01:R.drawable.button_music_play02);
        }else {
            viewHolder.iv_play.setImageResource(R.drawable.button_music_play02);
        }
        viewHolder.iv_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MC.getInstance(mContext).playAudio(audioInfo);
            }
        });

        //AudioMenuAdapter
        viewHolder.tv_artist.setText(audioInfo.getArtist());
        viewHolder.tv_title.setText(audioInfo.getTitle());
        viewHolder.tv_rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(audioInfo.getData());
                if (file.exists()) {
                    //showRenameDialog(file);
                } else {
                    QdToast.show("文件不存在");
                }
            }
        });
        /*viewHolder.tv_add_sheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });*/
        viewHolder.tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        viewHolder.tv_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SongEditActivity.class);
                Bundle bundle = new Bundle();
                bundle.putLong("audioId", audioInfo.getAudioId());
                intent.putExtras(bundle);
                mContext.startActivity(intent);
            }
        });

        String[] strs = new String[]{"删除","重命名","收藏","加入歌单"};//,"分享"
        int[] ids = new int[]{R.drawable.ic_delete,R.drawable.ic_write,R.drawable.ic_like,R.drawable.ic_add_3,R.drawable.ic_share,R.drawable.ic_add_1};

        if (MC.getInstance(mContext).isFavorite(audioInfo.getAudioId())) {
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
        AudioMenuAdapter adapter = new AudioMenuAdapter(mContext,menuList);
        //这里使用线性布局像listview那样展示列表,第二个参数可以改为 HORIZONTAL实现水平展示
       // LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false);
        //使用网格布局展示
        viewHolder.recycler_menu.setLayoutManager(new GridLayoutManager(mContext, 4));
        //recy_drag.setLayoutManager(linearLayoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL);
        //设置分割线使用的divider
        //rv_songs.addItemDecoration(dividerItemDecoration);
        viewHolder.recycler_menu.setAdapter(adapter);
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
                        if (MC.getInstance(mContext).isFavorite(audioInfo.getAudioId())) {
                            MusicDataManager.getInstance(mContext).removeFavorite(mContext, audioInfo.getAudioId());
                            ((ImageView)view.findViewById(R.id.iv_menu_icon)).setImageResource(R.drawable.ic_likefill);
                        } else {
                            MusicDataManager.getInstance(mContext).addFavorite(mContext, audioInfo.getAudioId());
                            ((ImageView)view.findViewById(R.id.iv_menu_icon)).setImageResource(R.drawable.ic_like);
                        }
                        break;
                    case 3:
                        showMenuDialog(audioInfo);
                        break;
                    case 4:
                        shareCurrentTrack(audioInfo);
                        break;
                }
            }

            @Override
            public void showContextMenu(View view, int position) {

            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, position);
                }
            }
        });
        // viewHolder.tv_valuestr.setTextColor(mContext.getResources().getColor((!qiGuanModels.get(position).getValueStr().contains("-")) ? R.color.jps_green_01 : R.color.jps_red_01));
        //AnimationUtil.addScaleAnimition(view,null);
        container.addView(view);
        return view;
    }
    /**
     * @return Share intent
     * @throws RemoteException
     * @param audioInfo
     */
    private String shareCurrentTrack(AudioInfo audioInfo) {
        Intent shareIntent = new Intent();
        String currentTrackMessage = null;
        if (audioInfo != null) {
            currentTrackMessage = mContext.getResources().getString(R.string.now_listening_to) + " "
                    + MC.getInstance(mContext).getCurrentInfo().getTitle() + " " + mContext.getResources().getString(R.string.by) + " "
                    + MC.getInstance(mContext).getCurrentInfo().getArtist();

            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, currentTrackMessage);
            mContext.startActivity(Intent.createChooser(shareIntent,
                    mContext.getResources().getString(R.string.share_track_using)));
        }
        return currentTrackMessage;
    }
    private void showDeleteDialog(AudioInfo audioInfo) {
        QDDialog qdDialog = new QDDialog.Builder(mContext)
                .setMessage("确定要删除歌曲《"+ audioInfo.getTitle()+"-"+ audioInfo.getArtist()+"》吗？（同时删除文件）")
                .addAction("取消")
                .addAction("确定", new OnClickActionListener() {
                    @Override
                    public void onClick(Dialog dialog, View view, Object tag) {
                        dialog.dismiss();
                        QDFileUtil.delete(audioInfo.getData());
                        QDFileUtil.updateMediaFile(mContext, audioInfo.getData(), new MediaScannerConnection.OnScanCompletedListener() {
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

    private MusicRecycleViewAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(MusicRecycleViewAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public static interface OnItemClickListener {
        void onItemClick(View view, int position);

        void showContextMenu(View view, int position);
    }

    private void updata(List<AudioInfo> musicList, MusicRecycleViewAdapter2 adapter, long sheetId) {
        List<AudioInfo> audioInfoList = MusicDataManager.getInstance(mContext).getSongSheetListById(mContext, sheetId);
        //QDLogger.i("musicInfoList:" + musicInfoList.size());
        musicList.clear();
        if (audioInfoList != null && audioInfoList.size() > 0) {
            musicList.addAll(audioInfoList);
        }
        adapter.notifyDataSetChanged();
    }

    private void playMusic(List<AudioInfo> musicList, MusicRecycleViewAdapter2 adapter, int i) {
        if (i < musicList.size()) {
            MC.getInstance(mContext).playAudio(musicList.get(i));
        }
        adapter.notifyDataSetChanged();
    }

    List<AudioSheet> audioSheetList;
    private void showMenuDialog(AudioInfo audioInfo) {
        String[] menus = {"创建歌单"};
        audioSheetList = MusicDataManager.getInstance(mContext).getSongSheet(mContext);
        if(audioSheetList != null){
            menus=new String[audioSheetList.size()+1];
            menus[0] = "创建歌单";
            for (int i = 0; i< audioSheetList.size(); i++){
                AudioSheet collection = audioSheetList.get(i);
                menus[i+1] = collection.getName();
            }
        }
        new QDSheetDialog.MenuBuilder(mContext)
                .setData(menus)
                .setOnDialogActionListener(new QDSheetDialog.OnDialogActionListener() {
                    @Override
                    public void onItemClick(QDSheetDialog dialog, int position, List<String> data) {
                        dialog.dismiss();
                        if (position == 0) {
                            mContext.startActivity(new Intent(mContext, AddSongSheetActivity.class));
                        }else {
                            MusicDataManager.getInstance(mContext).addToSheet(mContext, audioSheetList.get(position-1).getId(), audioInfo.getId());
                        }
                    }
                })
                .create()
                .show();
    }

    private void showRenameDialog(File file) {
        new QDInputDialog.Builder(mContext).setTitle("重命名")
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
                            QDFileUtil.updateMediaFile(mContext, file1.getAbsolutePath(), new MediaScannerConnection.OnScanCompletedListener() {
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

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    class ViewHolder {
        ImageView iv_cover;
        ImageView iv_play;
        TextView tv_index;
        TextView tv_title;
        TextView tv_artist;
        TextView tv_delete;
        TextView tv_rename;
        TextView tv_add_sheet;
        TextView tv_edit;
        RecyclerView recycler_menu;

        public ViewHolder(View convertView) {
            tv_index = convertView.findViewById(R.id.tv_index);
            iv_cover = convertView.findViewById(R.id.iv_cover);
            iv_play = convertView.findViewById(R.id.iv_play);
            tv_title = convertView.findViewById(R.id.tv_title);
            tv_artist = convertView.findViewById(R.id.tv_artist);
            tv_delete = convertView.findViewById(R.id.tv_delete);
            tv_rename = convertView.findViewById(R.id.tv_rename);
            tv_add_sheet = convertView.findViewById(R.id.tv_add_sheet);
            tv_edit = convertView.findViewById(R.id.tv_edit);
            recycler_menu = convertView.findViewById(R.id.recycler_menu);
        }
    }
}