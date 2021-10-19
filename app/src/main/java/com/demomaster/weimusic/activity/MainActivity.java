package com.demomaster.weimusic.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.dialog.SheetDialog;
import com.demomaster.weimusic.player.service.MC;
import com.demomaster.weimusic.ui.adapter.MyChildAdapter;
import com.demomaster.weimusic.ui.fragment.SheetFragment2;
import com.demomaster.weimusic.ui.fragment.SheetFragment3;
import com.demomaster.weimusic.view.MainLayout;
import com.demomaster.weimusic.view.Wallpaper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;

import cn.demomaster.huan.quickdeveloplibrary.helper.toast.QdToast;
import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.util.QDBitmapUtil;
import cn.demomaster.huan.quickdeveloplibrary.util.ScreenShotUitl;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDDialog;
import cn.demomaster.qdlogger_library.QDLogger;
import cn.demomaster.qdrouter_library.actionbar.ACTIONBAR_TYPE;
import cn.demomaster.qdrouter_library.base.fragment.QuickFragment;
import cn.demomaster.qdrouter_library.manager.QuickFragmentHelper;
import cn.demomaster.quickpermission_library.PermissionHelper;
import cn.demomaster.quicksticker_annotations.BindView;
import cn.demomaster.quicksticker_annotations.QuickStickerBinder;

public class MainActivity extends BaseActivity {

