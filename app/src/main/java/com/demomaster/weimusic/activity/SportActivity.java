package com.demomaster.weimusic.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.WeiApplication;
import com.demomaster.weimusic.player.service.MusicService;

import java.text.DecimalFormat;
import java.util.List;

import cn.demomaster.qdlogger_library.QDLogger;
import cn.demomaster.quickpermission_library.PermissionHelper;
import cn.demomaster.sporthealth_library.AMapUtils;
import cn.demomaster.sporthealth_library.GPSUtils;
import cn.demomaster.sporthealth_library.PathRecord;
import cn.demomaster.sporthealth_library.RecordPoint;
import cn.demomaster.sporthealth_library.service.SportService;

public class SportActivity extends BaseActivity {

    TextView tv_dis;
    TextView tv_speed;
    TextView tv_time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport);
        tv_dis = findViewById(R.id.tv_dis);
        tv_speed = findViewById(R.id.tv_speed);
        tv_time = findViewById(R.id.tv_time);
        setTitle("Sports");
        //record = new PathRecord();
        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionHelper.requestPermission(SportActivity.this, PERMISSIONS_POSITION, new PermissionHelper.PermissionListener() {
                    @Override
                    public void onPassed() {
                        Intent intent = new Intent(mContext, MusicService.class);
                        mContext.bindService(intent, (ServiceConnection) WeiApplication.getInstance(), Context.BIND_AUTO_CREATE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            mContext.startForegroundService(new Intent(mContext, SportService.class));
                        } else {
                            mContext.startService(new Intent(mContext, MusicService.class));
                        }
                        //getposition();
                    }

                    @Override
                    public void onRefused() {

                    }
                });
            }
        });

        handler.postDelayed(runnable,1000);
    }
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            handler.removeCallbacks(runnable);
            if(SportService.record !=null){
                tv_dis.setText("里程：" + SportService.record.getCurrentDistance());
                tv_speed.setText("速度："+SportService.record.getSpeed());
                tv_time.setText("用时："+ formatTime(SportService.record.getCurrentDuration()/1000));
            }
            handler.postDelayed(runnable,2000);//record
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    //private PathRecord record;
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

                /*record.addpoint(new RecordPoint(location.getLatitude(), location.getLongitude()));

                //计算配速
                float distance = getDistance(record.getPathline());
                QDLogger.i("onLocationChanged：里程：" + distance+",记录点个数："+record.getPathLinePoints().size()+",停留间隔："+record.getPathLinePoints().get(record.getPathLinePoints().size()-1).getStayTime()/1000f+"s");

                tv_dis.setText("里程：" + distance);
                //速度用最近的两点计算
                if(record.getPathLinePoints().size()>1){
                    RecordPoint point1 = record.getPathLinePoints().get(record.getPathLinePoints().size()-1);
                    RecordPoint point2 = record.getPathLinePoints().get(record.getPathLinePoints().size()-2);
                    double distance_off = AMapUtils.calculateLineDistance(point2,point1);
                    double duration = (point2.getStartTime()-point1.getEndTime())/1000f;
                    double speed = distance_off/duration;
                    tv_speed.setText("速度："+speed);
                }
                long t = System.currentTimeMillis()-record.getPathLinePoints().get(0).getStartTime();

                //SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss");//设置日期格式,这里只取出小时和分钟
                //tv_time.setText("用时："+ simpleDateFormat.format(new Date(t)));
                tv_time.setText("用时："+ formatTime(t/1000));*/
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

}