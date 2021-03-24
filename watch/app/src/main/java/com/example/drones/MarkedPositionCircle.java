package com.example.drones;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class MarkedPositionCircle extends View {

    private Paint paint = null;
    private float x;
    private float y;
    private int color;

    public MarkedPositionCircle(Context context, float x, float y, int color) {
        super(context);
        this.x = x;
        this.y = y;
        this.color = color;
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(this.color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float radius = Constant.MARKED_POINT_SIZE;
        canvas.drawCircle(this.x, this.y, radius, paint);
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    public int getColor() {
        return color;
    }
}