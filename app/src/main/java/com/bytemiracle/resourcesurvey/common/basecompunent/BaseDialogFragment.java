package com.bytemiracle.resourcesurvey.common.basecompunent;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bytemiracle.base.framework.fragment.AnnotationPresenter;
import com.bytemiracle.base.framework.view.ShadowLinearLayout;
import com.bytemiracle.base.framework.view.apptitle.AppTitleController;
import com.bytemiracle.resourcesurvey.R;
import com.xuexiang.xutil.display.ScreenUtils;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * 类功能：弹窗fragment的基类
 *
 * @author gwwang
 * @date 2021/5/22 15:27
 */
public abstract class BaseDialogFragment extends DialogFragment {
    protected AppTitleController appTitleController;
    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        View view = inflater.inflate(getLayoutId(), window.findViewById(android.R.id.content), false);
        unbinder = ButterKnife.bind(this, view);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        int screenWidth = (int) (ScreenUtils.getScreenWidth() * getWidthRatio());
        int screenHeight = (int) (ScreenUtils.getScreenHeight() * getHeightRatio());
        window.setLayout(screenWidth, screenHeight);
        setCancelable(false);
        return addCommonTitleLayout(view);
    }

    private boolean useCustomSize() {
        return false;
    }

    protected float getHeightRatio() {
        return .9f;
    }

    protected float getWidthRatio() {
        return .9f;
    }

    private View addCommonTitleLayout(View view) {
        ShadowLinearLayout linearLayout = new ShadowLinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        appTitleController = new AppTitleController(getContext());
        appTitleController.insert2Parent(linearLayout, 0);
        //设置阴影
        appTitleController.ivBack.post(() -> {
            int measuredHeight = appTitleController.ivBack.getMeasuredHeight();
            appTitleController.wrapShadowEffect(linearLayout, measuredHeight);
        });
        //设置title内容和返回事件
        appTitleController.tvTitle.setText(new AnnotationPresenter(this.getClass()).findDefinedFragmentTag());
        appTitleController.ivBack.setOnClickListener(v -> this.dismiss());
        //设置title的风格
        appTitleController.resetBackgroundColor(getContext().getColor(R.color.common_dark));
        appTitleController.setNeedLight(true);
        appTitleController.getRightButton().setTextColor(getContext().getColor(R.color.common_dark));
        linearLayout.addView(view);
        initViews(view);
        return linearLayout;
    }

    protected abstract void initViews(View view);

    protected abstract int getLayoutId();

    @Override
    public void onDestroyView() {
        unbinder.unbind();
        super.onDestroyView();
    }
}
