package com.audiencerate.error;

import com.audiencerate.model.response.ErrorEnvelope;
import com.audiencerate.model.response.ErrorResponse;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger log = LoggerFactory.getLogger(GenericExceptionMapper.class);

    @Override
    public Response toResponse(Exception ex) {
        log.error("Unhandled exception", ex);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorEnvelope(new ErrorResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Internal server error")))
                .build();
    }
}
