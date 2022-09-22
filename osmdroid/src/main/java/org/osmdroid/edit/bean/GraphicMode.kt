package org.osmdroid.edit.bean

/**
 * 类功能：图形的编辑方式
 *
 * @author gwwang
 * @date 2022/2/8 14:03
 */
enum class GraphicMode {
    /**
     * 正常绘制
     */
    FINAL_GRAPHIC,

    /**
     * 删除HOLE
     */
    DELETE_HOLE,

    /**
     * 线分割
     */
    DIVIDE_POLYGON,

    /**
     * 面合并
     */
    COMBINE_POLYGON,

    /**
     * 创建HOLE
     */
    CREATE_UNION_HOLE,

    /**
     * 更新、删除顶点
     */
    UD_POINT,

    /**
     * 创建孔洞顶点
     */
    C_HOLE_POINT
}