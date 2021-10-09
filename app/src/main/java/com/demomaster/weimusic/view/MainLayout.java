package com.demomaster.weimusic.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class MainLayout extends RelativeLayout {
    public MainLayout(Context context) {
        super(context);
    }

    public MainLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MainLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(onInterceptTouchListener!=null){
           return onInterceptTouchListener.interceptTouchEvent(ev);
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(onDispatchTouchListener!=null){
            onDispatchTouchListener.dispatchTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    OnDispatchTouchListener onDispatchTouchListener;
    public void setOnDispatchTouchListener(OnDispatchTouchListener onDispatchTouchListener) {
        this.onDispatchTouchListener = onDispatchTouchListener;
    }

    public static interface OnDispatchTouchListener{
        boolean dispatchTouchEvent(MotionEvent ev);
    }
    OnInterceptTouchListener onInterceptTouchListener;

    public void setOnInterceptTouchListener(OnInterceptTouchListener onInterceptTouchListener) {
        this.onInterceptTouchListener = onInterceptTouchListener;
    }

    public static interface OnInterceptTouchListener{
        boolean interceptTouchEvent(MotionEvent ev);
    }
}
