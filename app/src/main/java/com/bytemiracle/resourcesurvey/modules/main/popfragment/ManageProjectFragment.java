package com.bytemiracle.resourcesurvey.modules.main.popfragment;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.listener.CommonAsync2Listener;
import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.base.framework.listener.QuickListListener;
import com.bytemiracle.base.framework.utils.ImServerFileUtils;
import com.bytemiracle.base.framework.utils.XToastUtils;
import com.bytemiracle.base.framework.utils.common.ListUtils;
import com.bytemiracle.base.framework.utils.sp.SerializeUtils;
import com.bytemiracle.base.framework.view.recyclerTools.QuickAdapter;
import com.bytemiracle.base.framework.view.recyclerTools.QuickList;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.cache_greendao.DBFieldDictDao;
import com.bytemiracle.resourcesurvey.cache_greendao.DBProjectDao;
import com.bytemiracle.resourcesurvey.common.EventCluster;
import com.bytemiracle.resourcesurvey.common.FileConstant;
import com.bytemiracle.resourcesurvey.common.ProjectUtils;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;
import com.bytemiracle.resourcesurvey.common.database.GreenDaoManager;
import com.bytemiracle.resourcesurvey.common.date.AppTime;
import com.bytemiracle.resourcesurvey.common.dbbean.DBFieldDict;
import com.bytemiracle.resourcesurvey.common.dbbean.DBProject;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;
import com.bytemiracle.resourcesurvey.common.zip.MZipUtils;
import com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.bizz.XMLUtils;
import com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.bizz.XmlDictBean;
import com.scwang.smartrefresh.layout.adapter.SmartViewHolder;
import com.xuexiang.xutil.file.FileIOUtils;
import com.xuexiang.xutil.file.FileUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import butterknife.BindView;
import me.rosuh.filepicker.config.FilePickerManager;

/**
 * 类功能：工程管理
 *
 * @author gwwang
 * @date 2021/5/22 15:04
 */
@FragmentTag(name = "工程管理")
public class ManageProjectFragment extends BaseDialogFragment {
    private static final String TAG = "ManageProjectFragment";

    private static final int PICK_ZIP_CODE = 1021;

