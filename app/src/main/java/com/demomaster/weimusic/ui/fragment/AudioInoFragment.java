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
        View mView = inflater.inflate(R.layout.dialog_audio_detail, null);
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
                //QDLogger.println("bitmap w2=???" + bitmap.getWidth());
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {//???????????????????????????
            finish();
            return true;//?????????true?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????false???????????????????????????????????????????????????????????????????????????????????????????????????
        }
        return super.onKeyDown(keyCode, event);
    }
    public static class ScaleTransformer implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.30f;

        @Override
        public void transformPage(View page, float position) {
            //?????????????????????,??????ViewPager???margin????????????????????????????????????
            ViewPager viewPager = ((ViewPager)page.getParent());
            int margin = -((ViewGroup.MarginLayoutParams)viewPager.getLayoutParams()).leftMargin;
            int clientWidth = viewPager.getMeasuredWidth() - viewPager.getPaddingLeft() - viewPager.getPaddingRight()+margin;
            position = (float) (page.getLeft() - viewPager.getScrollX()) / clientWidth;

/*???1?????????View1?????????View2?????????transformPage?????????????????????????????????(????????????)???
            view2???posion???1 -> 0;
            view1???posion???0 -> -1;
            ???2??????View2?????????View1?????????transformPage?????????????????????????????????(????????????)???
            view2???posion???0 -> 1;
            view1???posion???-1 -> 0;
            view3???posion???1 -> 2;
???2??????View2?????????View3?????????transformPage?????????????????????????????????(????????????)???
            view2???posion???0 -> -1;
            view3???posion???1 -> 0;
            view1???posion???-1 -> -2;

            ?????????????????????
            ?????????https://www.jianshu.com/p/50f59a6a87e8
            ???????????????*/

            float progress =0;
            if (position < -1 ) {
                //progress = position+2;
                progress = 0;
            }else if(position<0){
                progress = 1+position;
            }else if(position<1){//??????View1?????????View2?????????transformPage?????????????????????????????????(????????????)???view2???posion???1 -> 0;view1???posion???0 -> -1;
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
