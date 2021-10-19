package com.demomaster.weimusic.view;

/**
 * 仿饿了么店铺内商品列表页面
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.Scroller;

import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;
public class ElemeDetailView extends LinearLayout /*implements NestedScrollingParent*/ {
    View headerView, scrollView, headerHeightMaxAlignView, headerHeightMinAlignView;
    int minMarginTop, maxMarginTop;

    NestedScrollingParentHelper helper = new NestedScrollingParentHelper(this);
    Listener listener;

    public ElemeDetailView(Context context) {
        super(context);
        handleCustomAttrs(context, null);
    }

    public ElemeDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        handleCustomAttrs(context, attrs);
    }

    public void setMinMarginTop(int minMarginTop) {
        this.minMarginTop = minMarginTop;
    }

    int headerViewId = -1;
    int scrollViewId = -1;
    int headerHeightMaxAlignViewId = -1;
    int headerHeightMinAlignViewId = -1;
    private Scroller mScroller;

    private void handleCustomAttrs(Context context, AttributeSet attrs) {
        mScroller = new Scroller(context);
        if (attrs == null) {
            return;
        }
       /* TypedArray typedArray = context.obtainStyledAttributes(attrs, cn.demomaster.huan.quickdeveloplibrary.R.styleable.QDNestedScrollParent);
        //int srcTop = typedArray.getColor(R.styleable.MyRadioButton_srcTop, -1);
        headerViewId = typedArray.getResourceId(cn.demomaster.huan.quickdeveloplibrary.R.styleable.QDNestedScrollParent_topView, -1);
        scrollViewId = typedArray.getResourceId(cn.demomaster.huan.quickdeveloplibrary.R.styleable.QDNestedScrollParent_scrollView, -1);

        minMarginTop = typedArray.getDimensionPixelOffset(cn.demomaster.huan.quickdeveloplibrary.R.styleable.QDNestedScrollParent_headerHeightMin, 0);
        maxMarginTop = typedArray.getDimensionPixelOffset(cn.demomaster.huan.quickdeveloplibrary.R.styleable.QDNestedScrollParent_headerHeightMax, 0);
        headerHeightMaxAlignViewId = typedArray.getResourceId(cn.demomaster.huan.quickdeveloplibrary.R.styleable.QDNestedScrollParent_headerHeightMinAlign, -1);
        headerHeightMinAlignViewId = typedArray.getResourceId(cn.demomaster.huan.quickdeveloplibrary.R.styleable.QDNestedScrollParent_headerHeightMaxAlign, -1);
*/

        if (scrollViewId != -1) {
            scrollView = findViewById(scrollViewId);
        }
        if (headerViewId != -1) {
            headerView = findViewById(headerViewId);
        }

       // typedArray.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (scrollViewId != -1) {
            scrollView = findViewById(scrollViewId);
        }
        if (headerViewId != -1) {
            headerView = findViewById(headerViewId);
        }
        if (headerHeightMaxAlignViewId != -1) {
            headerHeightMaxAlignView = findViewById(headerHeightMaxAlignViewId);
            maxMarginTop = headerHeightMaxAlignView.getMeasuredHeight();
        }
        if (headerHeightMinAlignViewId != -1) {
            headerHeightMinAlignView = findViewById(headerHeightMinAlignViewId);
            if(headerHeightMinAlignView!=null) {
                minMarginTop = headerHeightMinAlignView.getMeasuredHeight();
            }
        }
        //监听edv_content的位置改变
        //edv_content的移动范围为titleHeight~titleHeight+headerHeight
        if (scrollView != null) {
            //据此算出一个百分比
            scrollView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    if (listener != null) {
                        int v = (maxMarginTop - minMarginTop);
                        if(v>0) {
                            float fraction = (scrollView.getY() - minMarginTop) / v;
                            listener.onContentPostionChanged(fraction);
                        }
                    }
                    return true;
                }
            });
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //QDLogger.e("onMeasure widthMeasureSpec="+widthMeasureSpec+",heightMeasureSpec="+heightMeasureSpec);
        if (headerView != null && scrollView != null) {
            //edv_title.getMeasuredHeight();
            int t = ((MarginLayoutParams) scrollView.getLayoutParams()).topMargin;
            int b = ((MarginLayoutParams) scrollView.getLayoutParams()).bottomMargin;
            maxMarginTop = headerView.getMeasuredHeight() + t;
            //重新测量高度
            int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight() - minMarginTop - b, MeasureSpec.EXACTLY);//MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY);
            // super.onMeasure(widthMeasureSpec, newHeightMeasureSpec);
            measureChild(scrollView, widthMeasureSpec, newHeightMeasureSpec);
        }
        //重新测量高度
        //int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight()+ , MeasureSpec.EXACTLY);
        //super.onMeasure(widthMeasureSpec, newHeightMeasureSpec);
    }

    boolean isFirst;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        //QDLogger.e("onLayout changed="+changed+",l="+l+",t="+t+",r="+r+",b="+b);

        int scrollViewTop=0;
        if(scrollView!=null){
            scrollViewTop = scrollView.getTop();
        }
        super.onLayout(changed, l, t, r, b);
        if (scrollView!=null&&isFirst && scrollViewTop != scrollView.getTop()) {
            scrollView.setTop(scrollViewTop);
            //offset(scrollViewTop - scrollView.getTop(),new int[2]);
            //QDLogger.e("scrollViewTop="+scrollViewTop+",getTop="+scrollView.getTop());
        }
        isFirst = true;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public static interface Listener {
        void onContentPostionChanged(float fraction);
    }

