package com.audiencerate.dao.sql;

import com.audiencerate.dao.DestinationDao;

/**
 * SQL constants for {@link DestinationDao}.
 */
public final class DestinationSql {

    private DestinationSql() {}

    public static final String SELECT_ALL = "SELECT id, name, color FROM destinations ORDER BY id";

    public static final String SELECT_BY_ID = "SELECT id, name, color FROM destinations WHERE id = ?";
}
