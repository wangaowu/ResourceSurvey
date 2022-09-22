package com.bytemiracle.resourcesurvey.modules.media.video;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.GridView;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.listener.CommonAsync2Listener;
import com.bytemiracle.base.framework.preview.media.MediaCommon;
import com.bytemiracle.base.framework.preview.media.adapter.BaseEditableAdapter;
import com.bytemiracle.base.framework.utils.file.MFileUtils;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.dialog.CommonConfirmDialog;
import com.bytemiracle.resourcesurvey.modules.media.BaseMediaFragment;
import com.luck.picture.lib.config.PictureConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;

/**
 * 类功能：多媒体视频界面
 *
 * @author gwwang
 * @date 2021/6/7 10:13
 */
@FragmentTag(name = "多媒体视频")
public class MediaVideoFragment extends BaseMediaFragment {
    private static final String TAG = "MediaVideoFragment";
    private static final int MAX_SUPPORT_COUNT = 4;

    //多媒体布局
    @BindView(R.id.grid_content)
    GridView gridVideos;

    private File videoDir;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_media_content;
    }

    @Override
    protected void initViews() {
        super.initViews();
        videoDir = getConfigMediaDir("视频");
        updateVideoGridView();
    }

    private void updateVideoGridView() {
        List<String> videoList = new ArrayList<>();
        if (videoDir.listFiles() != null) {
            videoList.addAll(Arrays.stream(videoDir.listFiles())
                    .map(file -> file.getAbsolutePath())
                    .filter(s -> !new File(s).isDirectory())
                    .collect(Collectors.toList()));
        }
        BaseEditableAdapter.OnClickListener onClickListener = new BaseEditableAdapter.OnClickListener() {
            @Override
            public void clickAddBtn() {
                //点击添加
                File destFile = new File(videoDir + File.separator + "video_" + System.currentTimeMillis() + ".mp4");
                Uri fileUri = null;
                // Android7.0以及以后的版本需要使用FileProvider的方式
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    fileUri = FileProvider.getUriForFile(getContext(), "com.bytemiracle.resourcesurvey.fileprovider", destFile);
                } else {
                    fileUri = Uri.fromFile(destFile);
                }
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                startActivityForResult(intent, PictureConfig.REQUEST_CAMERA);
            }

            @Override
            public void clickItem(int index) {
                //本地文件
                MFileUtils.openFile(getContext(), videoList.get(index));
            }

            @Override
            public void clickDelete(int removeIndex) {
                //删除条目
                new CommonConfirmDialog("提示", "确认移除该视频吗?", new CommonAsync2Listener<CommonConfirmDialog>() {
                    @Override
                    public void doSomething1(CommonConfirmDialog commonConfirmDialog) {
                        new File(videoList.get(removeIndex)).delete();
                        updateVideoGridView();
                        commonConfirmDialog.dismiss();
                    }

                    @Override
                    public void doSomething2(CommonConfirmDialog commonConfirmDialog) {
                        commonConfirmDialog.dismiss();
                    }
                }).show(getChildFragmentManager(), "");
            }
        };
        MediaCommon.initEditableVideos(gridVideos, MAX_SUPPORT_COUNT, videoList, onClickListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK || requestCode != PictureConfig.REQUEST_CAMERA) {
            return;
        }
        updateVideoGridView();
    }
}
