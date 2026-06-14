package com.audiencerate.model.response;

import java.util.List;

public record PagedResponse<T>(List<T> data, PaginationMeta pagination) {
}
