package com.demomaster.weimusic.player.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.Nullable;

import com.demomaster.weimusic.IMusicService;

import java.lang.ref.WeakReference;

import cn.demomaster.qdlogger_library.QDLogger;

public class MusicService extends Service {
    private ServiceStub mBinder = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        QDLogger.d("onBind");
        if (mBinder == null) {
            mBinder = new ServiceStub(getApplicationContext());
            // mBinder.init(getApplicationContext());
        }
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        QDLogger.d("onCreate");
        MusicNotification.getInstance(this).init(this);
        /*NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        // 【适配Android8.0】设置Notification的Channel_ID,否则不能正常显示
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("notification_id");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel("notification_id", "notification_name", NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
            status = builder.build();
        } else {
            status = new Notification.Builder(this).build();
        }
        startForeground(1, status);*/
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        QDLogger.d("onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        QDLogger.d("onDestroy");
        MC.getInstance(getApplicationContext()).onDestroy();
    }

    /*
     * By making this a static class with a WeakReference to the Service, we
     * ensure that the Service can be GCd even when the system process still has
     * a remote reference to the stub.
     */
    public static class ServiceStub extends IMusicService.Stub {
        Context mContext;
        WeakReference<MC> mc;

        ServiceStub(Context context) {
            mc = new WeakReference<MC>(MC.getInstance(context));
        }

        @Override
        public boolean isPlaying() {
            return mc.get().isPlaying();
        }

        @Override
        public void stop() {
            mc.get().stop();
        }

        @Override
        public void pause() {
            mc.get().pause();
        }

        @Override
        public void play() {
            mc.get().play();
        }

        @Override
        public void prev() {
            mc.get().playPrev();
        }

        @Override
        public void next() {
            mc.get().playNext();
        }

        @Override
        public long duration() throws RemoteException {
            return mc.get().getDuration();
        }

        @Override
        public long position() throws RemoteException {
            return mc.get().getPosition();
        }

        @Override
        public int seek(int pos) throws RemoteException {
            return mc.get().seek(pos);
        }

        @Override
        public String getTrackName() {
            return mc.get().getCurrentInfo().getTitle();
        }

        @Override
        public String getAlbumName() {
            return mc.get().getCurrentInfo().getAlbum();
        }

        @Override
        public long getAlbumId() {
            return -1;
        }

    }
}
