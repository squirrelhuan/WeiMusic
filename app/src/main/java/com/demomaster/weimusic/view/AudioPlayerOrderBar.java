package com.demomaster.weimusic.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.RemoteException;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.demomaster.weimusic.R;
import com.demomaster.weimusic.constant.SequenceType;
import com.demomaster.weimusic.player.helpers.MusicHelper;
import com.demomaster.weimusic.player.service.MC;

import static com.demomaster.weimusic.constant.SequenceType.Normal;


/**
 * Created by squirrel桓 on 2018/1/22.
 */

public class AudioPlayerOrderBar extends AppCompatImageView {

    public AudioPlayerOrderBar(Context context) {
        super(context);
    }

    public AudioPlayerOrderBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AudioPlayerOrderBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initSize();
    }

    private Bitmap bitmap;
  /*  public void setImageResource(int resourceID) {
        Resources res = getResources();
        bitmap = BitmapFactory.decodeResource(res, resourceID);
        postInvalidate();
    }*/

    private PointF pointf_center = new PointF();//圆心
    private float radius = 0f;//半径

    public void initSize() {
        pointf_center = new PointF(getWidth() / 2f, getHeight() / 2f);
        radius = getWidth() / 2f < getHeight() / 2f ? getWidth() / 2f : getHeight() / 2f - getPaddingLeft();

    }

    //半透明边框
    private Paint paint_boun = new Paint();
    private int paint_boun_color = 0x33000000;
    private float paint_boun_width = 5f;

    //黑色圆环
    private Paint paint_ring = new Paint();
    private int paint_ring_color = Color.BLACK;

    //黑色圆环
    private Paint paint_ring_mask = new Paint();
    private boolean isCustom = false;

    int lineWidth = 20;
    SequenceType sequenceType = Normal;
    @Override
    protected void onDraw(Canvas canvas) {
        if (isCustom) {
            LinearGradient mShader = new LinearGradient(0, 0, getMeasuredWidth(), getMeasuredHeight(), new int[]{
                    0x77000000, 0x77000000, 0x44ffffff, 0x77000000, 0x77000000}, new float[]{0, 0.3f, 0.5f, 0.7f, 1.0f},
                    Shader.TileMode.MIRROR);
            // Shader.TileMode三种模式
            // REPEAT:沿着渐变方向循环重复
            // CLAMP:如果在预先定义的范围外画的话，就重复边界的颜色
            // MIRROR:与REPEAT一样都是循环重复，但这个会对称重复

            //绘制边缘
            paint_boun.setAntiAlias(true);
            paint_boun.setColor(paint_boun_color);
            paint_boun.setStyle(Paint.Style.FILL);  //设置为
            // canvas.drawCircle(pointf_center.x, pointf_center.y, radius, paint_boun);

            //绘制黑色环带
            paint_ring.setAntiAlias(true);
            paint_ring.setColor(paint_ring_color);
            paint_ring.setStyle(Paint.Style.FILL);//设置为
            canvas.drawCircle(pointf_center.x, pointf_center.y, radius - paint_boun_width, paint_ring);

            //绘制黑色圆环上的光影效果
            paint_ring_mask.setAntiAlias(true);
            paint_ring_mask.setShader(mShader);
            paint_ring_mask.setStyle(Paint.Style.FILL); //设置为
            canvas.drawCircle(pointf_center.x, pointf_center.y, radius - paint_boun_width, paint_ring_mask);

            if (bitmap != null) {
                Paint paint = new Paint();
                paint.setAntiAlias(true);
                Rect mSrcRect = new Rect(0, 0, getWidth() * 2, getHeight() * 2);
                Rect mDestRect = new Rect(0, 0, getWidth(), getHeight());
                canvas.drawBitmap(bitmap, mSrcRect, mDestRect, paint);
            }
        }
       // Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.music_bar_005);
       // canvas.drawBitmap(bitmap,(getWidth()-bitmap.getWidth())/2,(getHeight()-bitmap.getHeight())/2,paint_boun);//music_bar_005

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        /*LinearGradient mShader = new LinearGradient(0, 0, getMeasuredWidth()/3, getMeasuredHeight()/3, new int[]{
                0xff4D4D4D, 0xff4D4D4D, 0xff000000}, new float[]{0, 0.9f, 0.1f},
                Shader.TileMode.CLAMP);*/
        RadialGradient  mShader =  new RadialGradient(pointf_center.x,pointf_center.y,getWidth()/3,new int[] {0xff4D4D4D,0xff000000},new float[]{0.85f,1f},Shader.TileMode.REPEAT);
        paint.setShader(mShader);
        canvas.drawCircle(pointf_center.x,pointf_center.y,getWidth()/3,paint);

        float w = getWidth()/3;
        float h = getHeight()/3;
        lineWidth = (int) (w/10);
        float l = getWidth()/2-w/2;
        float t = getHeight()/2-h/2;
        float r = getWidth()/2+w/2;
        float b = getHeight()/2+h/2;
        RectF rectF = new RectF(l,t,r,b);
        paint = new Paint();
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        paint.setAntiAlias(true);
        paint.setStrokeWidth(lineWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getResources().getColor(R.color.transparent_light_99));

        sequenceType = MC.getInstance(getContext()).getRepeatMode();
        float w2 = 0;
        switch (sequenceType){
            case REPEAT_ALL:
                canvas.drawArc(rectF,0,120,false,paint);
                canvas.drawArc(rectF,180,120,false,paint);
                paint.setStrokeWidth(1);
                paint.setStyle(Paint.Style.FILL);

                w2 = lineWidth*2;
                Path path = new Path();
                path.moveTo(l+lineWidth/2-w2,getHeight()/2);
                path.lineTo(l+lineWidth/2,getHeight()/2);
                path.lineTo(l+lineWidth/2,getHeight()/2+w2);
                path.close();
                canvas.drawPath(path,paint);

                Path path1 = new Path();
                path1.moveTo(r-lineWidth/2,getHeight()/2);
                path1.lineTo(r-lineWidth/2+w2,getHeight()/2);
                path1.lineTo(r-lineWidth/2,getHeight()/2-w2);
                path1.close();
                canvas.drawPath(path1,paint);
                break;
            case REPEAT_CURRENT:
                //canvas.drawOval(,paint);
                canvas.drawArc(rectF,0,120,false,paint);
                canvas.drawArc(rectF,180,120,false,paint);
                paint.setStrokeWidth(1);
                paint.setStyle(Paint.Style.FILL);
                //canvas.drawLine(0,getHeight()/2,getWidth(),getHeight()/2,paint);

                w2 = lineWidth*2;
                path = new Path();
                paint.setAntiAlias(true);
                path.moveTo(l+lineWidth/2-w2,getHeight()/2);
                path.lineTo(l+lineWidth/2,getHeight()/2);
                path.lineTo(l+lineWidth/2,getHeight()/2+w2);
                path.close();
                canvas.drawPath(path,paint);

                path1 = new Path();
                paint.setAntiAlias(true);
                path1.moveTo(r-lineWidth/2,getHeight()/2);
                path1.lineTo(r-lineWidth/2+w2,getHeight()/2);
                path1.lineTo(r-lineWidth/2,getHeight()/2-w2);
                path1.close();
                canvas.drawPath(path1,paint);
                float textSize = rectF.width()/7*5;
                paint.setTextSize(textSize);
                paint.setTextAlign(Paint.Align.CENTER);
                Paint.FontMetrics fontMetrics=paint.getFontMetrics();
                /*fontMetrics.top
                fontMetrics.ascent
                fontMetrics.descent
                fontMetrics.bottom*/
                float a = (fontMetrics.bottom - fontMetrics.top)/2 - fontMetrics.bottom;
                canvas.drawText("1",getWidth()/2,getHeight()/2+a,paint);
                break;
            case Shuffle:
                rectF = new RectF(rectF.left-lineWidth,rectF.top+rectF.height()/5,rectF.right+lineWidth,-rectF.height()/5+rectF.bottom);
                float w3 = rectF.width()/4;
                path = new Path();
                path.moveTo(rectF.left,rectF.top);
                path.lineTo(getWidth()/2-w3,rectF.top);
               // path.quadTo(getWidth()/2,rectF.top,getWidth()/2,getHeight()/2);
               // path.quadTo(getWidth()/2,rectF.bottom,getWidth()/2+w3,rectF.bottom);
                path.lineTo(getWidth()/2+w3,rectF.bottom);
                path.lineTo(rectF.right,rectF.bottom);
                canvas.drawPath(path,paint);

                path = new Path();
                path.moveTo(rectF.left,rectF.bottom);
                path.lineTo(getWidth()/2-w3,rectF.bottom);
                //path.quadTo(getWidth()/2,rectF.bottom,getWidth()/2,getHeight()/2);
               // path.quadTo(getWidth()/2,rectF.top,getWidth()/2+w3,rectF.top);
                path.lineTo(getWidth()/2+w3,rectF.top);
                path.lineTo(rectF.right,rectF.top);
                canvas.drawPath(path,paint);


                w2 = lineWidth/3*2;
                path = new Path();
                path.moveTo(rectF.right,rectF.top);
                path.lineTo(rectF.right-w2,rectF.top);
                path.lineTo(rectF.right-w2,rectF.top-w2);
                path.close();
                canvas.drawPath(path,paint);

                path = new Path();
                path.moveTo(rectF.right,rectF.bottom);
                path.lineTo(rectF.right-w2,rectF.bottom);
                path.lineTo(rectF.right-w2,rectF.bottom+w2);
                path.close();
                canvas.drawPath(path,paint);
                break;
            case Normal:
                canvas.drawArc(rectF,0,300,false,paint);
                w2 = lineWidth;
                path1 = new Path();
                path1.moveTo(r-lineWidth/2,getHeight()/2);
                path1.lineTo(r-lineWidth/2+w2,getHeight()/2);
                path1.lineTo(r-lineWidth/2,getHeight()/2-w2);
                path1.close();
                canvas.drawPath(path1,paint);
                break;
        }

        Matrix matrix = canvas.getMatrix();
        matrix.postScale(0.5f, 0.5f, pointf_center.x, pointf_center.y);
        canvas.setMatrix(matrix);
        super.onDraw(canvas);
    }

    public SequenceType getSequenceType() {
        return sequenceType;
    }

    public void setSequenceType(SequenceType sequenceType) {
        this.sequenceType = sequenceType;
        postInvalidate();
    }
}
