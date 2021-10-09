
package com.demomaster.weimusic.player.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.demomaster.weimusic.player.helpers.MusicHelper;

public class ServiceBinder implements ServiceConnection {
    private final ServiceConnection mCallback;

    public ServiceBinder(ServiceConnection callback) {
        mCallback = callback;
    }

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
       // MusicHelper.getInstance().mService = IApolloService.Stub.asInterface(service);
        if (mCallback != null)
            mCallback.onServiceConnected(className, service);
    }

    @Override
    public void onServiceDisconnected(ComponentName className) {
        if (mCallback != null)
            mCallback.onServiceDisconnected(className);
        MusicHelper.getInstance().disConnected();
    }
}
