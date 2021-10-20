package com.demomaster.weimusic.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.dialog.AudioInfoDialog;
import com.demomaster.weimusic.player.service.MusicDataManager;
import com.demomaster.weimusic.ui.adapter.AudioInfoAdapter;
import com.demomaster.weimusic.ui.adapter.MusicRecycleViewAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.qdlogger_library.QDLogger;
import cn.demomaster.qdrouter_library.base.fragment.QuickFragment;

import static com.demomaster.weimusic.constant.AudioStation.CURSOR_CHANGED;

public class AudioInoFragment extends QuickFragment {

    @Override
    public boolean isUseActionBarLayout() {
        return false;
    }
    @Override
    public View onGenerateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_music_info, null);
        return mView;
    }

    private int selectIndex;
    private ViewPager viewPager2;
    AudioInfoAdapter audioInfoAdapter;
    Bitmap bitmap;
    @Override
    public void initView(View rootView) {
        /*bitmap = ScreenShotUitl.shotActivity(getActivity(),true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                bitmap = QDBitmapUtil.zoomImageWithWidth(bitmap, 164);
                //QDLogger.println("bitmap w2=：" + bitmap.getWidth());
                bitmap = BlurUtil.doBlur(bitmap, 12, 0.2f);
                QdThreadHelper.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rootView.setBackground(new BitmapDrawable(bitmap));
                    }
                });
            }
        }).start();*/
        Intent intent = getIntent();
        if(intent !=null)
        {
            Bundle bundle = intent.getExtras();
            selectIndex = bundle.getInt("selectIndex");
            /*byte [] bis=intent.getByteArrayExtra("bitmap");
            Bitmap bitmap= BitmapFactory.decodeByteArray(bis, 0, bis.length);
            rootView.setBackground(new BitmapDrawable(bitmap));*/
        }
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        viewPager2 = findViewById(R.id.viewpager_audioinfo);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.setPageTransformer(true,new AudioInfoDialog.ScaleTransformer());
        audioInfoAdapter = new AudioInfoAdapter(getContext(), MusicDataManager.getInstance(mContext).getLocalSheet());
        viewPager2.setAdapter(audioInfoAdapter);
        viewPager2.setCurrentItem(selectIndex);
        audioInfoAdapter.setOnItemClickListener(new MusicRecycleViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                finish();
            }

            @Override
            public void showContextMenu(View view, int position) {

            }
        });
        viewPager2.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                EventMessage message = new EventMessage(CURSOR_CHANGED.value());
                message.setObj(position);
                EventBus.getDefault().post(message);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(EventMessage message) {
        AudioStation station = AudioStation.getEnum(message.code);
        if (station != null) {
            switch (station) {
                case song_changed:
                case Play:
                case PLAYSTATE_CHANGED:
                case audio_ready:
                    //adater2.notifyDataSetChanged();
                    break;
                case sheet_create:
                case sheet_changed:
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {//默认只处理回退事件
            finish();
            return true;//当返回true时，表示已经完整地处理了这个事件，并不希望其他的回调方法再次进行处理，而当返回false时，表示并没有完全处理完该事件，更希望其他回调方法继续对其进行处理
        }
        return super.onKeyDown(keyCode, event);
    }
    public static class ScaleTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.30f;

        @Override
        public void transformPage(View page, float position) {
            //重新计算偏移量,因为ViewPager的margin导致计算误差所以重新计算
            ViewPager viewPager = ((ViewPager)page.getParent());
            int margin = -((ViewGroup.MarginLayoutParams)viewPager.getLayoutParams()).leftMargin;
            int clientWidth = viewPager.getMeasuredWidth() - viewPager.getPaddingLeft() - viewPager.getPaddingRight()+margin;
            position = (float) (page.getLeft() - viewPager.getScrollX()) / clientWidth;

/*（1）当有View1左滑到View2时，由transformPage函数的日志获得以下数据(注意顺序)：
            view2的posion由1 -> 0;
            view1的posion由0 -> -1;
            （2）当View2右滑到View1时，由transformPage函数的日志获得以下数据(注意顺序)：
            view2的posion有0 -> 1;
            view1的posion由-1 -> 0;
            view3的posion有1 -> 2;
（2）当View2右滑到View3时，由transformPage函数的日志获得以下数据(注意顺序)：
            view2的posion有0 -> -1;
            view3的posion有1 -> 0;
            view1的posion有-1 -> -2;

            作者：墨源为水
            链接：https://www.jianshu.com/p/50f59a6a87e8
            来源：简书*/

            float progress =0;
            if (position < -1 ) {
                //progress = position+2;
                progress = 0;
            }else if(position<0){
                progress = 1+position;
            }else if(position<1){//当有View1左滑到View2时，由transformPage函数的日志获得以下数据(注意顺序)：view2的posion由1 -> 0;view1的posion由0 -> -1;
                progress = 1-position;
            }else if(position<2){
                //progress = position-1;
                progress = 0;
            }
            //ViewGroup.LayoutParams layoutParams = page.getLayoutParams();
            //QDLogger.e("transformPage: "+page.hashCode()+",position="+position+",progress:" +progress +",y="+page.getY()+",getTranslationY()="+page.getTranslationY());
            page.setScaleX(progress*(1-MIN_SCALE)+MIN_SCALE);
            page.setScaleY(progress*(1-MIN_SCALE)+MIN_SCALE);
            QDLogger.e("getScaleX: "+page.getScaleX()+",position="+position+",progress="+progress+",a="+(progress*(1-MIN_SCALE)+MIN_SCALE));

            page.setY(page.getHeight()*(1-page.getScaleX())/2);
            //Log.d("google_lenve_fb", "transformPage: scaleX:" + scaleX);
              //  page.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
        }
    }

}
