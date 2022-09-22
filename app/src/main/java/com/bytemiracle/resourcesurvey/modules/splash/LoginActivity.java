package com.bytemiracle.resourcesurvey.modules.splash;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.bytemiracle.base.framework.component.BaseActivity;
import com.bytemiracle.base.framework.preview.PreviewUtils;
import com.bytemiracle.base.framework.utils.common.ListUtils;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.FileConstant;
import com.bytemiracle.resourcesurvey.common.database.GreenDaoManager;
import com.bytemiracle.resourcesurvey.modules.main.MainActivity;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import butterknife.BindView;

/**
 * 类功能：登陆页面
 *
 * @author gwwang
 * @date 2021/3/16 14:22
 */
public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";

    //所要申请的权限
    private static final Pair<String, String>[] PERM_DTO_LIST = new Pair[]{
            new Pair(Manifest.permission.READ_EXTERNAL_STORAGE, "工程备份导入/媒体存储"),
            new Pair(Manifest.permission.WRITE_EXTERNAL_STORAGE, "工程备份导入/媒体存储"),
            new Pair(Manifest.permission.CAMERA, "要素多媒体属性拍照"),
            new Pair(Manifest.permission.RECORD_AUDIO, "要素多媒体属性录音"),
            new Pair(Manifest.permission.ACCESS_FINE_LOCATION, "定位功能"),
            new Pair(Manifest.permission.ACCESS_COARSE_LOCATION, "定位功能"),
            new Pair(Manifest.permission.READ_PHONE_STATE, "手机状态辅助定位"),
            new Pair(Manifest.permission.ACCESS_NETWORK_STATE, "网络辅助定位"),
            new Pair(Manifest.permission.CHANGE_WIFI_STATE, "WIFI辅助定位"),
            new Pair(Manifest.permission.ACCESS_WIFI_STATE, "WIFI辅助定位")};


    @BindView(R.id.btn_login)
    View btnLogin;

    private ActivityResultLauncher<String[]> permissionLauncher;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initViewsWithSavedInstanceState(Bundle savedInstanceState) {
        if (!hasPermissions(PERM_DTO_LIST)) {
            permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), requestResult -> {
                List<Pair<String, String>> deniedPermissionList = Arrays.stream(PERM_DTO_LIST)
                        .filter(p -> {
                            String permKey = p.first;
                            return requestResult.containsKey(permKey) && !requestResult.get(permKey).booleanValue();
                        })
                        .collect(Collectors.toList());
                if (!ListUtils.isEmpty(deniedPermissionList)) {
                    Pair<String, String>[] deniedPermissionArray = new Pair[deniedPermissionList.size()];
                    deniedPermissionArray = deniedPermissionList.toArray(deniedPermissionArray);
                    showPermissionsIntroduce(deniedPermissionArray);
                }
            });
            showPermissionsIntroduce(PERM_DTO_LIST);
        }
        btnLogin.setOnClickListener(v -> {
            GreenDaoManager.getInstance().init(FileConstant.getDatabaseFile());
            PreviewUtils.openActivity(this, MainActivity.class);
            finish();
        });
    }

    private boolean hasPermissions(Pair<String, String>[] PERM_DTO_LIST) {
        for (Pair<String, String> perm : PERM_DTO_LIST) {
            if (ContextCompat.checkSelfPermission(this, perm.first)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void showPermissionsIntroduce(Pair<String, String>[] requestPermissions) {
        String permsIntroduce = Arrays.stream(requestPermissions)
                .map((Function<Pair<String, String>, String>) p -> p.first + "\n\t用途: " + p.second)
                .collect(Collectors.joining("\n\n"));
        new AlertDialog.Builder(this)
                .setTitle("权限说明")
                .setMessage(permsIntroduce)
                .setPositiveButton("我已了解", (dialogInterface, i) -> {
                    checkPermissions(requestPermissions);
                }).create().show();
    }

    private void checkPermissions(Pair<String, String>[] requestPermissions) {
        List<String> perms = Arrays.stream(requestPermissions)
                .map((Function<Pair<String, String>, String>) p -> p.first)
                .collect(Collectors.toList());
        String[] permArrays = new String[perms.size()];
        permArrays = perms.toArray(permArrays);
        permissionLauncher.launch(permArrays);
    }

    @Override
    protected int getStatusBarHeight() {
        return 0;
    }
}
