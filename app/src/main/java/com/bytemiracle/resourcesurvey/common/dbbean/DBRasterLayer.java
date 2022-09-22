package com.bytemiracle.resourcesurvey.common.dbbean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;

/**
 * 类功能：数据库内栅格底图结构
 *
 * @author gwwang
 * @date 2021/5/26 16:11
 */
@Entity
public class DBRasterLayer implements Serializable {

    private static final long serialVersionUID = 1564654616133L;

    @Id
    private Long id;

    /**
     * 工程id
     */
    private Long projectId;
    /**
     * 方式名称
     */
    private String filePath;

    @Generated(hash = 1114179371)
    public DBRasterLayer(Long id, Long projectId, String filePath) {
        this.id = id;
        this.projectId = projectId;
        this.filePath = filePath;
    }

    @Generated(hash = 1537519981)
    public DBRasterLayer() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return this.projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


}
