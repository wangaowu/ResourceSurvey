package org.jts.utils

import com.bytemiracle.base.framework.utils.common.ListUtils
import org.osmdroid.util.GeoPoint

/**
 * 类功能：集合操作类
 *
 * @author gwwang
 * @date 2022/4/28 15:56
 */
class MCollections {

    companion object {
        /**
         * 深拷贝点集合
         */
        open fun deepCopyPoints(pts: MutableList<GeoPoint>) =
            pts.map { GeoPoint(it.latitude, it.longitude) }

        /**
         * 深拷贝点集合的集合
         */
        open fun deepCopyPointsList(ptss: MutableList<MutableList<GeoPoint>>) =
            ptss.map { pts ->
                pts.map { GeoPoint(it.latitude, it.longitude) }
            }

        /**
         * 确认数据已经闭合
         */
        open fun ensureClosedPoints(pts: MutableList<GeoPoint>): MutableList<GeoPoint> {
            if (!ListUtils.isEmpty(pts) && pts.first() != pts.last()) {
                pts.add(pts.first())
            }
            return pts
        }
    }
}