package com.demomaster.weimusic.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.demomaster.weimusic.SystemSetting;
import com.demomaster.weimusic.constant.Constants;
import com.demomaster.weimusic.constant.ThemeConstants;
import com.demomaster.weimusic.model.AudioInfo;
import com.demomaster.weimusic.player.service.MC;
import com.demomaster.weimusic.player.service.MusicDataManager;
import com.demomaster.weimusic.util.ThemeUtil;

import cn.demomaster.huan.quickdeveloplibrary.animation.QDValueAnimator;
import cn.demomaster.huan.quickdeveloplibrary.helper.QDSharedPreferences;
import cn.demomaster.huan.quickdeveloplibrary.util.QDBitmapUtil;
import cn.demomaster.qdlogger_library.QDLogger;

import static cn.demomaster.huan.quickdeveloplibrary.util.QDBitmapUtil.getBitmapFromPath;


/**
 * Created by huan on 2018/1/20.
 */
public class AudioPlayerView extends View {
    public ThemeConstants.CoverType coverType;
    QDValueAnimator anim;
    float rotionProgress;
    OnAnimationListener onAnimationListener;
    OnClickActionListener onClickActionListener;
    /***********************************      切换动画              ************************************************************/
    QDValueAnimator animChangeSong;
    float changeSongProgress;
    AudioPlayerBar.OnPlayerStateChangeListener onPlayerStateChangeListener;
    AudioPlayerBar.ActionType actionType = AudioPlayerBar.ActionType.play;
    ActionEnum actionEnum = ActionEnum.isPaused;
    private PointF pointf_center = new PointF();//圆心
    private float radius = 0f;//半径
    //半透明边框
    private Paint paint_boun = new Paint();
    private int paint_boun_color = 0x33000000;
    private float paint_boun_width = 13 / 380f;
    //黑色圆环
    private Paint paint_ring = new Paint();
    private int paint_ring_color = Color.BLACK;
    private float paint_ring_width = 100f;
    //黑色圆环
    private Paint paint_ring_mask = new Paint();
    private int paint_ring_mask_color = Color.BLACK;
    private float paint_ring_mask_width = 100f;
    //红色圆心
    private Paint paint_circle_center = new Paint();
    private int paint_circle_center_color = Color.RED;
    private float paint_circle_center_width = 13 / 38f;
    //红色圆心边界阴影
    private Paint paint_circle_center_boun = new Paint();
    private int paint_circle_center_boun_color = Color.BLACK;
    private float paint_circle_center_boun_width = 150f;
    private int colorStyle = 0;
    private String bitmapPath;
    private boolean isClicked;

    public AudioPlayerView(Context context) {
        super(context);
    }

