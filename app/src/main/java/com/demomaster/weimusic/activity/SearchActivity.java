package com.demomaster.weimusic.activity;


import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.model.AudioInfo;
import com.demomaster.weimusic.player.service.MusicDataManager;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.demomaster.huan.quickdeveloplibrary.base.activity.QDActivity;
import cn.demomaster.huan.quickdeveloplibrary.helper.toast.QdToast;
import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.util.QDFileUtil;
import cn.demomaster.qdlogger_library.QDLogger;
import cn.demomaster.quickpermission_library.PermissionHelper;

import static com.demomaster.weimusic.constant.AudioStation.QUEUE_CHANGED;

public class SearchActivity extends QDActivity {

    Button btn_scan;
    CheckBox cb_scan_repeat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        cb_scan_repeat = findViewById(R.id.cb_scan_repeat);
        btn_scan = findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchMusic();
            }
        });
    }

    private List<AudioInfo> mAudioInfoList = new ArrayList<>();
    public static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private void searchMusic() {

        boolean b = PermissionHelper.getInstance().getPermissionStatus(mContext, PERMISSIONS);
        if (b) {

            if(cb_scan_repeat.isChecked()){
                MusicDataManager.getInstance(mContext).loadData(mContext, 60000, new MusicDataManager.OnLoadDataListener() {
                    @Override
                    public void loadComplete(int ret, List<AudioInfo> audioInfoList) {
                        if (ret == 1) {
                            if (audioInfoList != null) {
                                Map<String,String> md5PathMap = new HashMap<>();
                                Map<String,String> repeatMap = new HashMap<>();
                                for(AudioInfo audioInfo:audioInfoList){
                                    String md5 = QDFileUtil.getFileUniqueKey(new File(audioInfo.data));
                                    // String md5 = QDFileUtil.getFileUniqueKey();
                                     if(md5PathMap.containsKey(md5)){
                                         md5PathMap.put(md5,md5PathMap.get(md5)+";"+audioInfo.data);
                                     }else {
                                         md5PathMap.put(md5, audioInfo.data);
                                     }
                                }
                                for(Map.Entry entry:md5PathMap.entrySet()){
                                    String value = (String) entry.getValue();
                                    if(value.contains(";")){
                                        QDLogger.i("重复文件："+value);
                                    }
                                }
                                /*QdToast.show("共搜索到：" + audioInfoList.size());
                                EventBus.getDefault().post(new EventMessage(QUEUE_CHANGED.value()));
                                //转化为文件
                                toFileList(audioInfoList);
                                mAudioInfoList.clear();
                                mAudioInfoList.addAll(audioInfoList);*/
                            }
                        }
                    }
                });
            }else {
                MusicDataManager.getInstance(mContext).loadData(mContext, 60000, new MusicDataManager.OnLoadDataListener() {
                    @Override
                    public void loadComplete(int ret, List<AudioInfo> audioInfoList) {
                        if (ret == 1) {
                            if (audioInfoList != null) {
                                QdToast.show("共搜索到：" + audioInfoList.size());
                                EventBus.getDefault().post(new EventMessage(QUEUE_CHANGED.value()));
                                //转化为文件
                                toFileList(audioInfoList);
                                mAudioInfoList.clear();
                                mAudioInfoList.addAll(audioInfoList);
                            }
                        }
                    }
                });
            }
        }
    }

    private void toFileList(List<AudioInfo> audioInfoList) {
        List<File> fileList = new ArrayList<>();
        for(AudioInfo audioInfo : audioInfoList){
           fileList.add(new File(audioInfo.getData()));
        }
        Map<String,List<File>> map = new LinkedHashMap<>();
        for(AudioInfo audioInfo : audioInfoList){
            File file = new File(audioInfo.getData());
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