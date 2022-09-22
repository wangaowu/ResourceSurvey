package com.bytemiracle.resourcesurvey.modules.media.picture;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.GridView;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
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
import com.luck.picture.lib.config.PictureConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;

/**
 * 类功能：媒体图片界面
 *
 * @author gwwang
 * @date 2021/6/7 10:13
 */
@FragmentTag(name = "多媒体图片")
public class MediaPictureFragment extends BaseMediaFragment {
    private static final String TAG = "MediaPictureFragment";
    private static final int MAX_SUPPORT_COUNT = 8;

    //多媒体布局
    @BindView(R.id.grid_content)
    GridView gridPictures;

    private File pictureDir;
    private File destFile;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_media_content;
    }

    @Override
    protected void initViews() {
        super.initViews();
        pictureDir = getConfigMediaDir("图片");
        updatePictureGridView();
    }

    private void updatePictureGridView() {
        List<String> pictureList = new ArrayList<>();
        if (pictureDir.listFiles() != null) {
            pictureList.addAll(Arrays.stream(pictureDir.listFiles())
                    .map(file -> file.getAbsolutePath())
                    .filter(s -> !new File(s).isDirectory())
                    .collect(Collectors.toList()));
        }
        BaseEditableAdapter.OnClickListener onClickListener = new BaseEditableAdapter.OnClickListener() {
            @Override
            public void clickAddBtn() {
                //点击添加
                destFile = new File(pictureDir + File.separator + "capture_" + System.currentTimeMillis() + ".jpg");
                Uri fileUri = null;
                // Android7.0以及以后的版本需要使用FileProvider的方式
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    fileUri = FileProvider.getUriForFile(getContext(), "com.bytemiracle.resourcesurvey.fileprovider", destFile);
                } else {
                    fileUri = Uri.fromFile(destFile);
                }
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(intent, PictureConfig.REQUEST_CAMERA);
            }

            @Override
            public void clickItem(int index) {
                //点击条目
                PreviewUtils.viewImage(getActivity(), pictureList.get(index), "多媒体图片");
            }

            @Override
            public void clickDelete(int removeIndex) {
                //删除条目
                new CommonConfirmDialog("提示", "确认移除该图片吗?", new CommonAsync2Listener<CommonConfirmDialog>() {
                    @Override
                    public void doSomething1(CommonConfirmDialog commonConfirmDialog) {
                        new File(pictureList.get(removeIndex)).delete();
                        updatePictureGridView();
                        commonConfirmDialog.dismiss();
                    }

                    @Override
                    public void doSomething2(CommonConfirmDialog commonConfirmDialog) {
                        commonConfirmDialog.dismiss();
                    }
                }).show(getChildFragmentManager(), "");
            }
        };
        MediaCommon.initEditablePictures(gridPictures, MAX_SUPPORT_COUNT, pictureList, onClickListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK || requestCode != PictureConfig.REQUEST_CAMERA) {
            return;
        }
        if (destFile != null) {
            mLoadingDialog.updateMessage("正在添加水印..");
            mLoadingDialog.show();
            GlobalInstanceHolder.newSingleExecutor().execute(() -> {
                String destPath = destFile.getAbsolutePath();
                Bitmap bitmap = BitmapFactory.decodeFile(destPath);
                List<String> watermarkContentRows = WaterMarkUtils.getWatermarkContentRows();
                Bitmap bitmapWithWatermark = WaterMarkUtils.addWatermark(bitmap, watermarkContentRows);
                destFile.delete();
                FileUtils.getInstance(getActivity()).saveBitmap(bitmapWithWatermark, destPath, saveSuccess -> {
                    if (saveSuccess) {
                        updatePictureGridView();
                    }
                    mLoadingDialog.dismiss();
                });
            });
        }
    }
}
