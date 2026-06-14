package com.audiencerate.service;

import com.audiencerate.model.HealthStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthServiceTest {

    @Mock DataSource profilesDs;
    @Mock DataSource segmentsDs;
    @Mock DataSource activationsDs;
    @Mock Connection connection;
    @Mock PreparedStatement ps;
    @Mock ResultSet rs;

    private HealthService service;

    @BeforeEach
    void setUp() {
        service = new HealthService(profilesDs, segmentsDs, activationsDs);
    }

    // all up

    @Test
    void shouldReturnOkWhenAllPoolsHealthy() throws SQLException {
        when(profilesDs.getConnection()).thenReturn(connection);
        when(segmentsDs.getConnection()).thenReturn(connection);
        when(activationsDs.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT 1")).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        var result = service.check();

        assertEquals("ok", result.status());
        assertEquals("up", result.databases().get("profiles"));
        assertEquals("up", result.databases().get("segments"));
        assertEquals("up", result.databases().get("activations"));
    }

    // one down

    @Test
    void shouldReturnErrorWhenOnePoolFails() throws SQLException {
        when(profilesDs.getConnection()).thenReturn(connection);
        when(segmentsDs.getConnection()).thenThrow(new SQLException("connection refused"));
        when(activationsDs.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT 1")).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        var result = service.check();

        assertEquals("error", result.status());
        assertEquals("up", result.databases().get("profiles"));
        assertEquals("down", result.databases().get("segments"));
        assertEquals("up", result.databases().get("activations"));
    }

    // all down

    @Test
    void shouldReturnErrorWhenAllPoolsFail() throws SQLException {
        when(profilesDs.getConnection()).thenThrow(new SQLException("down"));
        when(segmentsDs.getConnection()).thenThrow(new SQLException("down"));
        when(activationsDs.getConnection()).thenThrow(new SQLException("down"));

        var result = service.check();

        assertEquals("error", result.status());
        assertEquals("down", result.databases().get("profiles"));
        assertEquals("down", result.databases().get("segments"));
        assertEquals("down", result.databases().get("activations"));
    }
}
