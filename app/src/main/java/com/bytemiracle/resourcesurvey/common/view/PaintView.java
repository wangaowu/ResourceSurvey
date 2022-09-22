package com.bytemiracle.resourcesurvey.common.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import com.bytemiracle.base.framework.component.BaseActivity;
import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.base.framework.utils.common.ListUtils;
import com.bytemiracle.resourcesurvey.common.dialog.CommonGetTextDialog;
import com.bytemiracle.resourcesurvey.common.viewutil.ContextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 画板功能
 *
 * @author wanggaowu
 */
public class PaintView extends View implements View.OnTouchListener {
    private static final String TAG = "PaintView";
    private static final int BOUND = 2;

    //正在绘制的数据集
    private Path drawingPath;  //画笔路径
    private Path drawingErase; //橡皮路径
    private RectF drawingRect;//矩形
    private RectF drawingCircle; //圆形
    private RectF drawingLine; //线条

    //固定的边界
    private Pair<Integer, RectF> drawBoundary = null;
    private int width, height;

    private Canvas bitmapCanvas;
    private Paint paint;
    private Paint textPaint;
    private Paint erasePaint;
    private PaintType paintType;
    private int paintColor = Color.BLACK;

    //步骤列表
    private List<GeometrySlice> cacheGeometrySlices = new ArrayList<>();

    private CommonAsyncListener<PaintType> paintTypeChangedListener;
    boolean isdraw;
    Bitmap bitmap = null;

    public PaintView(Context context) {
        this(context, null);
    }

    public PaintView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaintView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PaintView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    /**
     * 返回上一步
     */
    public void back2Previous() {
        if (!ListUtils.isEmpty(cacheGeometrySlices)) {
            cacheGeometrySlices.remove(cacheGeometrySlices.size() - 1);
            invalidate();
        }
    }

    /**
     * 设置画画类型
     *
     * @param paintType
     */
    public void setPaintType(PaintType paintType) {
        this.paintType = paintType;
        if (paintTypeChangedListener != null) {
            paintTypeChangedListener.doSomething(paintType);
        }
    }

    /**
     * 设置画笔颜色
     *
     * @param paintColor
     */
    public void setPaintColor(int paintColor) {
        this.paintColor = paintColor;
    }

    /**
     * 设置paintType变化监听
     *
     * @param paintTypeChangedListener
     */
    public void initPaintTypeChangedListener(CommonAsyncListener<PaintType> paintTypeChangedListener) {
        this.paintTypeChangedListener = paintTypeChangedListener;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        setPaintType(PaintType.PAINT);
        width = getWidth();
        height = getHeight();
        bitmapCanvas = new Canvas(bitmap = reCreateBitmap());
        bitmapCanvas.drawColor(Color.WHITE);
        drawBoundary = new Pair(paintColor, new RectF(BOUND, BOUND, width - BOUND, height - BOUND));
    }

