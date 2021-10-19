package cn.demomaster.sporthealth_library;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;

import java.util.List;

import cn.demomaster.qdlogger_library.QDLogger;

/**
 * Created by Administrator on 2018/4/17.
 * 获取用户的地理位置
 */
public class GPSUtils {

    private static GPSUtils instance;
    private final Context mContext;
    private LocationManager locationManager;

    private GPSUtils(Context context) {
        this.mContext = context;
    }

    public static GPSUtils getInstance(Context context) {
        if (instance == null) {
            instance = new GPSUtils(context);
        }
        return instance;
    }

    /**
     * 获取经纬度
     * @return
     */
    public void getLngAndLat(LocationResultListener onLocationResultListener) {
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if(mOnLocationListener!=null) {
            locationManager.removeUpdates(mOnLocationListener);
        }
        mOnLocationListener = onLocationResultListener;

        String locationProvider = null;
        //获取所有可用的位置提供器
        List<String> providers = locationManager.getProviders(true);
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            //如果是GPS
            locationProvider = LocationManager.GPS_PROVIDER;
            QDLogger.i("GPS定位");
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是Network
            locationProvider = LocationManager.NETWORK_PROVIDER;
            QDLogger.i("网路定位");
        } else {
            QDLogger.i("no gps no network");
            Intent i = new Intent();
            i.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            mContext.startActivity(i);
            return ;
        }
        Location location = null;
        //获取Location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                QDLogger.i("no permission");
            } else {
                location = locationManager.getLastKnownLocation(locationProvider);
            }
        } else {
            location = locationManager.getLastKnownLocation(locationProvider);
        }
        if (location != null) {
            //不为空,显示地理位置经纬度
            if (mOnLocationListener != null) {
                mOnLocationListener.onLocationResult(location);
            }
        }
        //监视地理位置变化
        locationManager.requestLocationUpdates(locationProvider, 10000, 0, mOnLocationListener);
    }

    public void removeListener() {
        if(mOnLocationListener!=null) {
            locationManager.removeUpdates(mOnLocationListener);
        }
    }

    private LocationResultListener mOnLocationListener;

    public abstract static class LocationResultListener implements LocationListener{
       protected abstract void onLocationResult(Location location);

        @Override
        public void onLocationChanged(@NonNull Location location) {

        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {

        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }

        @NonNull
        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

}