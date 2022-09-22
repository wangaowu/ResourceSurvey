package org.osmdroid.customImpl.shp;

import android.util.Log;

import com.lzy.okgo.utils.IOUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 类功能：矢量图层附加信息解析绑定器
 *
 * @author gwwang
 * @date 2021/5/25 11:22
 */
public class Shp_ {
    private static final String TAG = "Shp_";
    public static final String SHP_PRIMARY_KEY = "fid";


    public static class FileUtils {
        //前三个是必须，prj是投影坐标系，cpg是解决中文问题（由导出时生成）
        private static final String[] SHP_BUNDLE = {".shp", ".shx", ".dbf", ".prj", ".cpg"};

        private static List<String> getRelationFiles(String shpPath) {
            String shpNameNoExtension = com.xuexiang.xutil.file.FileUtils.getFileNameNoExtension(shpPath);
            String parentDir = new File(shpPath).getParent();
            return Arrays.stream(SHP_BUNDLE)
                    .map(extension -> parentDir + File.separator + shpNameNoExtension + extension)
                    .collect(Collectors.toList());
        }

        /**
         * 写入shp文件的坐标系
         *
         * @param wktContent
         * @param shpPath
         */
        public static boolean writeShpPrj(String wktContent, String shpPath) {
            FileWriter fileWriter = null;
            try {
                //构建xx.prj文件
                String shpName = com.xuexiang.xutil.file.FileUtils.getFileNameNoExtension(shpPath);
                String parentPath = new File(shpPath).getParentFile().getAbsolutePath();
                File prjFile = new File(parentPath, shpName + ".prj");
                //写入坐标系内容
                fileWriter = new FileWriter(prjFile);
                fileWriter.write(wktContent);
                fileWriter.flush();
            } catch (IOException e) {
                Log.e(TAG, "setShpPrj: 写入失败!");
                return false;
            } finally {
                IOUtils.closeQuietly(fileWriter);
                return true;
            }
        }

        /**
         * 是否完整的shp文件
         *
         * @param shpPath
         * @return
         */
        public static boolean isAvailableShp(String shpPath) {
            List<String> existRelationFiles = getExistFiles(getRelationFiles(shpPath));
            return checkAvailable(existRelationFiles);
        }

        private static List<String> getExistFiles(List<String> relationFiles) {
            return relationFiles.stream()
                    .filter(filePath -> new File(filePath).exists())
                    .collect(Collectors.toList());
        }

        private static boolean checkAvailable(List<String> relationFiles) {
            long count = relationFiles.stream()
                    .filter(s -> !s.endsWith(".prj"))//.prj不是所必须的文件 .cpg是必须文件（防止编码问题）
                    .count();
            return count >= 4;
        }
    }


    public static class GeometryUtils {
        private static final String TAG = "Shp_$Geometry";


        private static boolean invokeSet(Object instance, String field, Object value) {
            try {
                Class clazz = instance.getClass();
                Field declaredField = clazz.getDeclaredField(field);
                declaredField.setAccessible(true);
                declaredField.set(instance, value);
                return true;
            } catch (Exception e) {
                Log.e(TAG, "更新值失败: " + e.toString());
                return false;
            }
        }

        public static Object invokeGet(Object instance, String field) {
            try {
                Class clazz = instance.getClass();
                Field declaredField = clazz.getDeclaredField(field);
                declaredField.setAccessible(true);
                return declaredField.get(instance);
            } catch (Exception e) {
                Log.e(TAG, "更新值失败: " + e.toString());
                return null;
            }
        }
    }


}
