package com.audiencerate.validation;

import com.audiencerate.dao.DataSourceDao;
import com.audiencerate.error.ValidationException;
import com.audiencerate.model.request.CreateSegmentRequest;
import com.audiencerate.model.request.UpdateSegmentRequest;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SegmentValidatorTest {

    @Mock DataSourceDao dataSourceDao;

    private SegmentValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SegmentValidator(dataSourceDao);
    }

    @Test
    void shouldPassWithValidCreateRequest() {
        var req = Instancio.of(CreateSegmentRequest.class)
                .set(Select.field(CreateSegmentRequest::name), "Valid Name")
                .set(Select.field(CreateSegmentRequest::status), "active")
                .set(Select.field(CreateSegmentRequest::dataSourceIds), List.of())
                .create();
        assertDoesNotThrow(() -> validator.validateCreate(req),
                "valid request should pass without exception");
        verifyNoInteractions(dataSourceDao);
    }

    @Test
    void shouldFailWhenNameIsNull() {
        var req = Instancio.of(CreateSegmentRequest.class)
                .set(Select.field(CreateSegmentRequest::name), null)
                .create();
        var ex = assertThrows(ValidationException.class, () -> validator.validateCreate(req),
                "null name must throw ValidationException");
        assertTrue(ex.getDetails().containsKey("name"), "error details must contain 'name' field");
        verifyNoInteractions(dataSourceDao);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ab", "  a  "})
    void shouldFailWhenNameTooShort(String name) {
        var req = Instancio.of(CreateSegmentRequest.class)
                .set(Select.field(CreateSegmentRequest::name), name)
                .create();
        var ex = assertThrows(ValidationException.class, () -> validator.validateCreate(req),
                "name shorter than 3 chars must throw ValidationException");
        assertTrue(ex.getDetails().containsKey("name"), "error details must contain 'name' field");
        verifyNoInteractions(dataSourceDao);
    }

    @Test
    void shouldFailWhenNameTooLong() {
        var req = Instancio.of(CreateSegmentRequest.class)
                .set(Select.field(CreateSegmentRequest::name), "A".repeat(81))
                .create();
        var ex = assertThrows(ValidationException.class, () -> validator.validateCreate(req),
                "name longer than 80 chars must throw ValidationException");
        assertTrue(ex.getDetails().containsKey("name"), "error details must contain 'name' field");
        verifyNoInteractions(dataSourceDao);
    }

    @Test
    void shouldFailWhenStatusInvalid() {
        var req = Instancio.of(CreateSegmentRequest.class)
                .set(Select.field(CreateSegmentRequest::name), "Valid Name")
                .set(Select.field(CreateSegmentRequest::status), "deleted")
                .create();
        var ex = assertThrows(ValidationException.class, () -> validator.validateCreate(req),
                "invalid status must throw ValidationException");
        assertTrue(ex.getDetails().containsKey("status"), "error details must contain 'status' field");
        verifyNoInteractions(dataSourceDao);
    }

    @Test
    void shouldPassValidUpdateRequest() {
        var req = Instancio.of(UpdateSegmentRequest.class)
                .set(Select.field(UpdateSegmentRequest::name), "Updated Name")
                .set(Select.field(UpdateSegmentRequest::status), "draft")
                .set(Select.field(UpdateSegmentRequest::dataSourceIds), List.of())
                .create();
        assertDoesNotThrow(() -> validator.validateUpdate(req),
                "valid update request should pass without exception");
        verifyNoInteractions(dataSourceDao);
    }

    @Test
    void shouldFailUpdateWhenStatusInvalid() {
        var req = Instancio.of(UpdateSegmentRequest.class)
                .set(Select.field(UpdateSegmentRequest::status), "invalid")
                .create();
        var ex = assertThrows(ValidationException.class, () -> validator.validateUpdate(req),
                "invalid status in update must throw ValidationException");
        assertTrue(ex.getDetails().containsKey("status"), "error details must contain 'status' field");
        verifyNoInteractions(dataSourceDao);
    }

    @Test
    void shouldPassUpdateWithNullFields() {
        var req = new UpdateSegmentRequest(null, null, null, null, null);
        assertDoesNotThrow(() -> validator.validateUpdate(req),
                "update with all null fields should pass without exception");
        verifyNoInteractions(dataSourceDao);
    }
}
