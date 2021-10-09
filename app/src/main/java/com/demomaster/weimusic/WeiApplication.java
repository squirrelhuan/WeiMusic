package com.demomaster.weimusic;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.os.IBinder;
import android.text.TextUtils;

import com.demomaster.weimusic.model.AudioInfo;
import com.demomaster.weimusic.model.AudioSheet;
import com.demomaster.weimusic.player.helpers.MusicHelper;
import com.demomaster.weimusic.player.service.MC;
import com.demomaster.weimusic.player.service.MediaButtonIntentReceiver;

import cn.demomaster.huan.quickdeveloplibrary.QDApplication;
import cn.demomaster.huan.quickdeveloplibrary.constant.AppConfig;
import cn.demomaster.huan.quickdeveloplibrary.util.CrashHandler;
import cn.demomaster.qdlogger_library.QDLogger;
import cn.demomaster.quickdatabaselibrary.QuickDbHelper;

/**
 * Created by huan on 2018/1/27.
 */
public class WeiApplication extends QDApplication implements
        ServiceConnection {
   // public ServiceToken mToken;

    @Override
    public void onCreate() {
        QDLogger.setFouceUseExternalStorage(true);
        //处理异常
        CrashHandler.getInstance().init(this, null);
        CrashHandler.getInstance().setCrashDealType(CrashHandler.CrashDealType.showError);
        super.onCreate();

        //生成表
        creatTable();
        updatDb();
        registerReceiver();
        QDLogger.d("WeiApplication onCreate");
        MC.getInstance(this).init();
        MusicHelper.getInstance().bindToService(this);
    }
    public QuickDbHelper getDbHelper() {
        if (dbHelper == null) {
            String dbpath = null;
            if (AppConfig.getInstance().getConfigMap().containsKey("dbpath")) {
                dbpath = (String) AppConfig.getInstance().getConfigMap().get("dbpath");
            }

            if (!TextUtils.isEmpty(dbpath)) {
                dbHelper = new QuickDbHelper(this, dbpath, null, 11, this);
                // dbHelper = new QuickDb(this, dbpath, null, 7, this);
            }
        }
        return dbHelper;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
    }


    private void creatTable() {
        QDLogger.i("创建表");
        try {
            getDbHelper().createTable(AudioInfo.class);
            getDbHelper().createTable(AudioSheet.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void updatDb() {
        try {
            dbHelper.updateTable(AudioInfo.class);
            dbHelper.updateTable(AudioSheet.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BroadcastReceiver audioBroadcastReceiver;
    private void registerReceiver() {
        if(audioBroadcastReceiver==null) {
            audioBroadcastReceiver = new MediaButtonIntentReceiver();
            registerReceiver(audioBroadcastReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
            registerReceiver(audioBroadcastReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
            registerReceiver(audioBroadcastReceiver, new IntentFilter(Intent.ACTION_MEDIA_BUTTON));
        }
    }
/*
    @Override
    public String getBugglyAppID() {
        return "03ca5cf7e4";
    }*/

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
       // MusicHelper.getInstance().mService = IApolloService.Stub.asInterface(service);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
       // MusicHelper.getInstance().mService = null;
    }
}
