package com.sakaryamiras.app.ui.detail;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class BeforeAfterSliderView extends View {

    @Nullable
    private Bitmap beforeBitmap;
    @Nullable
    private Bitmap afterBitmap;
    private float sliderRatio = 0.5f;

    private final Paint dividerPaint;
    private final Paint handleFillPaint;
    private final Paint handleStrokePaint;
    private final Paint placeholderPaint;
    private final Rect destRect = new Rect();

    public BeforeAfterSliderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dividerPaint.setColor(Color.WHITE);
        dividerPaint.setStyle(Paint.Style.FILL);

        handleFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        handleFillPaint.setColor(Color.WHITE);
        handleFillPaint.setStyle(Paint.Style.FILL);

        handleStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        handleStrokePaint.setColor(Color.parseColor("#C9942C"));
        handleStrokePaint.setStyle(Paint.Style.STROKE);
        handleStrokePaint.setStrokeWidth(dp(3));

        placeholderPaint = new Paint();
        placeholderPaint.setColor(Color.parseColor("#E0D9CC"));
    }

    public void setBeforeBitmap(@Nullable Bitmap bitmap) {
        this.beforeBitmap = bitmap;
        invalidate();
    }

    public void setAfterBitmap(@Nullable Bitmap bitmap) {
        this.afterBitmap = bitmap;
        invalidate();
    }

    public void setSliderRatio(float ratio) {
        this.sliderRatio = Math.max(0f, Math.min(1f, ratio));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        destRect.set(0, 0, width, height);

        if (afterBitmap != null) {
            canvas.drawBitmap(afterBitmap, null, destRect, null);
        } else {
            canvas.drawRect(destRect, placeholderPaint);
        }

        int sliderX = (int) (width * sliderRatio);

        int save = canvas.save();
        canvas.clipRect(0, 0, sliderX, height);
        if (beforeBitmap != null) {
            canvas.drawBitmap(beforeBitmap, null, destRect, null);
        } else {
            canvas.drawRect(destRect, placeholderPaint);
        }
        canvas.restoreToCount(save);

        float dividerHalfWidth = dp(2);
        canvas.drawRect(sliderX - dividerHalfWidth, 0,
                sliderX + dividerHalfWidth, height, dividerPaint);

        float cy = height / 2f;
        float radius = dp(20);
        canvas.drawCircle(sliderX, cy, radius, handleFillPaint);
        canvas.drawCircle(sliderX, cy, radius, handleStrokePaint);

        Paint arrowPaint = new Paint(handleStrokePaint);
        arrowPaint.setStrokeWidth(dp(2));
        float arrowSize = dp(6);
        canvas.drawLine(sliderX - arrowSize, cy, sliderX - arrowSize - dp(4), cy - dp(4), arrowPaint);
        canvas.drawLine(sliderX - arrowSize, cy, sliderX - arrowSize - dp(4), cy + dp(4), arrowPaint);
        canvas.drawLine(sliderX + arrowSize, cy, sliderX + arrowSize + dp(4), cy - dp(4), arrowPaint);
        canvas.drawLine(sliderX + arrowSize, cy, sliderX + arrowSize + dp(4), cy + dp(4), arrowPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN
                || action == MotionEvent.ACTION_MOVE) {
            getParent().requestDisallowInterceptTouchEvent(true);
            sliderRatio = Math.max(0f, Math.min(1f, event.getX() / getWidth()));
            invalidate();
            return true;
        }
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            getParent().requestDisallowInterceptTouchEvent(false);
        }
        return super.onTouchEvent(event);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
