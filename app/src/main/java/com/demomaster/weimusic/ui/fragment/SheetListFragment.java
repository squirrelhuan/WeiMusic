package com.demomaster.weimusic.ui.fragment;

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
import com.demomaster.weimusic.activity.MainActivity;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.model.AudioSheet;
import com.demomaster.weimusic.player.service.MusicDataManager;
import com.demomaster.weimusic.ui.adapter.MyChildAdapter;
import com.demomaster.weimusic.ui.adapter.SheetBodyAdapter;
import com.demomaster.weimusic.ui.adapter.SheetFragmentAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.util.DisplayUtil;
import cn.demomaster.huan.quickdeveloplibrary.view.banner.BannerCursorView;
import cn.demomaster.qdlogger_library.QDLogger;
import cn.demomaster.qdrouter_library.base.fragment.QuickFragment;

public class SheetListFragment extends QuickFragment {

    @Override
    public boolean isUseActionBarLayout() {
        return false;
    }

    @Override
    public View onGenerateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_layout_sheet3, null);
        return mView;
    }

    //ImageView iv_icon;
    ImageView iv_edit;
    TextView tv_name;
    ViewGroup rl_content;
    private ViewPager sheetBodyViewPager;
    SheetFragmentAdapter sheetFragmentAdapter;
    //private SheetBodyAdapter sheetAdapter;
    //SheetHeaderAdapter adater;
    List<AudioSheet> audioSheets;
    BannerCursorView cursorView;
    long sheetId;

    @Override
    public void initView(View rootView) {
        /*getActionBarTool().setActionBarType(ACTIONBAR_TYPE.NO_ACTION_BAR);*/
       /* Intent intent = getIntent();
        if (intent != null) {
            byte[] bis = intent.getByteArrayExtra("bitmap");
            Bitmap bitmap = BitmapFactory.decodeByteArray(bis, 0, bis.length);
            rootView.setBackground(new BitmapDrawable(bitmap));
        }*/
        rl_content = findViewById(R.id.rl_content);
        iv_edit = findViewById(R.id.iv_edit);
        sheetBodyViewPager = findViewById(R.id.viewpager_sheet_body);
        cursorView = findViewById(R.id.cursorView);
        cursorView.setRadius(DisplayUtil.dip2px(getContext(), 3), DisplayUtil.dip2px(getContext(), 4));
        cursorView.setCursorPointColor(getResources().getColor(R.color.transparent_light_77), getResources().getColor(R.color.white));
   /*     iv_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putLong("sheetId", sheetId);
                Intent intent = new Intent(getContext(), AddSongSheetActivity.class);
                intent.putExtras(bundle);
                getContext().startActivity(intent);
            }
        });*/

        audioSheets = new ArrayList<>();
        audioSheets.addAll(MusicDataManager.getInstance(mContext).getSongSheet());

        //vl_layout = rootView.findViewById(R.id.vl_layout);
        //vl_layout.setGravity(Gravity.TOP);
        //iv_icon = rootView.findViewById(R.id.iv_icon);
        tv_name = rootView.findViewById(R.id.tv_name);
        cursorView.setIndicatorCount(audioSheets.size());
       /* sheetHeaderViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
                sheetHeaderAdapter.setCurrent(sheetHeaderViewPager,position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });*/

       // sheetBodyViewPager.setOffscreenPageLimit(3);
       // sheetAdapter = new SheetBodyAdapter(this, audioSheets);
        sheetFragmentAdapter = new SheetFragmentAdapter(getChildFragmentManager());
        sheetBodyViewPager.setAdapter(sheetFragmentAdapter);
        sheetBodyViewPager.addOnPageChangeListener(new BaseLinkPageChangeListener(sheetBodyViewPager, null) {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                cursorView.selecte(position);
                //tv_name.setText(audioSheets.get(position).getName());
            }
        });

        /*viewPager2.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
                tabLayout.onPageScrolled(position, positionOffset, positionOffsetPixels);
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
                    sliding_layout.setScrollableView(scrollView);
                }
                //QDLogger.e("getImgSrc="+audioSheets.get(position).getImgSrc());
                //Glide.with(mContext).load(audioSheets.get(position).getImgSrc()).into(iv_icon);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });*/

        /*tv_play_sheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MC.getInstance(mContext).playSheet(sheetId);
            }
        });*/
        sheetId = MusicDataManager.getInstance(mContext).getCurrentSheetId();
        if (sheetId == -1 && audioSheets != null && audioSheets.size() > 0) {
            sheetId = audioSheets.get(0).getId();
        }
        rootView.setBackgroundColor(getResources().getColor(R.color.transparent_dark_99));
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        sheetBodyViewPager.post(new Runnable() {
            @Override
            public void run() {
                if (audioSheets != null) {
                    for (int i = 0; i < audioSheets.size(); i++) {
                        if (audioSheets.get(i).getId() == sheetId) {
                            if (i > 0) {
                                sheetBodyViewPager.setCurrentItem(i);
                            }
                            break;
                        }
                    }
                } else {
                    sheetBodyViewPager.setCurrentItem(0);
                }
                //tv_name.setText(audioSheets.get(sheetBodyViewPager.getCurrentItem()).getName());
                //sheetHeaderAdapter.setCurrent(sheetHeaderViewPager, sheetBodyViewPager.getCurrentItem());
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
                    //sheetAdapter.notifyDataSetChanged();
                    sheetFragmentAdapter.notifyDataSetChanged();
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
            ViewPager viewPager = ((ViewPager) page.getParent());
            int margin = -((ViewGroup.MarginLayoutParams) viewPager.getLayoutParams()).leftMargin;
            int clientWidth = viewPager.getMeasuredWidth() - viewPager.getPaddingLeft() - viewPager.getPaddingRight() + margin;
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

            float progress = 0;
            if (position < -1) {
                //progress = position+2;
                progress = 0;
            } else if (position < 0) {
                progress = 1 + position;
            } else if (position < 1) {//??????View1?????????View2?????????transformPage?????????????????????????????????(????????????)???view2???posion???1 -> 0;view1???posion???0 -> -1;
                progress = 1 - position;
            } else if (position < 2) {
                //progress = position-1;
                progress = 0;
            }
            //ViewGroup.LayoutParams layoutParams = page.getLayoutParams();
            //QDLogger.e("transformPage: "+page.hashCode()+",position="+position+",progress:" +progress +",y="+page.getY()+",getTranslationY()="+page.getTranslationY());
            page.setScaleX(progress * (1 - MIN_SCALE) + MIN_SCALE);
            page.setScaleY(progress * (1 - MIN_SCALE) + MIN_SCALE);
            //QDLogger.e("getScaleX: "+page.getScaleX()+",position="+position+",progress="+progress+",a="+(progress*(1-MIN_SCALE)+MIN_SCALE));

            page.setY(page.getHeight() * (1 - page.getScaleX()) / 2);
            //Log.d("google_lenve_fb", "transformPage: scaleX:" + scaleX);
            //  page.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
        }
    }

    public class BaseLinkPageChangeListener implements ViewPager.OnPageChangeListener {

        private ViewPager linkViewPager;
        private ViewPager selfViewPager;

        private int pos;

        public BaseLinkPageChangeListener(ViewPager selfViewPager, ViewPager linkViewPager) {
            this.linkViewPager = linkViewPager;
            this.selfViewPager = selfViewPager;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if(linkViewPager!=null) {
                int marginX = ((selfViewPager.getWidth() + selfViewPager.getPageMargin()) * position
                        + positionOffsetPixels) * (linkViewPager.getWidth() + linkViewPager.getPageMargin()) / (
                        selfViewPager.getWidth()
                                + selfViewPager.getPageMargin());

                if (linkViewPager.getScrollX() != marginX) {
                    linkViewPager.scrollTo(marginX, 0);
                }
            }
        }

        @Override
        public void onPageSelected(int position) {
            this.pos = position;
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (linkViewPager!=null&&state == ViewPager.SCROLL_STATE_IDLE) {
                linkViewPager.setCurrentItem(pos);
            }
        }
    }

    public static class ScaleTransformer2 implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.30f;

        @Override
        public void transformPage(View page, float position) {
            //?????????????????????,??????ViewPager???margin????????????????????????????????????
            ViewPager viewPager = ((ViewPager) page.getParent());
            int margin = -((ViewGroup.MarginLayoutParams) viewPager.getLayoutParams()).leftMargin;
            int clientWidth = viewPager.getMeasuredWidth() - viewPager.getPaddingLeft() - viewPager.getPaddingRight() + margin;
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

            float progress = 0;
            if (position < -1) {
                //progress = position+2;
                progress = 0;
            } else if (position < 0) {
                progress = 1 + position;
            } else if (position < 1) {//??????View1?????????View2?????????transformPage?????????????????????????????????(????????????)???view2???posion???1 -> 0;view1???posion???0 -> -1;
                progress = 1 - position;
            } else if (position < 2) {
                //progress = position-1;
                progress = 0;
            }
            //ViewGroup.LayoutParams layoutParams = page.getLayoutParams();
            //QDLogger.e("transformPage: "+page.hashCode()+",position="+position+",progress:" +progress +",y="+page.getY()+",getTranslationY()="+page.getTranslationY());
            page.setScaleX(progress * (1 - MIN_SCALE) + MIN_SCALE);
            page.setScaleY(progress * (1 - MIN_SCALE) + MIN_SCALE);
            QDLogger.e("getScaleX: " + page.getScaleX() + ",position=" + position + ",progress=" + progress + ",a=" + (progress * (1 - MIN_SCALE) + MIN_SCALE));

            int y = (int) (page.getHeight() * (1 - page.getScaleX()) / 2);
            page.setY(-y + 100 * page.getScaleX());
            //Log.d("google_lenve_fb", "transformPage: scaleX:" + scaleX);
            //  page.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
        }
    }

    public static class ScaleTransformer3 implements ViewPager.PageTransformer {
        private static final float MIN_SCALE = 0.50f;

        @Override
        public void transformPage(View page, float position) {
            //?????????????????????,??????ViewPager???margin????????????????????????????????????
            ViewPager viewPager = ((ViewPager) page.getParent());
            int margin = -((ViewGroup.MarginLayoutParams) viewPager.getLayoutParams()).leftMargin;
            int clientWidth = viewPager.getMeasuredWidth() - viewPager.getPaddingLeft() - viewPager.getPaddingRight() + margin;
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

            float progress = 0;
            if (position < -1) {
                //progress = position+2;
                progress = 0;
            } else if (position < 0) {
                progress = 1 + position;
            } else if (position < 1) {//??????View1?????????View2?????????transformPage?????????????????????????????????(????????????)???view2???posion???1 -> 0;view1???posion???0 -> -1;
                progress = 1 - position;
            } else if (position < 2) {
                //progress = position-1;
                progress = 0;
            }
            page.setAlpha(progress + .45f);
            //ViewGroup.LayoutParams layoutParams = page.getLayoutParams();
            QDLogger.e("transformPage: " + page.hashCode() + ",position=" + position + ",progress:" + progress + ",y=" + page.getY() + ",getTranslationY()=" + page.getTranslationY());
            page.setScaleX(progress * (1 - MIN_SCALE) + MIN_SCALE);
            page.setScaleY(progress * (1 - MIN_SCALE) + MIN_SCALE);
            /*page.setTranslationX((1-progress)*viewPager.getMeasuredWidth());
            page.setTranslationX(20);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if(progress==1) {
                    page.setZ(1);
                }else {
                    page.setZ(0);
                }
            }*/
            //QDLogger.e("getScaleX: "+page.getScaleX()+",position="+position+",progress="+progress+",a="+(progress*(1-MIN_SCALE)+MIN_SCALE));

            //page.setY(page.getHeight()*(1-page.getScaleX())/2);
            //Log.d("google_lenve_fb", "transformPage: scaleX:" + scaleX);
            //  page.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));


           /* int pageWidth = page.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                page.setAlpha(0);

            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                page.setAlpha(1);
                page.setTranslationX(0);
                page.setScaleX(1);
                page.setScaleY(1);

            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                page.setAlpha(1 - position);

                // Counteract the default slide transition
                page.setTranslationX(pageWidth * -position);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE
                        + (1 - MIN_SCALE) * (1 - Math.abs(position));
                page.setScaleX(scaleFactor);
                page.setScaleY(scaleFactor);

            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                page.setAlpha(0);
            }*/
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).hideSheetFragment();
        }
    }
}
