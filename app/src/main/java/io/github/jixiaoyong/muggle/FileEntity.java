package io.github.jixiaoyong.muggle;

public class FileEntity {
    private String name;
    private long lastModified;
    private String absolutePath;
    /**
     * -1 本地比在线新
     * 0 本地和在线一样
     * 1 在线比本地新
     * 2 本地文件，无在线版本
     */
    private int isSynced = 2;

    public FileEntity() {
    }

    public FileEntity(String name, long lastModified, String absolutePath) {
        this.name = name;
        this.lastModified = lastModified;
        this.absolutePath = absolutePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public int getIsSynced() {
        return isSynced;
    }

    public void setIsSynced(int isSynced) {
        this.isSynced = isSynced;
    }
}
