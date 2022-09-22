package com.bytemiracle.resourcesurvey.modules.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bytemiracle.resourcesurvey.R;

import java.util.ArrayList;

/**
 * TreeViewAdapter
 * @author carrey
 *
 */
public class TreeViewAdapter extends BaseAdapter {
    /** 元素数据源 */
    private ArrayList<Element> elementsData;
    /** 树中元素 */
    private ArrayList<Element> elements;
    /** LayoutInflater */
    private LayoutInflater inflater;
    /** item的行首缩进基数 */
    private int indentionBase;

    public TreeViewAdapter(ArrayList<Element> elements, ArrayList<Element> elementsData, LayoutInflater inflater) {
        this.elements = elements;
        this.elementsData = elementsData;
        this.inflater = inflater;
        indentionBase = 50;
    }

    public ArrayList<Element> getElements() {
        return elements;
    }

    public ArrayList<Element> getElementsData() {
        return elementsData;
    }

    @Override
    public int getCount() {
        return elements.size();
    }

    @Override
    public Object getItem(int position) {
        return elements.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.treeview_item, null);
            holder.disclosureImg = (ImageView) convertView.findViewById(R.id.disclosureImg);
            holder.contentText = (TextView) convertView.findViewById(R.id.contentText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Element element = elements.get(position);
        int level = element.getLevel();
        ImageView imageView = holder.disclosureImg;
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(imageView.getLayoutParams());
        lp.setMargins(indentionBase * (level + 1), lp.topMargin, lp.rightMargin, lp.bottomMargin);
        imageView.setLayoutParams(lp);
        holder.contentText.setText(element.getContentText());
        if (element.isHasChildren() && !element.isExpanded()) {
            holder.disclosureImg.setImageResource(R.drawable.tree_right);
            //这里要主动设置一下icon可见，因为convertView有可能是重用了"设置了不可见"的view，下同。
            holder.disclosureImg.setVisibility(View.VISIBLE);
        } else if (element.isHasChildren() && element.isExpanded()) {
            holder.disclosureImg.setImageResource(R.drawable.tree_down);
            holder.disclosureImg.setVisibility(View.VISIBLE);
        } else if (!element.isHasChildren()) {
            holder.disclosureImg.setImageResource(R.drawable.tree_right);
            holder.disclosureImg.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    /**
     * 优化Holder
     * @author carrey
     *
     */
    static class ViewHolder{
        ImageView disclosureImg;
        TextView contentText;
    }
}