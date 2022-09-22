package com.bytemiracle.resourcesurvey.modules.media.paint;

import android.graphics.Bitmap;
import android.widget.GridView;

import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.listener.CommonAsync2Listener;
import com.bytemiracle.base.framework.preview.PreviewUtils;
import com.bytemiracle.base.framework.preview.media.MediaCommon;
import com.bytemiracle.base.framework.preview.media.adapter.BaseEditableAdapter;
import com.bytemiracle.base.framework.utils.file.FileUtils;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.dialog.CommonConfirmDialog;
import com.bytemiracle.resourcesurvey.common.watermark.WaterMarkUtils;
import com.bytemiracle.resourcesurvey.modules.media.BaseMediaFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;

/**
 * 类功能：多媒体绘图界面
 *
 * @author gwwang
 * @date 2021/6/7 10:13
 */
@FragmentTag(name = "多媒体绘图")
public class MediaCustomPaintFragment extends BaseMediaFragment {
    private static final String TAG = "MediaCustomPaintFragment";
    private static final int MAX_SUPPORT_COUNT = 16;

    @BindView(R.id.grid_content)
    GridView gridPaints;//绘图

    private File paintDir;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_media_content;
    }

    @Override
    protected void initViews() {
        super.initViews();
        paintDir = getConfigMediaDir("绘图");
        updatePaintsGridView();
    }

    private void updatePaintsGridView() {
        List<String> pictureList = new ArrayList<>();
        if (paintDir.listFiles() != null) {
            pictureList.addAll(Arrays.stream(paintDir.listFiles())
                    .map(file -> file.getAbsolutePath())
                    .filter(s -> !new File(s).isDirectory())
                    .collect(Collectors.toList()));
        }
        BaseEditableAdapter.OnClickListener onClickListener = new BaseEditableAdapter.OnClickListener() {
            @Override
            public void clickAddBtn() {
                //点击添加
                new GetCustomPaintDialog((getCustomPaintDialog, bitmap) -> {
                    if (bitmap != null) {
                        //保存签名
                        List<String> watermarkContentRows = WaterMarkUtils.getWatermarkContentRows();
                        Bitmap bitmapWithWatermark = WaterMarkUtils.addWatermark(bitmap, watermarkContentRows);
                        String signPath = paintDir + File.separator + "signOn" + System.currentTimeMillis() + ".jpg";
                        FileUtils.getInstance(getActivity()).saveBitmap(bitmapWithWatermark, signPath, saveSuccess -> {
                            if (saveSuccess) {
                                updatePaintsGridView();
                            }
                        });
                    }
                    getCustomPaintDialog.dismiss();
                }).show(getChildFragmentManager(), "");
            }

            @Override
            public void clickItem(int index) {
                //点击条目
                PreviewUtils.viewImage(getActivity(), pictureList.get(index), "多媒体图片");
            }

            @Override
            public void clickDelete(int removeIndex) {
                //删除条目
                new CommonConfirmDialog("提示", "确认移除该绘图吗?", new CommonAsync2Listener<CommonConfirmDialog>() {
                    @Override
                    public void doSomething1(CommonConfirmDialog commonConfirmDialog) {
                        new File(pictureList.get(removeIndex)).delete();
                        updatePaintsGridView();
                        commonConfirmDialog.dismiss();
                    }

                    @Override
                    public void doSomething2(CommonConfirmDialog commonConfirmDialog) {
                        commonConfirmDialog.dismiss();
                    }
                }).show(getChildFragmentManager(), "");
            }
        };
        MediaCommon.initEditablePictures(gridPaints, MAX_SUPPORT_COUNT, pictureList, onClickListener);
    }
}
