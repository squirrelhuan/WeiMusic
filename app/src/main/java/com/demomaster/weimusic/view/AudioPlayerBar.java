package com.demomaster.weimusic.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.demomaster.weimusic.player.service.MC;

import cn.demomaster.huan.quickdeveloplibrary.animation.QDObjectAnimator;
import cn.demomaster.huan.quickdeveloplibrary.animation.QDValueAnimator;

/**
 * Created by squirrel桓 on 2018/1/22.
 */
public class AudioPlayerBar extends AppCompatImageView {

    public AudioPlayerBar(Context context) {
        super(context);
    }

    public AudioPlayerBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AudioPlayerBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initSize();
    }

    private PointF pointf_center = new PointF();//圆心
    private float radius = 0f;//半径

    public void initSize() {
        pointf_center = new PointF(getWidth() / 2f, getHeight() / 2f);
        radius = getWidth() / 2f < getHeight() / 2f ? getWidth() / 2f : getHeight() / 2f - getPaddingLeft();
    }

    //半透明边框
    private Paint p = new Paint();
    private int paint_boun_color = 0x33000000;
    private float paint_boun_width = 5f;

    //黑色条光影
    private Paint paint_ring_mask = new Paint();
    private int paint_ring_mask_color = Color.BLACK;
    private float paint_ring_mask_width = 100f;
    private boolean isCustom = true;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float lineWidth = getWidth() / 8;
        if (isCustom) {
            LinearGradient mShader = new LinearGradient(0, 0, getMeasuredWidth(), 0, new int[]{
                    0x77000000, 0x77000000, 0x44ffffff, 0x77000000, 0x77000000}, new float[]{0, 0.4f, 0.5f, 0.60f, 1.0f},
                    Shader.TileMode.MIRROR);
            // Shader.TileMode三种模式
            // REPEAT:沿着渐变方向循环重复
            // CLAMP:如果在预先定义的范围外画的话，就重复边界的颜色
            // MIRROR:与REPEAT一样都是循环重复，但这个会对称重复
            RadialGradient mShader3 = new RadialGradient(pointf_center.x, pointf_center.y, getWidth(), new int[]{0xff4D4D4D, 0xff000000}, new float[]{0.8f, 1f}, Shader.TileMode.REPEAT);

            Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(0xff000000);
            // paint.setShader(mShader3);
            // paint.setShadowLayer(5f, 0, 5, 0xff000000);
           /* LinearGradient mShader4 = new LinearGradient(getWidth()-lineWidth, 0, getWidth()+lineWidth, 0, new int[]{
                    0xff000000, 0xff4D4D4D, 0xff4D4D4D, 0xff4D4D4D, 0xff000000}, new float[]{0, 0.4f, 0.5f, 0.6f, 1.0f},
                    Shader.TileMode.MIRROR);*/
            //paint.setShader(mShader4);
            paint.setStrokeWidth(lineWidth);
            PointF pointF01 = new PointF(getWidth() / 2, 0);
            PointF pointF02 = new PointF(getWidth() / 2, getHeight() / 3);

            paint.setStyle(Paint.Style.STROKE);
            Path path1 = new Path();
            path1.moveTo(pointF01.x, pointF01.y);
            path1.lineTo(pointF02.x, pointF02.y);
            path1.quadTo(pointF02.x + lineWidth, (getHeight() / 3 + getHeight() / 2) / 2, pointF02.x + lineWidth, getHeight() / 2);
            path1.moveTo(pointF02.x + lineWidth, getHeight() / 2);
            path1.lineTo(pointF02.x + lineWidth, getHeight());
            canvas.drawPath(path1, paint);
            paint.setStyle(Paint.Style.FILL);

            float heigh1 = getHeight() / 6;
            float width3 = lineWidth + lineWidth * 4 / 5;
            PointF pointF0 = new PointF(getWidth() / 2 + lineWidth, getHeight() - heigh1);

            Path path3 = new Path();
            path3.moveTo(pointF0.x, pointF0.y);
            path3.quadTo(pointF0.x + width3 / 2, pointF0.y, pointF0.x + width3 / 2, pointF0.y + heigh1 / 7);
            path3.lineTo(pointF0.x + width3 / 2, pointF0.y + heigh1 / 4 - heigh1 / 20);
            path3.lineTo(pointF0.x + width3 / 2 + width3 / 2, pointF0.y + heigh1 / 4);
            path3.lineTo(pointF0.x + width3 / 2, pointF0.y + heigh1 / 4 + heigh1 / 20);

            path3.lineTo(pointF0.x + width3 / 2, getHeight());
            path3.lineTo(pointF0.x - width3 / 2, getHeight());
            path3.lineTo(pointF0.x - width3 / 2, pointF0.y + heigh1 / 7);
            path3.quadTo(pointF0.x - width3 / 2, pointF0.y, pointF0.x, pointF0.y);
            path3.close(); // 使这些点构成封闭的多边形
            canvas.drawPath(path3, paint);

            Path path4 = new Path();
            float width4 = lineWidth + lineWidth / 4;
            float height4 = lineWidth + lineWidth / 4;
            PointF pointFc = new PointF(pointF0.x, getHeight() - heigh1 - height4 * 7 / 6);
            path4.moveTo(pointFc.x + lineWidth / 2, pointFc.y);
            path4.lineTo(pointFc.x + width4 / 2, pointFc.y + height4 / 5);
            path4.lineTo(pointFc.x + width4 / 2, pointFc.y + height4);
            path4.lineTo(pointFc.x - width4 / 2, pointFc.y + height4);
            path4.lineTo(pointFc.x - width4 / 2, pointFc.y + height4 / 5);
            path4.lineTo(pointFc.x - lineWidth / 2, pointFc.y);
            path4.close();
            canvas.drawPath(path4, paint);

        }
        float lineWidth2 = lineWidth / 3;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(0xff4D4D4D);
        paint.setShadowLayer(lineWidth / 2, 0, 5, 0xff4D4D4D);
        paint.setStrokeWidth(lineWidth2);
        PointF pointF01 = new PointF(getWidth() / 2, 0);
        PointF pointF02 = new PointF(getWidth() / 2, getHeight() / 3);

        paint.setStyle(Paint.Style.STROKE);
        Path path1 = new Path();
        path1.moveTo(pointF01.x, pointF01.y);
        path1.lineTo(pointF02.x, pointF02.y);
        path1.quadTo(pointF02.x + lineWidth, (getHeight() / 3 + getHeight() / 2) / 2, pointF02.x + lineWidth, getHeight() / 2);
        path1.moveTo(pointF02.x + lineWidth, getHeight() / 2);
        path1.lineTo(pointF02.x + lineWidth, getHeight());
        canvas.drawPath(path1, paint);
        paint.setStyle(Paint.Style.FILL);

        float heigh1 = getHeight() / 6;
        float width3 = lineWidth2 + lineWidth * 4 / 5;
        PointF pointF0 = new PointF(getWidth() / 2 + lineWidth, getHeight() - heigh1);

        Path path3 = new Path();
        path3.moveTo(pointF0.x, pointF0.y);
        path3.quadTo(pointF0.x + width3 / 2, pointF0.y, pointF0.x + width3 / 2, pointF0.y + heigh1 / 7);
        path3.lineTo(pointF0.x + width3 / 2, pointF0.y + heigh1 / 4 - heigh1 / 20);
        path3.lineTo(pointF0.x + width3 / 2 + width3 / 2, pointF0.y + heigh1 / 4);
        path3.lineTo(pointF0.x + width3 / 2, pointF0.y + heigh1 / 4 + heigh1 / 20);

        path3.lineTo(pointF0.x + width3 / 2, getHeight());
        path3.lineTo(pointF0.x - width3 / 2, getHeight());
        path3.lineTo(pointF0.x - width3 / 2, pointF0.y + heigh1 / 7);
        path3.quadTo(pointF0.x - width3 / 2, pointF0.y, pointF0.x, pointF0.y);
        path3.close(); // 使这些点构成封闭的多边形
        canvas.drawPath(path3, paint);

        Path path4 = new Path();
        float width4 = lineWidth2 + lineWidth2 / 4;
        float height4 = lineWidth * 2 + lineWidth * 2 / 4;
        PointF pointFc = new PointF(pointF0.x, getHeight() - heigh1 - height4 * 7 / 6);
        path4.moveTo(pointFc.x + lineWidth2 / 2, pointFc.y);
        path4.lineTo(pointFc.x + width4 / 2, pointFc.y + height4 / 5);
        path4.lineTo(pointFc.x + width4 / 2, pointFc.y + height4);
        path4.lineTo(pointFc.x - width4 / 2, pointFc.y + height4);
        path4.lineTo(pointFc.x - width4 / 2, pointFc.y + height4 / 5);
        path4.lineTo(pointFc.x - lineWidth2 / 2, pointFc.y);
        path4.close();
        canvas.drawPath(path4, paint);
    }

    QDObjectAnimator animator;
    private long animatDuration = 300;
    float start = 0;
    float end = 20;
    QDValueAnimator.AnimationListener animationListener;
    public void start(AnimationCall call) {
        if (animator != null) {
            //QDLogger.println("唱臂播放动画 start " + animator.getState() + "," + animator.getValue());
            if (!animator.isFrward()) {
                animator.setTag(call);
                animator.start();
                return;
            }

            if (call != null) {
                call.call();
            }
        } else {
            animator = QDObjectAnimator.ofFloat(this, "rotation", start, end);
            animator.setTag(call);
            setPivotX(getWidth() / 2);
            setPivotY(0);
            //执行事件
            animator.setDuration(animatDuration);
            if(animationListener==null) {
                animationListener = new QDValueAnimator.AnimationListener() {
                    @Override
                    public void onOpening(Object value) {
                       // QDLogger.println(animator.hashCode()+"]onOpening value1=" + value);
                    }

                    @Override
                    public void onEndOpen(Object value) {
                        //QDLogger.println("onEndOpen value1=" + value);
                        //QDLogger.i("onEndOpen animator.getTag()=" + animator.getTag() + ",value=" + value);
                        if (animator.getTag() != null) {
                            AnimationCall call1 = (AnimationCall) animator.getTag();
                            // QDLogger.i("call1.action=" + call1.action);
                            if (call1.action == ActionType.play || call1.action == ActionType.next || call1.action == ActionType.prev) {
                                call1.call();
                            }
                        }
                    }

                    @Override
                    public void onClosing(Object value) {
                       // QDLogger.println( animator.hashCode()+"]onClosing value1=" + value);
                    }

                    @Override
                    public void onEndClose(Object value) {
                        //QDLogger.println("onEndClose animator.getTag()=" + animator.getTag());
                        if (animator.getTag() != null) {
                            AnimationCall call1 = (AnimationCall) animator.getTag();
                            //QDLogger.i("onEndClose call1=" + call1.action);
                            if (call1 != null && call1.action == ActionType.pause) {
                                call1.call();
                            }
                        }
                    }
                };
            }
            animator.setAnimationListener(animationListener);
            animator.setRepeatMode(ValueAnimator.REVERSE);
            animator.start();
        }
    }

    @Override
    public void setRotation(float rotation) {
        super.setRotation(rotation);
        //QDLogger.i("setRotation = "+rotation);
    }

    public void stop(AnimationCall call) {
        if (animator != null) {
            //QDLogger.println("唱臂停止 " + animator.isRunning()+",state="+animator.getState()+",isReversing="+animator.isFrward());
            if(animator.isFrward()){
                animator.setTag(call);
                animator.reverse();
                return;
            }
        }
        if (call != null) {
            call.call();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.cancel();
        }
    }

    /**
     * 重置状态
     */
    public void reseatState() {
        //QDLogger.println("isPlaying="+MC.getInstance(getContext()).isPlaying());
        if (MC.getInstance(getContext()).isPlaying()) {
            start(null);
        } else {
            stop(null);
        }
    }

    public static interface OnPlayerStateChangeListener {
        void onPlay();

        void onPause();

        void onNext();

        void onPrev();
    }

    public static enum ActionType {
        play,
        pause,
        next,
        prev
    }

    public static abstract class AnimationCall implements IAnimationCall {
        ActionType action = ActionType.play;

        public AnimationCall(ActionType actionType) {
            action = actionType;
        }

        public void setAction(ActionType action) {
            this.action = action;
        }

    }

    public static interface IAnimationCall {

        void call();
    }
}
