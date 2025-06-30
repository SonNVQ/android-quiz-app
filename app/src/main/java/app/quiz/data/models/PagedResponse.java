package app.quiz.data.models;

import java.util.List;

/**
 * Generic paged response model for API responses with pagination
 * @param <T> The type of items in the response
 */
public class PagedResponse<T> {
    private List<T> items;
    private int totalCount;
    private int pageNumber;
    private int pageSize;
    private int totalPages;
    
    // Default constructor
    public PagedResponse() {}
    
    // Constructor
    public PagedResponse(List<T> items, int totalCount, int pageNumber, int pageSize, int totalPages) {
        this.items = items;
        this.totalCount = totalCount;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
    }
    
    // Getters and setters
    public List<T> getItems() {
        return items;
    }
    
    public void setItems(List<T> items) {
        this.items = items;
    }
    
    public int getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    
    public int getPageNumber() {
        return pageNumber;
    }
    
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    // Utility methods
    public boolean hasNextPage() {
        return pageNumber < totalPages;
    }
    
    public boolean hasPreviousPage() {
        return pageNumber > 1;
    }
    
    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
    
    @Override
    public String toString() {
        return "PagedResponse{" +
                "itemCount=" + (items != null ? items.size() : 0) +
                ", totalCount=" + totalCount +
                ", pageNumber=" + pageNumber +
                ", pageSize=" + pageSize +
                ", totalPages=" + totalPages +
                '}';
    }
}