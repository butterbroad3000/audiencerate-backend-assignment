package com.audiencerate.error;

import com.audiencerate.model.response.ErrorEnvelope;
import com.audiencerate.model.response.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {
    @Override
    public Response toResponse(ValidationException ex) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorEnvelope(new ErrorResponse(Response.Status.BAD_REQUEST.getStatusCode(), ex.getMessage(), ex.getDetails())))
                .build();
    }
}
