package com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.bizz;

import com.bytemiracle.resourcesurvey.R;
import com.bytemiracle.resourcesurvey.common.dbbean.DBFieldDict;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

/**
 * 类功能：字典配置的xml映射类
 *
 * @author gwwang
 * @date 2022/5/30 0030 21:31
 */
@XStreamAlias("layer")
public class XmlDictBean {

    public static int getCheckIntType(String checkType) {
        switch (checkType) {
            case "单选":
                return DBFieldDict.TYPE_SINGLE_CHECK;
            case "多选":
                return DBFieldDict.TYPE_MULTI_CHECK;
            case "文本":
            default:
                return DBFieldDict.TYPE_INPUT_CHECK;
        }
    }

    public static String getCheckStringType(int checkType) {
        switch (checkType) {
            case DBFieldDict.TYPE_SINGLE_CHECK:
                return "单选";
            case DBFieldDict.TYPE_MULTI_CHECK:
                return "多选";
            case DBFieldDict.TYPE_INPUT_CHECK:
            default:
                return "文本";
        }
    }

    public static android.util.Pair<String, Integer> getDisplayInfo(String checkType) {
        switch (checkType) {
            case "单选":
                return new android.util.Pair("单选", R.drawable.ic_single);
            case "多选":
                return new android.util.Pair("多选", R.drawable.ic_multi);
            case "文本":
            default:
                return new android.util.Pair("文本", R.drawable.ic_input);
        }
    }

    @XStreamAsAttribute
    public String layerName;

    @XStreamImplicit
    public List<Dict> dicts;

    @XStreamAlias("field")
    public static class Dict {
        @XStreamAsAttribute
        public String fieldName;
        @XStreamAsAttribute
        public String checkType;
        @XStreamImplicit
        public List<Pair> values;

        @Override
        public String toString() {
            return "Dict{" +
                    "fieldName='" + fieldName + '\'' +
                    ", checkType='" + checkType + '\'' +
                    ", values=" + values +
                    '}';
        }
    }

    @XStreamAlias("item")
    public static class Pair {
        @XStreamAsAttribute
        public String key;
        @XStreamAsAttribute
        public String value;

        public Pair(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String toString() {
            return "Pair{" +
                    "key='" + key + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "XmlDictBean{" +
                "layerName='" + layerName + '\'' +
                ", dicts=" + dicts +
                '}';
    }
}
