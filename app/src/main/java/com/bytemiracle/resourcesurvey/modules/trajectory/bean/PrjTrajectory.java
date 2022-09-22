package com.bytemiracle.resourcesurvey.modules.trajectory.bean;

import com.bytemiracle.base.framework.view.BaseCheckPojo;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

/**
 * 类功能：轨迹列表
 *
 * @author gwwang
 * @date 2021/7/19 8:42
 */
@Entity
public class PrjTrajectory extends BaseCheckPojo {

    @Id(autoincrement = true)
    private Long id;
    /**
     * 项目id
     */
    private long projectId;
    /**
     * 轨迹名称
     */
    private String trajectoryName;
    /**
     * 点数量
     */
    private long pointNumber;
    /**
     * 开始时间
     */
    private long startTime;
    /**
     * 结束时间
     */
    private long endTime;
    /**
     * 轨迹长度
     */
    private double trajectoryLength;
    /**
     * 是否选中(该字段暂时忽略)
     */
    private boolean isSelected;

    @Generated(hash = 752141208)
    public PrjTrajectory(Long id, long projectId, String trajectoryName,
                         long pointNumber, long startTime, long endTime, double trajectoryLength,
                         boolean isSelected) {
        this.id = id;
        this.projectId = projectId;
        this.trajectoryName = trajectoryName;
        this.pointNumber = pointNumber;
        this.startTime = startTime;
        this.endTime = endTime;
        this.trajectoryLength = trajectoryLength;
        this.isSelected = isSelected;
    }

    @Generated(hash = 2013158821)
    public PrjTrajectory() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getProjectId() {
        return this.projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getTrajectoryName() {
        return this.trajectoryName;
    }

    public void setTrajectoryName(String trajectoryName) {
        this.trajectoryName = trajectoryName;
    }

    public long getPointNumber() {
        return this.pointNumber;
    }

    public void setPointNumber(long pointNumber) {
        this.pointNumber = pointNumber;
    }

    public long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return this.endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public double getTrajectoryLength() {
        return this.trajectoryLength;
    }

    public void setTrajectoryLength(double trajectoryLength) {
        this.trajectoryLength = trajectoryLength;
    }

    public boolean getIsSelected() {
        return this.isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }


}
