package com.demomaster.weimusic.dialog;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.demomaster.weimusic.R;
import com.demomaster.weimusic.activity.AddSongSheetActivity;
import com.demomaster.weimusic.model.AudioSheet;
import com.demomaster.weimusic.player.service.MC;
import com.demomaster.weimusic.player.service.MusicDataManager;
import com.demomaster.weimusic.ui.adapter.SheetBodyAdapter;

import java.util.ArrayList;
import java.util.List;

import cn.demomaster.huan.quickdeveloplibrary.util.DisplayUtil;
import cn.demomaster.huan.quickdeveloplibrary.view.banner.BannerCursorView;
import cn.demomaster.huan.quickdeveloplibrary.widget.dialog.QDDialog2;
import cn.demomaster.huan.quickdeveloplibrary.widget.layout.VisibleLayout;
import cn.demomaster.qdlogger_library.QDLogger;

public class SheetDialog extends QDDialog2 {
    Activity activity;

    public SheetDialog(Activity context) {
        super(context);
        this.activity = context;
        setContentView(R.layout.dialog_layout_sheet);
        //getWindow().setBackgroundDrawableResource(android.R.color.holo_red_dark);
        initView();
    }

    public boolean isHasPadding() {
        return false;
    }

    ViewGroup ll_controll;
    ImageView iv_icon;
    ImageView iv_edit;
    ImageView iv_play_sheet;
    TextView tv_name,tv_count;
    ViewGroup rl_content;
    private ViewPager viewPager2;
    private SheetBodyAdapter adater2;
    //SheetHeaderAdapter adater;
    List<AudioSheet> audioSheets;
    VisibleLayout vl_layout;
    BannerCursorView cursorView;
    long sheetId;
    public int animationStyleID = cn.demomaster.huan.quickdeveloplibrary.R.style.qd_dialog_animation_fade;

   /* @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public <T extends View> T findViewById(int id) {
        return rootView.findViewById(id);
    }*/

    public void initView() {
        Window win = getWindow();
        if (animationStyleID != -1) {
            win.setWindowAnimations(animationStyleID);
        }
       /* bitmap = QDBitmapUtil.zoomImage(bitmap, bitmap.getWidth() / 3, bitmap.getHeight() / 3);
        //QDLogger.println("bitmap w2=：" + bitmap.getWidth());
        bitmap = BlurUtil.doBlur(bitmap, 50, 0.2f);
        rootView.setBackground(new BitmapDrawable(bitmap));*/

        cursorView = findViewById(R.id.cursorView);
        cursorView.setRadius(DisplayUtil.dip2px(getContext(), 3), DisplayUtil.dip2px(getContext(), 4));
        cursorView.setCursorPointColor(getContext().getResources().getColor(R.color.transparent_light_77), getContext().getResources().getColor(R.color.white));
        rl_content = findViewById(R.id.rl_content);
        rl_content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        ll_controll = findViewById(R.id.ll_controll);

        vl_layout = findViewById(R.id.vl_layout);
        vl_layout.setGravity(Gravity.BOTTOM);
        iv_icon = findViewById(R.id.iv_icon);
        tv_name = findViewById(R.id.tv_name);
        tv_count = findViewById(R.id.tv_count);

        audioSheets = new ArrayList<>();
        audioSheets.addAll(MusicDataManager.getInstance(getContext()).getSongSheet(getContext()));
        cursorView.setIndicatorCount(audioSheets.size());

        viewPager2 = findViewById(R.id.viewpager2);
        viewPager2.setOffscreenPageLimit(1);
       // adater2 = new SheetAdapter(this.activity, audioSheets);
        viewPager2.setAdapter(adater2);

        //viewPager.setPageTransformer(false, new ScaleTransformer());
        viewPager2.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (positionOffsetPixels != 0) {
                    if (positionOffset > 0.5) {
                        positionOffset = (positionOffset - 0.5f);
                    } else {
                        positionOffset = 0.5f - positionOffset;
                    }
                    vl_layout.setProgress(positionOffset * 2);
                }
            }

            @Override
            public void onPageSelected(int position) {
                sheetId = audioSheets.get(position).getId();
                updatePosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        iv_play_sheet = findViewById(R.id.iv_play_sheet);
        iv_edit = findViewById(R.id.iv_edit);
        iv_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putLong("sheetId", sheetId);
                Intent intent = new Intent(getContext(), AddSongSheetActivity.class);
                intent.putExtras(bundle);
                getContext().startActivity(intent);
            }
        });

        iv_play_sheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MC.getInstance(getContext()).playSheet(sheetId);
            }
        });
        sheetId = MusicDataManager.getInstance(getContext()).getCurrentSheetId();
        if (sheetId == -1 && audioSheets != null && audioSheets.size() > 0) {
            sheetId = audioSheets.get(0).getId();
        }
        QDLogger.i("sheetId=" + sheetId + ",audioSheets=" + audioSheets.size());
        updateUI();
        // EventBus.getDefault().register(this);
    }

    public void updateUI() {
        audioSheets.clear();
        audioSheets.addAll(MusicDataManager.getInstance(getContext()).getSongSheet(getContext()));
        for (int i = 0; i < audioSheets.size(); i++) {
            if (audioSheets.get(i).getId() == sheetId) {
                //viewPager.setCurrentItem(i);
                updatePosition(i);
                break;
            }
        }
    }

    public void updatePosition(int i) {
        //viewPager.setCurrentItem(i);
        viewPager2.setCurrentItem(i);
        cursorView.selecte(i);
        tv_name.setText(audioSheets.get(i).getName());
       // iv_play_sheet.setImageResource(MusicDataManager.getInstance().getCurrentSheetId()==audioSheets.get(i).getId()?R.drawable.button_music_play01:R.drawable.button_music_play02);
        tv_count.setText("");//"("+audioSheets.get(i).size()+")");
        Glide.with(getContext()).load(audioSheets.get(i).getImgSrc()).into(iv_icon);
        //ll_controll.setBackgroundColor(audioSheets.get(i).getThemeColor());
        if (viewPager2.getCurrentItem() != i) {
            viewPager2.setCurrentItem(i);
        }
        if (!TextUtils.isEmpty(audioSheets.get(i).getImgSrc())) {
            iv_icon.setVisibility(View.VISIBLE);
            Glide.with(getContext()).load(audioSheets.get(i).getImgSrc()).into(iv_icon);
        } else {
            iv_icon.setVisibility(View.GONE);
        }
    }
}
