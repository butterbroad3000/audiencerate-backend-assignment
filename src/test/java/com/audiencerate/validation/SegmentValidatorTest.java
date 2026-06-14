package com.audiencerate.validation;

import com.audiencerate.error.ValidationException;
import com.audiencerate.model.request.CreateSegmentRequest;
import com.audiencerate.model.request.UpdateSegmentRequest;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class SegmentValidatorTest {

    private final SegmentValidator validator = new SegmentValidator();

    @Test
    void shouldPassWithValidCreateRequest() {
        var req = Instancio.of(CreateSegmentRequest.class)
                .set(Select.field(CreateSegmentRequest::name), "Valid Name")
                .set(Select.field(CreateSegmentRequest::status), "active")
                .create();
        assertDoesNotThrow(() -> validator.validateCreate(req));
    }

    @Test
    void shouldFailWhenNameIsNull() {
        var req = Instancio.of(CreateSegmentRequest.class)
                .set(Select.field(CreateSegmentRequest::name), null)
                .create();
        var ex = assertThrows(ValidationException.class, () -> validator.validateCreate(req));
        assertTrue(ex.getDetails().containsKey("name"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"ab", "  a  "})
    void shouldFailWhenNameTooShort(String name) {
        var req = Instancio.of(CreateSegmentRequest.class)
                .set(Select.field(CreateSegmentRequest::name), name)
                .create();
        var ex = assertThrows(ValidationException.class, () -> validator.validateCreate(req));
        assertTrue(ex.getDetails().containsKey("name"));
    }

    @Test
    void shouldFailWhenNameTooLong() {
        var req = Instancio.of(CreateSegmentRequest.class)
                .set(Select.field(CreateSegmentRequest::name), "A".repeat(81))
                .create();
        var ex = assertThrows(ValidationException.class, () -> validator.validateCreate(req));
        assertTrue(ex.getDetails().containsKey("name"));
    }

    @Test
    void shouldFailWhenStatusInvalid() {
        var req = Instancio.of(CreateSegmentRequest.class)
                .set(Select.field(CreateSegmentRequest::name), "Valid Name")
                .set(Select.field(CreateSegmentRequest::status), "deleted")
                .create();
        var ex = assertThrows(ValidationException.class, () -> validator.validateCreate(req));
        assertTrue(ex.getDetails().containsKey("status"));
    }

    @Test
    void shouldPassValidUpdateRequest() {
        var req = Instancio.of(UpdateSegmentRequest.class)
                .set(Select.field(UpdateSegmentRequest::name), "Updated Name")
                .set(Select.field(UpdateSegmentRequest::status), "draft")
                .create();
        assertDoesNotThrow(() -> validator.validateUpdate(req));
    }

    @Test
    void shouldFailUpdateWhenStatusInvalid() {
        var req = Instancio.of(UpdateSegmentRequest.class)
                .set(Select.field(UpdateSegmentRequest::status), "invalid")
                .create();
        var ex = assertThrows(ValidationException.class, () -> validator.validateUpdate(req));
        assertTrue(ex.getDetails().containsKey("status"));
    }

    @Test
    void shouldPassUpdateWithNullFields() {
        var req = new UpdateSegmentRequest(null, null, null, null, null);
        assertDoesNotThrow(() -> validator.validateUpdate(req));
    }
}
