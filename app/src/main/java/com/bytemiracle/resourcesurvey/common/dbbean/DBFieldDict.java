package com.bytemiracle.resourcesurvey.common.dbbean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.util.List;

/**
 * 类功能：图层内属性字典
 *
 * @author gwwang
 * @date 2022/5/10 15:22
 */
@Entity
public class DBFieldDict {
    //输入
    public static final int TYPE_INPUT_CHECK = 0;
    //单选
    public static final int TYPE_SINGLE_CHECK = 1;
    //多选
    public static final int TYPE_MULTI_CHECK = 2;

    @Id(autoincrement = true)
    private Long id;
    /**
     * 工程id
     */
    private Long projectId;
    /**
     * 图层名称
     */
    private String layerName;
    /**
     * 选择类型
     */
    private int checkType;
    /**
     * 字段名称
     */
    private String fieldName;
    /**
     * 字段值
     */
    private String fieldValuePool;
    /**
     * 自定义字段，可选字典的容器
     */
    @Transient
    private List<FieldDict.Pair> dictValues;

    @Generated(hash = 2067639431)
    public DBFieldDict(Long id, Long projectId, String layerName, int checkType,
                       String fieldName, String fieldValuePool) {
        this.id = id;
        this.projectId = projectId;
        this.layerName = layerName;
        this.checkType = checkType;
        this.fieldName = fieldName;
        this.fieldValuePool = fieldValuePool;
    }

    @Generated(hash = 2019513748)
    public DBFieldDict() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return this.projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getLayerName() {
        return this.layerName;
    }

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public int getCheckType() {
        return this.checkType;
    }

    public void setCheckType(int checkType) {
        this.checkType = checkType;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldValuePool() {
        return this.fieldValuePool;
    }

    public void setFieldValuePool(String fieldValuePool) {
        this.fieldValuePool = fieldValuePool;
    }

    public List<FieldDict.Pair> getDictValues() {
        return dictValues;
    }

    public void setDictValues(List<FieldDict.Pair> dictValues) {
        this.dictValues = dictValues;
    }
}