    private void init() {
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(paintColor);
        paint.setStrokeWidth(8);

        erasePaint = new Paint();
        erasePaint.setAntiAlias(true);
        erasePaint.setStyle(Paint.Style.STROKE);
        erasePaint.setColor(Color.WHITE);
        erasePaint.setStrokeWidth(30);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(paintColor);
        textPaint.setTextSize(30);

        isdraw = false;
        setOnTouchListener(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制缓冲区元素
        drawBufferCanvas(canvas);
        //绘制最终的元素
        drawFinalCanvas(canvas);
    }

    //缓冲区包括：path(实时buffer绘制一次), 动画补间矩形，动画补间圆，动画补间线条,边界
    private void drawBufferCanvas(Canvas canvas) {
        if (drawingPath != null) {
            paint.setColor(paintColor);
            canvas.drawPath(drawingPath, paint);
        }
        if (drawingRect != null) {
            paint.setColor(paintColor);
            canvas.drawRect(drawingRect, paint);
        }
        if (drawingCircle != null) {
            paint.setColor(paintColor);
            double longLine = Math.sqrt(Math.pow(drawingCircle.width(), 2) + Math.pow(drawingCircle.height(), 2));
            canvas.drawCircle(drawingCircle.centerX(), drawingCircle.centerY(), (float) (longLine * .5f), paint);
        }
        if (drawingLine != null) {
            paint.setColor(paintColor);
            canvas.drawLine(drawingLine.left, drawingLine.top, drawingLine.right, drawingLine.bottom, paint);
        }
        if (drawingErase != null) {
            canvas.drawPath(drawingErase, erasePaint);
        }
        if (drawBoundary != null) {
            paint.setColor(Color.BLACK);
            canvas.drawRect(drawBoundary.second, paint);
        }
    }

    //最终的元素： 路径、线条、矩形、圆、文字、橡皮
    private void drawFinalCanvas(Canvas canvas) {
        bitmapCanvas.setBitmap(bitmap = reCreateBitmap());
        for (GeometrySlice geometrySlice : cacheGeometrySlices) {
            Object slice = geometrySlice.slice;
            if (slice == null) continue;
            switch (geometrySlice.paintType) {
                case PAINT:
                    //路径
                    Pair<Integer, Path> drawPath = (Pair<Integer, Path>) slice;
                    paint.setColor(drawPath.first);
                    canvas.drawPath(drawPath.second, paint);
                    bitmapCanvas.drawPath(drawPath.second, paint);
                    break;
                case LINE:
                    //线条
                    Pair<Integer, RectF> drawLine = (Pair<Integer, RectF>) slice;
                    paint.setColor(drawLine.first);
                    canvas.drawLine(drawLine.second.left, drawLine.second.top, drawLine.second.right, drawLine.second.bottom, paint);
                    bitmapCanvas.drawLine(drawLine.second.left, drawLine.second.top, drawLine.second.right, drawLine.second.bottom, paint);
                    break;
                case RECT:
                    //矩形
                    Pair<Integer, RectF> drawRect = (Pair<Integer, RectF>) slice;
                    paint.setColor(drawRect.first);
                    canvas.drawRect(drawRect.second, paint);
                    bitmapCanvas.drawRect(drawRect.second, paint);
                    break;
                case CIRCLE:
                    //圆
                    Pair<Integer, RectF> drawCircle = (Pair<Integer, RectF>) slice;
                    paint.setColor(drawCircle.first);
                    double longLine = Math.sqrt(Math.pow(drawCircle.second.width(), 2) + Math.pow(drawCircle.second.height(), 2));
                    canvas.drawCircle(drawCircle.second.centerX(), drawCircle.second.centerY(), (float) (longLine * .5f), paint);
                    bitmapCanvas.drawCircle(drawCircle.second.centerX(), drawCircle.second.centerY(), (float) (longLine * .5f), paint);
                    break;
                case TEXT:
                    //文字
                    Pair<Integer, Pair<Point, String>> drawText = (Pair<Integer, Pair<Point, String>>) slice;
                    Point position = drawText.second.first;
                    String text = drawText.second.second;
                    textPaint.setColor(drawText.first);
                    canvas.drawText(text, position.x, position.y, textPaint);
                    bitmapCanvas.drawText(text, position.x, position.y, textPaint);
                    break;
                case ERASE:
                    //橡皮
                    Pair<Integer, Path> erasePath = (Pair<Integer, Path>) slice;
                    canvas.drawPath(erasePath.second, erasePaint);
                    bitmapCanvas.drawPath(erasePath.second, erasePaint);
                    break;
            }
        }
    }

    private Point downPoint = null;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        isdraw = true;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downPoint = new Point((int) event.getX(), (int) event.getY());
                switch (paintType) {
                    case PAINT:
                        drawingPath = new Path();
                        drawingPath.moveTo(downPoint.x, downPoint.y);
                        break;
                    case RECT:
                        drawingRect = null;
                        break;
                    case CIRCLE:
                        drawingCircle = null;
                        break;
                    case LINE:
                        drawingLine = null;
                        break;
                    case ERASE:
                        drawingErase = new Path();
                        drawingErase.moveTo(downPoint.x, downPoint.y);
                        break;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                switch (paintType) {
                    case PAINT:
                        drawingPath.lineTo(event.getX(), event.getY());
                        invalidate();
                        break;
                    case RECT:
                        drawingRect = new RectF(downPoint.x, downPoint.y, event.getX(), event.getY());
                        invalidate();
                        break;
                    case CIRCLE:
                        drawingCircle = new RectF(downPoint.x, downPoint.y, event.getX(), event.getY());
                        invalidate();
                        break;
                    case LINE:
                        drawingLine = new RectF(downPoint.x, downPoint.y, event.getX(), event.getY());
                        invalidate();
                        break;
                    case ERASE:
                        drawingErase.lineTo(event.getX(), event.getY());
                        invalidate();
                        break;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                switch (paintType) {
                    case PAINT:
                        Pair paintSnapShoot = new Pair(paintColor, drawingPath);
                        cacheGeometrySlices.add(new GeometrySlice(paintType, paintSnapShoot));
                        invalidate();
                        break;
                    case RECT:
                        Pair rectSnapShoot = new Pair(paintColor, drawingRect);
                        cacheGeometrySlices.add(new GeometrySlice(paintType, rectSnapShoot));
                        invalidate();
                        setPaintType(PaintType.PAINT);
                        break;
                    case CIRCLE:
                        Pair circleSnapShoot = new Pair(paintColor, drawingCircle);
                        cacheGeometrySlices.add(new GeometrySlice(paintType, circleSnapShoot));
                        invalidate();
                        setPaintType(PaintType.PAINT);
                        break;
                    case LINE:
                        Pair lineSnapShoot = new Pair(paintColor, drawingLine);
                        cacheGeometrySlices.add(new GeometrySlice(paintType, lineSnapShoot));
                        invalidate();
                        break;
                    case TEXT:
                        prepareDrawText(new Point((int) event.getX(), (int) event.getY()));
                        break;
                    case ERASE:
                        Pair eraseSnapShoot = new Pair(paintColor, drawingErase);
                        cacheGeometrySlices.add(new GeometrySlice(paintType, eraseSnapShoot));
                        invalidate();
                        break;
                }
                downPoint = null;
                drawingRect = null;
                drawingCircle = null;
                drawingLine = null;
                drawingPath = null;
                drawingErase = null;
                break;
        }
        return true;
    }

    /**
     * 绘制文本
     *
     * @param downPoint
     */
    private void prepareDrawText(Point downPoint) {
        new CommonGetTextDialog(text -> {
            Pair textSnapShoot = new Pair(paintColor, new Pair(downPoint, text));
            cacheGeometrySlices.add(new GeometrySlice(PaintType.TEXT, textSnapShoot));
            postInvalidate();
            setPaintType(PaintType.PAINT);
        }).show(((BaseActivity) ContextUtils.getActivity(getContext())).getSupportFragmentManager(), "");
    }

    public Bitmap getBitmap() {
        if (!isdraw)
            return null;
        return bitmap;
    }

    public void clear() {
        drawingPath = null;
        drawingErase = null;
        drawingRect = null;
        drawingCircle = null;
        drawingLine = null;

        cacheGeometrySlices.clear();

        bitmap = reCreateBitmap();
        invalidate();
    }

    private Bitmap reCreateBitmap() {
        if (bitmap != null && bitmap.isRecycled()) {
            bitmap.recycle();
        }
        Bitmap bitmap = Bitmap.createBitmap(width - BOUND, height - BOUND, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
        bitmapCanvas.drawColor(Color.WHITE);
        return bitmap;
    }


    /**
     * 绘制类型
     */
    public enum PaintType {
        PAINT, LINE, RECT, CIRCLE, TEXT, ERASE
    }

    /**
     * 每次绘制图形的切片，只要涉及像素添加均抽象为图形
     */
    public static class GeometrySlice {
        public PaintType paintType;

        public Object slice;

        public GeometrySlice(PaintType paintType, Object slice) {
            this.paintType = paintType;
            this.slice = slice;
        }
    }
}
