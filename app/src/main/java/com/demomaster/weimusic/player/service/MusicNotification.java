package com.demomaster.weimusic.player.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RemoteViews;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.activity.MainActivity;

import cn.demomaster.huan.quickdeveloplibrary.helper.NotificationHelper;

import static android.app.Notification.FLAG_NO_CLEAR;
import static android.content.Context.NOTIFICATION_SERVICE;

public class MusicNotification {

    private static MusicNotification instance;

    public static MusicNotification getInstance(Context context) {
        if(instance==null){
            instance = new MusicNotification(context);
        }
        return instance;
    }

    private MusicNotification(Context context) {
        mContext = context.getApplicationContext();
    }

    public static final String CMDNOTIF = "buttonId";
    Context mContext;
    public void updateNotification() {
        Bitmap b = getAlbumBitmap();
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.status_bar_small);
        RemoteViews bigViews = new RemoteViews(mContext.getPackageName(), R.layout.status_bar_expanded);

        //QDLogger.println("updateNotification="+b);
        if (b != null) {
            views.setViewVisibility(R.id.status_bar_album_art, View.VISIBLE);
            views.setImageViewBitmap(R.id.status_bar_album_art, b);
            bigViews.setImageViewBitmap(R.id.status_bar_album_art, b);
           // QDLogger.i("setImageViewBitmap="+b);
        }
        
        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                MediaButtonIntentReceiver.class.getName());

        //暂停/播放
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(CMDNOTIF, 1);
        intent.setComponent(componentName);
        KeyEvent mediaKey = new KeyEvent(KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
        if(!isPlaying()){
            mediaKey = new KeyEvent(KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_MEDIA_PLAY);
        }
        intent.putExtra(Intent.EXTRA_KEY_EVENT, mediaKey);
        PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(mContext,
                1, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.status_bar_play, mediaPendingIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_play, mediaPendingIntent);

        //播放下一首
        intent.putExtra(CMDNOTIF, 2);
        mediaKey = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_NEXT);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, mediaKey);
        mediaPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 2,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        views.setOnClickPendingIntent(R.id.status_bar_next, mediaPendingIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_next, mediaPendingIntent);

        //播放上一首
        intent.putExtra(CMDNOTIF, 4);
        mediaKey = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, mediaKey);
        mediaPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 4,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        bigViews.setOnClickPendingIntent(R.id.status_bar_prev, mediaPendingIntent);

        //停止
        intent.putExtra(CMDNOTIF, 3);
        mediaKey = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_STOP);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, mediaKey);
        mediaPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 3,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

       /* views.setOnClickPendingIntent(R.id.status_bar_collapse, mediaPendingIntent);
        bigViews.setOnClickPendingIntent(R.id.status_bar_collapse, mediaPendingIntent);*/

        if(!isPlaying()) {
            views.setImageViewResource(R.id.status_bar_play, R.drawable.ic_notify_play);
            bigViews.setImageViewResource(R.id.status_bar_play, R.drawable.ic_notify_play);
        }else {
            views.setImageViewResource(R.id.status_bar_play, R.drawable.ic_notify_pause);
            bigViews.setImageViewResource(R.id.status_bar_play, R.drawable.ic_notify_pause);
        }

        views.setImageViewResource(R.id.status_bar_prev, R.drawable.button_notify_prev);
        bigViews.setImageViewResource(R.id.status_bar_prev, R.drawable.button_notify_prev);
        views.setImageViewResource(R.id.status_bar_next, R.drawable.button_notify_next);
        bigViews.setImageViewResource(R.id.status_bar_next, R.drawable.button_notify_next);
        views.setTextViewText(R.id.status_bar_track_name, getTrackName());
        bigViews.setTextViewText(R.id.status_bar_track_name, getTrackName());

        views.setTextViewText(R.id.status_bar_artist_name, getArtistName());
        bigViews.setTextViewText(R.id.status_bar_artist_name, getArtistName());

       /* if(notification==null) {
            notification = new Notification.Builder(mContext.getApplicationContext()).build();
        }*/
        if(notification==null){
            return;
        }
        notification.tickerText = getTrackName();
        notification.contentView = views;
        notification.bigContentView = bigViews;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification.visibility = (Notification.VISIBILITY_PUBLIC);
        }
        //notification.priority = Notification.PRIORITY_MAX;
        // notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.flags = FLAG_NO_CLEAR;
        notification.icon = R.drawable.stat_notify_music;
        notification.contentIntent = PendingIntent.getActivity(mContext, 0,
                new Intent(mContext, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                0);
        //service.startForeground(1, notification);
        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notification);
    }

    private boolean isPlaying() {
        return  MC.getInstance(mContext).isPlaying();
    }

    private Bitmap getAlbumBitmap() {
        Bitmap bitmap = MusicDataManager.getInstance(mContext).getAlbumPicture(mContext, MC.getInstance(mContext).getCurrentInfo());
        if(bitmap==null){
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher_pp);
        }return bitmap;
    }

    private CharSequence getAlbumName() {
        return "";
    }

    private CharSequence getArtistName() {
        if(MC.getInstance(mContext).getCurrentInfo()!=null){
            return  MC.getInstance(mContext).getCurrentInfo().getArtist();
        }
        return "";
    }

    private CharSequence getTrackName() {
        if(MC.getInstance(mContext).getCurrentInfo()!=null){
           return MC.getInstance(mContext).getCurrentInfo().getTitle();
        }
       return "";
    }

    private Notification notification;
    public void init(Service service ) {
        NotificationHelper.Builer builer = new NotificationHelper.Builer(mContext);
        notification= builer.setTitle("消息通知（系统默认）")
                .setEnableVibration(false)
                .setEnableSound(false)
                .setContentText("")
                .setOngoing(true)
                .send();
        updateNotification();
        service.startForeground(1, notification);
    }
}