//以下：NestedScrollingParent接口------------------------------

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return true;
    }

    //移动edv_content
    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if (scrollView != null) {
            float supposeY = scrollView.getY() - dy;//希望移动到的位置

            //往上滑,y的边界为titleHeight
            if (dy > 0) {
                if (supposeY >= minMarginTop) {
                    offset(dy, consumed);
                } else {
                    offset((int) (scrollView.getY() - minMarginTop), consumed);
                }
            }

            //往下滑,y的边界为titleHeight + headerHeight
            if (dy < 0) {
                if (!ViewCompat.canScrollVertically(target, dy)) {//当target不能向下滑时
                    if (supposeY <= maxMarginTop) {
                        offset(dy, consumed);
                    } else {
                        offset((int) (scrollView.getY() - maxMarginTop), consumed);
                    }
                }
            }
        }
    }

    private void offset(int dy, int[] consumed) {
        //QDLogger.d("scrollView top="+scrollView.getTop()+",getScrollY()="+getScrollY());
        //第二个参数为正代表向下，为负代表向上
        ViewCompat.offsetTopAndBottom(scrollView, -dy);
        consumed[0] = 0;
        consumed[1] = dy;
        //scrollTo(0,getScrollY()+dy);

        //QDLogger.d("offset dy="+ dy+",getScrollY="+getScrollY()+",top="+scrollView.getTop());
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {

    }

    //以下：NestedScrollingParent接口的其他方法
    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        helper.onNestedScrollAccepted(child, target, nestedScrollAxes);
    }

    @Override
    public void onStopNestedScroll(View target) {
        helper.onStopNestedScroll(target);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        //QDLogger.d("onNestedFling velocityY=" + velocityY);
        fling(velocityY);
        return true;
    }

    public void fling(float velocityY) {
        //mScroller.startScroll(mScroller.getFinalX(), mScroller.getFinalY(), 0, 1000, 2000);
        if (velocityY > 0) {
            mScroller.fling(0, 0, 0, (int) velocityY, 0, 0, 0, maxMarginTop);
        } else {
            mScroller.fling(0, 0, 0, (int) velocityY, 0, 0, -maxMarginTop, 0);
        }
        lastScrollY = 0;
        invalidate();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            //QDLogger.e("mScroller.getCurrY()2=" + mScroller.getCurrY());
            flingScroll(0, mScroller.getCurrY());
            return;
        }
    }

    int lastScrollY;
    public void flingScroll(int x, int y) {
        int dy = y - lastScrollY;
        lastScrollY = y;
        //QDLogger.d("scrollTo y=" +y+",dy="+ dy+",scrollView.getTop()="+scrollView.getTop());
        int[] consumed = new int[2];
        //往上滑,y的边界为titleHeight
        if (scrollView!=null&&scrollView.getTop() <= maxMarginTop && scrollView.getTop() >= minMarginTop) {
            int y2 = scrollView.getTop() - minMarginTop;
            dy = Math.min(y2, dy);
            offset(dy, consumed);
        }
        invalidate();
        //super.scrollTo(x, getScrollY());
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public int getNestedScrollAxes() {
        return helper.getNestedScrollAxes();
    }
}