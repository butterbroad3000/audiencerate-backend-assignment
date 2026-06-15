package com.audiencerate.dao;

/**
 * SQL constants for {@link DestinationDao}.
 */
public final class DestinationSql {

    private DestinationSql() {}

    static final String SELECT_ALL = "SELECT id, name, color FROM destinations ORDER BY id";

    static final String SELECT_BY_ID = "SELECT id, name, color FROM destinations WHERE id = ?";
}
