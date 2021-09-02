package com.demomaster.weimusic.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.util.ThemeUtil;

import cn.demomaster.huan.quickdeveloplibrary.base.activity.QDActivity;

import static com.demomaster.weimusic.constant.Constants.Action_Theme_Change;

/**
 * Created by huan on 2018/1/27.
 */

public abstract class BaseActivity extends QDActivity {
    // 权限
    public static String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE};
    public void setContentView(int layoutResID) {
        setupActivityBeforeCreate();
        super.setContentView(layoutResID);
    }

    @Override
    public View getHeaderlayout() {
        return View.inflate(this, R.layout.activity_actionbar_common,null);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Action_Theme_Change)) {
                notifyByThemeChanged();
            }
        }
    };

    protected void setupActivityBeforeCreate() {
        setTheme(ThemeUtil.getThemeResId());
        sendBroadcast(broadcastReceiver, Action_Theme_Change);
    }

    public void sendBroadcast(BroadcastReceiver broadcastReceiver, String... actions) {
        IntentFilter intentFilter = new IntentFilter();
        for (String action : actions) {
            intentFilter.addAction(action);
        }
        registerReceiver(broadcastReceiver, intentFilter);
    }

    public void notifyByThemeChanged() {
        recreate();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            //MusicHelper.getInstance().unbindFromService(mToken);
            unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
