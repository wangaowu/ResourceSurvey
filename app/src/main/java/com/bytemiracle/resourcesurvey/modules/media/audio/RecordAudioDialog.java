package com.bytemiracle.resourcesurvey.modules.media.audio;

import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.listener.CommonAsync3Listener;
import com.bytemiracle.base.framework.utils.XToastUtils;
import com.bytemiracle.base.framework.view.volumeView.VolumeView;
import com.bytemiracle.base.framework.view.volumeView.VolumeViewController;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;
import com.bytemiracle.resourcesurvey.common.record.Pcm2WavUtils;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import butterknife.BindView;

/**
 * 类功能：录音机界面
 *
 * @author gwwang
 * @date 2021/6/7 15:28
 */
@FragmentTag(name = "录制音频")
public class RecordAudioDialog extends BaseDialogFragment {
    private static final String TAG = "RecordAudioDialog";
    //采样率
    private static int SAMPLE_RATE = 16000;
    //录制格式
    private static int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    //音频源
    private static int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    //单声道
    private static int CHANNEL_CONFIG = 1;

    @BindView(R.id.btn_function_speak)
    ImageButton btnSpeak;
    @BindView(R.id.tv_function_state)
    TextView tvFunctionState;
    @BindView(R.id.vl_view)
    VolumeView volumeView;

    private boolean isRecording = false;
    private File recordingFile = null;
    private AudioRecord audioRecord;

    private String savedDir;
    private CommonAsync3Listener<DialogFragment, String> onRecordSuccessListener;
    private VolumeViewController volumeViewController;

    /**
     * 构造方法
     *
     * @param savedDir                保存文件路径
     * @param onRecordSuccessListener 录制完成监听
     */
    public RecordAudioDialog(String savedDir, CommonAsync3Listener<DialogFragment, String> onRecordSuccessListener) {
        this.savedDir = savedDir;
        this.onRecordSuccessListener = onRecordSuccessListener;
    }

    @Override
    protected void initViews(View view) {
        this.volumeViewController = new VolumeViewController(volumeView);

        File savedDirFile = new File(savedDir);
        if (!savedDirFile.exists()) {
            savedDirFile.mkdirs();
        }
        btnSpeak.setOnClickListener(v -> {
            if (volumeViewController.isRecording()) {
                XToastUtils.info("正在录音中..");
                return;
            }
            //开始录音
            btnSpeak.setImageResource(R.drawable.ic_record_stop);
            tvFunctionState.setText("停止录制");
            volumeViewController.playAnimation();
            try {
                recordingFile = File.createTempFile("sound_", ".pcm", new File(savedDir));
                executeRecord();
            } catch (Exception e) {
            }

            //停止录音
            btnSpeak.setOnClickListener(v1 -> {
                volumeViewController.stopAnimation();
                stopRecord();
            });
        });
    }

    private void stopRecord() {
        if (null != audioRecord) {
            isRecording = false;
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
            GlobalInstanceHolder.newSingleExecutor().execute(() -> {
                //并发线程处理流
                String outputName = recordingFile.getName().split("\\.")[0] + ".wav";
                File outputFile = new File(recordingFile.getParent() + File.separator + outputName);
                new Pcm2WavUtils(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT).pcmToWav(recordingFile, outputFile);
                recordingFile.delete();
                GlobalInstanceHolder.mainHandler().post(() -> {
                    //主线程回调数据
                    onRecordSuccessListener.doSomething(RecordAudioDialog.this, outputFile.getAbsolutePath());
                });
            });
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_record_audio;
    }

    private void executeRecord() {
        GlobalInstanceHolder.newSingleExecutor().execute(() -> {
            isRecording = true;
            DataOutputStream dos = null;
            try {
                int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
                audioRecord = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);
                audioRecord.startRecording();
                byte[] buffer = new byte[bufferSize];
                dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(recordingFile)));
                while (isRecording) {
                    int bufferReadResult = audioRecord.read(buffer, 0, bufferSize);
                    dos.write(buffer, 0, bufferReadResult);
                }
                audioRecord.stop();
                dos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected float getHeightRatio() {
        return .3f;
    }

    @Override
    protected float getWidthRatio() {
        return .6f;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        stopRecord();
        super.onDismiss(dialog);
    }
}
