package com.demomaster.weimusic.activity;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.util.ThemeUtil;

import cn.demomaster.huan.quickdeveloplibrary.base.activity.QDActivity;
import cn.demomaster.huan.quickdeveloplibrary.view.loading.EmoticonView;
import cn.demomaster.huan.quickdeveloplibrary.view.loading.LoadStateType;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDActionDialog;

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

   public BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
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



    public QDActionDialog qdActionDialog;
    public void showLoading(int time) {
        showTip(getString(R.string.please_wait_a_moment), LoadStateType.LOADING, time);//"请稍后..."
    }

    public void showLoading() {
        showTip(getString(R.string.please_wait_a_moment), LoadStateType.LOADING, 3000);//"请稍后..."
    }

    public void showLoading(String message) {
        //getString(R.string.please_wait_a_moment)
        showTip(message, LoadStateType.LOADING, 3000);//"请稍后..."
    }

    public void showTip(String message, LoadStateType stateType) {
        showTip(message, stateType, 3000);
    }

    public void showTip(String message, LoadStateType stateType, Long time) {
        showTip(message, stateType, time);
    }

    /**
     * 显示加载中
     *
     * @param message
     */
    public void showTip(String message, LoadStateType stateType, int time) {
        hideLoading();
        qdActionDialog = new QDActionDialog.Builder(mContext).setBackgroundRadius(50).setContentbackgroundColor(Color.TRANSPARENT).setContentViewLayout(R.layout.item_dialog_tip).setDelayMillis(time).create();
        EmoticonView emoticonView = qdActionDialog.getContentView().findViewById(R.id.ev_emtion);
        emoticonView.setStateType(stateType);
        TextView tv_tip = qdActionDialog.getContentView().findViewById(R.id.tv_tip);
        tv_tip.setText(message);
        qdActionDialog.show();
    }

    public void hideLoading() {
        if (qdActionDialog != null && qdActionDialog.isShowing()) {
            qdActionDialog.dismiss();
        }
    }

}
