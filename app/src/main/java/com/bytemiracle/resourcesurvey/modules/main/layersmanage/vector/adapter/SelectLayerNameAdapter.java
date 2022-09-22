package com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.adapter;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bytemiracle.base.framework.listener.CommonAsyncListener;
import com.bytemiracle.base.framework.view.BaseCheckPojo;
import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.bizz.SelectLayerName;

import java.util.List;

/**
 * 类功能：TODO
 *
 * @author gwwang
 * @date 2022/5/11 9:28
 */
public class SelectLayerNameAdapter extends BaseAdapter {

    private final int DPX_5;
    private Context context;
    private List<SelectLayerName> layers;
    private CommonAsyncListener<String> onCheckNameListener;

    public SelectLayerNameAdapter(Context context, List<SelectLayerName> layers, CommonAsyncListener<String> onCheckNameListener) {
        this.context = context;
        this.layers = layers;
        this.onCheckNameListener = onCheckNameListener;
        this.DPX_5 = context.getResources().getDimensionPixelSize(R.dimen.dpx_5);
    }

    @Override
    public int getCount() {
        return layers.size();
    }

    @Override
    public Object getItem(int i) {
        return layers.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView textView = new TextView(context);
        SelectLayerName layer = layers.get(i);
        textView.setText(layer.name);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(0, DPX_5, 0, DPX_5);
        textView.setBackgroundResource(layer.isChecked() ?
                R.drawable.bg_app_text_radius_5_orange :
                R.drawable.bg_app_common_input_radius_5_stroke_rectangle);
        textView.setTextColor(layer.isChecked() ?
                context.getColor(R.color.white) :
                context.getColor(R.color.gray_1_divider));
        textView.setOnClickListener(v -> {
            BaseCheckPojo.checkedSingleItem(layers, i);
            notifyDataSetChanged();
            onCheckNameListener.doSomething(layer.name);
        });
        return textView;
    }
}
