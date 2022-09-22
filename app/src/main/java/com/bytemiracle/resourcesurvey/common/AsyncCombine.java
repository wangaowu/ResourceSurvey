package com.bytemiracle.resourcesurvey.common;

import com.bytemiracle.base.framework.listener.CommonAsyncListener;

/**
 * 类功能：协同异步任务
 *
 * @author gwwang
 * @date 2021/6/16 16:27
 */
public class AsyncCombine {
    private CommonAsyncListener allCompletedListener;
    private int asyncSize;
    private int completeIndex = 0;

    /**
     * 构造方法
     *
     * @param asyncSize            异步任务数量
     * @param allCompletedListener 全部任务完成监听
     */
    public AsyncCombine(int asyncSize, CommonAsyncListener allCompletedListener) {
        this.asyncSize = asyncSize;
        this.allCompletedListener = allCompletedListener;
    }

    public void completeSelf() {
        ++completeIndex;
        if (completeIndex == asyncSize) {
            allCompletedListener.doSomething(null);
        }
    }
}
