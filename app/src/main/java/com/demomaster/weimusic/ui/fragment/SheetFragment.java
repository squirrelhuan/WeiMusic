package com.demomaster.weimusic.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.activity.SongSheetEditActivity;
import com.demomaster.weimusic.activity.MainActivity;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.model.AudioSheet;
import com.demomaster.weimusic.player.service.MusicDataManager;
import com.demomaster.weimusic.ui.adapter.SheetBodyAdapter;
import com.demomaster.weimusic.ui.adapter.SheetHeaderAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.widget.layout.VisibleLayout;
import cn.demomaster.qdlogger_library.QDLogger;
import cn.demomaster.qdrouter_library.base.fragment.QuickFragment;

public class SheetFragment extends QuickFragment {

    @Override
    public boolean isUseActionBarLayout() {
        return false;
    }
    @Override
    public View onGenerateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_layout_sheet2, null);
        return mView;
    }

    ImageView iv_edit;
    TextView tv_name;
    private ViewPager sheetHeaderViewPager;
    private ViewPager viewPager2;
    private SheetBodyAdapter adater2;
    private ViewGroup rl_header_bg;
    //SheetHeaderAdapter adater;
    List<AudioSheet> audioSheets;
    VisibleLayout vl_layout;
    //BannerCursorView cursorView;
    SheetHeaderAdapter sheetHeaderAdapter;
    long sheetId;
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
        rl_header_bg = findViewById(R.id.rl_header_bg);
        /*cn.demomaster.huan.quickdeveloplibrary.widget.slidingpanellayout.SlidingUpPanelLayout sliding_layout = findViewById(R.id.sliding_layout);
        sliding_layout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    finish();
                }
            }
        });*/

       /* cursorView = findViewById(R.id.cursorView);
        cursorView.setRadius(DisplayUtil.dip2px(getContext(),3),DisplayUtil.dip2px(getContext(),4));
        cursorView.setCursorPointColor(getResources().getColor(R.color.transparent_light_77),getResources().getColor(R.color.white));*/
        /*rl_content = findViewById(R.id.rl_content);
        rl_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/

        iv_edit = findViewById(R.id.iv_edit);
        iv_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putLong("sheetId", sheetId);
                Intent intent = new Intent(getContext(), SongSheetEditActivity.class);
                intent.putExtras(bundle);
                getContext().startActivity(intent);
            }
        });

        //vl_layout = rootView.findViewById(R.id.vl_layout);
        //vl_layout.setGravity(Gravity.TOP);
        //iv_icon = rootView.findViewById(R.id.iv_icon);
        tv_name = rootView.findViewById(R.id.tv_name);

        audioSheets = new ArrayList<>();
        audioSheets.addAll(MusicDataManager.getInstance(mContext).getSongSheet());
       // cursorView.setIndicatorCount(audioSheets.size());
        sheetHeaderViewPager = findViewById(R.id.viewpager);
        sheetHeaderViewPager.setOffscreenPageLimit(3);
        sheetHeaderAdapter = new SheetHeaderAdapter(getContext(), audioSheets);
        sheetHeaderViewPager.setAdapter(sheetHeaderAdapter);
        sheetHeaderViewPager.setPageTransformer(false, new ScaleTransformer());
        sheetHeaderViewPager.post(new Runnable() {
            @Override
            public void run() {
                int margin = -((ViewGroup.MarginLayoutParams)sheetHeaderViewPager.getLayoutParams()).leftMargin;
                int paddingLeft = sheetHeaderViewPager.getPaddingLeft();
                QDLogger.i("setPageMargin="+margin+","+sheetHeaderViewPager.getPageMargin()+",paddingLeft="+paddingLeft);
                //sheetHeaderViewPager.setPageMargin(margin);
                ViewGroup.LayoutParams layoutParams = sheetHeaderViewPager.getLayoutParams();
                layoutParams.height = sheetHeaderViewPager.getMeasuredWidth();
            }
        });
        sheetHeaderViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
               //Bitmap bitmap = QDBitmapUtil.getBitmapFromPath(audioSheets.get(position).getImgSrc());
               //bitmap = QDBitmapUtil.zoomImage(bitmap, bitmap.getWidth() / 4, bitmap.getHeight() / 4);
                //bitmap = BlurUtil.doBlur(bitmap,30,0.2f);
                //rl_header_bg.setBackground(new BitmapDrawable(bitmap));
                if(viewPager2.getCurrentItem()!=position) {
                    viewPager2.setCurrentItem(position);
                }
                tv_name.setText(audioSheets.get(position).getName());
                //sheetHeaderAdapter.setCurrent(sheetHeaderViewPager,position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        viewPager2 = findViewById(R.id.viewpager2);
        viewPager2.setOffscreenPageLimit(3);
        //adater2 = new SheetAdapter(getContext(), audioSheets);
        viewPager2.setAdapter(adater2);

        viewPager2.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if(positionOffsetPixels!=0){
                    if(positionOffset>0.5) {
                        positionOffset = (positionOffset-0.5f);
                    }else {
                        positionOffset = 0.5f-positionOffset;
                    }
                    //vl_layout.setProgress(positionOffset*2);
                }
            }

            @Override
            public void onPageSelected(int position) {
                sheetId = audioSheets.get(position).getId();
                //cursorView.selecte(position);
                if(sheetHeaderViewPager.getCurrentItem()!=position) {
                    sheetHeaderViewPager.setCurrentItem(position);
                }
                View view = adater2.getCurrentView(viewPager2,position);
                if(view!=null) {
                    View scrollView = view.findViewById(R.id.rv_songs);
                    //sliding_layout.setScrollableView(scrollView);
                }
                //QDLogger.e("getImgSrc="+audioSheets.get(position).getImgSrc());
                //Glide.with(mContext).load(audioSheets.get(position).getImgSrc()).into(iv_icon);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        /*tv_play_sheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MC.getInstance(mContext).playSheet(sheetId);
            }
        });*/
        sheetId = MusicDataManager.getInstance(mContext).getCurrentSheetId();
        if(sheetId==-1&&audioSheets!=null&&audioSheets.size()>0){
            sheetId = audioSheets.get(0).getId();
        }
        viewPager2.post(new Runnable() {
            @Override
            public void run() {
                if(audioSheets !=null){
                    for(int i = 0; i< audioSheets.size(); i++){
                        if(audioSheets.get(i).getId()==sheetId){
                            if(i>0){
                                viewPager2.setCurrentItem(i);
                            }
                            break;
                        }
                    }
                }else {
                    viewPager2.setCurrentItem(0);
                }
                View view = adater2.getCurrentView(viewPager2,viewPager2.getCurrentItem());
                if(view!=null) {
                    View scrollView = view.findViewById(R.id.rv_songs);
                    //sliding_layout.setScrollableView(scrollView);
                }
                tv_name.setText(audioSheets.get(viewPager2.getCurrentItem()).getName());
                //sheetHeaderAdapter.setCurrent(sheetHeaderViewPager,viewPager2.getCurrentItem());
            }
        });

        EventBus.getDefault().register(this);
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
                    //adater.notifyDataSetChanged();
                    adater2.notifyDataSetChanged();
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

    public static class ScaleTransformer2 implements ViewPager.PageTransformer {
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

            int y = (int) (page.getHeight()*(1-page.getScaleX())/2);
            page.setY(-y+100*page.getScaleX());
            //Log.d("google_lenve_fb", "transformPage: scaleX:" + scaleX);
            //  page.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((MainActivity)getActivity()).hideSheetFragment();
    }
}
