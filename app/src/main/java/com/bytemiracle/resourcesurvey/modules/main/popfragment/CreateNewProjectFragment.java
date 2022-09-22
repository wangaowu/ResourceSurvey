package com.bytemiracle.resourcesurvey.modules.main.popfragment;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.base.framework.utils.XToastUtils;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.cache_greendao.DBProjectDao;
import com.bytemiracle.resourcesurvey.common.ProjectUtils;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;
import com.bytemiracle.resourcesurvey.common.database.GreenDaoManager;
import com.bytemiracle.resourcesurvey.common.date.AppTime;
import com.bytemiracle.resourcesurvey.common.dbbean.DBProject;
import com.bytemiracle.resourcesurvey.common.dbbean.SpatialSystemProcessor;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;
import com.bytemiracle.resourcesurvey.common.watermark.WaterMarkUtils;
import com.bytemiracle.resourcesurvey.giscommon.geopackage.GeoPackageFileUtils;
import com.bytemiracle.resourcesurvey.modules.main.Element;
import com.bytemiracle.resourcesurvey.modules.trajectory.TrajectoryConfigUtils;
import com.lzy.okgo.utils.IOUtils;
import com.xuexiang.xui.widget.picker.widget.TimePickerView;
import com.xuexiang.xui.widget.picker.widget.builder.TimePickerBuilder;
import com.xuexiang.xui.widget.picker.widget.configure.TimePickerType;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;

/**
 * 类功能：新建工程
 *
 * @author gwwang
 * @date 2021/5/24 10:44
 */
@FragmentTag(name = "新建工程")
public class CreateNewProjectFragment extends BaseDialogFragment {

    @BindView(R.id.name)
    EditText etName;

    @BindView(R.id.createBy)
    EditText etCreateBy;

    @BindView(R.id.prjSystem)
    EditText etPrjSystem;

    @BindView(R.id.createTimestamp)
    EditText etCreateTimestamp;

    @BindView(R.id.create)
    Button btnCreate;

    private CommonAsyncListener<DBProject> createSuccessListener;
    private TimePickerView mTimePickerDialog;
    private Element element;

    public CreateNewProjectFragment(CommonAsyncListener<DBProject> createSuccessListener) {
        this.createSuccessListener = createSuccessListener;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_new_project;
    }

    @Override
    protected void initViews(View view) {
        Date currentTime = new Date();
        etCreateTimestamp.setTag(currentTime);
        etCreateTimestamp.setText(AppTime.formatDateTime(currentTime));
        etPrjSystem.setOnClickListener(this::selectPrj);
        etCreateTimestamp.setOnClickListener(this::selectTime);
        btnCreate.setOnClickListener(this::createNewProject);
    }

    private void createNewProject(View view) {
        String name = etName.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            XToastUtils.info("工程名称不能为空!");
            return;
        }
        if (element == null) {
            XToastUtils.info("坐标系统不能为空!");
            return;
        }

        GlobalObjectHolder.getMainActivityObject().mLoadingDialog.show();
        //1.
        ProjectUtils.createProjectFileSystem(name);
        String sampleGeoPackage = ProjectUtils.getSampleGeoPackage().getAbsolutePath();
        String destGeoPackage = ProjectUtils.getProjectGeoPackage(name);
        //2.
        GeoPackageFileUtils.asyncCopyGeoPackageFile(sampleGeoPackage, destGeoPackage, copySuccess -> {
            if (!copySuccess) {
                GlobalObjectHolder.getMainActivityObject().mLoadingDialog.dismiss();
                XToastUtils.info("创建工程失败!");
                return;
            }
            //3.样板拷贝成功
            GlobalInstanceHolder.newSingleExecutor().execute(() -> {
                try {
                    Date date = (Date) etCreateTimestamp.getTag();
                    DBProject dbProject = new DBProject();
                    dbProject.setName(name);
                    dbProject.setCreateBy(etCreateBy.getText().toString().trim());
                    dbProject.setPrjWKT(getWkText(element.getPath()));
                    if (date != null) {
                        dbProject.setCreateTimestamps(date.getTime());
                    }
                    dbProject.setIsLatestProject(0);
                    dbProject.setId(null);

                    DBProjectDao dbProjectDao = GreenDaoManager.getInstance().getDaoSession().getDBProjectDao();
                    if (dbProjectDao.queryBuilder().where(DBProjectDao.Properties.Name.eq(dbProject.getName())).unique() != null) {
                        throw new Exception("已有同名工程,创建失败!");
                    }
                    dbProjectDao.insertOrReplace(dbProject);

                    //初始化该工程的水印配置和轨迹打点配置
                    WaterMarkUtils.initDefault(dbProject);
                    TrajectoryConfigUtils.initDefault(dbProject);

                    CreateNewProjectFragment.this.getActivity().runOnUiThread(() -> {
                        createSuccessListener.doSomething(dbProject);
                        GlobalObjectHolder.getMainActivityObject().mLoadingDialog.dismiss();
                        XToastUtils.info("创建成功");
                        dismiss();
                    });
                } catch (Exception e) {
                    CreateNewProjectFragment.this.getActivity().runOnUiThread(() -> {
                        GlobalObjectHolder.getMainActivityObject().mLoadingDialog.dismiss();
                        XToastUtils.error(e.getMessage());
                    });
                }
            });
        });
    }

    private String getWkText(String path) {
        InputStream is = null;
        BufferedReader br = null;
        try {
            is = CreateNewProjectFragment.this.getContext().getAssets().open(path);
            br = new BufferedReader(new InputStreamReader(is));
            return br.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(br);
        }
    }

    private void selectPrj(View view) {
        new PrjSelectFragment((element, prjSelectFragment) -> {
            prjSelectFragment.dismiss();
            etPrjSystem.setText(new SpatialSystemProcessor(getWkText(element.getPath())).getDisplayName());
            CreateNewProjectFragment.this.element = element;
        }).show(getChildFragmentManager(), "");
    }

    private void selectTime(View view) {
        if (mTimePickerDialog == null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            mTimePickerDialog = new TimePickerBuilder(getContext(), (date, v) -> {
                view.setTag(date);
                etCreateTimestamp.setText(AppTime.formatDateTime(date));
            })
                    .setTimeSelectChangeListener(date -> {
                    })
                    .setType(TimePickerType.ALL)
                    .setTitleText("工程创建时间")
                    .setTitleBgColor(getResources().getColor(R.color.common_dark))
                    .setTitleColor(getResources().getColor(R.color.white))
                    .setCancelColor(getResources().getColor(R.color.common_dark))
                    .setSubmitColor(getResources().getColor(R.color.common_dark))
                    .isDialog(true)
                    .setOutSideCancelable(false)
                    .setDate(calendar)
                    .build();
        }
        mTimePickerDialog.show();
    }
}