package com.demomaster.weimusic.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import com.demomaster.weimusic.BuildConfig;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import cn.demomaster.huan.quickdeveloplibrary.util.QDBitmapUtil;
import cn.demomaster.qdlogger_library.QDLogger;

public class ImageUtils {

    /**
     * Save image to the SD card
     *
     * @param photoBitmap
     * @param photoName
     * @param path
     */
    public static String savePhoto(Bitmap photoBitmap, String path,
                                   String photoName) {
        String localPath = null;
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File photoFile = new File(path, photoName + ".png");
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(photoFile);
                if (photoBitmap != null) {
                    if (photoBitmap.compress(CompressFormat.PNG, 100,
                            fileOutputStream)) { // ת�����
                        localPath = photoFile.getPath();
                        fileOutputStream.flush();
                    }
                }
            } catch (FileNotFoundException e) {
                photoFile.delete();
                localPath = null;
                e.printStackTrace();
            } catch (IOException e) {
                photoFile.delete();
                localPath = null;
                e.printStackTrace();
            } finally {
                try {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                        fileOutputStream = null;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return localPath;
    }

    /**
     * getRoundBiemap
     *
     * @param bitmap  ����Bitmap����
     * @param tempUri
     * @return
     */
    public static Bitmap toRoundBitmap(Bitmap bitmap, Uri tempUri) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            left = 0;
            top = 0;
            right = width;
            bottom = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right,
                (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top,
                (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst);

        paint.setAntiAlias(true);// ���û����޾��

        canvas.drawARGB(0, 0, 0, 0); // �������Canvas
        paint.setColor(color);

        // ���������ַ�����Բ,drawRounRect��drawCircle
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);//
        // ��Բ�Ǿ��Σ���һ������Ϊͼ����ʾ���򣬵ڶ��������͵����������ֱ���ˮƽԲ�ǰ뾶�ʹ�ֱԲ�ǰ뾶��
        canvas.drawCircle(roundPx, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));// ��������ͼƬ�ཻʱ��ģʽ,�ο�http://trylovecatch.iteye.com/blog/1189452
        canvas.drawBitmap(bitmap, src, dst, paint); // ��Mode.SRC_INģʽ�ϲ�bitmap���Ѿ�draw�˵�Circle
        return output;
    }

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

    /**
     * 图片裁剪功能
     *
     * @param context
     * @param srcUri      源路径
     * @param targetUri   输出路径
     * @param requestCode
     */
    public static void startPhotoZoom1(Activity context, Uri srcUri, Uri targetUri,
                                      int requestCode) {
        int dp = 800;
        Intent intent = new Intent("com.android.camera.action.CROP");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", new File(uri.getPath()));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        intent.setDataAndType(srcUri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);// 去黑边
        intent.putExtra("scaleUpIfNeeded", true);// 去黑边
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);//输出是X方向的比例
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高，切忌不要再改动下列数字，会卡死
        intent.putExtra("outputX", dp);//输出X方向的像素
        intent.putExtra("outputY", dp);
        intent.putExtra("outputFormat", CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        if (targetUri != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, targetUri);
        } else {
            if (Build.VERSION.SDK_INT < 30) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, targetUri);
            } else {
                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath();
                //storage/emulated/0/Pictures
                File mOnputFile = new File(path, System.currentTimeMillis() + ".png");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.parse("file://" + mOnputFile.getAbsolutePath()));
            }
        }
        intent.putExtra("return-data", false);//如果此处指定，返回值的data为null
        context.startActivityForResult(intent, requestCode);
    }

    /**
     * 通过Uir获取bitmap
     *
     * @param uri
     * @param mContext
     * @return
     */
    public static Bitmap getBitmapFromUri(Uri uri, Context mContext) {
        try {
            // 读取uri所在的图片
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
