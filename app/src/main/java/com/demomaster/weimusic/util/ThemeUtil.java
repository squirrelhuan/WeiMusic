package com.demomaster.weimusic.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.constant.AudioStation;
import com.demomaster.weimusic.constant.Constants;
import com.demomaster.weimusic.constant.ThemeConstants;

import org.greenrobot.eventbus.EventBus;

import cn.demomaster.huan.quickdeveloplibrary.helper.QDSharedPreferences;
import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.huan.quickdeveloplibrary.util.QDBitmapUtil;

import static com.demomaster.weimusic.constant.Constants.Key_Theme_Cover_Custom;
import static com.demomaster.weimusic.constant.Constants.Key_Theme_Cover_System;
import static com.demomaster.weimusic.constant.Constants.Key_Theme_Font_System;
import static com.demomaster.weimusic.constant.Constants.Key_Theme_WallPager_Custom;
import static com.demomaster.weimusic.constant.Constants.Key_Theme_WallPager_System;
import static com.demomaster.weimusic.constant.Constants.Key_Theme_Welcome_Custom;
import static com.demomaster.weimusic.constant.Constants.Key_Theme_Welcome_System;
import static com.demomaster.weimusic.constant.Constants.Theme_Cover_Type;
import static com.demomaster.weimusic.constant.Constants.Theme_WallPager_Type;
import static com.demomaster.weimusic.constant.Constants.Theme_Welcome_Type;
import static com.demomaster.weimusic.constant.Constants.Theme_Welcome_Type_Custom;
import static com.demomaster.weimusic.constant.Constants.Theme_Welcome_Type_System;
import static com.demomaster.weimusic.constant.ThemeConstants.WallPagerType.customPicture;
import static com.demomaster.weimusic.constant.ThemeConstants.WallPagerType.withMusic;
import static com.demomaster.weimusic.constant.ThemeConstants.WallPagerType.withSystem;

/**
 * Created by huan on 2018/1/27.
 */

public class ThemeUtil {



    /**
     * 改变SVG图片着色
     * @param imageView
     * @param iconResId svg资源id
     * @param color 期望的着色
     */
    public void changeSVGColor(Context mContext, ImageView imageView, int iconResId, int color){
        //AppCompatDrawableManager.get().getDrawable(this,0);
        Drawable drawable = ContextCompat.getDrawable(mContext, iconResId);
       // Drawable drawable =  AppCompatDrawableManager.get().getDrawable(mContext, iconResId);
        imageView.setImageDrawable(drawable);
        Drawable.ConstantState state = drawable.getConstantState();
        Drawable drawable1 = DrawableCompat.wrap(state == null ? drawable : state.newDrawable()).mutate();
        drawable1.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        DrawableCompat.setTint(drawable1, ContextCompat.getColor(mContext, color));
        imageView.setImageDrawable(drawable1);
    }

    //封面
    public static void setCover(ThemeConstants.CoverType type, Object data) {
        QDSharedPreferences.getInstance().putInt(Theme_Cover_Type, type.value());
        if(type==ThemeConstants.CoverType.withSystem){
            QDSharedPreferences.getInstance().putInt(Key_Theme_Cover_System, (int) data);
        }else  if(type==ThemeConstants.CoverType.customPicture){
            QDSharedPreferences.getInstance().putString(Key_Theme_Cover_Custom, (String) data);
        }else if(type ==ThemeConstants.CoverType.withMusic){
            if((boolean)data){
                QDSharedPreferences.getInstance().putInt(Theme_WallPager_Type, type.value());
            }else {
                String customerPath = QDSharedPreferences.getInstance().getString(Key_Theme_Cover_Custom, null);
                if(customerPath!=null){
                    QDSharedPreferences.getInstance().putInt(Theme_Cover_Type, customPicture.value());
                }else {
                    QDSharedPreferences.getInstance().putInt(Theme_Cover_Type, withSystem.value());
                }
            }
        }
        EventBus.getDefault().post(new EventMessage(AudioStation.ThemeCoverChange.value()));
    }

    //音乐封面
    /*public static void setCoverWithMusic(boolean on) {
        QDSharedPreferences.getInstance().putBoolean(Theme_Cover_Type_ByMusic, on);
    }
    //音乐封面
    public static boolean isCoverWithMusic() {
        return QDSharedPreferences.getInstance().getBoolean(Theme_Cover_Type_ByMusic, false);
    }*/
    //音乐封面
    public static ThemeConstants.CoverType getCoverType() {
        return ThemeConstants.CoverType.valueOf(QDSharedPreferences.getInstance().getInt(Theme_Cover_Type, withSystem.value()));
    }
   /* //音乐壁纸
    public static void setWallPaperWithMusic(boolean on) {
        QDSharedPreferences.getInstance().putBoolean(Theme_WallPager_With_Music, on);
    }
    //音乐壁纸
    public static boolean isWallPaperWithMusic() {
       return QDSharedPreferences.getInstance().getBoolean(Theme_WallPager_With_Music, false);
    }*/

