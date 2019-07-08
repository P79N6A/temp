package com.dfire.soa.item.partner.bo.query;

import java.io.Serializable;

/**
 * 
 * Created by zhishi.
 */
public class BaseQuery implements Serializable{
	private static final long serialVersionUID = 1L;
	/**
	 * 缺省页大小
	 */
    public static final int DEFAULT_PAGE_SIZE = 20;
    /** 
	 * 最大页大小 
	 */
    public static final int MAX_PAGE_SIZE     = 1000;
	
	/**
	 * 页大小
	 */
	protected int pageSize;
    /**
     * 页码
     */
    private int pageIndex;
	/**
     * 排序字段
     */
    private String orderBy;
    /**
     * 排序方式，默认null即true，有orderBy生效
	 *（true：desc降序，false：asc升序）
     */
    private Boolean desc;

	/**
	 * 是否使用分页
	 * false使用，true不使用
	 */
	private Boolean usePage = Boolean.FALSE;

    public int getPageSize() {
        if (pageSize < 1) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageIndex() {
        if (pageIndex < 1) {
            pageIndex = 1;
        }
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }
	
	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}
	
	public Boolean getDesc() {
		return desc;
	}

	public void setDesc(Boolean desc) {
		this.desc = desc;
	}

	/**
	 * startPos
	 * （sql查询起始位置 ）
	 */
    public int getStartPos() {
		return this.getPageSize() * (this.getPageIndex()-1);
    }

    /**
	 * endPos
     * （sql查询结束位置）
     */
    public int getEndPos() {
        return this.getPageIndex() * this.getPageSize();
    }

	public Boolean getUsePage() {
		return usePage;
	}

	public void setUsePage(Boolean usePage) {
		this.usePage = usePage;
	}
}


