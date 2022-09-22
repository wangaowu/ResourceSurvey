package com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector;

import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.bytemiracle.base.framework.GlobalInstanceHolder;
import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.fragment.controller.DataPageController;
import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.base.framework.listener.QuickListListener;
import com.bytemiracle.base.framework.utils.common.ListUtils;
import com.bytemiracle.base.framework.view.recyclerTools.QuickAdapter;
import com.bytemiracle.base.framework.view.recyclerTools.QuickList;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.ProjectUtils;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;
import com.bytemiracle.resourcesurvey.common.dbbean.DBFieldDict;
import com.bytemiracle.resourcesurvey.common.global.GlobalObjectHolder;
import com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.bizz.XMLUtils;
import com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.bizz.XmlDictBean;
import com.scwang.smartrefresh.layout.adapter.SmartViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * 类功能：导入预览字典配置
 *
 * @author gwwang
 * @date 2021/5/26 10:50
 */
@FragmentTag(name = "字典预览")
public class PreviewImportFieldFragment extends BaseDialogFragment {
    private static final String TAG = "PreviewImportFieldFragment";
    private static final String DICT_FILE_NAME = "dict.xml";

    @BindView(R.id.rv_content)
    RecyclerView rvContent;
    @BindView(R.id.layout_none_data)
    View layoutNoneData;
    @BindView(R.id.tv_dict_path)
    TextView tvDictPath;
    @BindView(R.id.tv_dict_guide)
    TextView tvDictGuide;

    private String fieldName;
    private CommonAsyncListener<DBFieldDict> onResultListener;

    private QuickAdapter<XmlDictBean.Dict> quickAdapter;
    private DataPageController dataPageController;

    /**
     * 构造方法
     *
     * @param fieldName        字段名称
     * @param onResultListener 监听器
     */
    public PreviewImportFieldFragment(String fieldName, CommonAsyncListener<DBFieldDict> onResultListener) {
        this.fieldName = fieldName;
        this.onResultListener = onResultListener;
    }

    @Override
    protected void initViews(View view) {
        dataPageController = new DataPageController(rvContent, layoutNoneData);
        quickAdapter = initAdapter();
        appTitleController.tvTitle.setText("字段: " + fieldName);
        tvDictGuide.setText("备注:第一项为最佳匹配,您也可以选择其他项作为当前的字典配置");
        File project = ProjectUtils.getProject(GlobalObjectHolder.getOpeningProject().getName());
        File xmlFile = new File(project, DICT_FILE_NAME);
        if (!xmlFile.exists()) {
            tvDictPath.setText("暂无字典配置，请将文件存放于: " + xmlFile.getAbsolutePath());
            return;
        }
        tvDictPath.setText("字典存放路径: " + xmlFile.getAbsolutePath());
        dataPageController.showLoadingView();
        GlobalInstanceHolder.newSingleExecutor().execute(() -> {
            XmlDictBean dictConfig = XMLUtils.getDictConfig(xmlFile);
            GlobalInstanceHolder.mainHandler().post(() -> {
                dataPageController.showNoneView(false);
                quickAdapter.refresh(bringSelf2First(dictConfig.dicts));
            });
        });
    }

    private List<XmlDictBean.Dict> bringSelf2First(List<XmlDictBean.Dict> dicts) {
        XmlDictBean.Dict selfElement = null;
        for (XmlDictBean.Dict dict : dicts) {
            if (dict.fieldName.equals(fieldName)) {
                selfElement = dict;
                break;
            }
        }
        if (selfElement != null) {
            dicts.remove(selfElement);
            dicts.add(0, selfElement);
        }
        return dicts;
    }

    private QuickAdapter initAdapter() {
        rvContent.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        return QuickList.instance().adapter(
                rvContent,
                R.layout.item_layer_field_dict_preview,
                new ArrayList(),
                new QuickListListener<XmlDictBean.Dict>() {
                    @Override
                    public void onBindItem(QuickAdapter<XmlDictBean.Dict> adapter, SmartViewHolder holder, XmlDictBean.Dict model) {
                        TextView tvCheckType = holder.findViewById(R.id.tv_check_type);
                        //字段名称
                        holder.text(R.id.tv_field_name, model.fieldName);
                        //字典类型
                        Pair<String, Integer> typePair = XmlDictBean.getDisplayInfo(model.checkType);
                        tvCheckType.setText(typePair.first);
                        tvCheckType.setCompoundDrawablesWithIntrinsicBounds(typePair.second, 0, 0, 0);
                        //可选值
                        LinearLayout llDictContainer = holder.findViewById(R.id.ll_dict_container);
                        llDictContainer.removeAllViews();
                        if (!"文本类型".equals(typePair.first)) {
                            setDictItems(llDictContainer, model.values);
                        }
                        //点击事件
                        boolean isSuit = fieldName.equals(model.fieldName);
                        Button btn = holder.findViewById(R.id.ib_import);
                        btn.setText((isSuit ? "最佳" : "") + "匹配");
                        btn.setOnClickListener(view -> {
                            DBFieldDict dict = XMLUtils.DTOConverter.toDBFieldDict(model);
                            onResultListener.doSomething(dict);
                            dismiss();
                        });
                    }
                });
    }

    private void setDictItems(LinearLayout llDictContainer, List<XmlDictBean.Pair> values) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        if (!ListUtils.isEmpty(values)) {
            for (XmlDictBean.Pair p : values) {
                View previewItem = View.inflate(llDictContainer.getContext(), R.layout.item_preview_field_dict_value, null);
                ((TextView) previewItem.findViewById(R.id.tv_value_content)).setText(p.value);
                ((TextView) previewItem.findViewById(R.id.tv_value_key)).setText(p.key);
                llDictContainer.addView(previewItem, params);
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_layer_field_dict_preview;
    }
}
