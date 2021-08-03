
package com.demomaster.weimusic.player.helpers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;

import com.demomaster.weimusic.IMusicService;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.player.service.MusicService;

import org.greenrobot.eventbus.EventBus;

import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;


/**
 * Various methods used to help with specific music statements
 */
public class MusicHelper implements
        ServiceConnection {
    IMusicService mService;
    private static MusicHelper instance;
    public static MusicHelper getInstance() {
        if (instance == null) {
            instance = new MusicHelper();
        }
        return instance;
    }

    private MusicHelper() {
    }

    Context mContext;
    public boolean bindToService() {
        return bindToService(mContext);
    }

    /**
     * 绑定service
     * @param context
     * @return
     */
    public boolean bindToService(Context context) {//, ServiceConnection callback
        mContext = context.getApplicationContext();
        Intent intent = new Intent(mContext, MusicService.class);
        mContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContext.startForegroundService(new Intent(mContext, MusicService.class));
        } else {
            mContext.startService(new Intent(mContext, MusicService.class));
        }
        return true;
    }

    public void disConnected() {
        mService = null;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mService = IMusicService.Stub.asInterface(service);
        EventBus.getDefault().post(new EventMessage(AudioStation.service_ready.value()));
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mService = null;
    }

}
