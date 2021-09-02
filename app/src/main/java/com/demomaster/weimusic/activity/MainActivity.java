package com.demomaster.weimusic.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.dialog.SheetDialog;
import com.demomaster.weimusic.player.service.MC;
import com.demomaster.weimusic.ui.adapter.MyChildAdapter;
import com.demomaster.weimusic.ui.fragment.SheetFragment;
import com.demomaster.weimusic.view.MainLayout;
import com.demomaster.weimusic.view.Wallpaper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import cn.demomaster.huan.quickdeveloplibrary.helper.toast.QdToast;
import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.util.GroundGlassUtil;
import cn.demomaster.huan.quickdeveloplibrary.util.ScreenShotUitl;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDDialog;
import cn.demomaster.qdlogger_library.QDLogger;
import cn.demomaster.qdrouter_library.actionbar.ACTIONBAR_TYPE;
import cn.demomaster.quickpermission_library.PermissionHelper;
import cn.demomaster.quicksticker_annotations.BindView;
import cn.demomaster.quicksticker_annotations.QuickStickerBinder;

public class MainActivity extends BaseActivity {
/*
    MainFragment1 mainFragment1;
    MainFragment2 mainFragment2;
    MainFragment3 mainFragment3;*/

    @BindView(R.id.main_layout)
    MainLayout mainLayout;
    @BindView(R.id.ll_bottom)
    LinearLayout ll_bottom;
    @BindView(R.id.iv_wallpager)
    Wallpaper iv_wallpager;
    @BindView(R.id.view_pager)
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        QuickStickerBinder.getInstance().bind(this);
        getActionBarTool().setActionBarType(ACTIONBAR_TYPE.NO_ACTION_BAR_NO_STATUS);

        EventBus.getDefault().register(this);
        boolean hsaPermission = PermissionHelper.getInstance().getPermissionStatus(mContext, PERMISSIONS);
        if (!hsaPermission) {
            showPermissionDialog();
        } else {
            initView();
        }

