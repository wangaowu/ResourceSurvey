package com.bytemiracle.resourcesurvey.modules.main.popfragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.bytemiracle.base.framework.fragment.FragmentTag;
import com.bytemiracle.base.framework.listener.CommonAsync3Listener;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.basecompunent.BaseDialogFragment;
import com.bytemiracle.resourcesurvey.modules.main.Element;
import com.bytemiracle.resourcesurvey.modules.main.TreeViewAdapter;
import com.bytemiracle.resourcesurvey.modules.main.TreeViewItemClickListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;

/**
 * 类功能：选择坐标系统
 *
 * @author gwwang
 * @date 2021/5/24 10:44
 */
@FragmentTag(name = "选择坐标系统")
public class PrjSelectFragment extends BaseDialogFragment {
    /**
     * 树中的元素集合
     */
    private ArrayList<Element> elements;
    /**
     * 数据源元素集合
     */
    private ArrayList<Element> elementsData;
    private CommonAsync3Listener<Element, PrjSelectFragment> prjSelectListener;

    @BindView(R.id.treeview)
    ListView treeview;

    public PrjSelectFragment(CommonAsync3Listener<Element, PrjSelectFragment> prjSelectListener) {
        this.prjSelectListener = prjSelectListener;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_prj_select;
    }

    @Override
    protected void initViews(View view) {
        Context context = getContext();
        new Thread(() -> {
            try {
                HashMap<String, Integer> map = new HashMap<String, Integer>() {{
                    put("global_id", -1);
                }};
                elements = new ArrayList<>();
                elementsData = new ArrayList<>();
                addElement(context, elements, elementsData, "coors", 0, map.get("global_id"), map);

                PrjSelectFragment.this.getActivity().runOnUiThread(() -> {
                    LayoutInflater inflater = (LayoutInflater) PrjSelectFragment.this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    TreeViewAdapter treeViewAdapter = new TreeViewAdapter(elements, elementsData, inflater);
                    TreeViewItemClickListener treeViewItemClickListener = new TreeViewItemClickListener(treeViewAdapter, PrjSelectFragment.this, prjSelectListener, context);
                    treeview.setAdapter(treeViewAdapter);
                    treeview.setOnItemClickListener(treeViewItemClickListener);
                });
            } catch (Exception e) {

            }
        }).start();
    }

    private void addElement(Context context, List<Element> elements, List<Element> elementsData, String path, int level_p, int id_p, HashMap<String, Integer> map) {
        try {
            String[] fileNames = context.getAssets().list(path);
            if (fileNames.length > 0) {
                for (int i = 0; i < fileNames.length; i++) {
                    boolean hasChildren = context.getAssets().list(path + File.separator + fileNames[i]).length > 0;
                    map.put("global_id", map.get("global_id") + 1);
                    Element e = new Element(fileNames[i].split("\\.")[0], level_p + 1, map.get("global_id"), id_p, hasChildren, false, path + File.separator + fileNames[i]);

                    elementsData.add(e);
                    if (e.getLevel() == 1) {
                        elements.add(e);
                    }

                    if (hasChildren) {
                        addElement(context, elements, elementsData, path + File.separator + fileNames[i], e.getLevel(), e.getId(), map);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    @Override
    protected float getWidthRatio() {
        return .6f;
    }
}