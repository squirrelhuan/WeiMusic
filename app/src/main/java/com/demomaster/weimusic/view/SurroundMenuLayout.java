package com.demomaster.weimusic.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

import androidx.annotation.Nullable;

import com.demomaster.weimusic.constant.AudioStation;

import org.greenrobot.eventbus.EventBus;

import cn.demomaster.huan.quickdeveloplibrary.animation.QDValueAnimator;
import cn.demomaster.huan.quickdeveloplibrary.model.EventMessage;
import cn.demomaster.qdlogger_library.QDLogger;

import static cn.demomaster.huan.quickdeveloplibrary.animation.QDValueAnimator.AnimationState.isClosed;
import static cn.demomaster.huan.quickdeveloplibrary.animation.QDValueAnimator.AnimationState.isOpened;
import static cn.demomaster.huan.quickdeveloplibrary.animation.QDValueAnimator.AnimationState.isOpening;

public class SurroundMenuLayout extends ViewGroup {
    public SurroundMenuLayout(Context context) {
        super(context);
    }

    public SurroundMenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SurroundMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public float getStartAngle() {
        return startAngle;
    }

    public void setStartAngle(float startAngle) {
        this.startAngle = startAngle;
    }

    public float getEndAngle() {
        return endAngle;
    }

    public void setEndAngle(float endAngle) {
        this.endAngle = endAngle;
    }

    public PointF getOriginPoint() {
        return originPoint;
    }

    public void setOriginPoint(PointF originPoint) {
        this.originPoint = originPoint;
    }

    public float getStartRadius() {
        return startRadius;
    }

    public void setStartRadius(float startRadius) {
        this.startRadius = startRadius;
    }

    public float getEndRadius() {
        return endRadius;
    }

    public void setEndRadius(float endRadius) {
        this.endRadius = endRadius;
    }

    public long getAnimatDuration() {
        return animatDuration;
    }

    public void setAnimatDuration(long animatDuration) {
        this.animatDuration = animatDuration;
    }

    private float startAngle = 0;//开始角度
    private float endAngle = 180;//结束角度
    private float startRadius = 0; //活动半径
    private float endRadius = 165; //活动半径

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        originPoint = new PointF(getWidth() / 2, getHeight() / 2);
        //QDLogger.d("l=" + l + ",t=" + t + ",r=" + r + ",b=" + b + "originPoint:x=" + originPoint.x + ",y=" + originPoint.y);
        int count = getChildCount();
        double a = endAngle % 360 - startAngle % 360;
        double ang = a / (count + 1);
        double radius2 = startRadius + (endRadius - startRadius) * progress;
        //QDLogger.d("a:" + a+",ang="+ang+",progress="+progress + ",startRadius="+startRadius);
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);
            view.setVisibility(VISIBLE);
            int height = view.getMeasuredHeight();
            int width = view.getMeasuredWidth();
            double anger = startAngle + ang * (i + 1);
            int x1 = (int) (originPoint.x + radius2 * Math.cos(anger / 180 * Math.PI));
            int y1 = (int) (originPoint.y + radius2 * Math.sin(anger / 180 * Math.PI));
            //QDLogger.d("i=" + i + ",xy:" + x1 + "," + y1 + ",anger=" + anger+",originPoint.x"+originPoint.x+",originPoint.y"+originPoint.y);
            view.layout(x1 - width / 2, y1 - height / 2, x1 + width / 2, y1 + height / 2);
            if(progress==0) {
                view.setVisibility(GONE);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        measureChildren(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            int groupWidth = getMaxWidth();
            int groupHeight = getTotalHeight();
            setMeasuredDimension(groupWidth, groupHeight);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(getMaxWidth(), height);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(width, getTotalHeight());
        }
    }

    private int getMaxWidth() {
        int count = getChildCount();
        int maxWidth = 0;
        for (int i = 0; i < count; i++) {
            int currentWidth = getChildAt(i).getMeasuredWidth();
            if (maxWidth < currentWidth) {
                maxWidth = currentWidth;
            }
        }
        return maxWidth;
    }

    private int getTotalHeight() {
        int count = getChildCount();
        int totalHeight = 0;
        for (int i = 0; i < count; i++) {
            totalHeight += getChildAt(i).getMeasuredHeight();
        }
        return totalHeight;
    }

    public boolean isShowing(){
        if(animator!=null){
            return ((float)animator.getValue()>0);
        }
        return false;
    }

    PointF originPoint = new PointF(0, 0);
    private float progress;
    QDValueAnimator animator;
    private long animatDuration = 300;
    public void startAnimation() {
        if (animator != null) {
            animator.forward();
        } else {
            final int start = 0;
            final int end = 1;
            animator = QDValueAnimator.ofFloat(start, end);
            animator.setDuration(animatDuration);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                        float value = (float) animation.getAnimatedValue();
                        progress = value;
                        //QDLogger.d( "menu progress=" + progress);
                        requestLayout();
                        if(onProgressChangeListener!=null){
                            onProgressChangeListener.onChanged((Float) value);
                        }
                }
            });
            animator.setRepeatMode(ValueAnimator.REVERSE);
            //animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setInterpolator(new AccelerateInterpolator());
            animator.start();
        }
    }

    private void hideChids() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).setVisibility(GONE);
        }
    }

    public void stopAnimation() {
        if (animator!=null) {
            animator.backward();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null)
            animator.cancel();
    }

    OnProgressChangeListener onProgressChangeListener;

    public void setOnProgressChangeListener(OnProgressChangeListener onProgressChangeListener) {
        this.onProgressChangeListener = onProgressChangeListener;
    }

    public static interface OnProgressChangeListener{
        void onChanged(float progress);
    }
}
