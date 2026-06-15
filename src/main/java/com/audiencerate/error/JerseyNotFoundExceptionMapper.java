package com.audiencerate.error;

import com.audiencerate.model.response.ErrorEnvelope;
import com.audiencerate.model.response.ErrorResponse;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JerseyNotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {
    @Override
    public Response toResponse(NotFoundException ex) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorEnvelope(new ErrorResponse(Response.Status.NOT_FOUND.getStatusCode(), "Not found")))
                .build();
    }
}