    @BindView(R.id.main_layout)
    MainLayout mainLayout;
    @BindView(R.id.ll_bottom)
    ViewGroup ll_bottom;
    @BindView(R.id.iv_wallpager)
    Wallpaper iv_wallpager;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    ViewGroup fl_main;
    ViewGroup rl_docker_panel;//

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
        if (sheetDialog != null && sheetDialog.isShowing()) {
            sheetDialog.updateUI();
        }
    }

    private void handAudioUri(Intent intent) {
        if (intent != null) {
            if (intent.getAction().equals(android.content.Intent.ACTION_VIEW)) {
                if (intent.getType().startsWith("audio/")) {
                    Uri uri = intent.getData();
                    QDLogger.i("uri=" + uri);
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
        ll_bottom.setBackgroundColor(Color.TRANSPARENT);
        fl_main = findViewById(R.id.fl_main);
        rl_docker_panel = findViewById(R.id.rl_docker_panel);
        rl_docker_panel.setBackgroundColor(Color.TRANSPARENT);
        fl_main.setVisibility(View.GONE);
        iv_wallpager.updateDrawable();
        mainLayout.setOnInterceptTouchListener(new MainLayout.OnInterceptTouchListener() {
            @Override
            public boolean interceptTouchEvent(MotionEvent ev) {
                if (ev.getAction() == KeyEvent.ACTION_DOWN) {
                    EventBus.getDefault().post(new EventMessage(AudioStation.LoseFocus.value(), ev));

                   /* if (sheetFragment != null&&ev.getAction()==KeyEvent.ACTION_DOWN) {
                        //QdToast.show("isAdded="+sheetFragment.isAdded());
                        if(!sheetFragment.isAdded()){
                            hideSheetFragment();
                            return true;
                        }
                        int[] location;
                        location = new int[2];
                        View v = findViewById(sheetFragment.containerViewId);
                        if(v!=null) {
                            v.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐标
                            if (ev.getX() < location[0] || ev.getY() < location[1]
                                    || ev.getX() > (location[0] + v.getWidth())
                                    || ev.getY() > (location[1] + v.getHeight())) {
                                hideSheetFragment();
                                return true;
                            }
                        }
                    }*/
                }
                return false;
            }
        });
        /*mainLayout.setOnDispatchTouchListener(new MainLayout.OnDispatchTouchListener() {
            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                if (ev.getAction() == KeyEvent.ACTION_DOWN) {
                    EventBus.getDefault().post(new EventMessage(AudioStation.LoseFocus.value(), ev));

                    if (sheetFragment != null&&ev.getAction()==KeyEvent.ACTION_DOWN) {
                        int[] location;
                        location = new int[2];
                        View v = findViewById(sheetFragment.containerViewId);
                        if(v!=null) {
                            v.getLocationOnScreen(location);//获取在整个屏幕内的绝对坐标
                            if (ev.getX() < location[0] || ev.getY() < location[1]
                                    || ev.getX() > (location[0] + v.getWidth())
                                    || ev.getY() > (location[1] + v.getHeight())) {
                                hideSheetFragment();
                                return true;
                            }
                        }
                    }
                }
                //QDLogger.d( "dispatchTouchEvent");
                return false;
            }
        });*/

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
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) ll_bottom.getLayoutParams();
            float ll_bottom_H = ll_bottom.getMeasuredHeight();//+layoutParams.bottomMargin;
            float ll_bottom_Y = mainLayout.getMeasuredHeight() - ll_bottom_H - layoutParams.bottomMargin;
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
            if (fragmentHelper.onKeyDown(mContext, keyCode, event)) {
                QDLogger.d("点击事件已被fragment" + getClass().getName() + "消费 keyCode=" + keyCode + ",event=" + event);
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
                    QdToast.show(mContext, "下载成功");
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
    SheetFragment3 sheetFragment;

    public void showSheetFragment() {
        fl_main.setVisibility(View.VISIBLE);
        if (sheetFragment != null) {
            hideSheetFragment();
        } else {
            // ll_bottom.setBackgroundResource(R.drawable.rect_round_docker_bottom_bg);
            //ll_bottom.setBackgroundResource(R.color.transparent_light_33);
            Bitmap bitmap = getBackagroundBitmap(200);
            //rl_docker_panel.setBackground(new BitmapDrawable(copyBitmap));
            //rl_docker_panel.setBackgroundColor(getResources().getColor(R.color.white));
            //ll_bottom.setBackgroundResource(R.drawable.rect_round_docker_bg);
            sheetFragment = new SheetFragment3();
            Intent intent = new Intent();
             /*ByteArrayOutputStream baos=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte [] bitmapByte =baos.toByteArray();
            intent.putExtra("bitmap", bitmapByte);*/
            startFragment(sheetFragment, R.id.fl_main, intent);
        }
        //Bitmap bitmap = ScreenShotUitl.getCacheBitmapFromView(findViewById(R.id.main_layout));
        //GroundGlassUtil glassUtil = new GroundGlassUtil(mContext);
        /*if(sheetDialog!=null){
            sheetDialog.dismiss();
        }
        sheetDialog = new SheetDialog(mContext);
        sheetDialog.show();*/
    }

    public Bitmap getBackagroundBitmap(int alpha) {
        Bitmap bitmap = ScreenShotUitl.getCacheBitmapFromView(iv_wallpager);
        bitmap = QDBitmapUtil.zoomImageWithWidth(bitmap, 164);
        Bitmap copyBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Paint paint = new Paint();
        Canvas canvas = new Canvas(copyBitmap);
        ColorMatrix colorMatrixS = new ColorMatrix();
        float one = 0;
        colorMatrixS.setRotate(0, one);
        colorMatrixS.setRotate(1, one);
        colorMatrixS.setRotate(2, one);
        ColorMatrix colorMatrixL = new ColorMatrix();
        float two = 0.7f;
        colorMatrixL.setScale(two, two, two, 1);
        ColorMatrix colorMatrixB = new ColorMatrix();
        float three = 0.5f;
        colorMatrixB.setSaturation(three);
        ColorMatrix colorMatriximg = new ColorMatrix();
        //通过postConcat()方法可以将以上效果叠加到一起
        colorMatriximg.postConcat(colorMatrixB);
        colorMatriximg.postConcat(colorMatrixL);
        colorMatriximg.postConcat(colorMatrixS);
        ColorMatrixColorFilter colorMatrixColorFilter = new ColorMatrixColorFilter(colorMatriximg);
        paint.setColorFilter(colorMatrixColorFilter);
        paint.setAlpha(alpha);
        canvas.drawBitmap(bitmap, new Matrix(), paint);
        return copyBitmap;
    }

    public void hideSheetFragment() {
        if (sheetFragment != null) {
            // ll_bottom.setBackgroundColor(Color.TRANSPARENT);
            rl_docker_panel.setBackgroundColor(Color.TRANSPARENT);
            sheetFragment.finish();
            sheetFragment = null;
        }
    }

    public void startFragment(QuickFragment fragment, int parentId, Intent intent) {
        //getFragmentHelper().navigate(mContext,fragment, parentId,intent);
        QuickFragmentHelper.Builder builder = new QuickFragmentHelper.Builder(mContext, fragment, getFragmentHelper());
        builder.setIntent(intent)
                .setContainerViewId(parentId);
        builder.setWithAnimation(false);
        builder.navigation();
    }

}
