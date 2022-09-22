package com.bytemiracle.resourcesurvey.modules.main.popfragment;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.fragment.dynamicitem.DynamicItemPresenter;
import com.bytemiracle.base.framework.fragment.dynamicitem.ItemController;
import com.bytemiracle.base.framework.fragment.dynamicitem.ItemData;
import com.bytemiracle.base.framework.listener.CommonAsync2Listener;
import com.bytemiracle.base.framework.listener.QuickListListener;
import com.bytemiracle.base.framework.view.recyclerTools.QuickAdapter;
import com.bytemiracle.base.framework.view.recyclerTools.QuickList;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.ProjectUtils;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;
import com.bytemiracle.resourcesurvey.common.date.AppTime;
import com.bytemiracle.resourcesurvey.common.dbbean.DBProject;
import com.bytemiracle.resourcesurvey.common.dialog.CommonConfirmDialog;
import com.bytemiracle.resourcesurvey.osmdroid.overlay.MapElementsHolder;
import org.osmdroid.overlay.utils.MapOverlayUtils;
import com.scwang.smartrefresh.layout.adapter.SmartViewHolder;

import org.osmdroid.overlay.render.PackageOverlay;

import java.util.List;

import butterknife.BindView;

/**
 * 类功能：工程信息
 *
 * @author gwwang
 * @date 2021/5/24 10:44
 */
@FragmentTag(name = "工程信息")
public class ProjectDetailFragment extends BaseDialogFragment {

    @BindView(R.id.ll_summary_container)
    LinearLayout llSummaryContainer;
    @BindView(R.id.title_vector_layer_info)
    View titleVectorLayerInfo;
    @BindView(R.id.rv_content)
    RecyclerView rvContent;
    @BindView(R.id.btn_open)
    Button btnOpen;

    private ItemController nameItem;
    private ItemController createByItem;
    private ItemController prjSystemItem;
    private ItemController createTimeItem;

    private DBProject projectInfo;
    private CommonAsync2Listener<DBProject> clickListener;

    /**
     * 构造方法
     *
     * @param projectInfo   工程信息
     * @param clickListener 点击事件  1/删除 2/打开
     */
    public ProjectDetailFragment(DBProject projectInfo, CommonAsync2Listener<DBProject> clickListener) {
        this.projectInfo = projectInfo;
        this.clickListener = clickListener;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_pop_project_detail;
    }

    @Override
    protected void initViews(View view) {
        btnOpen.setVisibility(clickListener != null ? View.VISIBLE : View.GONE);
        if (clickListener != null) {
            //顶部栏删除事件
            initTopBarListeners();
            //打开事件
            btnOpen.setOnClickListener(v -> openProject(projectInfo));
        }
        //设置概览信息
        DynamicItemPresenter dynamicItemPresenter = new DynamicItemPresenter(llSummaryContainer);
        nameItem = dynamicItemPresenter.addItem(new ItemData(ItemData.ItemType.CONTENT, "工程名称").content("", false));
        createByItem = dynamicItemPresenter.addItem(new ItemData(ItemData.ItemType.CONTENT, "创建人员").content("", false));
        prjSystemItem = dynamicItemPresenter.addItem(new ItemData(ItemData.ItemType.CONTENT, "坐标系统").content("", false));
        createTimeItem = dynamicItemPresenter.addItem(new ItemData(ItemData.ItemType.CONTENT, "创建时间").content("", false));
        //工程信息
        updateProjectInfo();
        //图层信息
        updateLayerInfoList();
    }

    private void openProject(DBProject projectInfo) {
        new CommonConfirmDialog("打开工程  \"" + projectInfo.getName() + "\" 吗?",
                "将会关闭正在打开的工程",
                new CommonAsync2Listener<CommonConfirmDialog>() {
                    @Override
                    public void doSomething1(CommonConfirmDialog commonConfirmDialog) {
                        ProjectUtils.switchProject(projectInfo.getName());
                        commonConfirmDialog.dismiss();
                        dismiss();
                        clickListener.doSomething2(projectInfo);
                    }

                    @Override
                    public void doSomething2(CommonConfirmDialog commonConfirmDialog) {
                        commonConfirmDialog.dismiss();
                    }
                }).show(getChildFragmentManager(), "");
    }

    private void initTopBarListeners() {
        appTitleController.getRightButton().setVisibility(View.VISIBLE);
        appTitleController.getRightButton().setText("删除");
        appTitleController.getRightButton().setOnClickListener(v ->
                new CommonConfirmDialog("提示", "删除工程将会移除所有数据",
                        new CommonAsync2Listener<CommonConfirmDialog>() {
                            @Override
                            public void doSomething1(CommonConfirmDialog commonConfirmDialog) {
                                deleteProject(commonConfirmDialog);
                            }

                            @Override
                            public void doSomething2(CommonConfirmDialog commonConfirmDialog) {
                                commonConfirmDialog.dismiss();
                            }
                        }).show(getFragmentManager(), ""));
    }

    //删除工程
    private void deleteProject(CommonConfirmDialog commonConfirmDialog) {
        commonConfirmDialog.dismiss();
        dismiss();
        //1为删除
        clickListener.doSomething1(projectInfo);
    }

    private void updateProjectInfo() {
        createByItem.findTextView().setText(projectInfo.getCreateBy());
        createTimeItem.findTextView().setText(AppTime.formatTimestamps(projectInfo.getCreateTimestamps()));
        nameItem.findTextView().setText(projectInfo.getName());
        prjSystemItem.findTextView().setText(projectInfo.getSpatialReferenceSimpleName());
    }

    private void updateLayerInfoList() {
        titleVectorLayerInfo.setBackgroundResource(R.color.gray_0_divider);
        List<PackageOverlay> mapOverlays = MapOverlayUtils.getMapGPKGFoldOverlays(MapElementsHolder.getMapView());
        QuickList.instance().adapter(rvContent, R.layout.item_vector_layer_info, mapOverlays,
                new QuickListListener<PackageOverlay>() {
                    @Override
                    public void onBindItem(QuickAdapter<PackageOverlay> quickAdapter, SmartViewHolder h, PackageOverlay mapOverlay) {
                        h.text(R.id.tv_layer_element_count, String.valueOf(mapOverlay.getItems().size()));
                        h.text(R.id.tv_layer_name, mapOverlay.getName());
                        h.text(R.id.tv_layer_type, MapOverlayUtils.matchTypeText(mapOverlay));
                    }
                });
    }
}