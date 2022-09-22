package com.bytemiracle.resourcesurvey.modules.media.paint;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.view.MotionEvent;
import android.view.View;

/**
 * 类功能：TODO
 *
 * @author gwwang
 * @date 2022/5/10 9:09
 */
class ColorPickerView extends View {
    private final int[] mCircleColors = new int[]{-65536, -65281, -16776961, -16711681, -16711936, -256, -65536};

    private float centerRadius;
    private boolean downInCircle = true;
    private boolean downInRect;
    private boolean highlightCenter;
    private boolean highlightCenterLittle;

    public Paint mCenterPaint;
    private Paint mLinePaint;
    private Paint mPaint;
    private Paint mRectPaint;

    private final int[] mRectColors;

    private float r;
    private Shader rectShader;

    private int mHeight;
    private int mWidth;

    private float rectLeft;
    private float rectRight;
    private float rectBottom;
    private float rectTop;

    public ColorPickerView(Context ctx) {
        super(ctx);
        SweepGradient sweepGradient = new SweepGradient(0.0F, 0.0F, this.mCircleColors, null);
        this.mPaint = new Paint(1);
        this.mPaint.setShader((Shader) sweepGradient);
        this.mPaint.setStyle(Paint.Style.STROKE);
        this.mPaint.setStrokeWidth(50.0F);
        this.mCenterPaint = new Paint(1);
        this.mCenterPaint.setColor(Color.WHITE);
        this.mCenterPaint.setStrokeWidth(5.0F);
        this.mLinePaint = new Paint(1);
        this.mLinePaint.setColor(Color.parseColor("#72A1D1"));
        this.mLinePaint.setStrokeWidth(4.0F);
        this.mRectColors = new int[]{-16777216, this.mCenterPaint.getColor(), -1};

        this.mRectPaint = new Paint(1);
        this.mRectPaint.setStrokeWidth(5.0F);
    }

    private int ave(int param1Int1, int param1Int2, float param1Float) {
        return Math.round((param1Int2 - param1Int1) * param1Float) + param1Int1;
    }

    private boolean inCenter(float param1Float1, float param1Float2, float param1Float3) {
        double d1 = param1Float3;
        double d2 = param1Float3;
        return (Math.PI * (param1Float1 * param1Float1 + param1Float2 * param1Float2) < d1 * Math.PI * d2);
    }

    private boolean inColorCircle(float param1Float1, float param1Float2, float param1Float3, float param1Float4) {
        double d1 = param1Float3;
        double d2 = param1Float3;
        double d3 = param1Float4;
        double d4 = param1Float4;
        double d5 = Math.PI * (param1Float1 * param1Float1 + param1Float2 * param1Float2);
        return (d5 < Math.PI * d1 * d2 && d5 > Math.PI * d3 * d4);
    }

    private boolean inRect(float param1Float1, float param1Float2) {
        return (param1Float1 <= this.rectRight && param1Float1 >= this.rectLeft && param1Float2 <= this.rectBottom && param1Float2 >= this.rectTop);
    }

    private int interpCircleColor(int[] param1ArrayOfint, float param1Float) {
        if (param1Float <= 0.0F)
            return param1ArrayOfint[0];
        if (param1Float >= 1.0F)
            return param1ArrayOfint[param1ArrayOfint.length - 1];
        param1Float *= (param1ArrayOfint.length - 1);
        int j = (int) param1Float;
        param1Float -= j;
        int i = param1ArrayOfint[j];
        j = param1ArrayOfint[j + 1];
        return Color.argb(ave(Color.alpha(i), Color.alpha(j), param1Float), ave(Color.red(i), Color.red(j), param1Float), ave(Color.green(i), Color.green(j), param1Float), ave(Color.blue(i), Color.blue(j), param1Float));
    }

    private int interpRectColor(int[] param1ArrayOfint, float param1Float) {
        if (param1Float < 0.0F) {
            int k = param1ArrayOfint[0];
            int m = param1ArrayOfint[1];
            param1Float = (this.rectRight + param1Float) / this.rectRight;
            return Color.argb(ave(Color.alpha(k), Color.alpha(m), param1Float), ave(Color.red(k), Color.red(m), param1Float), ave(Color.green(k), Color.green(m), param1Float), ave(Color.blue(k), Color.blue(m), param1Float));
        }
        int i = param1ArrayOfint[1];
        int j = param1ArrayOfint[2];
        param1Float /= this.rectRight;
        return Color.argb(ave(Color.alpha(i), Color.alpha(j), param1Float), ave(Color.red(i), Color.red(j), param1Float), ave(Color.green(i), Color.green(j), param1Float), ave(Color.blue(i), Color.blue(j), param1Float));
    }

