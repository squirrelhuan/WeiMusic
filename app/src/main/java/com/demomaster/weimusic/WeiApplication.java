package com.demomaster.weimusic;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.IBinder;
import android.util.Log;

import com.demomaster.weimusic.player.helpers.MusicHelper;
import com.demomaster.weimusic.player.service.MC;
import com.demomaster.weimusic.player.service.MediaButtonIntentReceiver;

import cn.demomaster.huan.quickdeveloplibrary.QDApplication;
import cn.demomaster.huan.quickdeveloplibrary.util.CrashHandler;
import cn.demomaster.qdlogger_library.QDLogger;

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
        registerReceiver();
        QDLogger.d("WeiApplication onCreate");
        MC.getInstance(this).init();
        MusicHelper.getInstance().bindToService(this);
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
