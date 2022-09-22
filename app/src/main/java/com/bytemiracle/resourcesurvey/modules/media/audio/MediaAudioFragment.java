package com.bytemiracle.resourcesurvey.modules.media.audio;

import android.widget.GridView;

import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.listener.CommonAsync2Listener;
import com.bytemiracle.base.framework.preview.media.MediaCommon;
import com.bytemiracle.base.framework.preview.media.adapter.BaseEditableAdapter;
import com.bytemiracle.base.framework.utils.file.MFileUtils;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.dialog.CommonConfirmDialog;
import com.bytemiracle.resourcesurvey.modules.media.BaseMediaFragment;
import com.xuexiang.xutil.common.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import butterknife.BindView;

/**
 * 类功能：媒体录音界面
 *
 * @author gwwang
 * @date 2021/6/7 10:13
 */
@FragmentTag(name = "多媒体录音")
public class MediaAudioFragment extends BaseMediaFragment {
    private static final String TAG = "MediaAudioFragment";
    private static final int MAX_SUPPORT_COUNT = 4;

    @BindView(R.id.grid_content)
    GridView gridAudios;//音频

    private String audioDir;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_media_content;
    }

    @Override
    protected void initViews() {
        super.initViews();
        audioDir = getConfigMediaDir("录音").getAbsolutePath();
        updateAudiosGridView();
    }

    private void updateAudiosGridView() {
        List<String> audioList = new ArrayList<>();
        if (new File(audioDir).listFiles() != null) {
            audioList.addAll(Arrays.stream(new File(audioDir).listFiles())
                    .map(file -> file.getAbsolutePath())
                    .filter(s -> !new File(s).isDirectory())
                    .collect(Collectors.toList()));
        }
        BaseEditableAdapter.OnClickListener onClickListener = new BaseEditableAdapter.OnClickListener() {
            @Override
            public void clickAddBtn() {
                //点击录音
                new RecordAudioDialog(audioDir, (dialog, audioPath) -> {
                    if (!StringUtils.isEmpty(audioPath)) {
                        updateAudiosGridView();
                    }
                    dialog.dismiss();
                }).show(getChildFragmentManager(), "");
            }

            @Override
            public void clickItem(int index) {
                //本地文件
                MFileUtils.openFile(getContext(), audioList.get(index));
            }

            @Override
            public void clickDelete(int removeIndex) {
                //删除条目
                new CommonConfirmDialog("提示", "确认移除该录音吗?", new CommonAsync2Listener<CommonConfirmDialog>() {
                    @Override
                    public void doSomething1(CommonConfirmDialog commonConfirmDialog) {
                        new File(audioList.get(removeIndex)).delete();
                        updateAudiosGridView();
                        commonConfirmDialog.dismiss();
                    }

                    @Override
                    public void doSomething2(CommonConfirmDialog commonConfirmDialog) {
                        commonConfirmDialog.dismiss();
                    }
                }).show(getChildFragmentManager(), "");
            }
        };
        MediaCommon.initEditableAudios(gridAudios, MAX_SUPPORT_COUNT, audioList, onClickListener);
    }
}
