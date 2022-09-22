package com.bytemiracle.resourcesurvey.modules.main;
/**
 * Element类
 * @author carrey
 *
 */
public class Element {
    /** 文字内容 */
    private String contentText;
    /** 在tree中的层级 */
    private int level;
    /** 元素的id */
    private int id;
    /** 父元素的id */
    private int parendId;
    /** 是否有子元素 */
    private boolean hasChildren;
    /** item是否展开 */
    private boolean isExpanded;

    private String path;

    public Element(String contentText, int level, int id, int parendId,
                   boolean hasChildren, boolean isExpanded, String path) {
        super();
        this.contentText = contentText;
        this.level = level;
        this.id = id;
        this.parendId = parendId;
        this.hasChildren = hasChildren;
        this.isExpanded = isExpanded;
        this.path = path;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getParendId() {
        return parendId;
    }

    public void setParendId(int parendId) {
        this.parendId = parendId;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "Element{" +
                "contentText='" + contentText + '\'' +
                ", level=" + level +
                ", id=" + id +
                ", parendId=" + parendId +
                ", hasChildren=" + hasChildren +
                ", isExpanded=" + isExpanded +
                ", path='" + path + '\'' +
                '}';
    }
}