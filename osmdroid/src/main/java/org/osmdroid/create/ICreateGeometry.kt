package org.osmdroid.create

import mil.nga.sf.Geometry

/**
 * 创建图形的接口
 */
interface ICreateGeometry {

    fun getCreatedGeometry(): Geometry

    fun cancelDrawEventListener();

    fun clearGraphicInfo()

    fun removeEndMeasureNode()

    fun redoCachedNodes()
}