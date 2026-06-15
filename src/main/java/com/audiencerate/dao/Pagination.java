package com.audiencerate.dao;

/**
 * Shared pagination bounds normalisation used by all DAOs.
 */
public final class Pagination {

    private Pagination() {}

    public static int normalisePage(int page) {
        return Math.max(page, 1);
    }

    public static int normalisePageSize(int pageSize) {
        if (pageSize < 1) return 12;
        return Math.min(pageSize, 100);
    }
}