        handAudioUri(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handAudioUri(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(sheetDialog!=null&&sheetDialog.isShowing()){
            sheetDialog.updateUI();
        }
    }

    private void handAudioUri(Intent intent) {
        if(intent!=null){
            if(intent.getAction().equals(android.content.Intent.ACTION_VIEW)){
                if(intent.getType().startsWith("audio/")) {
                    Uri uri = intent.getData();
                    QDLogger.i("uri="+uri);
                    MC.getInstance(this).playUri(uri);
                }
            }
        }
    }

    /**
     * 权限申请
     */
    private void showPermissionDialog() {
        QDDialog qdDialog = new QDDialog.Builder(mContext)
                .setContentViewLayout(R.layout.dialog_layout_request_permission)
                .setBackgroundColor(Color.TRANSPARENT)
                .create();
        qdDialog.setCancelable(false);
        TextView tv_left = qdDialog.findViewById(R.id.tv_left);
        tv_left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qdDialog.dismiss();
                finish();
            }
        });
        TextView tv_right = qdDialog.findViewById(R.id.tv_right);
        tv_right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                qdDialog.dismiss();
                v.setEnabled(true);
                //检查权限是否通过，通过则初始化，不通过则弹窗提示
                PermissionHelper.requestPermission(mContext, PERMISSIONS, new PermissionHelper.PermissionListener() {
                    @Override
                    public void onPassed() {
                        initView();
                        EventBus.getDefault().post(new EventMessage(AudioStation.permission_pass.value()));
                    }

                    @Override
                    public void onRefused() {
                        //showPermissionDialog();
                    }
                });
            }
        });
        qdDialog.show();
    }

    private MyChildAdapter fragmentAdapter;
    void initView() {
        iv_wallpager.updateDrawable();
        mainLayout.setOnDispatchTouchListener(new MainLayout.OnDispatchTouchListener() {
            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                if (ev.getAction() == KeyEvent.ACTION_DOWN) {
                    EventBus.getDefault().post(new EventMessage(AudioStation.LoseFocus.value(),ev));
                }
                //QDLogger.d( "dispatchTouchEvent");
                return false;
            }
        });

        fragmentAdapter = new MyChildAdapter(getSupportFragmentManager());
        viewPager.setAdapter(fragmentAdapter);

        // 设置启动之后展示的fragment
        viewPager.setCurrentItem(1);
        viewPager.setOffscreenPageLimit(3);
        viewPager.addOnPageChangeListener(mOnPageChangeListener);
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                currentPagerIndex = viewPager.getCurrentItem();
                return false;
            }
        });
    }

    int currentPagerIndex = 1;
    ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int arg0) {
           // QDLogger.d("滑动当前索引=" + arg0);
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            //QDLogger.d("CGQ", "CurrentItem= " + viewPager.getCurrentItem()
             //       + ",arg0=" + arg0 + ",arg1=" + arg1 + ",arg2=" + arg2);
            float ll_bottom_H = ll_bottom.getMeasuredHeight();
            float ll_bottom_Y = mainLayout.getMeasuredHeight()-ll_bottom_H;
            if (arg1 != 0) {
                if (arg1 == 1) {
                    currentPagerIndex = viewPager.getCurrentItem();
                }
                if (currentPagerIndex == 1) {
                    if (currentPagerIndex > arg0) {
                        iv_wallpager.scrollWallPaper(arg1);// 右滑动手指
                        arg1 = 1 - arg1;
                    } else {
                        iv_wallpager.scrollWallPaper(2 - (1 - arg1));
                    }
                    ll_bottom.setY(ll_bottom_Y + ll_bottom_H * arg1 * 2);
                } else if (arg2 != 0) {
                    switch (currentPagerIndex) {
                        case 0:
                            iv_wallpager.scrollWallPaper(arg1);
                            ll_bottom.setY(ll_bottom_Y + ll_bottom_H * (1 - arg1) * 2);
                            break;
                        case 2:
                            iv_wallpager.scrollWallPaper(2 - (1 - arg1));
                            ll_bottom.setY(ll_bottom_Y + ll_bottom_H * arg1 * 2);
                            break;
                        default:
                            break;
                    }
                }
            } else {
                currentPagerIndex = viewPager.getCurrentItem();
            }
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
            //QDLogger.d("滑动完成:" + arg0);
            if (arg0 == 0) {
                if (viewPager.getCurrentItem() != 1) {
                    ll_bottom.setVisibility(View.GONE);
                }
            } else {
                ll_bottom.setVisibility(View.VISIBLE);
            }
        }
    };
    //记录用户首次点击返回键的时间
    private long firstTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (fragmentHelper != null) {
            if(fragmentHelper.onKeyDown(mContext, keyCode, event)) {
                QDLogger.d("点击事件已被fragment"+getClass().getName()+"消费 keyCode=" + keyCode + ",event=" + event);
                return true;
            }
        }
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (viewPager.getCurrentItem() != 1) {
                viewPager.setCurrentItem(1, true);
            } else {
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime > 1000) {
                    Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    firstTime = secondTime;
                    return true;
                } else {
                    finish();
                }
            }
            return true;
        }
        return false;
    }

   /* String action = intent.getAction();
            if (Action_WallBackGround.equals(action)) {
        //iv_wallpager.setImageDrawable(ThemeUtil.getWallPagerDrawable(MainActivity.this));
        //iv_wallpager.postInvalidate();
        recreate();
    } */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventMessage message) {
        AudioStation station = AudioStation.getEnum(message.getCode());
        if (station != null) {
            QDLogger.println("首頁事件:" + station.getDesc());
            switch (station) {
                case permission_pass:
                    MC.getInstance(this).loadMusicList();
                    break;
                case ThemeCoverChange:

                    break;
                case ThemeWallPagerChange:
                    iv_wallpager.updateDrawable();
                    break;
                case loadData:
                    iv_wallpager.updateDrawable();
                    break;
                case song_changed:
                    iv_wallpager.updateDrawable();
                    break;
                case DOWNLOAD_SUCCESS:
                    QdToast.show(mContext,"下载成功");
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(headSetReceiver);
        QuickStickerBinder.getInstance().unBind(this);
        mainLayout.setOnDispatchTouchListener(null);
        if (fragmentAdapter != null) {
            fragmentAdapter.destroy();
        }
    }
    SheetDialog sheetDialog;
    public void showSheetFragment(){
        startFragment(new SheetFragment(),R.id.main_layout,null);
        //Bitmap bitmap = ScreenShotUitl.getCacheBitmapFromView(findViewById(R.id.main_layout));
        //GroundGlassUtil glassUtil = new GroundGlassUtil(mContext);
        /*if(sheetDialog!=null){
            sheetDialog.dismiss();
        }
        sheetDialog = new SheetDialog(mContext);
        sheetDialog.show();*/
    }
}
