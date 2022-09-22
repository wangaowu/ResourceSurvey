package com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.base.framework.utils.XToastUtils;
import com.bytemiracle.base.framework.utils.common.ListUtils;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.ProjectUtils;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;
import com.bytemiracle.resourcesurvey.common.dbbean.DBFieldDict;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;
import com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.adapter.SelectLayerNameAdapter;
import com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.bizz.DictItemPresenter;
import com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.bizz.FieldDictProvider;
import com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.bizz.SelectLayerName;
import com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.bizz.XMLUtils;
import com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.bizz.XmlDictBean;
import com.xuexiang.xui.widget.dialog.LoadingDialog;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import butterknife.BindView;
import me.rosuh.filepicker.config.FilePickerManager;

/**
 * 类功能：图层字典配置
 *
 * @author gwwang
 * @date 2021/5/26 10:50
 */
@FragmentTag(name = "字典配置")
public class ConfigLayerFieldPoolFragment extends BaseDialogFragment implements CommonAsyncListener<String> {
    private static final String TAG = "ConfigLayerFieldPoolFragment";
    private static final int PICK_XML = 101;

    @BindView(R.id.grid_layers)
    GridView gvLayers;
    @BindView(R.id.ll_field_container)
    LinearLayout llFieldContainer;

    private List<String> overlayNames;
    private CommonAsyncListener onDismissListener;

    private DictItemPresenter dictItemPresenter;
    private Map<String, List<DBFieldDict>> existFieldDict;

    /**
     * 构造方法
     *
     * @param overlayNames 图层名称
     */
    public ConfigLayerFieldPoolFragment(List<String> overlayNames) {
        this(overlayNames, null);
    }

    /**
     * 构造方法
     *
     * @param overlayNames      图层名称
     * @param onDismissListener 监听器
     */
    public ConfigLayerFieldPoolFragment(List<String> overlayNames, CommonAsyncListener onDismissListener) {
        this.overlayNames = overlayNames;
        this.onDismissListener = onDismissListener;
    }

    @Override
    protected void initViews(View view) {
        //title
        final String[] ITEMS = new String[]{"...", "导入", "导出"};
        appTitleController.getRightSpinner().setVisibility(View.VISIBLE);
        appTitleController.getRightSpinner()
                .setItems(ITEMS)
                .setSelectedIndex(0)
                .setOnItemSelectedListener((view1, selectedPosition, id, item) -> {
                    if ("导入".equals(item)) {
                        //导入
                        XToastUtils.info("请选择备份文件!");
                        FilePickerManager.from(this)
                                .enableSingleChoice()
                                .forResult(PICK_XML);
                    } else if ("导出".equals(item)) {
                        //导出
                        exportDictXml();
                    }
                });
        existFieldDict = FieldDictProvider.getFieldDict(getContext(), GlobalObjectHolder.getOpeningProject().getId());
        dictItemPresenter = new DictItemPresenter(getContext(), llFieldContainer);
        //initLayers
        List<SelectLayerName> layers = overlayNames.stream()
                .map(s -> new SelectLayerName(s, false))
                .collect(Collectors.toList());
        //默认选中第一个图层名称
        layers.get(0).setChecked(true);
        doSomething(layers.get(0).name);
        gvLayers.setAdapter(new SelectLayerNameAdapter(getContext(), layers, this));
    }

    private void exportDictXml() {
        if (existFieldDict == null || existFieldDict.size() == 0) {
            XToastUtils.info("数据为空，无需导出!");
            return;
        }
        LoadingDialog mLoadingDialog = GlobalObjectHolder.getMainActivityObject().mLoadingDialog;
        mLoadingDialog.show();
        mLoadingDialog.updateMessage("正在导出...");
        Executor executor = GlobalInstanceHolder.newSingleExecutor();
        for (Map.Entry<String, List<DBFieldDict>> entry : existFieldDict.entrySet()) {
            String layerName = entry.getKey();
            List<DBFieldDict> dictConfig = entry.getValue();
            XmlDictBean obj = XMLUtils.DTOConverter.toXmlDictBean(layerName, dictConfig);
            executor.execute(() -> {
                File project = ProjectUtils.getProject(GlobalObjectHolder.getOpeningProject().getName());
                File xmlFile = new File(project, layerName + ".xml");
                try {
                    XMLUtils.toXml(obj, xmlFile);
                    GlobalInstanceHolder.mainHandler().post(() -> {
                        mLoadingDialog.dismiss();
                        XToastUtils.info("导出成功!");
                    });
                } catch (Exception e) {
                    GlobalInstanceHolder.mainHandler().post(() -> {
                        mLoadingDialog.dismiss();
                        XToastUtils.info("导出失败:" + e.getMessage());
                    });
                }
            });
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_pop_config_field_pool;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_XML && resultCode == Activity.RESULT_OK) {
            try {
                File xmlConfigFile = new File(FilePickerManager.INSTANCE.obtainData().get(0));
                XmlDictBean dictConfig = XMLUtils.getDictConfig(xmlConfigFile);
                if (!ListUtils.isEmpty(dictConfig.dicts)) {
                    String layerName = dictConfig.layerName;
                    List<DBFieldDict> existDict = existFieldDict.get(layerName);
                    for (XmlDictBean.Dict dict : dictConfig.dicts) {
                        DBFieldDict waitChangeDBFieldDict = XMLUtils.DTOConverter.toDBFieldDict(dict);
                        updateFieldPool(existDict, waitChangeDBFieldDict);
                    }
                    //更新ui
                    doSomething(layerName);
                } else {
                    XToastUtils.info("没有匹配的数据，请检查字典文件配置!");
                }
            } catch (Exception e) {
                XToastUtils.info("字典文件配置错误: " + e.toString());
            }
        }
    }

    private void updateFieldPool(List<DBFieldDict> existDict, DBFieldDict waitChangeDBFieldDict) {
        if (!ListUtils.isEmpty(existDict)) {
            for (int i = 0; i < existDict.size(); i++) {
                DBFieldDict dbFieldDict = existDict.get(i);
                if (waitChangeDBFieldDict.getFieldName().equals(dbFieldDict.getFieldName())) {
                    dbFieldDict.setFieldValuePool(waitChangeDBFieldDict.getFieldValuePool());
                    dbFieldDict.setDictValues(waitChangeDBFieldDict.getDictValues());
                    dbFieldDict.setCheckType(waitChangeDBFieldDict.getCheckType());
                    break;
                }
            }
        }
    }

    //当图层名称选中时
    @Override
    public void doSomething(String selectedLayerName) {
        List<DBFieldDict> overlayFieldDict = this.existFieldDict.get(selectedLayerName);
        dictItemPresenter.updateFieldsUi(overlayFieldDict);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.doSomething(new Object());
        }
    }
}
