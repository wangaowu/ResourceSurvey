package com.bytemiracle.resourcesurvey.modules.trajectory.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 类功能：轨迹点
 *
 * @author gwwang
 * @date 2021/7/19 14:46
 */
@Entity
public class TrajectoryPoint {
    @Id(autoincrement = true)
    private Long id;
    /**
     * 归属id（属于哪一条轨迹）
     */
    private Long belongId;
    /**
     * 记录时间
     */
    private long recordTime;
    /**
     * 经度
     */
    private double longitude;
    /**
     * 纬度
     */
    private double latitude;
    /**
     * 海拔高度
     */
    private double altitude;
    @Generated(hash = 1355332340)
    public TrajectoryPoint(Long id, Long belongId, long recordTime,
            double longitude, double latitude, double altitude) {
        this.id = id;
        this.belongId = belongId;
        this.recordTime = recordTime;
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
    }
    @Generated(hash = 1929219761)
    public TrajectoryPoint() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getBelongId() {
        return this.belongId;
    }
    public void setBelongId(Long belongId) {
        this.belongId = belongId;
    }
    public long getRecordTime() {
        return this.recordTime;
    }
    public void setRecordTime(long recordTime) {
        this.recordTime = recordTime;
    }
    public double getLongitude() {
        return this.longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    public double getLatitude() {
        return this.latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getAltitude() {
        return this.altitude;
    }
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }
}