    @BindView(R.id.rv_content)
    RecyclerView rvContent;
    @BindView(R.id.view_none_cur_prj_tips)
    View viewNoneCurPrjTips;
    @BindView(R.id.cur_project_layout)
    View curProjectLayout;
    @BindView(R.id.btn_import_project)
    View btnImportProject;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_pop_manage_project;
    }

    @Override
    protected void initViews(View view) {
        initViewListeners();
        updateProjectList();

        DBProject openingProject = GlobalObjectHolder.getOpeningProject();
        if (openingProject == null || openingProject.getId() == null) {
            //没有正打开的工程
            viewNoneCurPrjTips.setVisibility(View.VISIBLE);
            curProjectLayout.setVisibility(View.GONE);
            return;
        }
        setTopCurrentProject(openingProject);
    }

    private void setTopCurrentProject(DBProject curProject) {
        viewNoneCurPrjTips.setVisibility(View.GONE);
        curProjectLayout.setVisibility(View.VISIBLE);
        ((TextView) curProjectLayout.findViewById(R.id.tv_create_by)).setText(curProject.getCreateBy());
        ((TextView) curProjectLayout.findViewById(R.id.tv_create_time)).setText(AppTime.formatTimestamps(curProject.getCreateTimestamps()));
        ((TextView) curProjectLayout.findViewById(R.id.tv_project_name)).setText(curProject.getName());
        ((TextView) curProjectLayout.findViewById(R.id.tv_project_path)).setText(ProjectUtils.getProject(curProject.getName()).getAbsolutePath());
        ((TextView) curProjectLayout.findViewById(R.id.tv_prj_system)).setText(curProject.getSpatialReferenceSimpleName());
        curProjectLayout.setOnClickListener(v -> {
            ProjectDetailFragment projectDetailFragment = new ProjectDetailFragment(curProject, null);
            projectDetailFragment.show(getChildFragmentManager(), "");
        });
        curProjectLayout.findViewById(R.id.btn_export_project).setOnClickListener(v -> asyncBackupProject(curProject));
    }

    private void updateProjectList() {
        DBProjectDao dbProjectDao = GreenDaoManager.getInstance().getDaoSession().getDBProjectDao();
        List<DBProject> allProjects = dbProjectDao.queryBuilder().list();
        List<DBProject> filterCurProjects = allProjects.stream()
                .filter(dbProject -> dbProject.getIsLatestProject() != 1)
                .collect(Collectors.toList());
        QuickList.instance().adapter(rvContent, R.layout.item_project_list_layout, filterCurProjects, new QuickListListener<DBProject>() {
            @Override
            public void onBindItem(QuickAdapter<DBProject> quickAdapter, SmartViewHolder h, DBProject dbProject) {
                h.text(R.id.tv_create_by, dbProject.getCreateBy());
                h.text(R.id.tv_create_time, AppTime.formatTimestamps(dbProject.getCreateTimestamps()));
                h.text(R.id.tv_project_name, dbProject.getName());
                h.text(R.id.tv_prj_system, dbProject.getSpatialReferenceSimpleName());
                h.text(R.id.tv_project_path, ProjectUtils.getProject(dbProject.getName()).getAbsolutePath());
                //点开工程
                h.itemView.setOnClickListener(v -> {
                    ProjectDetailFragment projectDetailFragment = new ProjectDetailFragment(dbProject, new CommonAsync2Listener<DBProject>() {
                        @Override
                        public void doSomething1(DBProject deleteProject) {
                            ProjectUtils.deleteProject(deleteProject);
                            updateProjectList();
                        }

                        @Override
                        public void doSomething2(DBProject dbProject) {
                            setTopCurrentProject(dbProject);
                            updateProjectList();
                            EventBus.getDefault().post(new EventCluster.EventChangeProject());
                        }
                    });
                    projectDetailFragment.show(getChildFragmentManager(), "");
                });
                //备份导出工程
                h.findView(R.id.btn_export_project).setOnClickListener(v -> asyncBackupProject(dbProject));
            }
        });
    }

    private void initViewListeners() {
        appTitleController.getRightButton().setVisibility(View.VISIBLE);
        appTitleController.getRightButton().setText("新建");
        appTitleController.getRightButton().setOnClickListener(v -> {
            new CreateNewProjectFragment(dbProject -> {
                ProjectUtils.switchProject(dbProject.getName());
                EventBus.getDefault().post(new EventCluster.EventChangeProject());
                setTopCurrentProject(GlobalObjectHolder.getOpeningProject());
                updateProjectList();
            }).show(getChildFragmentManager(), "");
        });
        btnImportProject.setOnClickListener(v -> {
            XToastUtils.info("请选择备份的文件!", Toast.LENGTH_LONG);
            FilePickerManager.from(this)
                    .enableSingleChoice()
                    .forResult(PICK_ZIP_CODE);
        });
    }

    private void asyncImportBackupZip(File projectZip) {
        GlobalInstanceHolder.newSingleExecutor().execute(() ->
                importBackupZip(projectZip, importError ->
                        GlobalInstanceHolder.mainHandler().post(() -> {
                            if (TextUtils.isEmpty(importError)) {
                                XToastUtils.info("导入成功!");
                                updateProjectList();
                            } else {
                                XToastUtils.info("导入失败!" + importError);
                            }
                        })
                )
        );
    }

    private void importBackupZip(File projectZip, CommonAsyncListener<String> importListener) {
        try {
            //1.解压文件到新建工程目录下
            String rSurveyProjectsDir = FileConstant.getExternalRootDir() + "/" + "工程列表";
            MZipUtils.upZipFile(projectZip, rSurveyProjectsDir);
            //2.插入文件信息到工程数据库
            String projectName = FileUtils.getFileNameNoExtension(projectZip);
            String infoContent = FileIOUtils.readFile2String(new File(ProjectUtils.getProject(projectName), projectName + ".info"));
            DBProjectDao dbProjectDao = GreenDaoManager.getInstance().getDaoSession().getDBProjectDao();
            long existCount = dbProjectDao.queryBuilder().count();
            DBProject waitImport = (DBProject) SerializeUtils.serializeToObject(infoContent);
            waitImport.setId(null);
            waitImport.setIsLatestProject(existCount == 0 ? 1 : 0);
            long projectId = dbProjectDao.insert(waitImport);
            //3.将xml配置写入数据库
            readLayersDictXml(ProjectUtils.getProject(projectName), projectId);
            //当没有工程时，将导入工程设为当前工程
            if (existCount == 0) {
                GlobalInstanceHolder.mainHandler().post(() -> {
                    setTopCurrentProject(waitImport);
                    ProjectUtils.switchProject(waitImport.getName());
                    EventBus.getDefault().post(new EventCluster.EventChangeProject());
                });
            }
            importListener.doSomething(null);
        } catch (Exception e) {
            importListener.doSomething(e.toString());
        }
    }

    private void readLayersDictXml(File projectRootFile, long projectId) {
        DBFieldDictDao dbFieldDictDao = GreenDaoManager.getInstance().getDaoSession().getDBFieldDictDao();
        for (File file : projectRootFile.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".xml")) {
                XmlDictBean dictConfig = XMLUtils.getDictConfig(file);
                for (XmlDictBean.Dict dict : dictConfig.dicts) {
                    DBFieldDict dbFieldDict = XMLUtils.DTOConverter.toDBFieldDict(dict);
                    dbFieldDict.setProjectId(projectId);
                    dbFieldDict.setLayerName(dictConfig.layerName);
                    dbFieldDictDao.insert(dbFieldDict);
                }
            }
        }
    }

    private void asyncBackupProject(DBProject project) {
        GlobalInstanceHolder.newSingleExecutor().execute(() ->
                backupProject(project, zipError ->
                        GlobalInstanceHolder.mainHandler().post(() -> {
                            if (TextUtils.isEmpty(zipError)) {
                                XToastUtils.info("备份成功!");
                            } else {
                                XToastUtils.info("备份失败!" + zipError);
                            }
                        })
                )
        );
    }

    private void backupProject(DBProject project, CommonAsyncListener<String> backupListener) {
        try {
            //1.将工程的sqlite数据写成文本
            String name = project.getName();
            File projectFile = ProjectUtils.getProject(name);
            String serializeContent = SerializeUtils.serialize(project);
            FileIOUtils.writeFileFromString(new File(projectFile, name + ".info"), serializeContent);
            //2.将工程内的图层字段字典导出成xml
            writeLayersDictXml(project, projectFile);
            //3.将1和工程的所有文件导出成zip
            String rSurveyDir = FileConstant.getExternalRootDir();
            MZipUtils.zipFile(projectFile, new File(rSurveyDir, name + ".zip"), null);
            backupListener.doSomething(null);
        } catch (Exception e) {
            backupListener.doSomething(e.toString());
        }
    }

    private void writeLayersDictXml(DBProject project, File projectFile) throws Exception {
        DBFieldDictDao dbFieldDictDao = GreenDaoManager.getInstance().getDaoSession().getDBFieldDictDao();
        List<DBFieldDict> list = dbFieldDictDao.queryBuilder().where(DBFieldDictDao.Properties.ProjectId.eq(project.getId())).list();
        if (!ListUtils.isEmpty(list)) {
            Set<String> layerNameSet = list.stream()
                    .map(dbFieldDict -> dbFieldDict.getLayerName())
                    .collect(Collectors.toSet());
            Map<String, List<DBFieldDict>> layerNameMap = new HashMap<>();
            for (String layerName : layerNameSet) {
                layerNameMap.put(layerName, new ArrayList<>());
            }
            for (DBFieldDict dbFieldDict : list) {
                layerNameMap.get(dbFieldDict.getLayerName()).add(dbFieldDict);
            }
            for (Map.Entry<String, List<DBFieldDict>> entry : layerNameMap.entrySet()) {
                String layerName = entry.getKey();
                XmlDictBean obj = XMLUtils.DTOConverter.toXmlDictBean(layerName, entry.getValue());
                File xmlFile = new File(projectFile, layerName + ".xml");
                XMLUtils.toXml(obj, xmlFile);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_ZIP_CODE && resultCode == Activity.RESULT_OK) {
            String zipFilePath =  FilePickerManager.INSTANCE.obtainData().get(0);;
            asyncImportBackupZip(new File(zipFilePath));
        }
    }
}
