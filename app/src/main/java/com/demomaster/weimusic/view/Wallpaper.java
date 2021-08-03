package com.demomaster.weimusic.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.constant.ThemeConstants;
import com.demomaster.weimusic.player.service.MC;
import com.demomaster.weimusic.player.service.MusicDataManager;
import com.demomaster.weimusic.util.ThemeUtil;

import cn.demomaster.huan.quickdeveloplibrary.helper.QDSharedPreferences;
import cn.demomaster.huan.quickdeveloplibrary.util.QDBitmapUtil;
import cn.demomaster.huan.quickdeveloplibrary.view.drawable.QDRoundButtonDrawable;
import cn.demomaster.huan.quickdeveloplibrary.widget.button.QDButton;
import cn.demomaster.qdlogger_library.QDLogger;

import static com.demomaster.weimusic.constant.Constants.Key_Theme_WallPager_Custom;
import static com.demomaster.weimusic.constant.Constants.Key_Theme_WallPager_System;

public class Wallpaper extends AppCompatImageView {
    public Wallpaper(@NonNull Context context) {
        super(context);
        init();
    }

    public Wallpaper(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Wallpaper(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setScaleType(ScaleType.FIT_XY);
    }

    int wallPaperWidth;
    int wallPaperHeight;

    int screen_count = 3;//屏幕数 默认3屏显示
    float offset_width_percent = 0.2f;//每一屏幕偏移比
    float progress;

    @Override
    protected void onDraw(Canvas canvas) {
        float offset_dx = getWidth() * offset_width_percent * progress;// 得到在x轴的移动距离
        //QDLogger.println("onDraw 得到在x轴的移动距离"+offset_dx);
        Matrix matrix = new Matrix();// 在没有进行移动之前的位置基础上进行移动
        //matrix.postScale(scale, scale);
        matrix.postTranslate(-offset_dx, 0);
        canvas.setMatrix(matrix);
           /* Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.RED);
            canvas.drawRect(new Rect(0,0,bitmap.getWidth(),bitmap.getHeight()),paint);*/
        //setImageMatrix(matrix);
        //  QDLogger.e("getWidth()=" + getWidth() + ",getHeight()=" + getHeight() + ",scale=" + scale);
        //canvas.drawBitmap(bitmap,0,0,new Paint());
        if (getDrawable() == null) {
            QDLogger.println("getDrawable=null");
            super.onDraw(canvas);
            return;
        }

        if (getDrawable().getIntrinsicWidth() == 0 || getDrawable().getIntrinsicHeight() == 0) {
            QDLogger.println("getIntrinsicWidth=" + getDrawable().getIntrinsicWidth() + ",getIntrinsicHeight=" + getDrawable().getIntrinsicHeight());
            return;
        }

        if (getImageMatrix() != null && getPaddingTop() == 0 && getPaddingLeft() == 0) {
            //QDLogger.e("getDrawable().draw(canvas) w=" + getDrawable().getIntrinsicWidth() + ",h=" + getDrawable().getIntrinsicHeight());
            if (bitmap != null && bitmap.getWidth() != 0 && bitmap.getHeight() != 0) {
                Rect rect_src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
                Rect rect_target = new Rect(0, 0, wallPaperWidth, getMeasuredHeight());
                canvas.drawBitmap(bitmap, rect_src, rect_target, new Paint());
            } else {
                canvas.setMatrix(new Matrix());
                super.onDraw(canvas);
            }
        }
        //QDRoundButtonDrawable bg_normal = QDRoundButtonDrawable.fromAttributeSet(context, attrs, defStyleAttr);
        LinearGradient mShader = new LinearGradient(0, 0, 0, getHeight(), new int[]{0x33000000, 0x33000000}, null, Shader.TileMode.REPEAT);
        Paint paint = new Paint();
        paint.setShader(mShader);
        canvas.drawRect(new Rect(0, 0, wallPaperWidth, getHeight()), paint);
    }

    private Bitmap dealBitamp() {
        //获取当前需要的尺寸
        wallPaperWidth = (int) (getMeasuredWidth() * (1 + (screen_count - 1) * offset_width_percent));
        wallPaperHeight = getMeasuredHeight();
        if (bitmap == null) {
            return null;
        }
        //QDLogger.println("width=" + wallPaperWidth + "，height=" + wallPaperHeight);
        //获取原图
        //生成新的图片
        //模糊背景
        long t1 = System.currentTimeMillis();
        //QDLogger.println("bitmap w1=：" + bitmap.getWidth());
        bitmap = QDBitmapUtil.zoomImage(bitmap, Math.min(bitmap.getWidth() / 2, wallPaperWidth / 3), Math.min(bitmap.getHeight() / 2, wallPaperHeight / 3));
        //QDLogger.println("bitmap w2=：" + bitmap.getWidth());
        bitmap = cn.demomaster.huan.quickdeveloplibrary.util.BlurUtil.doBlur(bitmap, 50, 0.2f);
        long t2 = System.currentTimeMillis();
        //QDLogger.println("bitmap1：w=" + bitmap.getWidth() + ",h=" + bitmap.getHeight());
        QDLogger.println("模糊用时：" + (t2 - t1));
        bitmap = QDBitmapUtil.zoomImage(bitmap, wallPaperWidth, wallPaperHeight);
        return bitmap;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private Bitmap blur(Bitmap bkg, View view, float radius) {
        Bitmap overlay = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.drawBitmap(bkg, -view.getLeft(), -view.getTop(), null);
        RenderScript rs = RenderScript.create(getContext());
        Allocation overlayAlloc = Allocation.createFromBitmap(rs, overlay);
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, overlayAlloc.getElement());
        blur.setInput(overlayAlloc);
        blur.setRadius(radius);
        blur.forEach(overlayAlloc);
        overlayAlloc.copyTo(overlay);
        view.setBackground(new BitmapDrawable(getResources(), overlay));
        rs.destroy();
        return bitmap;
    }

    public void scrollWallPaper(float p) {
        progress = p;
        //QDLogger.e("scrollWallPaper p="+p);
        postInvalidate();
    }

    //boolean imageChanged;

    @Override
    public void setImageResource(int resId) {
        //imageChanged = true;
        super.setImageResource(resId);
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        //imageChanged = true;
        //图片渐变对象
        TransitionDrawable imageTransitionDrawable = null;
        if (drawable != null && getDrawable() != null) {
            imageTransitionDrawable = new TransitionDrawable(
                    new Drawable[]{
                            getDrawable(),
                            drawable
                    }
            );
//设置背景图片为渐变图片
            super.setImageDrawable(imageTransitionDrawable);
//经过1000ms的图片渐变过程
            imageTransitionDrawable.startTransition(1000);
        } else {
            super.setImageDrawable(drawable);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        wallPaperWidth = (int) (getWidth() * (1 + (screen_count - 1) * offset_width_percent));
        wallPaperHeight = getHeight();
        updateDrawable();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //updateDrawable();
    }

    Bitmap bitmap;

    public void updateDrawable() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = QDSharedPreferences.getInstance().getString(Key_Theme_WallPager_Custom, null);
                int resID = QDSharedPreferences.getInstance().getInt(Key_Theme_WallPager_System, 0);
                Drawable drawable = null;
                int drawableId = R.drawable.background_wall_001;
                ThemeConstants.WallPagerType type = ThemeUtil.getWallPagerType();
                switch (type) {
                    case withMusic://壁紙跟随音乐专辑封面
                        bitmap = MusicDataManager.getInstance().getAlbumPicture(getContext(), MC.getInstance(getContext()).getCurrentInfo(), true);
                        if (bitmap != null) {
                            dealBitamp();
                            drawable = new BitmapDrawable(bitmap);
                        }
                        break;
                    case customPicture://使用自定义壁纸
                        if (path != null) {
                            try {
                                //long t1 = System.currentTimeMillis();
                                bitmap = QDBitmapUtil.getBitmapFromPath(path);
                                //QDLogger.println("time3=" + (System.currentTimeMillis() - t1));
                                // dealBitamp();
                                drawable = new BitmapDrawable(bitmap);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case withSystem://使用系统壁纸
                        if (resID != 0) {
                            drawableId = resID;
                        }
                        bitmap = null;// BitmapFactory.decodeResource(getContext().getResources(),drawableId);
                        drawable = (getContext().getResources().getDrawable(drawableId));
                        break;
                }

                if (drawable == null) {
                    if (path != null) {//如果找不到壁纸， 使用自定义壁纸
                        try {
                            bitmap = QDBitmapUtil.getBitmapFromPath(path);
                            drawable = new BitmapDrawable(bitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (drawable == null) {//如果找不到壁纸， 使用系统壁纸
                        if (resID != 0) {
                            drawableId = resID;
                        }
                        bitmap = null;//BitmapFactory.decodeResource(getContext().getResources(), drawableId);
                        drawable = getContext().getResources().getDrawable(drawableId);
                    }
                }
                Drawable drawable1 = drawable;
                //postInvalidate();
                post(new Runnable() {
                    @Override
                    public void run() {
                        setImageDrawable(drawable1);
                    }
                });
            }
        }).start();
    }
}
