package com.audiencerate.service;

import com.audiencerate.dao.DataSourceDao;
import com.audiencerate.model.DataSourceInfo;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSourceServiceTest {

    @Mock DataSourceDao dao;

    private DataSourceService service;

    @BeforeEach
    void setUp() {
        service = new DataSourceService(dao);
    }

    @Test
    void shouldReturnDataSourceList() {
        var sources = Instancio.ofList(DataSourceInfo.class).size(3).create();
        when(dao.findAll()).thenReturn(sources);

        var result = service.list();

        assertEquals(3, result.size(), "three data sources returned");
        verify(dao).findAll();
        verifyNoMoreInteractions(dao);
    }

    @Test
    void shouldReturnEmptyList() {
        when(dao.findAll()).thenReturn(List.of());

        var result = service.list();

        assertTrue(result.isEmpty(), "empty list when no data sources");
        verify(dao).findAll();
        verifyNoMoreInteractions(dao);
    }
}
