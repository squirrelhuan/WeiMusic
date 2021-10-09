package com.demomaster.weimusic.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageUtils {

    /**
     * 照片水印处理
     * @author Administrator
     */
    public static class PhotoTask extends Thread {
        private String file;
        private Drawable drawable = null;

        private boolean isFinished;

        public PhotoTask(String file,/* String newfile, */Drawable drawable) {
            this.file = file;
            this.drawable = drawable;
        }

        @Override
        public void run() {
            BufferedOutputStream bos = null;
            Bitmap icon = null;
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(file, options); //此时返回bm为空
                float percent = options.outHeight > options.outWidth ? options.outHeight / 960f : options.outWidth / 960f;

                if (percent < 1) {
                    percent = 1;
                }
                int width = (int) (options.outWidth / percent);
                int height = (int) (options.outHeight / percent);
                icon = Bitmap.createBitmap(width, height, Config.RGB_565);

                //初始化画布 绘制的图像到icon上
                Canvas canvas = new Canvas(icon);
                //建立画笔
                Paint photoPaint = new Paint();
                //获取跟清晰的图像采样
                photoPaint.setDither(true);
                //过滤一些
                //                    photoPaint.setFilterBitmap(true);
                options.inJustDecodeBounds = false;

                Bitmap prePhoto = BitmapFactory.decodeFile(file);
                if (percent > 1) {
                    prePhoto = Bitmap.createScaledBitmap(prePhoto, width, height, true);
                }

                canvas.drawBitmap(prePhoto, 0, 0, photoPaint);

                if (prePhoto != null && !prePhoto.isRecycled()) {
                    prePhoto.recycle();
                    prePhoto = null;
                    System.gc();
                }

                //设置画笔
                Paint textPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
                //字体大小
                textPaint1.setTextSize(30.0f);
                //采用默认的宽度
                textPaint1.setTypeface(Typeface.DEFAULT);
                //采用的颜色
                textPaint1.setColor(Color.WHITE);
                //阴影设置
                textPaint1.setShadowLayer(3f, 1, 1, Color.DKGRAY);

                // 地点水印
                String mark = "";
                float textWidth = textPaint1.measureText(mark);
                //textPaint.setShadowLayer(3f, 1, 1,getResources().getColor(R.color.gray2));//的设置
                canvas.drawText(mark, width - textWidth - 10, height - 60, textPaint1);

                //设置画笔
                Paint textPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
                //字体大小
                textPaint2.setTextSize(30.0f);
                //采用默认的宽度
                textPaint2.setTypeface(Typeface.DEFAULT);
                //采用的颜色
                textPaint2.setColor(Color.WHITE);
                //阴影设置
                textPaint2.setShadowLayer(3f, 1, 1, Color.DKGRAY);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                // 时间水印
                String mark2 = simpleDateFormat.format(new Date());
                float textWidth2 = textPaint2.measureText(mark2);
                //textPaint.setShadowLayer(3f, 1, 1,getResources().getColor(R.color.gray2));//的设置
                canvas.drawText(mark2, width - textWidth2 - 10, height - 26, textPaint2);

                if (drawable != null) {
                    //图片水印
                    //构建Paint时直接加上去锯齿属性
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
                    paint.setXfermode(new Xfermode());
                    Matrix max = new Matrix();
                    BitmapDrawable bDrawable = (BitmapDrawable) drawable;
                    //canvas.drawBitmap(bDrawable.getBitmap(),max,paint);
                    canvas.drawBitmap(bDrawable.getBitmap(), width - textWidth - bDrawable.getBitmap().getWidth() - 10, height
                            - bDrawable.getBitmap().getHeight() - 26, paint);
                }
                bos = new BufferedOutputStream(new FileOutputStream(file));
                //压缩
                int quaility = (int) (100 / percent > 80 ? 80 : 100 / percent);
                icon.compress(CompressFormat.JPEG, quaility, bos);
                bos.flush();
                //String filename = file.split("/")[file.split("/").length - 1];
                //FileUtils.fileCopyAndDeleteOld(file, Constants.APP_PATH_PICTURE + "/" + filename);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isFinished = true;
                if (bos != null) {
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (icon != null && !icon.isRecycled()) {
                    icon.recycle();
                    icon = null;
                    System.gc();
                }
            }
        }
    }

}
