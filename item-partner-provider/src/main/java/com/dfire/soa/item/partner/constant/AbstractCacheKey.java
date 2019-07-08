package com.dfire.soa.item.partner.constant;

public abstract class AbstractCacheKey {
    protected static final String PREFIX = "item-partner";
    protected static final String SPLITTER = ":";
    protected StringBuilder sb = new StringBuilder();
    private String version = "v1"; //版本号

    public AbstractCacheKey(String table) {
        sb.append(PREFIX).append(SPLITTER).append(table).append(SPLITTER);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
        sb.append(getVersion()).append(SPLITTER);
    }
}