    public AudioPlayerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AudioPlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        initSize();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //init();
    }

    public int getContenWidth() {
        return getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
    }

    public int getContenHeight() {
        return getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
    }

    public void initSize() {
        int contentWidth = getContenWidth();
        int paddingLeft = getPaddingLeft();
        int centerX = (int) (contentWidth / 2f + paddingLeft);
        int centerY = (int) (getContenHeight() / 2f + getPaddingTop());
        pointf_center = new PointF(centerX, centerY);
        radius = Math.min(getContenWidth(), getContenHeight()) / 2f;
       // QDLogger.e("getMeasuredWidth=" + getMeasuredWidth() + ",getMeasuredHeight=" + getMeasuredHeight() +",centerX=" + centerX + ",centerY=" + centerY);
    }

    /**
     * 设置转盘颜色
     *
     * @param colorStyle
     */
    public void setColorStyle(int colorStyle) {
        this.bitmapPath = null;
        this.colorStyle = colorStyle;
        this.paint_circle_center_color = colorStyle;
        this.coverType = ThemeConstants.CoverType.withSystem;
        postInvalidate();
    }

    /**
     * 设置转盘图片
     *
     * @param bitmapPath
     */
    public void setBitmapPath(String bitmapPath) {
        this.colorStyle = 0;
        this.bitmapPath = bitmapPath;
        this.coverType = ThemeConstants.CoverType.customPicture;
        postInvalidate();
    }

    public void init() {
        //initSize();
        if (colorStyle != 0) {
            this.paint_circle_center_color = colorStyle;
            return;
        }
        if (bitmapPath != null) {
            setAudioBitmap(getBitmapFromPath(bitmapPath));
            return;
        }
        reSeat();
    }

    @Override
    protected void onDraw(Canvas canvas1) {
        viewBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(viewBitmap);
        Paint paint = new Paint();
        //paint.setColor(Color.YELLOW);
        //canvas.drawRect(new Rect(0,0,getMeasuredWidth(),getMeasuredHeight()),paint);
        float scale = 1;
        if (actionEnum == ActionEnum.isChangeToNext || actionEnum == ActionEnum.isChangeToPrev) {
            scale = 0.6f + changeSongProgress * 0.4f;
        }

        //绘制唱盘
        if (Build.VERSION.SDK_INT > 23) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotionProgress, pointf_center.x, pointf_center.y);
            //matrix.postScale(scale,scale);
            canvas.setMatrix(matrix);
        } else {
            canvas.rotate(rotionProgress, pointf_center.x, pointf_center.y);
        }
        drawCover(canvas, scale, actionEnum,0);

        //绘制切换cd 动画
        if (actionEnum == ActionEnum.isChangeToNext || actionEnum == ActionEnum.isChangeToPrev) {
            float dy = -changeSongProgress * getHeight();
            //绘制唱盘
            if (Build.VERSION.SDK_INT > 23) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotionProgress, pointf_center.x, pointf_center.y);
                matrix.postTranslate(0, dy);
                canvas.setMatrix(matrix);
            } else {
                canvas.rotate(rotionProgress, pointf_center.x, pointf_center.y);
                canvas.restore();
                canvas.translate(0, dy);
            }
            float scale2 = 1f - changeSongProgress * 0.4f;
            drawCover(canvas, scale2, actionEnum,1);
        }
        canvas1.drawBitmap(viewBitmap, 0, 0, paint);
        super.onDraw(canvas1);
    }

    Bitmap viewBitmap;
    Bitmap audioBitmap;//自定义的图片
    Bitmap fromCoverBitmap;//上一曲封面图片
    Bitmap toCoverBitmap;//上一曲封面图片
    /**
     * 绘制唱盘
     * @param canvas
     * @param scale
     * @param audionIndex -1是上一首歌曲的封面 0 是当前 1是下一曲
     */
    public void drawCover(Canvas canvas, float scale, ActionEnum actionEnum,int audionIndex) {
        Bitmap bitmap = null;
       /* if(!isPreviewMode) {
            coverType =
        }*/
        if (coverType == ThemeConstants.CoverType.withSystem ) {
            bitmap = generateCDBitmap(null);
        } else if (coverType == ThemeConstants.CoverType.customPicture) {
            if(isPreviewMode) {
                bitmap = getBitmapFromPath(bitmapPath);
            }else {
                bitmap = getCustomBitmap();
            }
        } else {
            if (actionEnum == ActionEnum.isChangeToNext) {
                if (audionIndex == 0) {
                    if (toCoverBitmap == null || toCoverBitmap.isRecycled()) {
                        AudioInfo audioInfo = MusicDataManager.getInstance(getContext()).getMusicInfoById(getContext(), playAudioId);
                        toCoverBitmap = MusicDataManager.getInstance(getContext()).getAlbumPicture(getContext(), audioInfo);
                    }
                    bitmap = toCoverBitmap;
                } else {
                    if (fromCoverBitmap == null || fromCoverBitmap.isRecycled()) {
                        AudioInfo audioInfo = MusicDataManager.getInstance(getContext()).getMusicInfoById(getContext(), lastAudioId);
                        fromCoverBitmap = MusicDataManager.getInstance(getContext()).getAlbumPicture(getContext(), audioInfo);
                    }
                    bitmap = fromCoverBitmap;
                }
            } else if (actionEnum == ActionEnum.isChangeToPrev) {
                if (audionIndex == 0) {
                    if (toCoverBitmap == null || toCoverBitmap.isRecycled()) {
                        AudioInfo audioInfo = MusicDataManager.getInstance(getContext()).getMusicInfoById(getContext(), lastAudioId);
                        toCoverBitmap = MusicDataManager.getInstance(getContext()).getAlbumPicture(getContext(), audioInfo);
                    }
                    bitmap = toCoverBitmap;
                } else {
                    if (fromCoverBitmap == null || fromCoverBitmap.isRecycled()) {
                        AudioInfo audioInfo = MusicDataManager.getInstance(getContext()).getMusicInfoById(getContext(), playAudioId);
                        fromCoverBitmap = MusicDataManager.getInstance(getContext()).getAlbumPicture(getContext(), audioInfo);
                    }
                    bitmap = fromCoverBitmap;
                }
            } else {
                fromCoverBitmap = null;
                toCoverBitmap = null;
                if (audioBitmap == null || audioBitmap.isRecycled()) {
                    AudioInfo audioInfo = MusicDataManager.getInstance(getContext()).getMusicInfoById(getContext(), playAudioId);
                    audioBitmap = MusicDataManager.getInstance(getContext()).getAlbumPicture(getContext(), audioInfo);
                }
                bitmap = audioBitmap;
            }
            if(SystemSetting.getCoverStytle()==1&&bitmap!=null){
                bitmap = generateCDBitmap(bitmap);
            }
        }
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = generateCDBitmap(null);
        }
        bitmap = dealBitmap(bitmap);
       /* Path path = new Path();
        //按照逆时针方向添加一个圆
        path.addCircle(pointf_center.x, pointf_center.y, (radius_tmp - 10) * scale, Path.Direction.CCW);
        //设置为在圆形区域内绘制
        canvas.clipPath(path);*/
        //canvas.drawBitmap(bitmap, pointf_center.x - radius_tmp, pointf_center.y - radius_tmp, new Paint());
        if (bitmap == null) {
            return;
        }
        int left = 0;
        int top = 0;
        int right = bitmap.getWidth();
        int bottom = bitmap.getHeight();
        Rect srcRect = new Rect(left, top, right, bottom);

        int left2 = (int) (pointf_center.x - radius * scale);
        int top2 = (int) (pointf_center.y - radius * scale);
        int right2 = (int) (pointf_center.x + radius * scale);
        int bottom2 = (int) (pointf_center.y + radius * scale);
        //  QDLogger.e("scale="+scale+",left2="+left2+",top2="+top2+",right2="+right2+",bottom2="+bottom2);
        Rect dstRect = new Rect(left2, top2, right2, bottom2);
        /*Paint paint = new Paint();
        paint.setColor(Color.RED);
        canvas.drawRect(dstRect,paint);*/
        canvas.drawBitmap(bitmap, srcRect, dstRect, new Paint());
    }

    private Bitmap generateCDBitmap(Bitmap bitmap1) {
        float radius_cd = radius;
        int width = (int) radius_cd * 2;
        int height = width;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas1 = new Canvas(bitmap);
        // QDLogger.i("drawCover=" + 1 + ",coverType=" + coverType);
        LinearGradient mShader = new LinearGradient(0, 0, width, height, new int[]{
                0x77000000, 0x77000000, 0x44ffffff, 0x77000000, 0x77000000}, new float[]{0, 0.3f, 0.5f, 0.7f, 1.0f},
                Shader.TileMode.MIRROR);
        // Shader.TileMode三种模式
        // REPEAT:沿着渐变方向循环重复
        // CLAMP:如果在预先定义的范围外画的话，就重复边界的颜色
        // MIRROR:与REPEAT一样都是循环重复，但这个会对称重复
        Paint pain = new Paint();
        pain.setShader(mShader);// 用Shader中定义定义的颜色来话

        //绘制边缘
        paint_boun.setAntiAlias(true);
        paint_boun.setColor(paint_boun_color);
        paint_boun.setStyle(Paint.Style.FILL);  //设置为
        canvas1.drawCircle(width / 2, height / 2, radius_cd, paint_boun);

        //绘制黑色环带
        paint_ring.setAntiAlias(true);
        paint_ring.setColor(paint_ring_color);
        paint_ring.setStyle(Paint.Style.FILL);//设置为
        canvas1.drawCircle(width / 2, height / 2, radius_cd - paint_boun_width * radius_cd, paint_ring);

        //绘制黑色圆环上的光影效果
        paint_ring_mask.setAntiAlias(true);
        paint_ring_mask.setShader(mShader);
        paint_ring_mask.setStyle(Paint.Style.FILL); //设置为
        canvas1.drawCircle(width / 2, height / 2, radius_cd - paint_boun_width * radius_cd, paint_ring_mask);

        QDLogger.i(coverType+"-bitmap1=" + (bitmap1==null?"null:"+(SystemSetting.getCoverStytle()):(""+bitmap1.isRecycled())));
        if(SystemSetting.getCoverStytle()==0||bitmap1==null) {
            //绘制红色圆心
            paint_circle_center.setAntiAlias(true);
            paint_circle_center.setColor(paint_circle_center_color);
            paint_circle_center.setStyle(Paint.Style.FILL); //设置为
            canvas1.drawCircle(width / 2, height / 2, paint_circle_center_width * radius_cd, paint_circle_center);
        }else {
            Path path = new Path();
            //按照逆时针方向添加一个圆
            path.addCircle(width / 2, height / 2, paint_circle_center_width * radius_cd,Path.Direction.CCW);
            canvas1.clipPath(path);
            //设置为在圆形区域内绘制
            float l = width/2-paint_circle_center_width * radius_cd;
            float t = height/2-paint_circle_center_width * radius_cd;
            float r = width/2+paint_circle_center_width * radius_cd;
            float b = height/2+paint_circle_center_width * radius_cd;
            Rect rect = new Rect(0,0,bitmap1.getWidth(),bitmap1.getHeight());
            Rect rect2 = new Rect((int) l,(int)t,(int)r,(int)b);
            canvas1.drawBitmap(bitmap1,rect,rect2,pain);
        }
        //绘制红色圆心的光影效果
        LinearGradient mShader2 = new LinearGradient(0, 0, getMeasuredWidth(), getMeasuredHeight(), new int[]{
                0x55000000, 0x22000000, 0x55aabbcc, 0x22000000, 0x55000000}, new float[]{0f, 0.3f, 0.5f, 0.7f, 1.0f},
                Shader.TileMode.REPEAT);
        // Shader.TileMode三种模式
        // REPEAT:沿着渐变方向循环重复
        // CLAMP:如果在预先定义的范围外画的话，就重复边界的颜色
        // MIRROR:与REPEAT一样都是循环重复，但这个会对称重复
        paint_circle_center_boun.setAntiAlias(true);
        paint_circle_center_boun.setShader(mShader2);
        paint_circle_center_boun.setStyle(Paint.Style.STROKE);//设置为
        paint_circle_center_boun.setStrokeWidth(20f);
        paint_circle_center_boun.setColor(paint_circle_center_boun_color);
        canvas1.drawCircle(width / 2, height / 2, paint_circle_center_width * radius_cd - 10, paint_circle_center_boun);
        return bitmap;
    }

    @Override
    public Bitmap getDrawingCache(boolean autoScale) {
        // return super.getDrawingCache(autoScale);
        return viewBitmap;
    }

    @Override
    public Bitmap getDrawingCache() {
        // return super.getDrawingCache();
        return viewBitmap;
    }

    /**
     * 获取自定义的图片
     *
     * @return
     */
    private Bitmap getCustomBitmap() {
        String path = QDSharedPreferences.getInstance().getString(Constants.Key_Theme_Cover_Custom, null);
        Bitmap bitmap1 = getBitmapFromPath(path);
        return bitmap1;
    }

    //处理图片
    public Bitmap dealBitmap(Bitmap bitmap) {
        if(bitmap==null){
            return null;
        }
        //图片裁剪成正方形
        Bitmap cropBitmap = QDBitmapUtil.cropBitmap(bitmap);
        cropBitmap = QDBitmapUtil.scaleBitmap(cropBitmap, (int) (radius * 2), (int) (radius * 2));
        //裁剪成圆形
        Bitmap bitmap1 = Bitmap.createBitmap(cropBitmap.getWidth(), cropBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap1);
        Path path = new Path();
        //按照逆时针方向添加一个圆
        path.addCircle(bitmap1.getWidth() / 2, bitmap1.getWidth() / 2, bitmap1.getWidth() / 2, Path.Direction.CCW);
        //设置为在圆形区域内绘制
        canvas.clipPath(path);
        canvas.drawBitmap(cropBitmap, 0, 0, new Paint());
        return bitmap1;
    }

    /**
     * 设置音频图片
     *
     * @param bitmap
     */
    public void setAudioBitmap(Bitmap bitmap) {
        toCoverBitmap = null;
        fromCoverBitmap = null;
        if (bitmap != null) {
            this.audioBitmap = dealBitmap(bitmap);
        } else {
            this.audioBitmap = null;
        }
        postInvalidate();
    }

    // 动画实际执行
    public void startAnimation() {
        //QDLogger.i("anim="+ anim + ",state=" +(anim==null?"null": anim.getState()));
        if (anim != null && anim.isRunning()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (!anim.isPaused()) {
                    //QDLogger.println("唱盘 正在转动");
                    return;
                }
            }
        }
        if (anim == null) {
            // 第二个参数"rotation"表明要执行旋转
            // 0f -> 360f，从旋转360度，也可以是负值，负值即为逆时针旋转，正值是顺时针旋转。
            //anim = ObjectAnimator.ofFloat(this, "rotation", 0f, 360f);
            anim = QDValueAnimator.ofFloat(0f, 360f);
            // 动画的持续时间，执行多久？
            anim.setDuration(20000);
            // 回调监听
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    rotionProgress = (float) valueAnimator.getAnimatedValue();
                    invalidate();
                    // QDLogger.i("rotionProgress="+rotionProgress + "," + actionEnum);
                }
            });
            anim.setRepeatCount(-1);
            anim.setInterpolator(new LinearInterpolator());
            anim.start();
            if (onAnimationListener != null) {
                onAnimationListener.onStart();
            }
        } else {
            try {
                //QDLogger.i("anim.isRunning()=" + anim.isRunning() + ",isstarted=" + anim.isStarted() + ",isPause=" + anim.isPaused());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && anim.isPaused()) {
                    anim.resume();
                } else {
                    anim.start();
                }
                if (onAnimationListener != null) {
                    onAnimationListener.onStart();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void pause(AudioPlayerBar.AnimationCall call) {
        try {
            if (anim != null && anim.isRunning()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    anim.pause();
                } else {
                    anim.cancel();
                    anim = null;
                }
                if (onAnimationListener != null) {
                    onAnimationListener.onStop();
                }
                //anim.setupStartValues();
                //anim.setFloatValues((Float) anim.getAnimatedValue() + 360);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

  /*  @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onClickActionListener != null) {

                }
                if (l != null)
                    l.onClick(view);
            }
        });
    }*/

    public void stop(AudioPlayerBar.AnimationCall call) {
        try {
            if (anim != null) {
                anim.cancel();
                if (onAnimationListener != null) {
                    onAnimationListener.onStop();
                }
                anim.setupStartValues();
                anim.setFloatValues((Float) anim.getAnimatedValue() + 360);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (anim != null)
            anim.cancel();
    }

    public void setOnAnimationListener(OnAnimationListener onAnimationListener) {
        this.onAnimationListener = onAnimationListener;
    }

    public void reSeat() {
        Bitmap bitmap1 = null;
        if(!isPreviewMode) {
            coverType = ThemeUtil.getCoverType();
            this.paint_circle_center_color = QDSharedPreferences.getInstance().getInt(Constants.Key_Theme_Cover_System, Color.RED);
        }
        if (coverType == ThemeConstants.CoverType.withMusic) {//跟随歌曲图片
            bitmap1 = MusicDataManager.getInstance(getContext()).getAlbumPicture(getContext(), MC.getInstance(getContext()).getCurrentInfo());
            //QDLogger.println("bitmap1=" + bitmap1);
        } else if (coverType == ThemeConstants.CoverType.withSystem) {

        } else if (coverType == ThemeConstants.CoverType.customPicture) {
            bitmap1 = getCustomBitmap();
        }
        setAudioBitmap(bitmap1);
    }

    long lastAudioId=0;//之前的音频
    long playAudioId=0;//要播放的音频
    public void changeAudio(long id1,long id2) {
        fromCoverBitmap = null;
        toCoverBitmap = null;
        lastAudioId = id1;
        playAudioId = id2;
        AudioInfo audioInfo = MusicDataManager.getInstance(getContext()).getMusicInfoById(getContext(), lastAudioId);
        AudioInfo audioInfo1 = MusicDataManager.getInstance(getContext()).getMusicInfoById(getContext(), playAudioId);
        QDLogger.i("changeAudio:"+audioInfo.title+"-"+audioInfo1.title);
    }

    public void setOnClickActionListener(OnClickActionListener onClickActionListener) {
        this.onClickActionListener = onClickActionListener;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {//按下
            float x = event.getX();
            float y = event.getY();
            //处理点击圆外部分不响应触摸
            float w = Math.abs(x - pointf_center.x);
            float h = Math.abs(y - pointf_center.y);
            if ((Math.pow(w, 2) + Math.pow(h, 2)) > Math.pow(radius, 2)) {
                QDLogger.println(" 点击了圆外(" + x + "," + y + ")");
                return false;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    float lastX = 0;
    float lastY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        float x = event.getX();
        float y = event.getY();
        //QDLogger.d("MotionEvent(" + x + "," + y + ")");
        if (event.getAction() == MotionEvent.ACTION_DOWN) {//按下
            isClicked = true;
            lastX = x;
            lastY = y;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {//移动
           /* if (event.getRawX() > 10 || event.getRawY() > 10) {
                QDLogger.d("event (" + event.getRawX() + "," + event.getRawY() + ")");
                isClicked = false;
            }*/
            if (Math.abs(lastX - event.getX()) > 10 || Math.abs(lastY - event.getY()) > 10) {
                QDLogger.println("event gawX=" + Math.abs(lastX - event.getX()) + ",gawY=" + Math.abs(lastY - event.getY()));
                isClicked = false;
            }
        } else if (event.getAction() == MotionEvent.ACTION_UP) { //抬起
            if (isClicked) {
                float l = getWidth() / 2 - paint_circle_center_width * radius;
                float t = getHeight() / 2 - paint_circle_center_width * radius;
                float r = getWidth() / 2 + paint_circle_center_width * radius;
                float b = getHeight() / 2 + paint_circle_center_width * radius;
                if (x >= l && x <= r && y >= t && y <= b) {
                    if (onClickActionListener != null) {
                        onClickActionListener.onCenterClick();
                    }
                } else {
                    if (onClickActionListener != null) {
                        onClickActionListener.onEdgeClick();
                    }
                }
            }
        }
        return true;
        //return super.onTouchEvent(event);
    }

    // 动画实际执行
    public void startChangeSong(AudioPlayerBar.AnimationCall call) {
        initChangeSongAnmation(call);
        animChangeSong.start();
    }

    private void initChangeSongAnmation(AudioPlayerBar.AnimationCall call) {
        if (animChangeSong == null) {
            animChangeSong = QDValueAnimator.ofFloat(0f, 1f);
            // 动画的持续时间，执行多久？
            animChangeSong.setDuration(360);
            // 回调监听
            animChangeSong.setAnimationListener(new QDValueAnimator.AnimationListener() {
                @Override
                public void onStartOpen(Object value) {
                }

                @Override
                public void onOpening(Object value) {
                    //QDLogger.i("onOpening changeSongProgress=" + value);
                    changeSongProgress = (float) value;
                    actionEnum = ActionEnum.isChangeToNext;
                    invalidate();
                }

                @Override
                public void onEndOpen(Object value) {
                    //QDLogger.i("onEndOpen changeSongProgress");
                    actionEnum = ActionEnum.isPlaying;
                    //start();
                    //QDLogger.i("onEndOpen animator.getTag()=" + animChangeSong.getTag());
                    if (animChangeSong.getTag() != null) {
                        AudioPlayerBar.AnimationCall call1 = (AudioPlayerBar.AnimationCall) animChangeSong.getTag();
                        //QDLogger.i("call1.action=" + call1.action);
                        if (call1.action == AudioPlayerBar.ActionType.next) {
                            call1.call();
                        }
                    }
                }

                @Override
                public void onStartClose(Object value) {
                }

                @Override
                public void onClosing(Object value) {
                    //QDLogger.i("onClosing changeSongProgress=" + value);
                    changeSongProgress = (float) value;
                    actionEnum = ActionEnum.isChangeToPrev;
                    invalidate();
                }

                @Override
                public void onEndClose(Object value) {
                    QDLogger.println("切换歌曲动画执行完成");
                    actionEnum = ActionEnum.isPlaying;
                    //QDLogger.i("onEndOpen animator.getTag()=" + animChangeSong.getTag());
                    if (animChangeSong.getTag() != null) {
                        AudioPlayerBar.AnimationCall call1 = (AudioPlayerBar.AnimationCall) animChangeSong.getTag();
                        //QDLogger.i("call1.action=" + call1.action);
                        if (call1.action == AudioPlayerBar.ActionType.prev) {
                            call1.call();
                        }
                    }
                }
            });
            animChangeSong.setRepeatCount(0);
            animChangeSong.setInterpolator(new LinearInterpolator());
        }
        animChangeSong.setTag(call);
    }

    public void reverseBackChangeSong(AudioPlayerBar.AnimationCall call) {
        initChangeSongAnmation(call);
        if (animChangeSong.getState() != QDValueAnimator.AnimationState.isColosing) {
            animChangeSong.setupStartValues();
            animChangeSong.reverse();
        }
    }

    public void stopChangeSong() {
        try {
            if (animChangeSong != null && animChangeSong.isRunning()) {
                animChangeSong.cancel();
                animChangeSong.setupStartValues();
                animChangeSong.setFloatValues((Float) animChangeSong.getAnimatedValue() + 360);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新状态刷新界面
     */
    public void reseatState() {
        if (MC.getInstance(getContext()).isPlaying()) {
            startAnimation();
        } else {
            pause(null);
        }
    }

   /* public void setOnPlayerStateChangeListener(AudioPlayerBar.OnPlayerStateChangeListener onPlayerStateChangeListener) {
        this.onPlayerStateChangeListener = onPlayerStateChangeListener;
    }*/

    /**
     * 动画完成暂停音乐
     */
    public void pauseMusic() {
        QDLogger.i(getClass().getSimpleName() + " pauseMusic()");
        AudioPlayerBar.AnimationCall call = new AudioPlayerBar.AnimationCall(AudioPlayerBar.ActionType.pause) {
            @Override
            public void call() {
                QDLogger.i("call1= pauseMusic");
                if (onPlayerStateChangeListener != null) {
                    onPlayerStateChangeListener.onPause();
                }
            }
        };
        pause(call);
    }

    /**
     * 动画完成播放下一首
     */
    public void playNext() {
        startChangeSong(null);
    }

    /**
     * 动画完成播放上一首
     */
    public void playPrev() {
        reverseBackChangeSong(null);
    }

    private boolean isPreviewMode;//是否是预览模式
    public void setPreviewMode(boolean b) {
        isPreviewMode = b;
    }

    enum ActionEnum {
        isPlaying,
        isPaused,
        isChangeToNext,
        isChangeToPrev,
    }

    public static interface OnAnimationListener {
        void onStart();

        void onStop();
    }

    public static interface OnClickActionListener {
        void onCenterClick();

        void onEdgeClick();
    }

}
