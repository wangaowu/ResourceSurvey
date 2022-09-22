package org.osmdroid.edit.bean

/**
 * 类功能：操作步骤封装
 * @param pts 点坐标数据
 *
 * @author gwwang
 * @date 2022/3/8 11:42
 */
open class EditStepManager<T>(val pts: MutableList<T>, val circleQueue: Boolean) {

    data class Cache<T>(
        val action: Action,
        var element: T,
        var modifiedElement: T?,
        var indexBefore: Int,
        var isOperateSide: Boolean = false
    )

    enum class Action {
        ADD, DELETE, MODIFY
    }

    companion object {
        const val TAG = "EditStepCache"

        open fun <T> newInstance(dataList: MutableList<T>): EditStepManager<T> =
            EditStepManager(dataList)

        open fun <T> newInstance2(dataList: MutableList<T>): EditStepManager<T> =
            EditStepManager(dataList, false)
    }

    constructor(pts: MutableList<T>) : this(pts, true)

    //可以撤销的步骤
    private val canUndoSteps = arrayListOf<Cache<T>>()

    //可以恢复的步骤
    private val canRedoSteps = arrayListOf<Cache<T>>()

    /**
     * 撤销上次的操作
     */
    open fun undoCachedStep(): Boolean {
        if (canUndoSteps.isNotEmpty()) {
            val undoStep = canUndoSteps.last()
            when (undoStep.action) {
                Action.DELETE -> {
                    //删除的点-->插入到删除之前的位置
                    if (circleQueue && undoStep.isOperateSide) {
                        pts.add(0, undoStep.element)
                        pts.add(undoStep.element)
                    } else {
                        val indexBeforeDeleted = undoStep.indexBefore
                        pts.add(indexBeforeDeleted, undoStep.element)
                    }
                }
                Action.ADD -> {
                    //添加的点-->移除即可
                    pts.remove(undoStep.element)
                }
                Action.MODIFY -> {
                    //修改的点-->恢复原状
                    if (circleQueue && undoStep.isOperateSide) {
                        pts[0] = undoStep.element
                        pts[pts.lastIndex] = undoStep.element
                    } else {
                        pts[undoStep.indexBefore] = undoStep.element
                    }
                }
            }
            //撤销之后，添加到恢复步骤
            canRedoSteps.add(undoStep)
            canUndoSteps.remove(undoStep)
            return true
        } else {
            return false
        }
    }

    /**
     * 恢复操作
     */
    open fun redoCachedStep(): Boolean {
        if (canRedoSteps.isNotEmpty()) {
            val redoStep = canRedoSteps.last()
            when (redoStep.action) {
                Action.DELETE -> {
                    //删除的点-->执行删除
                    removeSpecialElement(redoStep.element, false)
                }
                Action.ADD -> {
                    //添加的点-->执行添加
                    insertElement(redoStep.indexBefore, redoStep.element, false)
                }
                Action.MODIFY -> {
                    //修改的点-->执行之前的点数据
                    if (circleQueue && redoStep.isOperateSide) {
                        pts[0] = redoStep.modifiedElement!!
                        pts[pts.lastIndex] = redoStep.modifiedElement!!
                    } else {
                        pts[redoStep.indexBefore] = redoStep.modifiedElement!!
                    }
                }
            }
            //撤销之后，添加到恢复步骤
            canUndoSteps.add(redoStep)
            canRedoSteps.remove(redoStep)
            return true
        } else {
            return false
        }
    }

    /**
     * 清空要素
     */
    open fun clearAll() {
        pts.clear()
        canUndoSteps.clear()
        canRedoSteps.clear()
    }

    /**
     * 移除指定点
     */
    open fun removeSpecialElement(element: T) {
        removeSpecialElement(element, true)
    }

    /**
     * 修改要素
     */
    open fun modifyElement(srcElement: T, modifiedElement: T) {
        modifyElement(srcElement, modifiedElement, true)
    }

    /**
     * 插入要素
     */
    open fun insertElement(suitableIndex: Int, element: T) {
        insertElement(suitableIndex, element, true)
    }

    private fun removeSpecialElement(element: T, invokeCache: Boolean) {
        if (isSideElement(element)) {
            //始末节点
            pts.removeFirst()
            pts.removeLast()
            if (invokeCache)
                canUndoSteps.add(Cache(Action.DELETE, element, null, 0, true))
        } else {
            //非始末节点
            val indexBeforeDeleted = pts.indexOf(element)
            pts.remove(element)
            if (invokeCache)
                canUndoSteps.add(Cache(Action.DELETE, element, null, indexBeforeDeleted))
        }
    }

    private fun modifyElement(srcElement: T, modifiedElement: T, invokeCache: Boolean) {
        if (isSideElement(srcElement)) {
            //始末节点
            pts[0] = modifiedElement
            pts[pts.lastIndex] = modifiedElement
            if (invokeCache)
                canUndoSteps.add(Cache(Action.MODIFY, srcElement, modifiedElement, 0, true))
        } else {
            //非始末节点
            val index = indexOf(srcElement)
            pts[index] = modifiedElement
            if (invokeCache)
                canUndoSteps.add(Cache(Action.MODIFY, srcElement, modifiedElement, index))
        }
    }

    private fun insertElement(suitableIndex: Int, element: T, invokeCache: Boolean) {
        pts.add(suitableIndex, element)
        if (invokeCache)
            canUndoSteps.add(Cache(Action.ADD, element, null, suitableIndex))
    }

    private fun indexOf(element: T) = pts.indexOf(element)

    private fun isSideElement(element: T): Boolean =
        circleQueue &&
                (pts.first() == element || pts.last() == element)
}