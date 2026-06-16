package com.audiencerate.service;

import com.audiencerate.dao.DestinationDao;
import com.audiencerate.model.Destination;
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
class DestinationServiceTest {

    @Mock DestinationDao dao;

    private DestinationService service;

    @BeforeEach
    void setUp() {
        service = new DestinationService(dao);
    }

    @Test
    void shouldReturnDestinationList() {
        var destinations = Instancio.ofList(Destination.class).size(2).create();
        when(dao.findAll()).thenReturn(destinations);

        var result = service.list();

        assertEquals(2, result.size(), "two destinations returned");
        verify(dao).findAll();
        verifyNoMoreInteractions(dao);
    }

    @Test
    void shouldReturnEmptyList() {
        when(dao.findAll()).thenReturn(List.of());

        var result = service.list();

        assertTrue(result.isEmpty(), "empty list when no destinations");
        verify(dao).findAll();
        verifyNoMoreInteractions(dao);
    }
}
