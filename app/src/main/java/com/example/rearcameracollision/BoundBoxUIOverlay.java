package com.example.rearcameracollision;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class BoundBoxUIOverlay extends View {

    private Rect boundingBox;

    private final Paint boxPaint = new Paint();
    private final Paint textPaint = new Paint();

    public BoundBoxUIOverlay(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        boxPaint.setColor(Color.RED);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(8f);

        textPaint.setColor(Color.RED);
        textPaint.setTextSize(48f);
        textPaint.setStyle(Paint.Style.FILL);
    }

    public void setBoundingBox(
            Rect rect,
            int imageWidth,
            int imageHeight) {

        float scaleX =
                (float) getWidth() / imageWidth;

        float scaleY =
                (float) getHeight() / imageHeight;

        boundingBox = new Rect(
                (int)(rect.left * scaleX) +60,
                (int)(rect.top * scaleY) -200,
                (int)(rect.right * scaleX) +60,
                (int)(rect.bottom * scaleY) -200
        );

        invalidate();
    }

    public void clearBoundingBox() {
        this.boundingBox = null;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (boundingBox != null) {

            canvas.drawRect(boundingBox, boxPaint);

            canvas.drawText(
                    "TRACKED OBJECT",
                    boundingBox.left+ 50,
                    boundingBox.top - 20,
                    textPaint
            );
        }
    }
}
