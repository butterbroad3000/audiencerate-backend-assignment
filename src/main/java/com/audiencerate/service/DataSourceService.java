package com.audiencerate.service;

import com.audiencerate.dao.DataSourceDao;
import com.audiencerate.model.DataSourceInfo;

import java.util.List;

public class DataSourceService {

    private final DataSourceDao dao;

    public DataSourceService(DataSourceDao dao) {
        this.dao = dao;
    }

    public List<DataSourceInfo> list() {
        return dao.findAll();
    }
}
