package cn.demomaster.sporthealth_library.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.util.List;

import cn.demomaster.qdlogger_library.QDLogger;
import cn.demomaster.sporthealth_library.AMapUtils;
import cn.demomaster.sporthealth_library.GPSUtils;
import cn.demomaster.sporthealth_library.NotificationHelper;
import cn.demomaster.sporthealth_library.PathRecord;
import cn.demomaster.sporthealth_library.RecordPoint;

import static android.app.Notification.FLAG_NO_CLEAR;

public class SportService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification notification;
    @Override
    public void onCreate() {
        super.onCreate();
        QDLogger.i("SportService-onCreate");
        NotificationHelper.Builer builer = new NotificationHelper.Builer(this);
        notification= builer.setTitle("消息通知（系统默认）")
                .setEnableVibration(false)
                .setEnableSound(false)
                .setContentText("")
                .setOngoing(true)
                .send();
        updateNotification();
        startForeground(1, notification);
        record = new PathRecord();
        getposition();
    }

    public static PathRecord record;
    private static String[] PERMISSIONS_POSITION = {
            Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private void getposition() {
        GPSUtils.getInstance(this).getLngAndLat(new GPSUtils.LocationResultListener() {

            @Override
            protected void onLocationResult(Location location) {
                QDLogger.i("当前位置：" + location.getLatitude() + "," + location.getLongitude());
            }

            @Override
            public void onLocationChanged(@NonNull Location location) {
                super.onLocationChanged(location);
                record.addpoint(new RecordPoint(location.getLatitude(), location.getLongitude()));
                //计算配速
                float distance = getDistance(record.getPathline());
                QDLogger.i("onLocationChanged：里程：" + distance+",记录点个数："+record.getPathLinePoints().size()+",停留间隔："+record.getPathLinePoints().get(record.getPathLinePoints().size()-1).getStayTime()/1000f+"s");

                record.setCurrentDistance(distance);
                //tv_dis.setText("里程：" + distance);
                //速度用最近的两点计算
                if(record.getPathLinePoints().size()>1){
                    RecordPoint point1 = record.getPathLinePoints().get(record.getPathLinePoints().size()-1);
                    RecordPoint point2 = record.getPathLinePoints().get(record.getPathLinePoints().size()-2);
                    double distance_off = AMapUtils.calculateLineDistance(point2,point1);
                    double duration = (point2.getStartTime()-point1.getEndTime())/1000f;
                    double speed = distance_off/duration;
                    record.setCurrentSpeed(speed);
                    //tv_speed.setText("速度："+speed);
                }
                long t = System.currentTimeMillis()-record.getPathLinePoints().get(0).getStartTime();
                //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss");//设置日期格式,这里只取出小时和分钟
                //tv_time.setText("用时："+ simpleDateFormat.format(new Date(t)));
                //tv_time.setText("用时："+ formatTime(t/1000));
                //EventBus.getDefault().post(new EventMessage(AudioStation.permission_pass.value()));
                record.setCurrentDuration(t);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                QDLogger.i("onStatusChanged：");
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                super.onProviderDisabled(provider);
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                super.onProviderEnabled(provider);
            }
        });
    }


    public static String formatTime(long duration) {
        DecimalFormat decimalFormat = new DecimalFormat("00");
        //String dd = decimalFormat.format(time / 3600/24);
        String hh = decimalFormat.format(duration / 3600);
        String mm = decimalFormat.format(duration % 3600 / 60);
        String ss = decimalFormat.format(duration % 60);
        return hh + ":" + mm + ":" + ss;
    }

    //计算距离
    private float getDistance(List<RecordPoint> list) {
        float distance = 0;
        if (list == null || list.size() == 0) {
            return distance;
        }
        for (int i = 0; i < list.size() - 1; i++) {
            RecordPoint firstRecordPoint = list.get(i);
            RecordPoint secondRecordPoint = list.get(i + 1);
            double betweenDis = AMapUtils.calculateLineDistance(firstRecordPoint,
                    secondRecordPoint);
            distance = (float) (distance + betweenDis);
        }
        return distance;
    }

    public void updateNotification() {
        if(notification==null){
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification.visibility = (Notification.VISIBILITY_PUBLIC);
        }
        //notification.priority = Notification.PRIORITY_MAX;
        // notification.flags = Notification.FLAG_ONGOING_EVENT;
        notification.flags = FLAG_NO_CLEAR;
       // notification.icon = R.drawable.stat_notify_music;
       /* notification.contentIntent = PendingIntent.getActivity(mContext, 0,
                new Intent(mContext, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                0);*/
        //service.startForeground(1, notification);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notification);
    }
}
