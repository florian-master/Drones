package com.example.drones;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class DiscoveredAreaCircle extends View {

    private Paint paint = null;
    private float x;
    private float y;
    private double zoom;

    public DiscoveredAreaCircle(Context context, float x, float y, double zoom) {
        super(context);
        this.x = x;
        this.y = y;
        this.zoom = zoom;
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLUE);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        float radius = Constant.SIGHT_RANGE / (float) this.zoom;
        canvas.drawCircle(this.x, this.y, radius, paint);
    }
}