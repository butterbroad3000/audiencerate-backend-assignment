package com.audiencerate.dao;

/**
 * Shared pagination bounds normalisation used by all DAOs.
 */
public final class PaginationUtils {

    private PaginationUtils() {}

    public static final int MIN_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 15;
    public static final int MAX_PAGE_SIZE = 100;

    public static int normalisePage(int page) {
        return Math.max(page, MIN_PAGE);
    }

    public static int normalisePageSize(int pageSize) {
        if (pageSize < MIN_PAGE) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }
}
