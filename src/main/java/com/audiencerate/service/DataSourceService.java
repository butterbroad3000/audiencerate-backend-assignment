package com.audiencerate.service;

import com.audiencerate.dao.DataSourceDao;
import com.audiencerate.model.DataSource;

import java.util.List;

public class DataSourceService {

    private final DataSourceDao dao;

    public DataSourceService(DataSourceDao dao) {
        this.dao = dao;
    }

    public List<DataSource> list() {
        return dao.findAll();
    }
}