    protected void onDraw(Canvas param1Canvas) {
        param1Canvas.translate((this.mWidth / 2), (this.mHeight / 2 - 50));
        param1Canvas.drawCircle(0.0F, 0.0F, this.centerRadius, this.mCenterPaint);
        if (this.highlightCenter || this.highlightCenterLittle) {
            int i = this.mCenterPaint.getColor();
            this.mCenterPaint.setStyle(Paint.Style.STROKE);
            if (this.highlightCenter) {
                this.mCenterPaint.setAlpha(255);
            } else if (this.highlightCenterLittle) {
                this.mCenterPaint.setAlpha(144);
            }
            param1Canvas.drawCircle(0.0F, 0.0F, this.centerRadius + this.mCenterPaint.getStrokeWidth(), this.mCenterPaint);
            this.mCenterPaint.setStyle(Paint.Style.FILL);
            this.mCenterPaint.setColor(i);
        }
        param1Canvas.drawOval(new RectF(-this.r, -this.r, this.r, this.r), this.mPaint);
        if (this.downInCircle)
            this.mRectColors[1] = this.mCenterPaint.getColor();
        this.rectShader = (Shader) new LinearGradient(this.rectLeft, 0.0F, this.rectRight, 0.0F, this.mRectColors, null, Shader.TileMode.MIRROR);
        this.mRectPaint.setShader(this.rectShader);
        param1Canvas.drawRect(this.rectLeft, this.rectTop, this.rectRight, this.rectBottom, this.mRectPaint);
        float f = this.mLinePaint.getStrokeWidth() / 2.0F;
        param1Canvas.drawLine(this.rectLeft - f, this.rectTop - f * 2.0F, this.rectLeft - f, f * 2.0F + this.rectBottom, this.mLinePaint);
        param1Canvas.drawLine(this.rectLeft - f * 2.0F, this.rectTop - f, f * 2.0F + this.rectRight, this.rectTop - f, this.mLinePaint);
        param1Canvas.drawLine(this.rectRight + f, this.rectTop - f * 2.0F, this.rectRight + f, f * 2.0F + this.rectBottom, this.mLinePaint);
        param1Canvas.drawLine(this.rectLeft - f * 2.0F, this.rectBottom + f, f * 2.0F + this.rectRight, this.rectBottom + f, this.mLinePaint);
        super.onDraw(param1Canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = MeasureSpec.getSize(widthMeasureSpec);
        mHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.r = (mHeight / 2) * 0.7F - this.mPaint.getStrokeWidth() * 0.5F;
        this.centerRadius = (this.r - this.mPaint.getStrokeWidth() / 2.0F) * 0.7F;
        this.rectLeft = -this.r - this.mPaint.getStrokeWidth() * 0.5F;
        this.rectRight = this.r + this.mPaint.getStrokeWidth() * 0.5F;
        this.rectLeft = -this.r - this.mPaint.getStrokeWidth() * 0.5F;
        this.rectTop = this.r + this.mPaint.getStrokeWidth() * 0.5F + this.mLinePaint.getStrokeMiter() * 0.5F + 40.0F;
        this.rectRight = this.r + this.mPaint.getStrokeWidth() * 0.5F;
        this.rectBottom = this.rectTop + 50.0F;
        postInvalidate();
    }

    float f1;
    float f2;
    boolean inCircle;
    boolean inCenter;
    boolean inRect;
    OnColorChangedListener colorChangedListener;

    public interface OnColorChangedListener {
        void colorChanged(int pickedColor);
    }

    /**
     * 设置回调
     *
     * @param initColor 初始化颜色
     * @param callback  回调
     */
    public void setColorCallback(int initColor, OnColorChangedListener callback) {
        this.mCenterPaint.setColor(initColor);
        this.colorChangedListener = callback;
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                f1 = event.getX() - (this.mWidth / 2);
                f2 = event.getY() - (this.mHeight / 2) + 50.0F;
                inCircle = inColorCircle(f1, f2, this.r + this.mPaint.getStrokeWidth() / 2.0F, this.r - this.mPaint.getStrokeWidth() / 2.0F);
                inCenter = inCenter(f1, f2, this.centerRadius);
                inRect = inRect(f1, f2);
                this.downInCircle = inCircle;
                this.downInRect = inRect;
                this.highlightCenter = inCenter;
                if (this.inCenter && colorChangedListener != null) {
                    colorChangedListener.colorChanged(this.mCenterPaint.getColor());
                }
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                f1 = event.getX() - (this.mWidth / 2);
                f2 = event.getY() - (this.mHeight / 2) + 50.0F;
                inCircle = inColorCircle(f1, f2, this.r + this.mPaint.getStrokeWidth() / 2.0F, this.r - this.mPaint.getStrokeWidth() / 2.0F);
                inCenter = inCenter(f1, f2, this.centerRadius);
                boolean inRect = inRect(f1, f2);
                if (this.downInCircle && inCircle) {
                    float f4 = (float) ((float) Math.atan2(f2, f1) / 6.283185307179586D);
                    float f3 = f4;
                    if (f4 < 0.0F)
                        f3 = f4 + 1.0F;
                    this.mCenterPaint.setColor(interpCircleColor(this.mCircleColors, f3));
                } else if (this.downInRect && inRect) {
                    this.mCenterPaint.setColor(interpRectColor(this.mRectColors, f1));
                }
                if ((this.highlightCenter && inCenter) || (this.highlightCenterLittle && inCenter)) {
                    this.highlightCenter = true;
                    this.highlightCenterLittle = false;
                } else if (this.highlightCenter || this.highlightCenterLittle) {
                    this.highlightCenter = false;
                    this.highlightCenterLittle = true;
                } else {
                    this.highlightCenter = false;
                    this.highlightCenterLittle = false;
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (this.downInCircle)
                    this.downInCircle = false;
                if (this.downInRect)
                    this.downInRect = false;
                if (this.highlightCenter)
                    this.highlightCenter = false;
                if (this.highlightCenterLittle)
                    this.highlightCenterLittle = false;
                break;
        }
        return super.onTouchEvent(event);
    }
}