package com.audiencerate.service;

import com.audiencerate.dao.DestinationDao;
import com.audiencerate.model.Destination;

import java.util.List;

public class DestinationService {

    private final DestinationDao dao;

    public DestinationService(DestinationDao dao) {
        this.dao = dao;
    }

    public List<Destination> list() {
        return dao.findAll();
    }
}
