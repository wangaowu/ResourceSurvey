package com.bytemiracle.resourcesurvey.common.dbbean;

import android.text.TextUtils;

import com.bytemiracle.base.framework.utils.common.ListUtils;
import com.bytemiracle.base.framework.utils.json.JsonParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 类功能：字典处理基类
 *
 * @author gwwang
 * @date 2022/6/20 18:01
 */
public class FieldDict {

    /**
     * 将已有的字典字串转为list
     *
     * @param valuePool 字典字串
     */
    public static List<Pair> convertPool2List(String valuePool) {
        List<Pair> dictValues = new ArrayList<>();
        if (!TextUtils.isEmpty(valuePool) && !valuePool.contains("null")) {
            Pair[] fieldValues = JsonParser.fromJson(valuePool, Pair[].class);
            dictValues.addAll(Arrays.asList(fieldValues));
        }
        return dictValues;
    }

    /**
     * 更新字典值
     *
     * @param dto
     */
    public static void updateDictValues(DBFieldDict dto) {
        List<Pair> dictValues = dto.getDictValues();
        if (!ListUtils.isEmpty(dictValues)) {
            dto.setFieldValuePool(JsonParser.toJson(dictValues));
        } else {
            dto.setCheckType(DBFieldDict.TYPE_INPUT_CHECK);
        }
    }

    public static String getValueArrayString(FieldDict.Pair[] pairs, String keys) {
        String valueArrayString = "";
        if (!TextUtils.isEmpty(keys)) {
            String[] keyArrays = keys.split(",");
            valueArrayString = Arrays.stream(pairs)
                    .filter(pair -> Arrays.stream(keyArrays).anyMatch(s -> s.equals(pair.key)))
                    .map(pair -> pair.value)
                    .collect(Collectors.joining(","));
        }
        return valueArrayString;
    }

    public static List<String> getValueArrays(Pair[] pairs) {
        return Arrays.stream(pairs).map(pair -> pair.value).collect(Collectors.toList());
    }

    public static String getKeyArrayString(Pair[] items, List<CharSequence> checkedValues) {
        List<String> matchedKeys = Arrays.stream(items)
                .filter(pair -> checkedValues.stream().anyMatch(s -> s.equals(pair.value)))
                .map(pair -> pair.key)
                .collect(Collectors.toList());
        if (ListUtils.isEmpty(matchedKeys)) {
            return checkedValues.stream().collect(Collectors.joining(","));
        }
        return matchedKeys.stream().collect(Collectors.joining(","));
    }

    public static class Pair {
        public String key;
        public String value;

        public Pair(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}
