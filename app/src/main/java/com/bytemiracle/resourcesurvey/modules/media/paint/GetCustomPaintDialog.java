package com.bytemiracle.resourcesurvey.modules.media.paint;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.listener.CommonAsync3Listener;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;
import com.bytemiracle.resourcesurvey.common.basecompunent.TViewProxy;
import com.bytemiracle.resourcesurvey.common.view.PaintView;
import com.bytemiracle.resourcesurvey.common.watermark.WaterMarkUtils;

import butterknife.BindView;

/**
 * 类功能：用户绘画
 *
 * @author gwwang
 * @date 2021/3/18 14:13
 */
@FragmentTag(name = "自定义绘图")
public class GetCustomPaintDialog extends BaseDialogFragment {
    private static final String TAG = "GetCustomPaintDialog";

    @BindView(R.id.paint_view)
    PaintView paintView;
    @BindView(R.id.proxy_pencle)
    TViewProxy proxyPencle;
    @BindView(R.id.proxy_rect)
    TViewProxy proxyRect;
    @BindView(R.id.proxy_circle)
    TViewProxy proxyCircle;
    @BindView(R.id.proxy_erase)
    TViewProxy proxyErase;
    @BindView(R.id.proxy_text)
    TViewProxy proxyText;
    @BindView(R.id.proxy_line)
    TViewProxy proxyLine;
    @BindView(R.id.proxy_back)
    TViewProxy proxyBack;
    @BindView(R.id.iv_color)
    ImageView ivColor;

    private CommonAsync3Listener<GetCustomPaintDialog, Bitmap> paintResultListener;

    public GetCustomPaintDialog(CommonAsync3Listener<GetCustomPaintDialog, Bitmap> paintResultListener) {
        this.paintResultListener = paintResultListener;
    }

    @Override
    protected void initViews(View view) {
        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            paintResultListener.doSomething(GetCustomPaintDialog.this, paintView.getBitmap());
        });
        view.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            paintResultListener.doSomething(GetCustomPaintDialog.this, null);
        });
        view.findViewById(R.id.btn_clear).setOnClickListener(v -> {
            paintView.clear();
            paintView.setPaintType(PaintView.PaintType.PAINT);
        });
        initPaintControl();
    }

    private void initPaintControl() {
        //撤回
        proxyBack.setOnClickListener(v -> paintView.back2Previous());
        //画笔颜色
        ivColor.setBackgroundColor(Color.BLACK);
        ivColor.setOnClickListener(v ->
                new ColorPickerDialog(Color.BLACK, pickedColor -> {
                    ivColor.setBackgroundColor(pickedColor);
                    paintView.setPaintColor(pickedColor);
                    WaterMarkUtils.setNewConfigValue("颜色", true, String.valueOf(pickedColor));
                }).show(getChildFragmentManager(), "")
        );
        //paintType
        paintView.initPaintTypeChangedListener(paintType -> {
            proxyRect.setButtonState(TViewProxy.STATE.CLICKABLE);
            proxyCircle.setButtonState(TViewProxy.STATE.CLICKABLE);
            proxyErase.setButtonState(TViewProxy.STATE.CLICKABLE);
            proxyPencle.setButtonState(TViewProxy.STATE.CLICKABLE);
            proxyLine.setButtonState(TViewProxy.STATE.CLICKABLE);
            proxyText.setButtonState(TViewProxy.STATE.CLICKABLE);
            switch (paintType) {
                case CIRCLE:
                    proxyCircle.setButtonState(TViewProxy.STATE.ENABLE);
                    break;
                case RECT:
                    proxyRect.setButtonState(TViewProxy.STATE.ENABLE);
                    break;
                case LINE:
                    proxyLine.setButtonState(TViewProxy.STATE.ENABLE);
                    break;
                case PAINT:
                    proxyPencle.setButtonState(TViewProxy.STATE.ENABLE);
                    break;
                case TEXT:
                    proxyText.setButtonState(TViewProxy.STATE.ENABLE);
                    break;
                case ERASE:
                    proxyErase.setButtonState(TViewProxy.STATE.ENABLE);
                    break;
            }
        });
        proxyRect.setOnClickListener(v -> paintView.setPaintType(PaintView.PaintType.RECT));
        proxyCircle.setOnClickListener(v -> paintView.setPaintType(PaintView.PaintType.CIRCLE));
        proxyErase.setOnClickListener(v -> paintView.setPaintType(PaintView.PaintType.ERASE));
        proxyPencle.setOnClickListener(v -> paintView.setPaintType(PaintView.PaintType.PAINT));
        proxyLine.setOnClickListener(v -> paintView.setPaintType(PaintView.PaintType.LINE));
        proxyText.setOnClickListener(v -> paintView.setPaintType(PaintView.PaintType.TEXT));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_get_paint_bitmap;
    }

    @Override
    protected float getWidthRatio() {
        return .9f;
    }

    @Override
    protected float getHeightRatio() {
        return .9f;
    }
}