    public static ThemeConstants.WallPagerType getWallPagerType() {
        return ThemeConstants.WallPagerType.valueOf(QDSharedPreferences.getInstance().getInt(Theme_WallPager_Type, withSystem.value()));
    }

    //壁纸
    public static void changeWallPaper(ThemeConstants.WallPagerType type, Object data) {
        QDSharedPreferences.getInstance().putInt(Theme_WallPager_Type, type.value());
        if(type ==customPicture){
            QDSharedPreferences.getInstance().putString(Key_Theme_WallPager_Custom, (String) data);
        }else  if(type ==withSystem){
            QDSharedPreferences.getInstance().putInt(Key_Theme_WallPager_System, (Integer) data);
        }else if(type ==withMusic){
            if((boolean)data){
                QDSharedPreferences.getInstance().putInt(Theme_WallPager_Type, type.value());
            }else {
                String customerPath = QDSharedPreferences.getInstance().getString(Key_Theme_WallPager_Custom, null);
                if(customerPath!=null){
                    QDSharedPreferences.getInstance().putInt(Theme_WallPager_Type, customPicture.value());
                }else {
                    QDSharedPreferences.getInstance().putInt(Theme_WallPager_Type, withSystem.value());
                }
            }
           // QDSharedPreferences.getInstance().putInt(Key_Theme_WallPager_System, (Integer) data);
        }
        EventBus.getDefault().post(new EventMessage(AudioStation.ThemeWallPagerChange.value()));
    }

    //欢迎页
    public static void changeWelcome(Context context, int type, Object data) {
        switch (type) {
            case Theme_Welcome_Type_Custom:
                QDSharedPreferences.getInstance().putString(Key_Theme_Welcome_Custom, (String) data);
                break;
            case Theme_Welcome_Type_System:
                QDSharedPreferences.getInstance().putInt(Key_Theme_Welcome_System, (int) data);
                break;
        }
        QDSharedPreferences.getInstance().putInt(Theme_Welcome_Type, type);
        Intent intent = new Intent();
        intent.setAction(Constants.Action_Theme_Welcome);
        context.sendBroadcast(intent);//发送普通广播
    }

    public static Drawable getWallPagerDrawable(Context context) {
        int type = QDSharedPreferences.getInstance().getInt(Theme_WallPager_Type, 0);
        Drawable drawable = null;
        if(type==customPicture.value()) {
            String path = QDSharedPreferences.getInstance().getString(Key_Theme_WallPager_Custom, null);
            if (path != null)
                try {
                    Bitmap bitmap1 = QDBitmapUtil.getBitmapFromPath(path);
                    drawable = new BitmapDrawable(bitmap1);
                    //fis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }else if(type==withSystem.value()){
            int resID = QDSharedPreferences.getInstance().getInt(Key_Theme_WallPager_System, 0);
            if (resID == 0) {
                drawable = context.getResources().getDrawable(R.drawable.background_wall_001);
            } else {
                drawable = context.getResources().getDrawable(resID);
            }
        }
        return drawable;
    }

    public static Drawable getWelComeDrawable(Context context) {
        int type = QDSharedPreferences.getInstance().getInt(Theme_Welcome_Type, 0);
        Drawable drawable = null;
        switch (type) {
            case Theme_Welcome_Type_Custom:
                String path = QDSharedPreferences.getInstance().getString(Key_Theme_Welcome_Custom, null);
                if (path != null)
                    try {
                        Bitmap bitmap1 = QDBitmapUtil.getBitmapFromPath(path);
                        drawable = new BitmapDrawable(bitmap1);
                        //fis.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                break;
            case Theme_Welcome_Type_System:
                int resID = QDSharedPreferences.getInstance().getInt(Key_Theme_Welcome_System, 0);
                if (resID == 0) {
                    drawable = context.getResources().getDrawable(R.drawable.bg_001);
                } else {
                    drawable = context.getResources().getDrawable(resID);
                }
                break;
        }

        return drawable;
    }

    public static void changeFont(Integer integer) {
        QDSharedPreferences.getInstance().putInt(Key_Theme_Font_System, integer);

    }

    public static int getThemeResId() {
        return QDSharedPreferences.getInstance().getInt(Key_Theme_Font_System, 0);
    }
}
