package com.demomaster.weimusic.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.demomaster.weimusic.R;

import org.jetbrains.annotations.NotNull;

public class SheetLableView extends androidx.appcompat.widget.AppCompatTextView {
    public SheetLableView(@NonNull @NotNull Context context) {
        super(context);
    }

    public SheetLableView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SheetLableView(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int x1 = 0;
        int y1 = getMeasuredHeight();
        int x2 = getMeasuredWidth()/2;
        int y2 = 0;
        int x3 = getMeasuredWidth();
        int y3 = getMeasuredHeight();
        //QDLogger.e("x1="+x1+",y1="+y1+",x2="+x2+",y2="+y2+",x3="+x3+",y3="+y3);

        int a =2*(x2-x1);
        int b = 2*(y2-y1);
        int c = x2*x2+y2*y2-x1*x1-y1*y1;
        int d = 2*(x3-x2);
        int e=2*(y3-y2);
        int f = x3*x3+y3*y3-x2*x2-y2*y2;
        int x = (b*f-e*c)/(b*d-e*a);
        int y = (d*c-a*f)/(b*d-e*a);
        double r = Math.sqrt((x-x1)*(x-x1)+(y-y1)*(y-y1));

        //QDLogger.e("x="+x+",y="+y+",r="+r);

        Paint paint =new Paint();
        paint.setAntiAlias(true);
        paint.setColor(getResources().getColor(R.color.transparent_light_a8));
        canvas.clipRect(new Rect(0,0,getMeasuredWidth(),getMeasuredHeight()));
        canvas.drawCircle(x,y, (float) r, paint);
        super.onDraw(canvas);
    }
}
