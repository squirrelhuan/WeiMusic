package com.demomaster.weimusic.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.model.MusicInfo;
import com.demomaster.weimusic.player.service.MusicDataManager;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.demomaster.huan.quickdeveloplibrary.helper.toast.QdToast;
import cn.demomaster.qdlogger_library.QDLogger;
import cn.demomaster.quickpermission_library.PermissionHelper;

public class SearchActivity extends BaseActivity {

    Button btn_scan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        btn_scan = findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchMusic();
            }
        });
    }

    private List<MusicInfo> mMusicInfoList = new ArrayList<>();
    private void searchMusic() {
        boolean b = PermissionHelper.getInstance().getPermissionStatus(mContext, PERMISSIONS);
        if (b) {
            MusicDataManager.getInstance().loadData(mContext,60000, new MusicDataManager.OnLoadDataListener() {
                @Override
                public void loadComplete(int ret, List<MusicInfo> musicInfoList) {
                    if (ret == 1) {
                        if (musicInfoList != null) {
                            QdToast.show("共搜索到："+musicInfoList.size());
                            //转化为文件
                            toFileList(musicInfoList);
                            mMusicInfoList.clear();
                            mMusicInfoList.addAll(musicInfoList);
                        }
                    }
                }
            });
        }
    }

    private void toFileList(List<MusicInfo> musicInfoList) {
        List<File> fileList = new ArrayList<>();
        for(MusicInfo musicInfo : musicInfoList){
           fileList.add(new File(musicInfo.getPath()));
        }
        Map<String,List<File>> map = new LinkedHashMap<>();
        for(MusicInfo musicInfo : musicInfoList){
            File file = new File(musicInfo.getPath());
            String path = file.getParentFile().getAbsolutePath();
            List<File> files;
            if(map.containsKey(path)){
                files = map.get(path);
            }else {
                files = new ArrayList<>();
            }
            files.add(file);
            map.put(path,files);
        }

        for(Map.Entry entry:map.entrySet()){
            QDLogger.e("path="+entry.getKey());
        }
    }
}