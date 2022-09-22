package com.bytemiracle.resourcesurvey.modules.main.layersmanage.vector.bizz;

import com.bytemiracle.base.framework.utils.common.ListUtils;
import com.bytemiracle.base.framework.utils.json.JsonParser;
import com.bytemiracle.resourcesurvey.common.dbbean.DBFieldDict;
import com.bytemiracle.resourcesurvey.common.dbbean.FieldDict;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 类功能：TODO
 *
 * @author gwwang
 * @date 2022/6/2 16:25
 */
public class XMLUtils {

    //解析xml
    public static XmlDictBean getDictConfig(File xmlFile) {
        XStream xStream = new XStream(new DomDriver());
        xStream.processAnnotations(XmlDictBean.class); //应用类的注解
        xStream.processAnnotations(XmlDictBean.Pair.class); //应用类的注解
        xStream.autodetectAnnotations(true);//自动检测注解
        xStream.alias("field", XmlDictBean.Dict.class);
        xStream.alias("item", XmlDictBean.Pair.class);
        return (XmlDictBean) xStream.fromXML(xmlFile, new XmlDictBean());
    }


    //导出xml
    public static void toXml(XmlDictBean obj, File xmlFile) throws Exception {
        XStream xStream = new XStream(new DomDriver());
        xStream.processAnnotations(XmlDictBean.class); //应用类的注解
        xStream.processAnnotations(XmlDictBean.Pair.class); //应用类的注解
        xStream.autodetectAnnotations(true);//自动检测注解
        xStream.alias("item", XmlDictBean.Pair.class);
        xStream.toXML(obj, new FileOutputStream(xmlFile));
    }

    //类转换
    public static class DTOConverter {
        /**
         * 将数据库配置对象转换为xml待写对象
         *
         * @param layerName
         * @param dictConfig
         * @return
         */
        public static XmlDictBean toXmlDictBean(String layerName, List<DBFieldDict> dictConfig) {
            XmlDictBean xmlDictBean = new XmlDictBean();
            xmlDictBean.layerName = layerName;
            xmlDictBean.dicts = new ArrayList<>();
            if (!ListUtils.isEmpty(dictConfig)) {
                for (DBFieldDict dict : dictConfig) {
                    XmlDictBean.Dict xmlDict = new XmlDictBean.Dict();
                    xmlDict.fieldName = dict.getFieldName();
                    if (!ListUtils.isEmpty(dict.getDictValues())) {
                        xmlDict.values = dict.getDictValues().stream().map(d -> new XmlDictBean.Pair(d.key, d.value)).collect(Collectors.toList());
                    } else {
                        xmlDict.values = new ArrayList<>();
                    }
                    xmlDict.checkType = XmlDictBean.getCheckStringType(dict.getCheckType());
                    xmlDictBean.dicts.add(xmlDict);
                }
            }
            return xmlDictBean;
        }


        /**
         * 将xml对象转换为数据库对象
         *
         * @param model
         * @return
         */
        public static DBFieldDict toDBFieldDict(XmlDictBean.Dict model) {
            DBFieldDict dict = new DBFieldDict();
            dict.setFieldName(model.fieldName);
            dict.setCheckType(XmlDictBean.getCheckIntType(model.checkType));
            if (!ListUtils.isEmpty(model.values)) {
                dict.setDictValues(model.values.stream().map(md -> new FieldDict.Pair(md.key, md.value)).collect(Collectors.toList()));
            } else {
                dict.setDictValues(new ArrayList<>());
            }
            dict.setFieldValuePool(JsonParser.toJson(model.values));
            return dict;
        }
    }
}
