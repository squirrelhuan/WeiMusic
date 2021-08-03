package com.demomaster.weimusic.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

public class AudioBarShield extends AppCompatImageView {
    public AudioBarShield(Context context) {
        super(context);
    }

    public AudioBarShield(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AudioBarShield(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth()/4;
        float h = getHeight()/3;

        Paint paint1 = new Paint();
        LinearGradient mShader1 = new LinearGradient(getWidth()/2+w/4, 0, getWidth()/2+w/2+w/4, 0, new int[]{
                0xff000000, 0xff4D4D4D, 0xff4D4D4D, 0xff4D4D4D, 0xff000000}, new float[]{0, 0.45f, 0.5f, 0.55f, 1.0f},
                Shader.TileMode.MIRROR);
        //paint1.setShader(mShader1);
        RectF rect = new RectF(getWidth()/2-w/2,getHeight()/2-h/2,getWidth()/2+w/2,getHeight()/2+h/2);
        canvas.drawRect(rect,paint1);


        Paint paint = new Paint();
        paint.setColor(0xff4D4D4D);
        LinearGradient mShader = new LinearGradient(getWidth()/2-w+w/4, 0, getWidth()/2+w/4, 0, new int[]{
                0xff000000, 0xff4D4D4D, 0xff4D4D4D, 0xff4D4D4D, 0xff000000}, new float[]{0, 0.4f, 0.5f, 0.60f, 1.0f},
                Shader.TileMode.MIRROR);
        paint.setShader(mShader);

        Path path1 = new Path();
        PointF pointF = new PointF(getWidth()/2,getHeight()/2);
        path1.moveTo(pointF.x-w*3/5,pointF.y-h);
        path1.lineTo(pointF.x-w*3/5,pointF.y);
        path1.quadTo(pointF.x+w/8,pointF.y-h/3,pointF.x+w/8,pointF.y+h/2);
        path1.lineTo(pointF.x+w/8,pointF.y-h/2);
        path1.quadTo(pointF.x+w/8,pointF.y-h,pointF.x-w/3*2,pointF.y-h);
        canvas.drawPath(path1,paint);
    }
}
