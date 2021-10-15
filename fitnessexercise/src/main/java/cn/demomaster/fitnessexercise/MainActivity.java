package cn.demomaster.fitnessexercise;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.demomaster.qdlogger_library.QDLogger;
import cn.demomaster.quickpermission_library.PermissionHelper;

public class MainActivity extends AppCompatActivity {

    TextView tv_dis;
    TextView tv_speed;
    TextView tv_time;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_dis = findViewById(R.id.tv_dis);
        tv_speed = findViewById(R.id.tv_speed);
        tv_time = findViewById(R.id.tv_time);
        record = new PathRecord();
        findViewById(R.id.btn_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionHelper.requestPermission(MainActivity.this, PERMISSIONS_POSITION, new PermissionHelper.PermissionListener() {
                    @Override
                    public void onPassed() {
                        getposition();
                    }

                    @Override
                    public void onRefused() {

                    }
                });
            }
        });
    }

    private PathRecord record;
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
                tv_time.setText("用时："+ formatTime(t/1000));
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